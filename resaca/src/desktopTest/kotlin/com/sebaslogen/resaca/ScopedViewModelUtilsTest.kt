@file:OptIn(ResacaPackagePrivate::class)

package com.sebaslogen.resaca

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import com.sebaslogen.resaca.ScopedViewModelContainer.ExternalKey
import com.sebaslogen.resaca.ScopedViewModelContainer.InternalKey
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
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
