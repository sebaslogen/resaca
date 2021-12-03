package com.sebaslogen.resaca

import androidx.lifecycle.*
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentSkipListSet

/**
 * [ViewModel] class used to store objects and [ViewModel]s as long as the
 * requester doesn't completely leave composition (even temporary)
 * or the scope of this [ViewModel] is cleared.
 *
 * The lifecycle of a Composable doesn't match the one from this ViewModel nor Activities/Fragments, it's alive
 * as long it's part of the composition and in some cases even after temporary leaving composition.
 *
 * In Compose, we don't know for sure at the moment of disposal if the
 * Composable will be disposed for good or if it will return again later.
 * Therefore, at the moment of disposal, we mark in our container the scoped
 * associated object to be disposed after a small delay (currently 5 seconds).
 * During the span of time of this delay a few things can happen:
 * - The Composable is not part of the composition anymore after the delay and the associated object is disposed.
 * - The [LifecycleOwner] of the disposed Composable (i.e. the navigation destination where the Composable lived)
 * is paused (e.g. screen went to background) before the delay finishes. Then the disposal of the scoped object is cancelled,
 * but the object is still marked for disposal at a later stage after resume of the [LifecycleOwner].
 *      * This can happen when the application goes through a configuration change and the container Activity/Fragment is recreated.
 *      * This can also happen when the Composable is part of a Fragment that has been pushed to the backstack.
 * - When the [LifecycleOwner] of the disposed Composable is resumed (i.e. screen comes back to foreground),
 * then the disposal of the associated object is scheduled again to happen after a small delay.
 * At this point two things can happen:
 *      * The Composable becomes part of the composition again and the [rememberScoped] function restores
 *      the associated object while also cancelling any pending delayed disposal.
 *      * The Composable is not part of the composition anymore after the delay and the associated object is disposed.
 *
 *
 * To detect when the requester Composable is not needed anymore (has left composition and
 * the screen for good), this class observes the [Lifecycle] of the owner of
 * this [ScopedViewModelContainer] (i.e. Activity, Fragment or Compose Navigation destination)
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
     * Schedules the object referenced by this [key] in the [scopedObjectsContainer]
     * to be removed (so it can be garbage collected) if the screen associated with this [isInForeground]
     */
    private fun scheduleToDisposeBeforeGoingToBackground(key: Key) {
        scheduleToDispose(key = key)
    }

    /**
     * Dispose item from [scopedObjectsContainer] after the screen resumes
     * This makes possible to garbage collect objects that were disposed by Compose right before the container screen went
     * to background ([Lifecycle.Event.ON_PAUSE]) and are not required anymore whe the screen is back in foreground ([Lifecycle.Event.ON_RESUME])
     *
     * Disposal delay: Instead of immediately disposing the object after [Lifecycle.Event.ON_RESUME], launch a coroutine with a delay
     * to give the Activity/Fragment enough time to recreate the desired UI after resuming or configuration change.
     * Upon disposal, [ViewModel] objects will also be requested to cancel all their coroutines in their [CoroutineScope]
     */
    private fun scheduleToDisposeAfterReturningFromBackground() {
        markedForDisposal.forEach { key ->
            scheduleToDispose(key = Key(key))
        }
    }

    /**
     * Dispose/Forget the object -if present- in [scopedObjectsContainer] after a small delay.
     * We store the deletion job with the given [key] in the [disposingJobs] to make sure we don't schedule the same work twice
     * An optional [removalCondition] is provided to check at removal time, to make sure no object is removed while in the background
     *
     * @param key Key of the object stored in either [scopedObjectsContainer] to be de-referenced for GC
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
                    ?.also { if (it is ViewModel) it.viewModelScope.cancel() }
            }
            disposingJobs.remove(key)
        }
        disposingJobs[key] = newDisposingJob
    }

    private fun cancelDisposal(key: Key) {
        disposingJobs.remove(key)?.cancel() // Cancel scheduled disposal
        markedForDisposal.remove(key.value) // Un-mark for disposal in case it's not yet scheduled for disposal
    }

    /**
     * Cancel all [ViewModel] coroutines in their [CoroutineScope]
     */
    override fun onCleared() {
        // Cancel disposal jobs, all those references will be garbage collected anyway with this ViewModel
        disposingJobs.forEach { (_, job) -> job.cancel() }
        // Cancel all coroutines from ViewModels hosted in this object
        scopedObjectsContainer.values.forEach { maybeScopedViewModel ->
            if (maybeScopedViewModel is ViewModel) maybeScopedViewModel.viewModelScope.cancel()
        }
        scopedObjectsContainer.clear() // Clear just in case this VM is leaked
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
                // No-Op: the other lifecycle event are irrelevant for this class
            }
        }
    }

    /**
     * Unique Key to identify objects store in the [ScopedViewModelContainer]
     */
    @JvmInline
    value class Key(val value: Int)
}