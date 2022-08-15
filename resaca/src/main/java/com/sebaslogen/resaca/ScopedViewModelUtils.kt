package com.sebaslogen.resaca

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import java.io.Closeable
import kotlin.coroutines.CoroutineContext


/**
 * Set of functions to help create, store, retrieve and clear a [ViewModel].
 */
object ScopedViewModelUtils {

    /**
     * Returns an existing [ViewModel] of type [T] or creates a new one if none was present in the [scopedObjectsContainer].
     *
     * This function will also compare [positionalMemoizationKey] and [externalKey] to determine
     * whether a new instance of [T] needs to be created or an existing one can be returned.
     *
     * The creation, storage, retrieval and clean up of the [ViewModel] will be taken care of
     * by a [ScopedViewModelOwner] which will be the actual object stored in the [scopedObjectsContainer].
     */
    @Composable
    fun <T : ViewModel> getOrBuildViewModel(
        modelClass: Class<T>,
        positionalMemoizationKey: String,
        externalKey: ScopedViewModelContainer.ExternalKey = ScopedViewModelContainer.ExternalKey(),
        factory: ViewModelProvider.Factory,
        viewModelStoreOwner: ViewModelStoreOwner,
        scopedObjectsContainer: MutableMap<String, Any>,
        scopedObjectKeys: MutableMap<String, ScopedViewModelContainer.ExternalKey>,
        cancelDisposal: ((String) -> Unit)
    ): T {
        cancelDisposal(positionalMemoizationKey)

        @Suppress("UNCHECKED_CAST")
        val originalScopedViewModelOwner: ScopedViewModelOwner<T>? = scopedObjectsContainer[positionalMemoizationKey] as? ScopedViewModelOwner<T>

        val viewModel: T =
            if (scopedObjectKeys.containsKey(positionalMemoizationKey)
                && (scopedObjectKeys[positionalMemoizationKey] == externalKey)
                && originalScopedViewModelOwner is ScopedViewModelOwner
            ) {
                // When the object is already present and the external key matches, then return the existing one in the ScopedViewModelOwner
                originalScopedViewModelOwner.viewModel
            } else { // First time ViewModel creation or externalKey changed
                scopedObjectKeys[positionalMemoizationKey] = externalKey // Set the new external key used to track and store the new object version
                val newScopedViewModelOwner = ScopedViewModelOwner(
                    key = positionalMemoizationKey,
                    modelClass = modelClass,
                    factory = factory,
                    viewModelStoreOwner = viewModelStoreOwner
                )
                scopedObjectsContainer[positionalMemoizationKey] = newScopedViewModelOwner

                // Clean-up if needed: the old object is cleared before it's forgotten
                originalScopedViewModelOwner?.let { clearLastDisposedViewModel(it, scopedObjectsContainer.values.toList()) }

                newScopedViewModelOwner.viewModel
            }
        return viewModel
    }

