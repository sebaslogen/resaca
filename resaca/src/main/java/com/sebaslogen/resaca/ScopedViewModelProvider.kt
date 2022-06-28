package com.sebaslogen.resaca

import androidx.compose.runtime.DisallowComposableCalls
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore

/**
 * Stores a [ViewModel] created with the provided [builder] constructor parameter.
 * This class uses an internal [ViewModelProvider] with a [factory] and a [ViewModelStore],
 * to create, store, retrieve and [clear] the ViewModel when externally requested to do so.
 *
 * The reason to be for this class is to support clearing of [ViewModel]s created via an external builder/factory.
 * The clear function of [ViewModel]s is not public, only a [ViewModelStore] can trigger it and the [ViewModelStore]
 * can only be filled with a [ViewModelProvider]
 */
class ScopedViewModelProvider<T : ViewModel>(private val modelClass: Class<T>, builder: @DisallowComposableCalls () -> T) {

    val viewModel: T
        @Suppress("ReplaceGetOrSet")
        get() = ViewModelProvider(store = internalViewModelStore, factory = factory).get(modelClass)

    private val internalViewModelStore = ViewModelStore()

    @Suppress("UNCHECKED_CAST")
    private val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = builder() as VM
    }

    fun clear() {
        internalViewModelStore.clear()
    }
}