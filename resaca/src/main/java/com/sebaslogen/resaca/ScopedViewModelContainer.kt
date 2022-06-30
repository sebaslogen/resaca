package com.sebaslogen.resaca

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.lifecycle.*
import kotlinx.coroutines.*
import java.io.Closeable
import java.util.concurrent.ConcurrentSkipListSet
import kotlin.coroutines.CoroutineContext

/**
 * [ViewModel] class used to store objects and [ViewModel]s as long as the
 * requester doesn't completely leave composition (even temporary)
 * or the scope of this [ViewModel] is cleared.
 *
 * ************************
 * * Composable lifecycle *
 * ************************
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
 * the screen for good), this class observes
 * - the [Lifecycle] of the owner of this [ScopedViewModelContainer] (i.e. Activity, Fragment or Compose Navigation destination)
 * - the foreground/background state of the container [LifecycleOwner]
 * - the configuration changing state of the container [Activity]
 */
class ScopedViewModelContainer : ViewModel(), LifecycleEventObserver {

    /**
     * Mark whether the container of this class (usually a screen like an Activity, a Fragment or a Compose destination)
     * is in foreground or in background to avoid disposing objects while in the background
     */
    private var isInForeground = true

    /**
     * Mark whether the Activity containing this class is changing configuration and use this
     * information to dispose objects that are completely gone after a configuration change
     */
    private var isChangingConfiguration = false

    /**
     * Container of object keys associated with their [ExternalKey],
     * the [ExternalKey] will be used to track and store new versions of the object to be stored/restored
     */
    private val scopedObjectKeys = mutableMapOf<String, ExternalKey>()

    /**
     * Generic objects container
     */
    private val scopedObjectsContainer = mutableMapOf<String, Any>()

    /**
     * List of keys for the objects that will be disposed (forgotten from this class so they can be garbage collected) in the near future
     */
    private val markedForDisposal = ConcurrentSkipListSet<String>()

    /**
     * List of [Job]s associated with an object (through its key) that is scheduled to be disposed very soon, unless
     * the object is requested again (and [cancelDisposal] is triggered) or
     * the container of this [ScopedViewModelContainer] class goes to the background (making [isInForeground] false)
     */
    private val disposingJobs = mutableMapOf<String, Job>()

    /**
     * Time to wait until disposing an object from the [scopedObjectsContainer] after it has been scheduled for disposal
     */
    private val disposeDelayTimeMillis: Long = 5000