    /**
     * Returns an existing [ViewModel] of type [T] or creates a new one if none was present in the [scopedObjectsContainer].
     *
     * The creation, storage, retrieval and clean up of the [ViewModel] will be taken care of
     * by a [ScopedViewModelOwner] which will be the actual object stored in the [scopedObjectsContainer].
     *
     * Note: There is no support for keys in Hilt therefore the same [ViewModelStore] per type is used for all Hilt
     * ViewModels of the same type [T] inside the container scope (Activity/Fragment/Nav. destination).
     * The same [ViewModel] will always be returned once created until disposal of all the Composables using it.
     * Support for keys in the Hilt library is still a WIP. See https://github.com/google/dagger/issues/2328
     */
    @Composable
    inline fun <T : ViewModel> getOrBuildHiltViewModel(
        modelClass: Class<T>,
        positionalMemoizationKey: String,
        externalKey: ScopedViewModelContainer.ExternalKey = ScopedViewModelContainer.ExternalKey(),
        factory: ViewModelProvider.Factory?,
        viewModelStoreOwner: ViewModelStoreOwner,
        scopedObjectsContainer: MutableMap<String, Any>,
        scopedObjectKeys: MutableMap<String, ScopedViewModelContainer.ExternalKey>,
        cancelDisposal: ((String) -> Unit)
    ): T {
        cancelDisposal(positionalMemoizationKey)

        val originalScopedViewModelOwner: ScopedViewModelOwner<T>? =
            restoreAndUpdateScopedViewModelOwner(positionalMemoizationKey, scopedObjectsContainer, viewModelStoreOwner)

        val scopedViewModelOwner = if (scopedObjectKeys.containsKey(positionalMemoizationKey) && (scopedObjectKeys[positionalMemoizationKey] == externalKey)) {
            // When the object is already present and the external key matches, then try to restore it
            originalScopedViewModelOwner
                ?: ScopedViewModelOwner(key = positionalMemoizationKey, modelClass = modelClass, factory = factory, viewModelStoreOwner = viewModelStoreOwner)
        } else { // First time object creation or externalKey changed
            scopedObjectKeys[positionalMemoizationKey] = externalKey // Set the external key used to track and store the new object version
            scopedObjectsContainer.remove(positionalMemoizationKey)
                ?.also { // Old object may need to be cleared before it's forgotten
                    clearLastDisposedObject(disposedObject = it, objectsContainer = scopedObjectsContainer.values.toList())
                }
            ScopedViewModelOwner(key = positionalMemoizationKey, modelClass = modelClass, factory = factory, viewModelStoreOwner = viewModelStoreOwner)
        }
        // Set the new external key used to track and store the new object version
        scopedObjectKeys[positionalMemoizationKey] = externalKey

        scopedObjectsContainer[positionalMemoizationKey] = scopedViewModelOwner

        return scopedViewModelOwner.viewModel
    }

    /**
     * Restore the stored [ScopedViewModelOwner], if any present, for the given [positionalMemoizationKey]
     * and update its dependencies to create a new [ViewModelProvider] (i.e. [CreationExtras] and default [ViewModelProvider.Factory])
     */
    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <T : ViewModel> restoreAndUpdateScopedViewModelOwner(
        positionalMemoizationKey: String,
        scopedObjectsContainer: MutableMap<String, Any>,
        viewModelStoreOwner: ViewModelStoreOwner
    ): ScopedViewModelOwner<T>? =
        (scopedObjectsContainer[positionalMemoizationKey] as? ScopedViewModelOwner<T>)
            ?.also { it.updateViewModelProvider(viewModelStoreOwner) }

    /**
     * An object that is being disposed should also be cleared only if there are no more references to it in this [objectsContainer]
     */
    @PublishedApi
    internal fun clearLastDisposedObject(disposedObject: Any, objectsContainer: List<Any>) {
        if (disposedObject is ScopedViewModelOwner<*>) {
            clearLastDisposedViewModel(scopedViewModelOwner = disposedObject, objectsContainer = objectsContainer)
        } else if (!objectsContainer.contains(disposedObject)) {
            // Clear, if possible, scoped object when disposing it
            when (disposedObject) {
                is ViewModelStore -> disposedObject.clear()
                is CoroutineScope -> disposedObject.cancel()
                is CoroutineContext -> disposedObject.cancel()
                is Closeable -> disposedObject.close()
            }
        }
    }

    /**
     * Check if the [ViewModel] contained in the given [scopedViewModelOwner] is the last one inside [objectsContainer] and if so,
     * clear the [ScopedViewModelOwner] and therefore the [ViewModel] inside.
     */
    @PublishedApi
    internal fun <T : ViewModel> clearLastDisposedViewModel(
        scopedViewModelOwner: ScopedViewModelOwner<T>,
        objectsContainer: List<Any>,
    ) {
        val sameViewModelFoundInContainer =
            objectsContainer
                .filterIsInstance<ScopedViewModelOwner<T>>()
                .any { storedObject ->
                    storedObject.viewModel == scopedViewModelOwner.viewModel
                }
        if (!sameViewModelFoundInContainer) scopedViewModelOwner.clear()
    }
}