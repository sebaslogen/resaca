package com.sebaslogen.resaca

import androidx.compose.runtime.DisallowComposableCalls
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

/**
 * Stores a [ViewModel] created with the provided [factory] constructor parameter.
 * This class uses an internal [ViewModelProvider] with the [factory] and a [ViewModelStore],
 * to create, store, retrieve and [clear] the ViewModel when externally requested to do so.
 *
 * The reason to be for this class is to support clearing of [ViewModel]s. The clear function of [ViewModel]s is not public,
 * only a [ViewModelStore] can trigger it and the [ViewModelStore] can only be read/written by a [ViewModelProvider].
 *
 * The creation of the [ViewModel] will be done by a [ViewModelProvider] and stored inside a [ViewModelStore].
 *
 * @param key Unique [key] required to support [SavedStateHandle] across multiple instances of the same [ViewModel] type.
 * @param modelClass Class type of the [ViewModel] to instantiate
 */
public class ScopedViewModelOwner<T : ViewModel>(
    private val key: String,
    private val modelClass: KClass<T>
) {

    private val viewModelStore = ViewModelStore()
    private val scopedViewModelProvider = ScopedViewModelProvider(viewModelStore)

    internal fun getViewModel(factory: ViewModelProvider.Factory?, viewModelStoreOwner: ViewModelStoreOwner, creationExtras: CreationExtras): T {
        val canonicalName = modelClass.qualifiedName ?: throw IllegalArgumentException("Local and anonymous classes can not be ViewModels")
        val viewModelProvider = scopedViewModelProvider.getViewModelProvider(factory, viewModelStoreOwner, creationExtras)
        @Suppress("ReplaceGetOrSet")
        return viewModelProvider.get("$canonicalName:$key", modelClass)
    }

    internal fun getCachedViewModel(): T? {
        val canonicalName = modelClass.qualifiedName ?: throw IllegalArgumentException("Local and anonymous classes can not be ViewModels")
        return scopedViewModelProvider.getCachedViewModelProvider()?.get("$canonicalName:$key", modelClass)
    }

    internal fun clear() {
        viewModelStore.clear()
    }

    internal companion object {
        /**
         * Returns a [ViewModelProvider.Factory] based on the given ViewModel [builder].
         */
        @Suppress("UNCHECKED_CAST")
        inline fun <T : ViewModel> viewModelFactoryFor(crossinline builder: @DisallowComposableCalls () -> T): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <VM : ViewModel> create(modelClass: KClass<VM>, extras: CreationExtras): VM = builder() as VM
            }
    }
}

