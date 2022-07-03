package com.sebaslogen.resaca

import androidx.compose.runtime.DisallowComposableCalls
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore

/**
 * Stores a [ViewModel] created with the provided [factory] constructor parameter.
 * This class uses an internal [ViewModelProvider] with the [factory] and a [ViewModelStore],
 * to create, store, retrieve and [clear] the ViewModel when externally requested to do so.
 *
 * The reason to be for this class is to support clearing of [ViewModel]s.
 * The clear function of [ViewModel]s is not public, only a [ViewModelStore] can trigger it and the [ViewModelStore]
 * can only be read/written with a [ViewModelProvider].
 *
 * The creation of the [ViewModel] will be done with a [ViewModelProvider] and stored inside a [ViewModelStore].
 */
class ScopedViewModelOwner<T : ViewModel>(val modelClass: Class<T>, val factory: ViewModelProvider.Factory) {

    private val viewModelStore = ViewModelStore()

    private val viewModelProvider = ViewModelProvider(store = viewModelStore, factory = factory)

    val viewModel: T
        @Suppress("ReplaceGetOrSet")
        get() = viewModelProvider.get(modelClass)

    fun clear() {
        viewModelStore.clear()
    }

    companion object {

        /**
         * Returns a [ViewModelProvider.Factory] based on the given ViewModel [builder].
         */
        @Suppress("UNCHECKED_CAST")
        inline fun <T : ViewModel> viewModelFactoryFor(crossinline builder: @DisallowComposableCalls () -> T): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = builder() as VM
            }
    }
}