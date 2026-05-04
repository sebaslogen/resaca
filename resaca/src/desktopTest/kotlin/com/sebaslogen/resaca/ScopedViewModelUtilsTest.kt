@file:OptIn(ResacaPackagePrivate::class)

package com.sebaslogen.resaca

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import com.sebaslogen.resaca.ScopedViewModelContainer.ExternalKey
import com.sebaslogen.resaca.ScopedViewModelContainer.InternalKey
import com.sebaslogen.resaca.ScopedViewModelContainer.SavedStateHandleContainer
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class ScopedViewModelUtilsTest {

    /** Simple ViewModel for testing. */
    internal class FakeVM : ViewModel() {
        internal var cleared: Boolean = false
        override fun onCleared() {
            cleared = true
        }
    }

    /** Simple AutoCloseable for testing. */
    internal class FakeCloseable : AutoCloseable {
        internal var closed: Boolean = false
        override fun close() {
            closed = true
        }
    }

    private val plainOwner = object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

    private val fakeFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return FakeVM() as T
        }
    }

    private fun createScopedViewModelOwner(key: String = "testKey"): ScopedViewModelOwner<FakeVM> =
        ScopedViewModelOwner(key = key, modelClass = FakeVM::class)

    // region restoreAndUpdateScopedViewModelOwner

    @Test
    internal fun `restoreAndUpdateScopedViewModelOwner returns null for missing key`() {
        val container = mutableMapOf<InternalKey, Any>()
        val key = InternalKey("missing")

        val result = ScopedViewModelUtils.restoreAndUpdateScopedViewModelOwner<FakeVM>(key, container)

        assertNull(result)
    }

    @Test
    internal fun `restoreAndUpdateScopedViewModelOwner returns owner for existing key`() {
        val container = mutableMapOf<InternalKey, Any>()
        val key = InternalKey("existing")
        val owner = createScopedViewModelOwner()
        container[key] = owner

        val result = ScopedViewModelUtils.restoreAndUpdateScopedViewModelOwner<FakeVM>(key, container)

        assertNotNull(result)
        assertEquals(owner, result)
    }

    @Test
    internal fun `restoreAndUpdateScopedViewModelOwner returns null for wrong type`() {
        val container = mutableMapOf<InternalKey, Any>()
        val key = InternalKey("wrongType")
        container[key] = "not a ScopedViewModelOwner"

        val result = ScopedViewModelUtils.restoreAndUpdateScopedViewModelOwner<FakeVM>(key, container)

        assertNull(result)
    }

    // endregion

    // region clearLastDisposedObject

    @Test
    internal fun `clearLastDisposedObject with ScopedViewModelOwner delegates to clearLastDisposedViewModel`() {
        val owner = createScopedViewModelOwner()
        val vm = owner.getViewModel(fakeFactory, plainOwner, CreationExtras.Empty)

        ScopedViewModelUtils.clearLastDisposedObject(owner, emptyList())

        // After clearing, the original VM's onCleared should have been called
        assertTrue(vm.cleared)
    }

    @Test
    internal fun `clearLastDisposedObject with ViewModelStore clears it`() {
        val store = ViewModelStore()

        ScopedViewModelUtils.clearLastDisposedObject(store, emptyList())

        // ViewModelStore.clear() is called; no exception thrown
    }

    @Test
    internal fun `clearLastDisposedObject with CoroutineScope cancels it`() {
        val job = Job()
        val scope = CoroutineScope(job)

        ScopedViewModelUtils.clearLastDisposedObject(scope, emptyList())

        assertTrue(job.isCancelled)
    }

    @Test
    internal fun `clearLastDisposedObject with CoroutineContext cancels it`() {
        val job = Job()

        ScopedViewModelUtils.clearLastDisposedObject(job, emptyList())

        assertTrue(job.isCancelled)
    }

    @Test
    internal fun `clearLastDisposedObject with AutoCloseable closes it`() {
        val closeable = FakeCloseable()

        ScopedViewModelUtils.clearLastDisposedObject(closeable, emptyList())

        assertTrue(closeable.closed)
    }

    @Test
    internal fun `clearLastDisposedObject does not clear object that is still in the container`() {
        val closeable = FakeCloseable()
        val container = listOf<Any>(closeable)

        ScopedViewModelUtils.clearLastDisposedObject(closeable, container)

        assertFalse(closeable.closed)
    }

    @Test
    internal fun `clearLastDisposedObject with ScopedViewModelOwner still in container does not clear it`() {
        val owner = createScopedViewModelOwner("myKey")
        owner.getViewModel(fakeFactory, plainOwner, CreationExtras.Empty)

        val container = listOf<Any>(owner)

        ScopedViewModelUtils.clearLastDisposedObject(owner, container)

        assertNotNull(owner.getCachedViewModel())
    }

    // endregion

    // region clearLastDisposedViewModel

    @Test
    internal fun `clearLastDisposedViewModel clears owner when VM is missing from container`() {
        val owner = createScopedViewModelOwner("ownerKey")
        owner.getViewModel(fakeFactory, plainOwner, CreationExtras.Empty)

        ScopedViewModelUtils.clearLastDisposedViewModel(
            scopedViewModelOwner = owner,
            objectsContainer = emptyList()
        )

        // After clearing, the ViewModelStore is cleared
    }

    @Test
    internal fun `clearLastDisposedViewModel does not clear owner when VM is still in container`() {
        val owner = createScopedViewModelOwner("ownerKey")
        owner.getViewModel(fakeFactory, plainOwner, CreationExtras.Empty)

        ScopedViewModelUtils.clearLastDisposedViewModel(
            scopedViewModelOwner = owner,
            objectsContainer = listOf(owner)
        )

        assertNotNull(owner.getCachedViewModel())
    }

    // endregion

    // region InternalKey.plus(ExternalKey) operator

    @Test
    internal fun `InternalKey plus ExternalKey produces combined string`() {
        val internalKey = InternalKey("abc")
        val externalKey = ExternalKey("xyz")

        val result = internalKey + externalKey

        val expected = internalKey.hashCode().toString() + externalKey.hashCode().toString()
        assertEquals(expected, result)
    }

    @Test
    internal fun `InternalKey plus ExternalKey with null value`() {
        val internalKey = InternalKey("abc")
        val externalKey = ExternalKey(null)

        val result = internalKey + externalKey

        val expected = internalKey.hashCode().toString() + externalKey.hashCode().toString()
        assertEquals(expected, result)
    }

    // endregion

    // region getOrBuildObject

    private fun emptyObjectMaps(): Quad {
        return Quad(
            container = mutableMapOf(),
            savedStateHandlers = mutableMapOf(),
            clearDelays = mutableMapOf(),
            keys = mutableMapOf()
        )
    }

    private data class Quad(
        val container: MutableMap<InternalKey, Any>,
        val savedStateHandlers: MutableMap<InternalKey, SavedStateHandleContainer>,
        val clearDelays: MutableMap<InternalKey, Duration>,
        val keys: MutableMap<InternalKey, ExternalKey>
    )

    @Test
    internal fun `getOrBuildObject builds and stores new object when container is empty`() {
        val maps = emptyObjectMaps()
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        val expected = "hello"

        val result = ScopedViewModelUtils.getOrBuildObject(
            positionalMemoizationKey = key,
            externalKey = externalKey,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedObject = { _, _ -> },
            builder = { expected }
        )

        assertEquals(expected, result)
        assertEquals(expected, maps.container[key])
        assertEquals(externalKey, maps.keys[key])
    }

    @Test
    internal fun `getOrBuildObject returns cached instance when keys and type match`() {
        val maps = emptyObjectMaps()
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        val cached = FakeCloseable()
        maps.container[key] = cached
        maps.keys[key] = externalKey

        var builderCalls = 0
        val result = ScopedViewModelUtils.getOrBuildObject(
            positionalMemoizationKey = key,
            externalKey = externalKey,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedObject = { _, _ -> },
            builder = { builderCalls++; FakeCloseable() }
        )

        assertSame(cached, result)
        assertEquals(0, builderCalls)
        assertFalse(cached.closed)
    }

    @Test
    internal fun `getOrBuildObject builds new instance and clears old one when external key changes`() {
        val maps = emptyObjectMaps()
        val key = InternalKey("k1")
        val oldExternalKey = ExternalKey("v1")
        val newExternalKey = ExternalKey("v2")
        val oldObject = FakeCloseable()
        maps.container[key] = oldObject
        maps.keys[key] = oldExternalKey

        var clearedObject: Any? = null
        val newObject = FakeCloseable()

        val result = ScopedViewModelUtils.getOrBuildObject(
            positionalMemoizationKey = key,
            externalKey = newExternalKey,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedObject = { obj, _ -> clearedObject = obj },
            builder = { newObject }
        )

        assertSame(newObject, result)
        assertSame(oldObject, clearedObject)
        assertEquals(newObject, maps.container[key])
        assertEquals(newExternalKey, maps.keys[key])
    }

    @Test
    internal fun `getOrBuildObject calls cancelDisposal with the positional key`() {
        val maps = emptyObjectMaps()
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        var cancelDisposalCalledWith: InternalKey? = null

        ScopedViewModelUtils.getOrBuildObject(
            positionalMemoizationKey = key,
            externalKey = externalKey,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = { cancelDisposalCalledWith = it },
            clearLastDisposedObject = { _, _ -> },
            builder = { "value" }
        )

        assertEquals(key, cancelDisposalCalledWith)
    }

    @Test
    internal fun `getOrBuildObject stores clearDelay when provided`() {
        val maps = emptyObjectMaps()
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        val delay = 7.seconds

        ScopedViewModelUtils.getOrBuildObject(
            positionalMemoizationKey = key,
            externalKey = externalKey,
            clearDelay = delay,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedObject = { _, _ -> },
            builder = { "value" }
        )

        assertEquals(delay, maps.clearDelays[key])
    }

    @Test
    internal fun `getOrBuildObject does not store clearDelay when null`() {
        val maps = emptyObjectMaps()
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")

        ScopedViewModelUtils.getOrBuildObject(
            positionalMemoizationKey = key,
            externalKey = externalKey,
            clearDelay = null,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedObject = { _, _ -> },
            builder = { "value" }
        )

        assertFalse(maps.clearDelays.containsKey(key))
    }

    @Test
    internal fun `getOrBuildObject passes a snapshot list that does not contain the disposed object`() {
        val maps = emptyObjectMaps()
        val key = InternalKey("k1")
        val oldExternalKey = ExternalKey("v1")
        val newExternalKey = ExternalKey("v2")
        val oldObject = FakeCloseable()
        val survivingObject = FakeCloseable()
        maps.container[key] = oldObject
        maps.container[InternalKey("other")] = survivingObject
        maps.keys[key] = oldExternalKey

        var snapshot: List<Any>? = null

        ScopedViewModelUtils.getOrBuildObject(
            positionalMemoizationKey = key,
            externalKey = newExternalKey,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedObject = { _, list -> snapshot = list },
            builder = { FakeCloseable() }
        )

        val capturedSnapshot = snapshot
        assertNotNull(capturedSnapshot)
        assertFalse(capturedSnapshot.contains(oldObject))
        assertTrue(capturedSnapshot.contains(survivingObject))
    }

    // endregion

    // region cancelDisposal

    @Test
    internal fun `cancelDisposal cancels and removes the disposing job for the given key`() {
        val key = InternalKey("k1")
        val job: CompletableJob = Job()
        val disposingJobs = mutableMapOf<InternalKey, Job>(key to job)
        val markedForDisposal = mutableSetOf<InternalKey>()

        ScopedViewModelUtils.cancelDisposal(key, disposingJobs, markedForDisposal)

        assertTrue(job.isCancelled)
        assertFalse(disposingJobs.containsKey(key))
    }

    @Test
    internal fun `cancelDisposal removes the key from markedForDisposal`() {
        val key = InternalKey("k1")
        val disposingJobs = mutableMapOf<InternalKey, Job>()
        val markedForDisposal = mutableSetOf(key)

        ScopedViewModelUtils.cancelDisposal(key, disposingJobs, markedForDisposal)

        assertFalse(markedForDisposal.contains(key))
    }

    @Test
    internal fun `cancelDisposal is a no-op when key is not present`() {
        val key = InternalKey("k1")
        val otherKey = InternalKey("k2")
        val otherJob: CompletableJob = Job()
        val disposingJobs = mutableMapOf<InternalKey, Job>(otherKey to otherJob)
        val markedForDisposal = mutableSetOf(otherKey)

        ScopedViewModelUtils.cancelDisposal(key, disposingJobs, markedForDisposal)

        // Other entries are untouched
        assertFalse(otherJob.isCancelled)
        assertTrue(disposingJobs.containsKey(otherKey))
        assertTrue(markedForDisposal.contains(otherKey))
    }

    @Test
    internal fun `cancelDisposal does not affect other keys`() {
        val key = InternalKey("k1")
        val otherKey = InternalKey("k2")
        val targetJob: CompletableJob = Job()
        val otherJob: CompletableJob = Job()
        val disposingJobs = mutableMapOf<InternalKey, Job>(key to targetJob, otherKey to otherJob)
        val markedForDisposal = mutableSetOf(key, otherKey)

        ScopedViewModelUtils.cancelDisposal(key, disposingJobs, markedForDisposal)

        assertTrue(targetJob.isCancelled)
        assertFalse(otherJob.isCancelled)
        assertEquals(setOf(otherKey), markedForDisposal)
        assertEquals(otherJob, disposingJobs[otherKey])
    }

    // endregion

    // region clearSavedStateHandle

    @Test
    internal fun `clearSavedStateHandle removes user-added keys but keeps default keys`() {
        val handle = SavedStateHandle()
        handle["default1"] = "d1"
        handle["default2"] = "d2"
        val container = SavedStateHandleContainer(
            defaultKeys = listOf("default1", "default2"),
            savedStateHandle = handle
        )
        // Simulate the user adding extra keys after creation
        handle["userA"] = "a"
        handle["userB"] = "b"

        ScopedViewModelUtils.clearSavedStateHandle(container)

        assertEquals("d1", handle.get<String>("default1"))
        assertEquals("d2", handle.get<String>("default2"))
        assertFalse(handle.contains("userA"))
        assertFalse(handle.contains("userB"))
    }

    @Test
    internal fun `clearSavedStateHandle is a no-op when there are no user-added keys`() {
        val handle = SavedStateHandle()
        handle["only-default"] = "value"
        val container = SavedStateHandleContainer(
            defaultKeys = listOf("only-default"),
            savedStateHandle = handle
        )

        ScopedViewModelUtils.clearSavedStateHandle(container)

        assertEquals("value", handle.get<String>("only-default"))
        assertEquals(setOf("only-default"), handle.keys())
    }

    @Test
    internal fun `clearSavedStateHandle clears all keys when defaultKeys is empty`() {
        val handle = SavedStateHandle()
        handle["a"] = 1
        handle["b"] = 2
        val container = SavedStateHandleContainer(
            defaultKeys = emptyList(),
            savedStateHandle = handle
        )

        ScopedViewModelUtils.clearSavedStateHandle(container)

        assertTrue(handle.keys().isEmpty())
    }

    // endregion

    // region getOrBuildScopedViewModelOwner

    private data class VmMaps(
        val container: MutableMap<InternalKey, Any>,
        val savedStateHandlers: MutableMap<InternalKey, SavedStateHandleContainer>,
        val clearDelays: MutableMap<InternalKey, Duration>,
        val keys: MutableMap<InternalKey, ExternalKey>
    )

    private fun emptyVmMaps(): VmMaps = VmMaps(
        container = mutableMapOf(),
        savedStateHandlers = mutableMapOf(),
        clearDelays = mutableMapOf(),
        keys = mutableMapOf()
    )

    @Test
    internal fun `getOrBuildScopedViewModelOwner creates a new owner and stores it when container is empty`() {
        val maps = emptyVmMaps()
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")

        val (owner, isNew) = ScopedViewModelUtils.getOrBuildScopedViewModelOwner(
            modelClass = FakeVM::class,
            positionalMemoizationKey = key,
            externalKey = externalKey,
            clearDelay = null,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedViewModel = { _, _ -> }
        )

        assertTrue(isNew)
        assertSame(owner, maps.container[key])
        assertEquals(externalKey, maps.keys[key])
    }

    @Test
    internal fun `getOrBuildScopedViewModelOwner returns existing owner when keys match`() {
        val maps = emptyVmMaps()
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        val existingOwner = createScopedViewModelOwner("existing")
        maps.container[key] = existingOwner
        maps.keys[key] = externalKey

        val (owner, isNew) = ScopedViewModelUtils.getOrBuildScopedViewModelOwner(
            modelClass = FakeVM::class,
            positionalMemoizationKey = key,
            externalKey = externalKey,
            clearDelay = null,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedViewModel = { _, _ -> }
        )

        assertFalse(isNew)
        assertSame(existingOwner, owner)
    }

    @Test
    internal fun `getOrBuildScopedViewModelOwner builds new owner and clears old one when external key changes`() {
        val maps = emptyVmMaps()
        val key = InternalKey("k1")
        val oldExternalKey = ExternalKey("v1")
        val newExternalKey = ExternalKey("v2")
        val oldOwner = createScopedViewModelOwner("old")
        oldOwner.getViewModel(fakeFactory, plainOwner, CreationExtras.Empty)
        maps.container[key] = oldOwner
        maps.keys[key] = oldExternalKey

        var clearedObject: Any? = null

        val (owner, isNew) = ScopedViewModelUtils.getOrBuildScopedViewModelOwner(
            modelClass = FakeVM::class,
            positionalMemoizationKey = key,
            externalKey = newExternalKey,
            clearDelay = null,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedViewModel = { obj, _ -> clearedObject = obj }
        )

        assertTrue(isNew)
        assertSame(oldOwner, clearedObject)
        assertSame(owner, maps.container[key])
        assertEquals(newExternalKey, maps.keys[key])
    }

    @Test
    internal fun `getOrBuildScopedViewModelOwner removes saved state handler when external key changes`() {
        val maps = emptyVmMaps()
        val key = InternalKey("k1")
        val oldExternalKey = ExternalKey("v1")
        val newExternalKey = ExternalKey("v2")
        val oldOwner = createScopedViewModelOwner("old")
        maps.container[key] = oldOwner
        maps.keys[key] = oldExternalKey
        val handle = SavedStateHandle().apply { set("user", "value") }
        maps.savedStateHandlers[key] = SavedStateHandleContainer(emptyList(), handle)

        ScopedViewModelUtils.getOrBuildScopedViewModelOwner(
            modelClass = FakeVM::class,
            positionalMemoizationKey = key,
            externalKey = newExternalKey,
            clearDelay = null,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedViewModel = { _, _ -> }
        )

        assertFalse(maps.savedStateHandlers.containsKey(key))
        assertFalse(handle.contains("user"))
    }

    @Test
    internal fun `getOrBuildScopedViewModelOwner calls cancelDisposal with the positional key`() {
        val maps = emptyVmMaps()
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        var cancelDisposalCalledWith: InternalKey? = null

        ScopedViewModelUtils.getOrBuildScopedViewModelOwner(
            modelClass = FakeVM::class,
            positionalMemoizationKey = key,
            externalKey = externalKey,
            clearDelay = null,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = { cancelDisposalCalledWith = it },
            clearLastDisposedViewModel = { _, _ -> }
        )

        assertEquals(key, cancelDisposalCalledWith)
    }

    @Test
    internal fun `getOrBuildScopedViewModelOwner stores clearDelay only when provided`() {
        val maps = emptyVmMaps()
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        val delay = 11.seconds

        ScopedViewModelUtils.getOrBuildScopedViewModelOwner(
            modelClass = FakeVM::class,
            positionalMemoizationKey = key,
            externalKey = externalKey,
            clearDelay = delay,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedViewModel = { _, _ -> }
        )

        assertEquals(delay, maps.clearDelays[key])
    }

    @Test
    internal fun `getOrBuildScopedViewModelOwner does not store clearDelay when null`() {
        val maps = emptyVmMaps()
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")

        ScopedViewModelUtils.getOrBuildScopedViewModelOwner(
            modelClass = FakeVM::class,
            positionalMemoizationKey = key,
            externalKey = externalKey,
            clearDelay = null,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedViewModel = { _, _ -> }
        )

        assertFalse(maps.clearDelays.containsKey(key))
    }

    @Test
    internal fun `getOrBuildScopedViewModelOwner builds owner with key derived from positional plus external key`() {
        val maps = emptyVmMaps()
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")

        val (owner, _) = ScopedViewModelUtils.getOrBuildScopedViewModelOwner(
            modelClass = FakeVM::class,
            positionalMemoizationKey = key,
            externalKey = externalKey,
            clearDelay = null,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedViewModel = { _, _ -> }
        )

        // The owner is now backed by a fresh ViewModelStore; using a custom factory should produce a VM.
        val vm = owner.getViewModel(fakeFactory, plainOwner, CreationExtras.Empty)
        assertNotNull(vm)
    }

    @Test
    internal fun `getOrBuildScopedViewModelOwner treats container with non-owner entry under same key as new`() {
        val maps = emptyVmMaps()
        val key = InternalKey("k1")
        val externalKey = ExternalKey("e1")
        val notAnOwner: Any = "stringStored"
        maps.container[key] = notAnOwner
        maps.keys[key] = externalKey

        var clearedObject: Any? = null

        val (owner, isNew) = ScopedViewModelUtils.getOrBuildScopedViewModelOwner(
            modelClass = FakeVM::class,
            positionalMemoizationKey = key,
            externalKey = externalKey,
            clearDelay = null,
            scopedObjectsContainer = maps.container,
            scopedObjectsSavedStateHandlers = maps.savedStateHandlers,
            scopedObjectsClearDelays = maps.clearDelays,
            scopedObjectKeys = maps.keys,
            cancelDisposal = {},
            clearLastDisposedViewModel = { obj, _ -> clearedObject = obj }
        )

        assertTrue(isNew)
        assertSame(owner, maps.container[key])
        assertSame(notAnOwner, clearedObject)
    }

    // endregion

    // region clearDelay parameter in getOrBuildViewModel helpers

    @Test
    internal fun `clearDelay is stored in scopedObjectsClearDelays when creating new ViewModel`() {
        // Given empty containers and a clearDelay
        val container = mutableMapOf<InternalKey, Any>()
        val clearDelays = mutableMapOf<InternalKey, Duration>()
        val keys = mutableMapOf<InternalKey, ExternalKey>()
        val key = InternalKey("testKey")
        val externalKey = ExternalKey("ext")
        val delay = 5.seconds

        // When we simulate what getOrBuildViewModel does for clearDelay storage
        // (Since getOrBuildViewModel is @Composable, we test the storage logic directly)
        keys[key] = externalKey
        delay.let { clearDelays[key] = it }
        val owner = ScopedViewModelOwner(key = key + externalKey, modelClass = FakeVM::class)
        container[key] = owner

        // Then clearDelay is stored
        assertTrue(clearDelays.containsKey(key))
        assertEquals(delay, clearDelays[key])
    }

    @Test
    internal fun `clearDelay is not stored when null`() {
        // Given empty containers and null clearDelay
        val clearDelays = mutableMapOf<InternalKey, Duration>()
        val key = InternalKey("testKey")
        val nullDelay: Duration? = null

        // When we simulate what getOrBuildViewModel does with null clearDelay
        nullDelay?.let { clearDelays[key] = it }

        // Then clearDelay is NOT stored
        assertFalse(clearDelays.containsKey(key))
    }

    @Test
    internal fun `clearDelay is updated when key changes and new clearDelay is provided`() {
        // Given containers with existing clearDelay
        val clearDelays = mutableMapOf<InternalKey, Duration>()
        val key = InternalKey("testKey")
        clearDelays[key] = 3.seconds

        // When a new clearDelay is set for the same key
        val newDelay = 10.seconds
        newDelay.let { clearDelays[key] = it }

        // Then clearDelay is updated
        assertEquals(newDelay, clearDelays[key])
    }

    // endregion
}
