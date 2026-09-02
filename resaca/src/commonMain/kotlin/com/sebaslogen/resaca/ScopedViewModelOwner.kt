package com.sebaslogen.resaca

import androidx.compose.runtime.DisallowComposableCalls
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import com.sebaslogen.resaca.utils.getCanonicalNameKey
import com.sebaslogen.resaca.utils.getClassName
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
@ResacaPackagePrivate
public class ScopedViewModelOwner<T : ViewModel>(
    private val key: String,
    private val modelClass: KClass<T>
) {

    private val viewModelStore = ViewModelStore()
    private val scopedViewModelProvider = ScopedViewModelProvider(viewModelStore)

    /**
     * The [ViewModel] currently held by [viewModelStore], or null while no [ViewModel] has been requested yet.
     *
     * This is a strong reference, but it does not extend the life of the [ViewModel] because [viewModelStore] already
     * holds it strongly and both are owned by this same object.
     */
    private var cachedViewModel: T? = null

    internal fun getViewModel(factory: ViewModelProvider.Factory?, viewModelStoreOwner: ViewModelStoreOwner, creationExtras: CreationExtras): T {
        val viewModelProvider = scopedViewModelProvider.getViewModelProvider(factory, viewModelStoreOwner, creationExtras)
        @Suppress("ReplaceGetOrSet")
        return viewModelProvider.get(modelClass.getCanonicalNameKey(key), modelClass).also { cachedViewModel = it }
    }

    /**
     * Returns the [ViewModel] this owner holds, or null when no [ViewModel] was created yet or it was already cleared.
     *
     * This is used to find out whether two Composables are sharing the same [ViewModel] instance, so it must never
     * create a [ViewModel] as a side effect and must never forget one that is still alive.
     */
    internal fun getCachedViewModel(): T? = cachedViewModel

    internal fun clear() {
        viewModelStore.clear()
        cachedViewModel = null
    }

    internal companion object {
        /**
         * Returns a [ViewModelProvider.Factory] based on the given ViewModel [builder].
         */
        @Suppress("UNCHECKED_CAST")
        inline fun <T : ViewModel> viewModelFactoryFor(savedStateHandle: SavedStateHandle, crossinline builder: @DisallowComposableCalls (savedStateHandle: SavedStateHandle) -> T): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <VM : ViewModel> create(modelClass: KClass<VM>, extras: CreationExtras): VM = builder(savedStateHandle) as VM
            }
    }
}