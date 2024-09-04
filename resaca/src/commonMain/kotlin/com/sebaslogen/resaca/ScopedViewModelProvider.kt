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
 */
@OptIn(ExperimentalNativeApi::class)
internal class ScopedViewModelProvider(
    private val factory: ViewModelProvider.Factory?,
    private val viewModelStore: ViewModelStore,
) {
    private var viewModelStoreOwnerDefaultViewModelProviderFactory: ViewModelProvider.Factory? = null

    /**
     * [cachedViewModelProvider] is a [WeakReference] to avoid memory leaks because the [ViewModelProvider] has a reference to the [CreationExtras],
     * which inside has references to Activity in Android.
     */
    private lateinit var cachedViewModelProvider: WeakReference<ViewModelProvider>

    /**
     * [cachedCreationExtras] needs to be cached to prevent creating and registering the same [ViewModelProvider] twice.
     * It is a [WeakReference] to avoid memory leaks because Android stores references to Activity in this object.
     */
    private lateinit var cachedCreationExtras: WeakReference<CreationExtras>

    internal fun getViewModelProvider(creationExtras: CreationExtras): ViewModelProvider =
        if (updateCreationExtras(creationExtras)) { // If the creationExtras are different, create a new ViewModelProvider
            createViewModelProvider(creationExtras)
        } else { // Return cached ViewModelProvider or new if cache is empty
            cachedViewModelProvider.get() ?: createViewModelProvider(creationExtras)
        }

    internal fun getCachedViewModelProvider(): ViewModelProvider? = cachedViewModelProvider.get()

    /**
     * Update the [cachedViewModelProvider] with the new [viewModelStoreOwner] and [CreationExtras].
     *
     * @param viewModelStoreOwner Used to extract possible defaultViewModelCreationExtras and defaultViewModelProviderFactory
     * @param newCreationExtras [CreationExtras] with default arguments that will be provided to the [ViewModel] through the [SavedStateHandle] and creationCallbacks.
     */
    @PublishedApi
    internal fun updateViewModelProvider(viewModelStoreOwner: ViewModelStoreOwner, newCreationExtras: CreationExtras) {
        val updatedCreationExtras = updateCreationExtras(newCreationExtras)
        val updatedViewModelProvider = updateViewModelProviderDependencies(viewModelStoreOwner)
        if (updatedCreationExtras || updatedViewModelProvider) {
            createViewModelProvider(newCreationExtras)
        }
    }

    private fun updateCreationExtras(newCreationExtras: CreationExtras): Boolean =
        if (!this::cachedCreationExtras.isInitialized || newCreationExtras != cachedCreationExtras.get()) {
            cachedCreationExtras = WeakReference(newCreationExtras)
            true
        } else {
            false
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
    private fun createViewModelProvider(creationExtras: CreationExtras): ViewModelProvider {
        val defaultFactory = viewModelStoreOwnerDefaultViewModelProviderFactory
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

