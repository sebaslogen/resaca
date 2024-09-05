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
 * @param viewModelStore Used to store and clear the [ViewModel]
 */
@OptIn(ExperimentalNativeApi::class)
internal class ScopedViewModelProvider(
    private val viewModelStore: ViewModelStore,
) {
    /**
     * [cachedDefaultViewModelFactory] caches the [ViewModelProvider.Factory] from the [ViewModelStoreOwner].
     */
    private var cachedDefaultViewModelFactory: WeakReference<ViewModelProvider.Factory>? = null

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

    /**
     * Returns a [ViewModelProvider] using the [viewModelStoreOwner] and [CreationExtras].
     *
     * Updates the [cachedCreationExtras] and [cachedDefaultViewModelFactory]
     * with the provided [CreationExtras] and [viewModelStoreOwner] respectively if they are new.
     *
     * If there was an update in the [CreationExtras] or [cachedDefaultViewModelFactory],
     * or if there is no cached [ViewModelProvider] in [cachedViewModelProvider], then a new [ViewModelProvider] will be created.
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
    ): ViewModelProvider {
        val updatedCreationExtras = updateCreationExtras(creationExtras)
        val updatedViewModelProvider = updateDefaultFactory(viewModelStoreOwner)
        return if (updatedCreationExtras || updatedViewModelProvider) {
            createViewModelProvider(factory, creationExtras)
        } else { // Return cached ViewModelProvider or new if cache is empty
            getCachedViewModelProvider() ?: createViewModelProvider(factory, creationExtras)
        }
    }

    /**
     * Returns the cached [ViewModelProvider] or null if it was not created yet.
     * Useful to get a reference to the [ViewModelProvider] to get a [ViewModel] from it if the [ViewModel] was already created.
     */
    internal fun getCachedViewModelProvider(): ViewModelProvider? = if (this::cachedViewModelProvider.isInitialized) cachedViewModelProvider.get() else null

    private fun updateCreationExtras(newCreationExtras: CreationExtras): Boolean =
        if (!this::cachedCreationExtras.isInitialized || newCreationExtras != cachedCreationExtras.get()) {
            cachedCreationExtras = WeakReference(newCreationExtras)
            true
        } else {
            false
        }

    private fun updateDefaultFactory(viewModelStoreOwner: ViewModelStoreOwner): Boolean {
        val newDefaultViewModelFactory: ViewModelProvider.Factory? =
            (viewModelStoreOwner as? HasDefaultViewModelProviderFactory)?.defaultViewModelProviderFactory
        val cachedFactory: WeakReference<ViewModelProvider.Factory>? = cachedDefaultViewModelFactory
        return when {
            // There is a new factory and it's different from the cached one
            newDefaultViewModelFactory != null && newDefaultViewModelFactory != cachedFactory?.get() -> {
                cachedDefaultViewModelFactory = WeakReference(newDefaultViewModelFactory)
                true
            }

            newDefaultViewModelFactory == null && cachedFactory != null -> {
                cachedDefaultViewModelFactory = null
                true
            }

            else -> false
        }
    }

    /**
     * Create a [ViewModelProvider] by either:
     * - using the existing [factory], or
     * - using the default factory provided by the [ViewModelStoreOwner] in [updateDefaultFactory], or
     * - creating a default factory (e.g. for [ViewModel]s with no parameters in the constructor) using the [viewModelStore].
     *
     * This function also caches the created [ViewModelProvider] in [cachedViewModelProvider].
     *
     * @param factory [ViewModelProvider] factory to create the requested [ViewModel]
     * @param creationExtras [CreationExtras] with default arguments that will be provided to the [ViewModel] through the [SavedStateHandle] and creationCallbacks.
     * @return [ViewModelProvider] created with the provided [factory] (or [cachedDefaultViewModelFactory]) and [viewModelStore]
     */
    private fun createViewModelProvider(factory: ViewModelProvider.Factory?, creationExtras: CreationExtras): ViewModelProvider {
        val defaultFactory = cachedDefaultViewModelFactory?.get()
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

