package com.sebaslogen.resaca

import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras

/**
 * This class provides a [ViewModelProvider] though its public [viewModelProvider] field.
 *
 * It creates the [ViewModelProvider] using the given [factory] and [viewModelStore] plus the
 * [CreationExtras] and default [ViewModelProvider.Factory] from the [ViewModelStoreOwner].
 *
 * The created [ViewModelProvider] is cached until the [ViewModelStoreOwner] is updated and
 * contains new [CreationExtras] or [ViewModelProvider.Factory].
 */
class ScopedViewModelProvider(
    private val factory: ViewModelProvider.Factory?,
    private val viewModelStore: ViewModelStore,
    viewModelStoreOwner: ViewModelStoreOwner
) {
    private var extras: CreationExtras = CreationExtras.Empty
    private var viewModelStoreOwnerDefaultViewModelProviderFactory: ViewModelProvider.Factory? = null
    lateinit var viewModelProvider: ViewModelProvider
        private set

    init {
        updateViewModelProvider(viewModelStoreOwner)
    }

    @PublishedApi
    internal fun updateViewModelProvider(viewModelStoreOwner: ViewModelStoreOwner) {
        val updated = updateViewModelProviderDependencies(viewModelStoreOwner)
        if (updated) updateViewModelProvider()
    }

    private fun updateViewModelProviderDependencies(viewModelStoreOwner: ViewModelStoreOwner):Boolean {
        val newExtras =
            if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
                viewModelStoreOwner.defaultViewModelCreationExtras
            } else {
                CreationExtras.Empty
            }
        val newViewModelStoreOwnerDefaultViewModelProviderFactory =
            (viewModelStoreOwner as? HasDefaultViewModelProviderFactory)?.defaultViewModelProviderFactory

        if (extras != newExtras || newViewModelStoreOwnerDefaultViewModelProviderFactory != viewModelStoreOwnerDefaultViewModelProviderFactory) {
            extras = newExtras
            viewModelStoreOwnerDefaultViewModelProviderFactory = newViewModelStoreOwnerDefaultViewModelProviderFactory
            return true
        }
        return false
    }

    /**
     * Create a [ViewModelProvider] by either:
     * - using the existing [factory], or
     * - using the default factory provided by the [ViewModelStoreOwner] in [updateViewModelProviderDependencies], or
     * - creating a default factory (e.g. for [ViewModel]s with no parameters in the constructor) using the [viewModelStore].
     */
    private fun updateViewModelProvider() {
        viewModelProvider = if (factory != null) {
            ViewModelProvider(viewModelStore, factory, extras)
        } else {
            viewModelStoreOwnerDefaultViewModelProviderFactory?.let { ViewModelProvider(viewModelStore, it, extras) }
                ?: ViewModelProvider { viewModelStore }
        }
    }
}