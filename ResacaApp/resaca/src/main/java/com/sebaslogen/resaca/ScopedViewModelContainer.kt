package com.sebaslogen.resaca

import androidx.lifecycle.*
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentSkipListSet

class ScopedViewModelContainer : ViewModel(), LifecycleObserver {
    private var isInForeground = true

    //private // TODO scopedContainer should be private, visible for debugging
    val scopedObjectContainer = mutableMapOf<Int, Any>()

    //private // TODO scopedContainer should be private, visible for debugging
    val scopedViewModelContainer = mutableMapOf<Int, ScopedViewModel>()
    private val markedForDisposal = ConcurrentSkipListSet<Int>()

    // TODO: use this one on immediate
    private val beforeGoingToBackgroundDisposingJobs = mutableMapOf<Int, Job>()
    private val afterReturningFromBackgroundDisposingJobs = mutableMapOf<Int, Job>()

    fun <T : Any> getOrBuild(
        key: Int,
        builder: () -> T
    ): T {
        cancelDisposal(key)
        // TODO: Can we improve casting?
        return scopedObjectContainer[key] as? T
            ?: builder.invoke().apply { scopedObjectContainer[key] = this }
    }

    fun <T : ScopedViewModel> getOrBuildScopedViewModel(
        key: Int,
        builder: () -> T
    ): T {
        cancelDisposal(key)
        // TODO: Can we improve casting?
        return scopedViewModelContainer[key] as? T
            ?: builder.invoke().apply { scopedViewModelContainer[key] = this }
    }

    // TODO: Fix/Update this doc
    fun onDisposedByCompose(key: Int) {
        markedForDisposal.add(key) // Marked to be disposed after onResume
        scheduleToDisposeBeforeGoingToBackground(key) // Schedule to dispose this object before onPause
    }

    /**
     * Schedules the object referenced by this [key] in the [scopedObjectContainer] and [scopedViewModelContainer]
     * to be removed (so it can be garbage collected) if the screen associated with this still [isInForeground]
     */
    private fun scheduleToDisposeBeforeGoingToBackground(key: Int) {
        scheduleToDispose(key = key, jobsContainer = beforeGoingToBackgroundDisposingJobs) { isInForeground }
    }

    private fun cancelDisposal(key: Int) {
        if (markedForDisposal.remove(key)) { // Unmark for disposal in case it's not yet scheduled for disposal
            //TODO Logger  { "Resuming with ${scopedViewModelContainer.keys}" }
        }
        if (afterReturningFromBackgroundDisposingJobs.containsKey(key)) { // Cancel scheduled disposal
            afterReturningFromBackgroundDisposingJobs.remove(key)?.cancel()
        }
        if (beforeGoingToBackgroundDisposingJobs.containsKey(key)) { // Cancel scheduled disposal
            beforeGoingToBackgroundDisposingJobs.remove(key)?.cancel()
        }
    }

    /**
     * Dispose item from [scopedObjectContainer] or [scopedViewModelContainer] after the screen resumes
     * This makes possible to garbage collect objects that were disposed by Compose right before the container screen went
     * to background ([onLifeCyclePause]) and are not required anymore whe the screen is back in foreground ([onLifeCycleResume])
     *
     * Disposal delay: Instead of immediately disposing the object after onResume, launch a coroutine with a delay
     * to give the Activity/Fragment enough time to recreate the desired UI after resuming or configuration change.
     * [ScopedViewModel] objects will also be requested to cancel all their coroutines in their [CoroutineScope]
     */
    private fun scheduleToDisposeAfterReturningFromBackground() {
        markedForDisposal.forEach { key ->
            scheduleToDispose(key = key, jobsContainer = afterReturningFromBackgroundDisposingJobs)
        }
    }

    /**
     *
     * @param key Key of the object stored in either [scopedObjectContainer] or [scopedViewModelContainer] to be de-referenced for GC
     * @param jobsContainer
     * @param removalCondition
     */
    private fun scheduleToDispose(key: Int, jobsContainer: MutableMap<Int, Job>, removalCondition: () -> Boolean = { true }) {
        if (jobsContainer.containsKey(key)) return // Already disposing, quit

        val disposeDelayTimeMillis: Long = 5000
        val newDisposingJob = viewModelScope.launch {
            delay(disposeDelayTimeMillis)
            if (removalCondition()) {
                markedForDisposal.remove(key)
                scopedObjectContainer.remove(key)
                scopedViewModelContainer.remove(key)?.viewModelScope?.cancel()
            }
            jobsContainer.remove(key)
        }
        jobsContainer[key] = newDisposingJob
    }

    /**
     * Cancel all [ScopedViewModel] coroutines in their [CoroutineScope]
     */
    override fun onCleared() {
        // Cancel disposal jobs, all those references will be garbage collected anyway with this ViewModel
        beforeGoingToBackgroundDisposingJobs.forEach { (_, job) -> job.cancel() }
        afterReturningFromBackgroundDisposingJobs.forEach { (_, job) -> job.cancel() }
        // Cancel all coroutines from ViewModels hosted in this object
        scopedViewModelContainer.forEach { (_, scopedViewModel) ->
            scopedViewModel.viewModelScope.cancel()
        }
        super.onCleared()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME) // Note Fragment View creation happens before this onResume
    private fun onLifeCycleResume() {
        isInForeground = true
        scheduleToDisposeAfterReturningFromBackground()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onLifeCyclePause() {
        isInForeground = false
    }
}