    @Suppress("UNCHECKED_CAST")
    @Composable
    fun <T : Any> getOrBuildObject(
        positionalMemoizationKey: String,
        externalKey: ExternalKey = ExternalKey(),
        builder: @DisallowComposableCalls () -> T
    ): T {
        @Composable
        fun buildAndStoreObject() = builder.invoke().apply { scopedObjectsContainer[positionalMemoizationKey] = this }

        cancelDisposal(positionalMemoizationKey)

        val originalObject = scopedObjectsContainer[positionalMemoizationKey]
        return if (scopedObjectKeys.containsKey(positionalMemoizationKey) && (scopedObjectKeys[positionalMemoizationKey] == externalKey)) {
            // When the object is already present and the external key matches, then try to restore it
            originalObject as? T ?: buildAndStoreObject()
        } else {
            scopedObjectKeys[positionalMemoizationKey] = externalKey // Set the external key used to track and store the new object version
            scopedObjectsContainer.remove(positionalMemoizationKey)?.also {
                if (shouldClearDisposedObject(it)) clearDisposedObject(it) // Old object may need to be cleared before it's forgotten
            }
            buildAndStoreObject()
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Composable
    fun <T : ViewModel> getOrBuildViewModel(
        modelClass: Class<T>,
        positionalMemoizationKey: String,
        externalKey: ExternalKey = ExternalKey(),
        builder: @DisallowComposableCalls () -> T
    ): T = getOrBuildViewModel(
        modelClass = modelClass,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        factory = ScopedViewModelHelper.viewModelFactoryFor(builder)
    )

    @Composable
    fun <T : ViewModel> getOrBuildViewModel(
        modelClass: Class<T>,
        positionalMemoizationKey: String,
        externalKey: ExternalKey = ExternalKey()
    ): T = getOrBuildViewModel(
        modelClass = modelClass,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        factory = ViewModelProvider.NewInstanceFactory.instance
    )

    @Suppress("UNCHECKED_CAST")
    @Composable
    fun <T : ViewModel> getOrBuildViewModel(
        modelClass: Class<T>,
        positionalMemoizationKey: String,
        externalKey: ExternalKey = ExternalKey(),
        factory: ViewModelProvider.Factory
    ): T = ScopedViewModelHelper.getOrBuildViewModel(
        modelClass = modelClass,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        factory = factory,
        scopedObjectsContainer = scopedObjectsContainer,
        scopedObjectKeys = scopedObjectKeys,
        cancelDisposal = ::cancelDisposal
    )

    @Suppress("UNCHECKED_CAST")
    @Composable
    fun <T : ViewModel> getOrBuildHiltViewModel(
        modelClass: Class<T>,
        positionalMemoizationKey: String,
        externalKey: ExternalKey = ExternalKey(),
        factory: ViewModelProvider.Factory
    ): T = ScopedViewModelHelper.getOrBuildHiltViewModel(
        modelClass = modelClass,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        factory = factory,
        scopedObjectsContainer = scopedObjectsContainer,
        scopedObjectKeys = scopedObjectKeys,
        cancelDisposal = ::cancelDisposal
    )

    /**
     * Clear, if possible, scoped object when disposing it
     */
    @Suppress("NOTHING_TO_INLINE")
    private inline fun clearDisposedObject(scopedObject: Any) {
        when (scopedObject) {
            is ViewModelStore -> scopedObject.clear()
            is CoroutineScope -> scopedObject.cancel()
            is CoroutineContext -> scopedObject.cancel()
            is Closeable -> scopedObject.close()
        }
    }

    /**
     * Triggered when a Composable that stored an object in this class is disposed and signals this container
     * that the object might be also disposed from this container only when the stored object
     * is not going to be used anymore (e.g. after configuration change or container fragment returning from backstack)
     */
    fun onDisposedFromComposition(key: String) {
        markedForDisposal.add(key) // Marked to be disposed after onResume
        scheduleToDisposeBeforeGoingToBackground(key) // Schedule to dispose this object before onPause
    }

    /**
     * Schedules the object referenced by this [key] in the [scopedObjectsContainer]
     * to be removed (so it can be garbage collected) if the screen associated with this is still in foreground ([isInForeground])
     */
    private fun scheduleToDisposeBeforeGoingToBackground(key: String) {
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
            scheduleToDispose(key)
        }
    }

    /**
     * Dispose/Forget the object -if present- in [scopedObjectsContainer] after a small delay.
     * We store the deletion job with the given [key] in the [disposingJobs] to make sure we don't schedule the same work twice.
     * An optional [removalCondition] is provided to check at removal time, e.g. to make sure no object is removed while in the background
     *
     * @param key Key of the object stored in either [scopedObjectsContainer] to be de-referenced for GC
     * @param removalCondition Last check at disposal time to prevent disposal when this condition is not met.
     *
     *                          By default we want to remove only when:
     *                          - scope is in the foreground, because if it's in the background it might be needed again when returning to foreground,
     *                          in that case the decision will be deferred to [scheduleToDisposeAfterReturningFromBackground]
     *                          - or when recreating due configuration changes, because after a configuration change, the [cancelDisposal] should have been
     *                          called once the scoped object was requested again, the fact that this is still scheduled means it's not needed in the new config.
     */
    private fun scheduleToDispose(key: String, removalCondition: () -> Boolean = { isInForeground || isChangingConfiguration }) {
        if (disposingJobs.containsKey(key)) return // Already disposing, quit

        val newDisposingJob = viewModelScope.launch {
            delay(disposeDelayTimeMillis)
            withContext(NonCancellable) { // We treat the disposal/remove/clear block as an atomic transaction
                if (removalCondition()) {
                    markedForDisposal.remove(key)
                    scopedObjectKeys.remove(key)
                    scopedObjectsContainer.remove(key)
                        ?.also {
                            if (shouldClearDisposedObject(it)) clearDisposedObject(it)
                        }
                }
                disposingJobs.remove(key)
            }
        }
        disposingJobs[key] = newDisposingJob
    }

    /**
     * An object that is being disposed should also be cleared only if it was the last instance present in this container
     */
    private fun shouldClearDisposedObject(disposedObject: Any): Boolean = !scopedObjectsContainer.containsValue(disposedObject)

    private fun cancelDisposal(key: String) {
        disposingJobs.remove(key)?.cancel() // Cancel scheduled disposal
        markedForDisposal.remove(key) // Un-mark for disposal in case it's not yet scheduled for disposal
    }

    /**
     * Cancel all [ViewModel] coroutines in their [CoroutineScope]
     */
    override fun onCleared() {
        // Cancel disposal jobs, all those references will be garbage collected anyway with this ViewModel
        disposingJobs.forEach { (_, job) -> job.cancel() }
        // Cancel all coroutines from ViewModels hosted in this object
        scopedObjectsContainer.values.forEach { clearDisposedObject(it) }
        scopedObjectsContainer.clear() // Clear just in case this VM is leaked
        super.onCleared()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> { // Note Fragment View creation happens before this onResume
                isInForeground = true
                isChangingConfiguration = false // Clear this flag when the scope is resumed
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

    fun setChangingConfigurationState(newState: Boolean) {
        isChangingConfiguration = newState
    }

    /**
     * Unique Key to identify versions objects stored in the [ScopedViewModelContainer]
     * When this external key does not match the one stored for an object's main key in [scopedObjectKeys],
     * then the old object is cleared, the new instance is stored (replacing old instance in storage) and
     * the new external key is stored in [scopedObjectKeys]
     */
    @JvmInline
    value class ExternalKey(private val value: Int = 0) {

        val isDefaultKey: Boolean
            get() = value == 0

        companion object {
            fun from(objectInstance: Any?): ExternalKey = ExternalKey(objectInstance.hashCode())
        }
    }
}