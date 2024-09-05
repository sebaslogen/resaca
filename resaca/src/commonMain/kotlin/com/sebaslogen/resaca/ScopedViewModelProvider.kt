package com.sebaslogen.resaca

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import com.sebaslogen.resaca.utils.WeakReference
import kotlin.experimental.ExperimentalNativeApi

/**
 * This class provides a [ViewModelProvider] though the [getViewModelProvider] function.
 *
 * It creates the [ViewModelProvider] using the given [ViewModelProvider.Factory] and [viewModelStore] plus the
 * [CreationExtras] and default [ViewModelProvider.Factory] from the [ViewModelStoreOwner].
 *
 * The created [ViewModelProvider] is cached in a [WeakReference] to avoid memory leaks.
 *
 * @param viewModelStore Used to store and clear the [ViewModel]
 */
@OptIn(ExperimentalNativeApi::class)
internal class ScopedViewModelProvider(
    private val viewModelStore: ViewModelStore,
) {
    /**
     * Caches the created [ViewModelProvider] in the first request for a [ViewModel], in case the same [ViewModel] is requested again.
     * It is a [WeakReference] to avoid memory leaks because the [ViewModelProvider] has a reference to the [CreationExtras],
     * which inside has references to Activity in Android.
     */
    private var cachedViewModelProvider: WeakReference<ViewModelProvider>? = null

    /**
     * Returns a [ViewModelProvider] using the [viewModelStoreOwner] and [CreationExtras].
     *
     * @param factory [ViewModelProvider] factory to create the requested [ViewModel]
     * @param viewModelStoreOwner Used to extract possible defaultViewModelCreationExtras and defaultViewModelProviderFactory
     * @param creationExtras [CreationExtras] with default arguments that will be provided to the [ViewModel] through the [SavedStateHandle] and creationCallbacks.
     * @return [ViewModelProvider] created with the provided [factory] (or [cachedDefaultViewModelFactory]) and [viewModelStore]
     */
    internal fun getViewModelProvider(
        factory: ViewModelProvider.Factory?,
        viewModelStoreOwner: ViewModelStoreOwner,
        creationExtras: CreationExtras
    ): ViewModelProvider =
        createViewModelProvider(
            factory = factory,
            defaultFactory = getDefaultFactory(viewModelStoreOwner),
            creationExtras = creationExtras
        )

    /**
     * Returns the cached [ViewModelProvider] or null if it was not created yet.
     * Useful to get a reference to the [ViewModelProvider] to get a [ViewModel] from it if the [ViewModel] was already created.
     */
    internal fun getCachedViewModelProvider(): ViewModelProvider? = cachedViewModelProvider?.get()

    private fun getDefaultFactory(viewModelStoreOwner: ViewModelStoreOwner): ViewModelProvider.Factory? =
        (viewModelStoreOwner as? HasDefaultViewModelProviderFactory)?.defaultViewModelProviderFactory

    /**
     * Create a [ViewModelProvider] by either:
     * - using the existing [factory], or
     * - using the default factory provided by the [ViewModelStoreOwner], or
     * - creating a default factory (e.g. for [ViewModel]s with no parameters in the constructor) using the [viewModelStore].
     *
     * This function also caches the created [ViewModelProvider] in [cachedViewModelProvider].
     *
     * @param factory [ViewModelProvider] factory to create the requested [ViewModel]
     * @param defaultFactory Default [ViewModelProvider.Factory] to create the requested [ViewModel] from the [ViewModelStoreOwner]
     * @param creationExtras [CreationExtras] with default arguments that will be provided to the [ViewModel] through the [SavedStateHandle] and creationCallbacks.
     * @return [ViewModelProvider] created with the provided [factory] (or [cachedDefaultViewModelFactory]) and [viewModelStore]
     */
    private fun createViewModelProvider(
        factory: ViewModelProvider.Factory?,
        defaultFactory: ViewModelProvider.Factory?,
        creationExtras: CreationExtras
    ): ViewModelProvider {
        val viewModelProvider = when {
            factory != null -> ViewModelProvider.create(viewModelStore, factory, creationExtras)
            defaultFactory != null -> ViewModelProvider.create(viewModelStore, defaultFactory, creationExtras)
            else -> ViewModelProvider.create(owner = object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore
                    get() = this@ScopedViewModelProvider.viewModelStore
            })
        }
        cachedViewModelProvider = WeakReference(viewModelProvider)
        return viewModelProvider
    }
}
