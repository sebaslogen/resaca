package com.sebaslogen.resaca

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras

/**
 * This class provides a [ViewModelProvider] though its public [viewModelProvider] field.
 *
 * It creates the [ViewModelProvider] using the given [factory] and [viewModelStore] plus the
 * [CreationExtras] and default [ViewModelProvider.Factory] from the [ViewModelStoreOwner].
 *
 * The created [ViewModelProvider] is cached until the [ViewModelStoreOwner] is updated and
 * contains new [CreationExtras] or [ViewModelProvider.Factory].
 *
 * @param factory [ViewModelProvider] factory to create the requested [ViewModel] when required
 * @param viewModelStore Used to store and clear the [ViewModel]
 * @param creationExtras [CreationExtras] with default arguments that will be provided to the [ViewModel] through the [SavedStateHandle] and creationCallbacks.
 * @param viewModelStoreOwner Used to extract possible defaultViewModelCreationExtras and defaultViewModelProviderFactory
 */
internal class ScopedViewModelProvider(
    private val factory: ViewModelProvider.Factory?,
    private val viewModelStore: ViewModelStore,
    private val creationExtras: CreationExtras,
    viewModelStoreOwner: ViewModelStoreOwner
) {
    //    private var extras: CreationExtras = CreationExtras.Empty.addDefaultArguments()
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

    private fun updateViewModelProviderDependencies(viewModelStoreOwner: ViewModelStoreOwner): Boolean {
        val newViewModelStoreOwnerDefaultViewModelProviderFactory =
            (viewModelStoreOwner as? HasDefaultViewModelProviderFactory)?.defaultViewModelProviderFactory

        if (newViewModelStoreOwnerDefaultViewModelProviderFactory != viewModelStoreOwnerDefaultViewModelProviderFactory) {
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
        val defaultFactory = viewModelStoreOwnerDefaultViewModelProviderFactory
        viewModelProvider = when {
            factory != null -> ViewModelProvider(viewModelStore, factory, creationExtras)
            defaultFactory != null -> ViewModelProvider(viewModelStore, defaultFactory, creationExtras)
            else -> ViewModelProvider(owner = object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore
                    get() = this@ScopedViewModelProvider.viewModelStore
            })
        }
    }
}