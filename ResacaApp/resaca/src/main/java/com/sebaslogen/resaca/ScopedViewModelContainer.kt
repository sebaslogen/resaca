package com.sebaslogen.resaca

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentSkipListSet

/**
 * [ViewModel] class used to store objects and [ScopedViewModel]s
 * as long as the requester doesn't completely leave composition
 * or the scope of this [ViewModel] is cleared.
 *
 * The lifecycle of a Composable doesn't match the one from this ViewModel nor Activities/Fragments.
 * To detect when the requester has left composition and the screen for good (it's not needed anymore)
 * this class observes the [Lifecycle] of the scope containing this [ScopedViewModelContainer] using
 * the following flow:
 * - When Composable leaves composition mark the composable to be forgotten/disposed from this object after a small delay
 * - This clean up can be cancelled by two events:
 *  * Same composable is requested before the small delay (e.g. when Android makes a configuration change)
 *  * The [Lifecycle] of the scope in which this [ScopedViewModelContainer] lives has been paused (e.g. app/screen went to background)
 * - When the clean-up is cancelled because the [Lifecycle] of the scope was paused then the objects
 * marked for disposal will be disposed after a small delay only once the [Lifecycle] of the
 * scope returns to resume (e.g. app or screen came back to foreground)
 * This clean up step after resume can also be cancelled for the same to reasons as above and go back to the first step of this flow
 *
 */
class ScopedViewModelContainer : ViewModel(), LifecycleEventObserver {

    /**
     * Mark whether the container of this class (usually a screen like an Activity, a Fragment or a Compose destination)
     * is in foreground or in background to avoid disposing objects while in the background
     */
    private var isInForeground = true

    /**
     * Generic objects container
     */
    private val scopedObjectsContainer = mutableMapOf<Key, Any>()

    /**
     * Generic [ViewModel]s container
     */
    private val scopedViewModelsContainer = mutableMapOf<Key, ScopedViewModel>()

    /**
     * List of [Key]s for the objects that will be disposed (forgotten from this class so they can be garbage collected) in the near future
     */
    private val markedForDisposal = ConcurrentSkipListSet<Int>()

    /**
     * List of [Job]s associated with an object [Key] that is scheduled to be disposed very soon, unless
     * the object is requested again (and [cancelDisposal] is triggered) or
     * the container of this [ScopedViewModelContainer] class goes to the background (making [isInForeground] false)
     */
    private val disposingJobs = mutableMapOf<Key, Job>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getOrBuildObject(
        key: Key,
        builder: () -> T
    ): T {
        cancelDisposal(key)
        return scopedObjectsContainer[key] as? T
            ?: builder.invoke().apply { scopedObjectsContainer[key] = this }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : ScopedViewModel> getOrBuildScopedViewModel(
        key: Key,
        builder: () -> T
    ): T {
        cancelDisposal(key)
        return scopedViewModelsContainer[key] as? T
            ?: builder.invoke().apply { scopedViewModelsContainer[key] = this }
    }

    /**
     * Triggered when a Composable that stored an object in this class is disposed and signals this container
     * that the object might be also disposed from this container only when the stored object
     * is not going to be used anymore (e.g. after configuration change or container fragment returning from backstack)
     */
    fun onDisposedFromComposition(key: Key) {
        markedForDisposal.add(key.value) // Marked to be disposed after onResume
        scheduleToDisposeBeforeGoingToBackground(key) // Schedule to dispose this object before onPause
    }

    /**
     * Schedules the object referenced by this [key] in the [scopedObjectsContainer] and [scopedViewModelsContainer]
     * to be removed (so it can be garbage collected) if the screen associated with this still [isInForeground]
     */
    private fun scheduleToDisposeBeforeGoingToBackground(key: Key) {
        scheduleToDispose(key = key)
    }

    /**
     * Dispose item from [scopedObjectsContainer] or [scopedViewModelsContainer] after the screen resumes
     * This makes possible to garbage collect objects that were disposed by Compose right before the container screen went
     * to background ([Lifecycle.Event.ON_PAUSE]) and are not required anymore whe the screen is back in foreground ([Lifecycle.Event.ON_RESUME])
     *
     * Disposal delay: Instead of immediately disposing the object after [Lifecycle.Event.ON_RESUME], launch a coroutine with a delay
     * to give the Activity/Fragment enough time to recreate the desired UI after resuming or configuration change.
     * Upon disposal, [ScopedViewModel] objects will also be requested to cancel all their coroutines in their [CoroutineScope]
     */
    private fun scheduleToDisposeAfterReturningFromBackground() {
        markedForDisposal.forEach { key ->
            scheduleToDispose(key = Key(key))
        }
    }

    /**
     * Dispose/Forget the object -if present- in [scopedObjectsContainer] or [scopedViewModelsContainer] after a small delay.
     * We store the deletion job with the given [key] in the given [jobsContainer] to make sure we don't schedule the same work twice
     * An optional [removalCondition] is provided to check at removal time to make sure no object is removed while in the background
     *
     * @param key Key of the object stored in either [scopedObjectsContainer] or [scopedViewModelsContainer] to be de-referenced for GC
     * @param jobsContainer Stores the disposal job to avoid duplicated requests
     * @param removalCondition Last check at disposal time to prevent disposal when this condition is not met
     */
    private fun scheduleToDispose(key: Key, removalCondition: () -> Boolean = { isInForeground }) {
        if (disposingJobs.containsKey(key)) return // Already disposing, quit

        val disposeDelayTimeMillis: Long = 5000
        val newDisposingJob = viewModelScope.launch {
            delay(disposeDelayTimeMillis)
            if (removalCondition()) {
                markedForDisposal.remove(key.value)
                scopedObjectsContainer.remove(key)
                scopedViewModelsContainer.remove(key)?.viewModelScope?.cancel()
            }
            disposingJobs.remove(key)
        }
        disposingJobs[key] = newDisposingJob
    }

    private fun cancelDisposal(key: Key) {
        disposingJobs.remove(key)?.cancel() // Cancel scheduled disposal
        if (markedForDisposal.remove(key.value)) { // Unmark for disposal in case it's not yet scheduled for disposal
            Log.d("ScopedVMContainer", "Resuming with ${scopedViewModelsContainer.keys}")
        }
    }

    /**
     * Cancel all [ScopedViewModel] coroutines in their [CoroutineScope]
     */
    override fun onCleared() {
        // Cancel disposal jobs, all those references will be garbage collected anyway with this ViewModel
        disposingJobs.forEach { (_, job) -> job.cancel() }
        // Cancel all coroutines from ViewModels hosted in this object
        scopedViewModelsContainer.values.forEach { scopedViewModel ->
            scopedViewModel.viewModelScope.cancel()
        }
        scopedObjectsContainer.clear() // Clear just in case this VM is leaked
        scopedViewModelsContainer.clear() // Clear just in case this VM is leaked
        super.onCleared()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> { // Note Fragment View creation happens before this onResume
                isInForeground = true
                scheduleToDisposeAfterReturningFromBackground()
            }
            Lifecycle.Event.ON_PAUSE -> {
                isInForeground = false
            }
            Lifecycle.Event.ON_DESTROY -> { // Remove ourselves so that this ViewModel can be garbage collected
                source.lifecycle.removeObserver(this)
            }
            else -> {
                // No-Op
            }
        }
    }

    @JvmInline
    value class Key(val value: Int)
}