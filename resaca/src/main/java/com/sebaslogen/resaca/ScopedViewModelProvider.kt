package com.sebaslogen.resaca

import android.os.Bundle
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras

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
 * @param defaultArguments [Bundle] of default arguments that will be provided to the [ViewModel] through the [SavedStateHandle]
 * @param viewModelStoreOwner Used to extract possible defaultViewModelCreationExtras and defaultViewModelProviderFactory
 */
internal class ScopedViewModelProvider(
    private val factory: ViewModelProvider.Factory?,
    private val viewModelStore: ViewModelStore,
    private val defaultArguments: Bundle,
    viewModelStoreOwner: ViewModelStoreOwner
) {
    private var extras: CreationExtras = CreationExtras.Empty.addDefaultArguments()
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
        val newExtras =
            if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
                viewModelStoreOwner.defaultViewModelCreationExtras.addDefaultArguments()
            } else {
                CreationExtras.Empty.addDefaultArguments()
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
        val defaultFactory = viewModelStoreOwnerDefaultViewModelProviderFactory
        viewModelProvider = when {
            factory != null -> ViewModelProvider(viewModelStore, factory, extras)
            defaultFactory != null -> ViewModelProvider(viewModelStore, defaultFactory, extras)
            else -> ViewModelProvider(owner = object : ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore
                    get() = this@ScopedViewModelProvider.viewModelStore

            })
        }
    }

    /**
     * Combine the default arguments present in the receiver's [CreationExtras] under the key [DEFAULT_ARGS_KEY] with the [defaultArguments] of this class.
     * When the default arguments are not present just add them.
     */
    private fun CreationExtras.addDefaultArguments(): CreationExtras =
        if (defaultArguments.isEmpty) {
            this
        } else {
            MutableCreationExtras(this).apply {
                val combinedBundle = (get(DEFAULT_ARGS_KEY) ?: Bundle()).apply { putAll(defaultArguments) }
                set(DEFAULT_ARGS_KEY, combinedBundle)
            }
        }
}