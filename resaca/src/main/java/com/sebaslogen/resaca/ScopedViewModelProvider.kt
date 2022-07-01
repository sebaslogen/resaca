package com.sebaslogen.resaca

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore


/**
 * Set of functions to help create, store, retrieve and clear a [ViewModel].
 */
@Suppress("NOTHING_TO_INLINE")
object ScopedViewModelProvider {

    /**
     * Returns an existing [ViewModel] of type [T] or creates a new one if none was present in the [scopedObjectsContainer].
     *
     * This function will also compare [positionalMemoizationKey] and [externalKey] to determine
     * if a new instance of [T] needs to be created or an existing one can be returned.
     *
     * The creation, storage, retrieval and clean up of the [ViewModel] will be taken care of
     * by a [ScopedViewModelOwner] which will be the actual object stored in the [scopedObjectsContainer].
     */
    @Composable
    inline fun <T : ViewModel> getOrBuildViewModel(
        modelClass: Class<T>,
        positionalMemoizationKey: String,
        externalKey: ScopedViewModelContainer.ExternalKey = ScopedViewModelContainer.ExternalKey(),
        factory: ViewModelProvider.Factory,
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
                val newScopedViewModelOwner = ScopedViewModelOwner(modelClass = modelClass, factory = factory)
                scopedObjectsContainer[positionalMemoizationKey] = newScopedViewModelOwner

                // Clean-up if needed: the old object is cleared before it's forgotten
                originalScopedViewModelOwner?.let { clearLastDisposedViewModel(originalScopedViewModelOwner, scopedObjectsContainer.values.toList()) }

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
     * ViewModels of the same [T] type inside the container scope (Activity/Fragment/Nav. destination),
     * and the same [ViewModel] will always be returned once created and until disposal of the Composables using it.
     * Support for keys in the Hilt library is still WIP. See https://github.com/google/dagger/issues/2328
     */
    @Composable
    inline fun <T : ViewModel> getOrBuildHiltViewModel(
        modelClass: Class<T>,
        positionalMemoizationKey: String,
        externalKey: ScopedViewModelContainer.ExternalKey = ScopedViewModelContainer.ExternalKey(),
        factory: ViewModelProvider.Factory,
        scopedObjectsContainer: MutableMap<String, Any>,
        scopedObjectKeys: MutableMap<String, ScopedViewModelContainer.ExternalKey>,
        cancelDisposal: ((String) -> Unit)
    ): T {
        cancelDisposal(positionalMemoizationKey)

        val newScopedViewModelOwner = scopedObjectsContainer.values.filterIsInstance<ScopedViewModelOwner<T>>().firstOrNull()
            ?: ScopedViewModelOwner(modelClass = modelClass, factory = factory)

        // Set the new external key used to track and store the new object version
        scopedObjectKeys[positionalMemoizationKey] = externalKey

        scopedObjectsContainer[positionalMemoizationKey] = newScopedViewModelOwner

        return newScopedViewModelOwner.viewModel
    }

    /**
     * Check if the [ViewModel] contained in the given [scopedViewModelOwner] is the last one inside [objectsContainer] and if so,
     * clear the [ScopedViewModelOwner] and therefore the [ViewModel] inside.
     */
    @PublishedApi
    internal inline fun <T : ViewModel> clearLastDisposedViewModel(
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