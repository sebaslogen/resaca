package com.sebaslogen.resaca

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.experimental.ExperimentalNativeApi

/**
 * This class provides a [ViewModelProvider] though the [getViewModelProvider] function.
 *
 * It creates the [ViewModelProvider] using the given [ViewModelProvider.Factory] and [viewModelStore] plus the
 * [CreationExtras] and default [ViewModelProvider.Factory] from the [ViewModelStoreOwner].
 *
 * @param viewModelStore Used to store and clear the [ViewModel]
 */
@OptIn(ExperimentalNativeApi::class)
internal class ScopedViewModelProvider(
    private val viewModelStore: ViewModelStore,
) {
    /**
     * Returns a [ViewModelProvider] using the [viewModelStoreOwner] and [CreationExtras].
     *
     * @param factory [ViewModelProvider] factory to create the requested [ViewModel]
     * @param viewModelStoreOwner Used to extract possible [ViewModelProvider.Factory] defaultViewModelProviderFactory
     * @param creationExtras [CreationExtras] with default arguments that will be provided to the [ViewModel] through the [SavedStateHandle] and creationCallbacks.
     *
     * @return [ViewModelProvider] created with the provided [factory] and [viewModelStore]
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

    private fun getDefaultFactory(viewModelStoreOwner: ViewModelStoreOwner): ViewModelProvider.Factory? =
        (viewModelStoreOwner as? HasDefaultViewModelProviderFactory)?.defaultViewModelProviderFactory

    /**
     * Create a [ViewModelProvider] by either:
     * - using the existing [factory], or
     * - using the default factory provided by the [ViewModelStoreOwner], or
     * - creating a default factory (e.g. for [ViewModel]s with no parameters in the constructor) using the [viewModelStore].
     *
     * @param factory [ViewModelProvider] factory to create the requested [ViewModel]
     * @param defaultFactory Default [ViewModelProvider.Factory] to create the requested [ViewModel] from the [ViewModelStoreOwner]
     * @param creationExtras [CreationExtras] with default arguments that will be provided to the [ViewModel] through the [SavedStateHandle] and creationCallbacks.
     *
     * @return [ViewModelProvider] created with the provided [factory] and [viewModelStore]
     */
    private fun createViewModelProvider(
        factory: ViewModelProvider.Factory?,
        defaultFactory: ViewModelProvider.Factory?,
        creationExtras: CreationExtras
    ): ViewModelProvider =
        when {
            factory != null -> ViewModelProvider.create(viewModelStore, factory, creationExtras)
            defaultFactory != null -> ViewModelProvider.create(viewModelStore, defaultFactory, creationExtras)
            else -> ViewModelProvider.create(owner = object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore
                    get() = this@ScopedViewModelProvider.viewModelStore
            })
        }
}
