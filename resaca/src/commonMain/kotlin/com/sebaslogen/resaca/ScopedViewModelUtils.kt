package com.sebaslogen.resaca

import androidx.annotation.MainThread
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import com.sebaslogen.resaca.ScopedViewModelContainer.ExternalKey
import com.sebaslogen.resaca.ScopedViewModelContainer.InternalKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass


/**
 * Set of functions to help create, store, retrieve and clear a [ViewModel].
 */
internal object ScopedViewModelUtils {

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
    public inline fun <T : ViewModel> getOrBuildViewModel(
        modelClass: KClass<T>,
        positionalMemoizationKey: InternalKey,
        externalKey: ExternalKey,
        factory: ViewModelProvider.Factory?,
        viewModelStoreOwner: ViewModelStoreOwner,
        creationExtras: CreationExtras,
        scopedObjectsContainer: MutableMap<InternalKey, Any>,
        scopedObjectKeys: MutableMap<InternalKey, ExternalKey>,
        cancelDisposal: (InternalKey) -> Unit,
        clearLastDisposedViewModel: (Any, List<Any>) -> Unit
    ): T {
        cancelDisposal(positionalMemoizationKey)

        val originalScopedViewModelOwner: ScopedViewModelOwner<T>? = restoreAndUpdateScopedViewModelOwner(positionalMemoizationKey, scopedObjectsContainer)

        val viewModel: T =
            if (originalScopedViewModelOwner != null
                && scopedObjectKeys.containsKey(positionalMemoizationKey)
                && (scopedObjectKeys[positionalMemoizationKey] == externalKey)
            ) {
                // When the object is already present and the external key matches, then return the existing one in the ScopedViewModelOwner
                originalScopedViewModelOwner.getViewModel(factory, viewModelStoreOwner, creationExtras)
            } else { // First time ViewModel's object creation or externalKey changed
                scopedObjectsContainer.remove(positionalMemoizationKey) // Remove in case key changed
                    ?.also { // Old object may need to be cleared before it's forgotten
                        clearLastDisposedViewModel(it, scopedObjectsContainer.values.toList())
                    }
                scopedObjectKeys[positionalMemoizationKey] = externalKey // Set the new external key used to track and store the new object version
                val newScopedViewModelOwner = ScopedViewModelOwner(
                    key = positionalMemoizationKey + externalKey, // Both keys needed to handle recreation by ViewModelProvider when any of these keys changes
                    modelClass = modelClass
                )
                scopedObjectsContainer[positionalMemoizationKey] = newScopedViewModelOwner
                newScopedViewModelOwner.getViewModel(factory, viewModelStoreOwner, creationExtras)
            }

        return viewModel
    }

    /**
     * Restore the stored [ScopedViewModelOwner], if any present, for the given [positionalMemoizationKey]
     */
    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <T : ViewModel> restoreAndUpdateScopedViewModelOwner(
        positionalMemoizationKey: InternalKey,
        scopedObjectsContainer: Map<InternalKey, Any>
    ): ScopedViewModelOwner<T>? =
        (scopedObjectsContainer[positionalMemoizationKey] as? ScopedViewModelOwner<T>)

    /**
     * An object that is being disposed should also be cleared only if there are no more references to it in this [objectsContainer]
     */
    @PublishedApi
    @MainThread
    internal fun clearLastDisposedObject(disposedObject: Any, objectsContainer: List<Any>) {
        if (disposedObject is ScopedViewModelOwner<*>) {
            clearLastDisposedViewModel(scopedViewModelOwner = disposedObject, objectsContainer = objectsContainer)
        } else if (!objectsContainer.contains(disposedObject)) {
            // Clear, if possible, scoped object when disposing it
            when (disposedObject) {
                is ViewModelStore -> disposedObject.clear()
                is CoroutineScope -> disposedObject.cancel()
                is CoroutineContext -> disposedObject.cancel()
                is AutoCloseable -> disposedObject.close()
            }
        }
    }

    /**
     * Check if the [ViewModel] contained in the given [scopedViewModelOwner] is nowhere to be found inside [objectsContainer],
     * if so, then clear the [ScopedViewModelOwner] and therefore the [ViewModel] inside.
     *
     * Otherwise, when the [ViewModel] is still present inside [objectsContainer],
     * it will be kept alive and will be reused by other Composable(s).
     */
    @PublishedApi
    internal fun <T : ViewModel> clearLastDisposedViewModel(
        scopedViewModelOwner: ScopedViewModelOwner<T>,
        objectsContainer: List<Any>,
    ) {
        val viewModelMissingInContainer =
            objectsContainer
                .filterIsInstance<ScopedViewModelOwner<T>>()
                .none { storedObject ->
                    val viewModel =
                        storedObject.getCachedViewModel() // This happens only when one of the ViewModels requested was never actually created before this function call
                    viewModel != null && viewModel == scopedViewModelOwner.getCachedViewModel()
                }
        if (viewModelMissingInContainer) scopedViewModelOwner.clear()
    }
}

private operator fun InternalKey.plus(externalKey: ExternalKey): String =
    this.hashCode().toString() + externalKey.hashCode().toString()
