package com.sebaslogen.resaca

import androidx.compose.runtime.DisallowComposableCalls
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras

// TODO: docs in Readme: Hilt
// TODO: docs in Readme: key not needed for new instance due to positional key?

// TODO: docs in this class
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
class ScopedViewModelOwner<T : ViewModel>(
    val key: String,
    val modelClass: Class<T>,
    val factory: ViewModelProvider.Factory?,
    viewModelStoreOwner: ViewModelStoreOwner
) {

    private val viewModelStore = ViewModelStore()
    private lateinit var extras: CreationExtras
    private var viewModelStoreOwnerDefaultViewModelProviderFactory: ViewModelProvider.Factory? = null

    init {
        updateViewModelProviderDependencies(viewModelStoreOwner)
    }

    @PublishedApi
    internal fun updateViewModelProviderDependencies(viewModelStoreOwner: ViewModelStoreOwner) {
        extras = if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
            viewModelStoreOwner.defaultViewModelCreationExtras
        } else {
            CreationExtras.Empty
        }
        viewModelStoreOwnerDefaultViewModelProviderFactory =
            (viewModelStoreOwner as? HasDefaultViewModelProviderFactory)?.defaultViewModelProviderFactory
    }

    private val viewModelProvider: ViewModelProvider =
        if (factory != null) {
            ViewModelProvider(viewModelStore, factory, extras)
        } else {
            viewModelStoreOwnerDefaultViewModelProviderFactory?.let { ViewModelProvider(viewModelStore, it, extras) }
                ?: ViewModelProvider { this@ScopedViewModelOwner.viewModelStore }
        }

    val viewModel: T
        @Suppress("ReplaceGetOrSet")
        get() = viewModelProvider.get(key, modelClass)

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

