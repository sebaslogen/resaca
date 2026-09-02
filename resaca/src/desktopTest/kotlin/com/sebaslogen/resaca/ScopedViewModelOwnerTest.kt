@file:OptIn(ResacaPackagePrivate::class)

package com.sebaslogen.resaca

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import java.lang.ref.WeakReference
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

internal class ScopedViewModelOwnerTest {

    /** Test ViewModel that tracks an id. */
    internal class FakeViewModel(internal val id: Int = 0) : ViewModel()

    // region helpers

    private val plainOwner = object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

    private val fakeFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return FakeViewModel(id = 42) as T
        }
    }

    // endregion

    @Test
    internal fun `getViewModel returns a ViewModel instance with custom factory`() {
        val owner = ScopedViewModelOwner(
            key = "testKey",
            modelClass = FakeViewModel::class
        )

        val vm = owner.getViewModel(
            factory = fakeFactory,
            viewModelStoreOwner = plainOwner,
            creationExtras = CreationExtras.Empty
        )

        assertNotNull(vm)
        assertEquals(42, vm.id)
    }

    @Test
    internal fun `getCachedViewModel returns null before getViewModel is called`() {
        val owner = ScopedViewModelOwner(
            key = "testKey",
            modelClass = FakeViewModel::class
        )

        assertNull(owner.getCachedViewModel())
    }

    @Test
    internal fun `getCachedViewModel returns same instance after getViewModel`() {
        val owner = ScopedViewModelOwner(
            key = "testKey",
            modelClass = FakeViewModel::class
        )

        val vm = owner.getViewModel(
            factory = fakeFactory,
            viewModelStoreOwner = plainOwner,
            creationExtras = CreationExtras.Empty
        )

        val cachedVm = owner.getCachedViewModel()
        assertNotNull(cachedVm)
        assertEquals(vm, cachedVm)
    }

    @Test
    internal fun `getCachedViewModel keeps returning the ViewModel once the ViewModelProvider is garbage collected`() {
        val owner = ScopedViewModelOwner(
            key = "testKey",
            modelClass = FakeViewModel::class
        )

        val vm = owner.getViewModel(
            factory = fakeFactory,
            viewModelStoreOwner = plainOwner,
            creationExtras = CreationExtras.Empty
        )
        // Nothing outside of this owner references the ViewModelProvider that created the ViewModel anymore
        forceGarbageCollection()

        // The owner still reports the ViewModel it holds, otherwise a ViewModel shared by two Composables
        // would be wrongly cleared as soon as one of them is disposed
        assertSame(vm, owner.getCachedViewModel())
    }

    @Test
    internal fun `getCachedViewModel returns null after clear`() {
        val owner = ScopedViewModelOwner(
            key = "testKey",
            modelClass = FakeViewModel::class
        )
        owner.getViewModel(
            factory = fakeFactory,
            viewModelStoreOwner = plainOwner,
            creationExtras = CreationExtras.Empty
        )

        owner.clear()

        assertNull(owner.getCachedViewModel())
    }

    @Test
    internal fun `clear clears the underlying ViewModelStore`() {
        val owner = ScopedViewModelOwner(
            key = "testKey",
            modelClass = FakeViewModel::class
        )

        // Create a ViewModel first
        owner.getViewModel(
            factory = fakeFactory,
            viewModelStoreOwner = plainOwner,
            creationExtras = CreationExtras.Empty
        )

        // Clear
        owner.clear()

        // After clear, the ViewModelStore is cleared. No exception should be thrown.
    }

    @Test
    internal fun `viewModelFactoryFor creates factory that invokes builder with SavedStateHandle`() {
        var capturedHandle: SavedStateHandle? = null
        val savedStateHandle = SavedStateHandle()

        val factory = ScopedViewModelOwner.viewModelFactoryFor(savedStateHandle) { handle ->
            capturedHandle = handle
            FakeViewModel(id = 99)
        }

        val vm = factory.create(FakeViewModel::class, CreationExtras.Empty)

        assertNotNull(vm)
        assertEquals(99, vm.id)
        assertEquals(savedStateHandle, capturedHandle)
    }

    /**
     * Best effort attempt to make the JVM collect everything that is only weakly reachable.
     * It stops as soon as a canary reference is collected, or after enough attempts to keep the test bounded.
     */
    private fun forceGarbageCollection() {
        val canary = WeakReference(Any())
        repeat(20) {
            if (canary.get() == null) return
            System.gc()
            @Suppress("UNUSED_EXPRESSION")
            ByteArray(1 shl 20) // Allocate garbage to give the collector a reason to run
        }
    }
}
