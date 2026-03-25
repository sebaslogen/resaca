package com.sebaslogen.resaca

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class ScopedViewModelProviderTest {

    /** Simple ViewModel for testing. Has a no-arg constructor so it can be created by default factories. */
    internal class TestViewModel : ViewModel()

    // region helpers

    /** A plain ViewModelStoreOwner (no HasDefaultViewModelProviderFactory). */
    private fun plainOwner(store: ViewModelStore) = object : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = store
    }

    /** A ViewModelStoreOwner that also provides a default factory. */
    private fun ownerWithDefaultFactory(store: ViewModelStore, factory: ViewModelProvider.Factory) =
        object : ViewModelStoreOwner, HasDefaultViewModelProviderFactory {
            override val viewModelStore: ViewModelStore = store
            override val defaultViewModelProviderFactory: ViewModelProvider.Factory = factory
            override val defaultViewModelCreationExtras: CreationExtras = CreationExtras.Empty
        }

    /** A simple factory that creates TestViewModel instances. */
    private val testFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
            @Suppress("UNCHECKED_CAST")
            return TestViewModel() as T
        }
    }

    // endregion

    @Test
    internal fun `getCachedViewModelProvider returns null before any call`() {
        val provider = ScopedViewModelProvider(ViewModelStore())
        assertNull(provider.getCachedViewModelProvider())
    }

    @Test
    internal fun `getViewModelProvider with custom factory creates provider and caches it`() {
        val store = ViewModelStore()
        val provider = ScopedViewModelProvider(store)

        val vmProvider = provider.getViewModelProvider(
            factory = testFactory,
            viewModelStoreOwner = plainOwner(store),
            creationExtras = CreationExtras.Empty
        )

        assertNotNull(vmProvider)
        // Verify caching
        assertNotNull(provider.getCachedViewModelProvider())
    }

    @Test
    internal fun `getViewModelProvider with null factory and owner with default factory uses default factory`() {
        val store = ViewModelStore()
        val provider = ScopedViewModelProvider(store)

        val vmProvider = provider.getViewModelProvider(
            factory = null,
            viewModelStoreOwner = ownerWithDefaultFactory(store, testFactory),
            creationExtras = CreationExtras.Empty
        )

        assertNotNull(vmProvider)
        // Verify it can actually create a ViewModel with the default factory
        val vm = vmProvider.get(TestViewModel::class)
        assertNotNull(vm)
    }

    @Test
    internal fun `getViewModelProvider with null factory and plain owner creates fallback provider`() {
        val store = ViewModelStore()
        val provider = ScopedViewModelProvider(store)

        val vmProvider = provider.getViewModelProvider(
            factory = null,
            viewModelStoreOwner = plainOwner(store),
            creationExtras = CreationExtras.Empty
        )

        assertNotNull(vmProvider)
        // The fallback path creates a basic ViewModelProvider from an anonymous ViewModelStoreOwner
        // It should be able to create ViewModels with no-arg constructors
        val vm = vmProvider.get(TestViewModel::class)
        assertNotNull(vm)
    }

    @Test
    internal fun `getCachedViewModelProvider returns non-null after getViewModelProvider call`() {
        val store = ViewModelStore()
        val provider = ScopedViewModelProvider(store)

        provider.getViewModelProvider(
            factory = testFactory,
            viewModelStoreOwner = plainOwner(store),
            creationExtras = CreationExtras.Empty
        )

        assertNotNull(provider.getCachedViewModelProvider())
    }

    @Test
    internal fun `getViewModelProvider with custom factory takes priority over owner default factory`() {
        val store = ViewModelStore()
        val provider = ScopedViewModelProvider(store)
        val customFactory = testFactory

        // Owner also has a default factory, but the explicit factory should be used
        val vmProvider = provider.getViewModelProvider(
            factory = customFactory,
            viewModelStoreOwner = ownerWithDefaultFactory(store, object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                    throw IllegalStateException("Default factory should not be called when custom factory is provided")
                }
            }),
            creationExtras = CreationExtras.Empty
        )

        assertNotNull(vmProvider)
        // If the default factory were used, it would throw
        val vm = vmProvider.get(TestViewModel::class)
        assertNotNull(vm)
    }
}
