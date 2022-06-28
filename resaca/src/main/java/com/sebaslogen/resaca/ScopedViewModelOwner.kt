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
 */
class ScopedViewModelOwner<T : ViewModel>(private val modelClass: Class<T>, private val factory: ViewModelProvider.Factory) {

    val viewModel: T
        @Suppress("ReplaceGetOrSet")
        get() = ViewModelProvider(store = internalViewModelStore, factory = factory).get(modelClass)

    private val internalViewModelStore = ViewModelStore()

    fun clear() {
        internalViewModelStore.clear()
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : ViewModel> viewModelFactoryFor(builder: @DisallowComposableCalls () -> T): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = builder() as VM
        }
    }
}