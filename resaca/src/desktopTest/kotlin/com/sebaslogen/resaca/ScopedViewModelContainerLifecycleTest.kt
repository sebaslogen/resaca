@file:OptIn(ResacaPackagePrivate::class, ExperimentalCoroutinesApi::class)

package com.sebaslogen.resaca

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.sebaslogen.resaca.ScopedViewModelContainer.ExternalKey
import com.sebaslogen.resaca.ScopedViewModelContainer.InternalKey
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Exercises the lifecycle and disposal state machine in [ScopedViewModelContainer] by driving
 * [Lifecycle.Event]s and disposal triggers directly. Uses [Dispatchers.setMain] so the container's
 * `viewModelScope` and `Dispatchers.Main.immediate` calls run on the test scheduler, allowing
 * [advanceUntilIdle] to drain pending work.
 */
internal class ScopedViewModelContainerLifecycleTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Closeable used to verify disposal: [closed] flips to true when the container clears it. */
    private class FakeCloseable : AutoCloseable {
        var closed: Boolean = false
        override fun close() {
            closed = true
        }
    }

    /** Fake [Lifecycle] that records observer add/remove calls without touching the main-thread checker. */
    private class RecordingLifecycle : Lifecycle() {
        val removed: MutableList<LifecycleObserver> = mutableListOf()
        override val currentState: State = State.RESUMED
        override fun addObserver(observer: LifecycleObserver) { /* not exercised by these tests */ }
        override fun removeObserver(observer: LifecycleObserver) {
            removed += observer
        }
    }

    private class FakeLifecycleOwner(val recordingLifecycle: RecordingLifecycle = RecordingLifecycle()) : LifecycleOwner {
        override val lifecycle: Lifecycle = recordingLifecycle
    }

    // region onStateChanged

    @Test
    fun `ON_RESUME sets isInForeground to true`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()
        val owner = FakeLifecycleOwner()

        container.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        advanceUntilIdle()

        assertTrue(container.isInForegroundForTest)
    }

    @Test
    fun `ON_PAUSE sets isInForeground to false`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()
        val owner = FakeLifecycleOwner()
        container.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        advanceUntilIdle()
        assertTrue(container.isInForegroundForTest)

        container.onStateChanged(owner, Lifecycle.Event.ON_PAUSE)
        advanceUntilIdle()

        assertFalse(container.isInForegroundForTest)
    }

    @Test
    fun `ON_DESTROY removes the observer from the source's lifecycle`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()
        val owner = FakeLifecycleOwner()

        container.onStateChanged(owner, Lifecycle.Event.ON_DESTROY)
        advanceUntilIdle()

        assertEquals(listOf<Any>(container), owner.recordingLifecycle.removed)
    }

    @Test
    fun `ON_RESUME after isReturningToForeground was set resets the flag`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()
        val owner = FakeLifecycleOwner()
        container.setShouldBeReturningToForeground { true }

        container.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        advanceUntilIdle()

        // After resume the query is replaced with one that returns false. Indirect assertion: a marked-for-disposal
        // entry would now be cleared on the next frame, since both isInForeground and "no longer returning" are true.
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        val obj = FakeCloseable()
        container.storeForTest(key, externalKey, obj)
        container.onRemovedFromComposition(key)
        advanceUntilIdle()
        assertFalse(container.storedObjectKeysForTest.contains(key))
    }

    @Test
    fun `ON_PAUSE then ON_RESUME flips the foreground flag back`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()
        val owner = FakeLifecycleOwner()

        container.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        advanceUntilIdle()
        container.onStateChanged(owner, Lifecycle.Event.ON_PAUSE)
        advanceUntilIdle()
        assertFalse(container.isInForegroundForTest)

        container.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        advanceUntilIdle()

        assertTrue(container.isInForegroundForTest)
    }

    // endregion

    // region scheduleToDispose / onRemovedFromComposition

    @Test
    fun `onRemovedFromComposition while in foreground clears the object`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()
        val owner = FakeLifecycleOwner()
        container.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        advanceUntilIdle()

        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        val obj = FakeCloseable()
        container.storeForTest(key, externalKey, obj)

        container.onRemovedFromComposition(key)
        advanceUntilIdle()

        assertFalse(container.storedObjectKeysForTest.contains(key))
        assertTrue(obj.closed)
    }

    @Test
    fun `onRemovedFromComposition while in background defers disposal`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()
        // Container has never received ON_RESUME, so isInForeground is false.

        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        val obj = FakeCloseable()
        container.storeForTest(key, externalKey, obj)

        container.onRemovedFromComposition(key)
        advanceUntilIdle()

        assertTrue(container.storedObjectKeysForTest.contains(key))
        assertTrue(container.markedForDisposalForTest.contains(key))
        assertFalse(obj.closed)
    }

    @Test
    fun `ON_RESUME after background-dispose clears marked objects`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()
        val owner = FakeLifecycleOwner()

        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        val obj = FakeCloseable()
        container.storeForTest(key, externalKey, obj)
        // Removed from composition while in background — disposal deferred.
        container.onRemovedFromComposition(key)
        advanceUntilIdle()
        assertTrue(container.storedObjectKeysForTest.contains(key))

        container.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        advanceUntilIdle()

        assertFalse(container.storedObjectKeysForTest.contains(key))
        assertTrue(obj.closed)
    }

    @Test
    fun `clearDelay defers disposal until the delay elapses`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()
        val owner = FakeLifecycleOwner()
        container.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        advanceUntilIdle()

        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        val obj = FakeCloseable()
        container.storeForTest(key, externalKey, obj, clearDelay = 5.seconds)

        container.onRemovedFromComposition(key)
        // Run currently scheduled tasks (the runner that schedules the disposal job) without advancing virtual time,
        // so the inner `delay(5.seconds)` is still pending.
        runCurrent()

        assertTrue(container.storedObjectKeysForTest.contains(key))
        assertFalse(obj.closed)
        // The disposal job exists and is suspended on the delay.
        assertTrue(container.disposingJobsForTest.containsKey(key))

        // Advance just under the delay: still not cleared.
        advanceTimeBy(4_999)
        runCurrent()
        assertTrue(container.storedObjectKeysForTest.contains(key))
        assertFalse(obj.closed)

        // Cross the delay boundary: now the disposal completes.
        advanceTimeBy(2)
        runCurrent()
        assertFalse(container.storedObjectKeysForTest.contains(key))
        assertTrue(obj.closed)
    }

    @Test
    fun `disposal is skipped when the external key resolver still claims the key is in scope`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()
        val owner = FakeLifecycleOwner()
        container.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        advanceUntilIdle()

        val key = InternalKey("k1")
        val resolverKey = "scopedKey"
        val externalKey = ExternalKey(ScopeKeyWithResolver(resolverKey, keyInScopeResolver = { _ -> true }))
        val obj = FakeCloseable()
        container.storeForTest(key, externalKey, obj)

        container.onRemovedFromComposition(key)
        advanceUntilIdle()

        // Resolver still claims the key is in scope: object is preserved even though composition removed it.
        assertTrue(container.storedObjectKeysForTest.contains(key))
        assertFalse(obj.closed)
    }

    @Test
    fun `disposal proceeds when the external key resolver reports the key is out of scope`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()
        val owner = FakeLifecycleOwner()
        container.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        advanceUntilIdle()

        val key = InternalKey("k1")
        val externalKey = ExternalKey(ScopeKeyWithResolver(key = "outOfScope", keyInScopeResolver = { _ -> false }))
        val obj = FakeCloseable()
        container.storeForTest(key, externalKey, obj)

        container.onRemovedFromComposition(key)
        advanceUntilIdle()

        assertFalse(container.storedObjectKeysForTest.contains(key))
        assertTrue(obj.closed)
    }

    // endregion

    // region onCleared

    @Test
    fun `onCleared cancels all pending disposing jobs`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()
        val owner = FakeLifecycleOwner()
        container.onStateChanged(owner, Lifecycle.Event.ON_RESUME)
        advanceUntilIdle()

        // Queue a long-delayed disposal so the job is still suspended on its `delay(60s)` when we trigger onCleared.
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        val obj = FakeCloseable()
        container.storeForTest(key, externalKey, obj, clearDelay = 60.seconds)
        container.onRemovedFromComposition(key)
        // Use runCurrent so the disposal job is registered but doesn't run past its delay.
        runCurrent()

        val pendingJob: Job = container.disposingJobsForTest.values.singleOrNull()
            ?: error("Expected exactly one pending disposal job")
        assertFalse(pendingJob.isCancelled)

        callOnCleared(container)
        runCurrent()

        assertTrue(pendingJob.isCancelled)
    }

    @Test
    fun `onCleared closes all stored AutoCloseable objects`() = runTest(testDispatcher) {
        val container = ScopedViewModelContainer()

        val a = FakeCloseable()
        val b = FakeCloseable()
        container.storeForTest(InternalKey("a"), ExternalKey("ea"), a)
        container.storeForTest(InternalKey("b"), ExternalKey("eb"), b)

        callOnCleared(container)
        advanceUntilIdle()

        assertTrue(a.closed)
        assertTrue(b.closed)
        assertTrue(container.storedObjectKeysForTest.isEmpty())
    }

    // endregion
}

/**
 * `ViewModel.onCleared` is `protected`. The standard production trigger is [ViewModelStore.clear], which is the only
 * public path that invokes `onCleared`. Storing the container under a [ViewModelStore] keyed by class name and then
 * clearing the store yields the same effect without reflection.
 */
private fun callOnCleared(container: ScopedViewModelContainer) {
    val store = androidx.lifecycle.ViewModelStore()
    val provider = androidx.lifecycle.ViewModelProvider.create(
        owner = object : androidx.lifecycle.ViewModelStoreOwner {
            override val viewModelStore: androidx.lifecycle.ViewModelStore = store
        },
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: kotlin.reflect.KClass<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T = container as T
        }
    )
    // Force the provider to register `container` in `store` under its class key.
    provider.get(ScopedViewModelContainer::class)
    store.clear()
}

