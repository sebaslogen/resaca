package com.sebaslogen.resaca

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * This custom [ViewModelStoreOwner] is designed to store a single [ViewModel] inside its [viewModelStore]
 * and provides a function to externally be able to [clear] the contained [ViewModel]
 *
 * Instances of this class are required to be able to clear [ViewModel]s from a [ScopedViewModelContainer]
 * and to interact with [ViewModelProvider] that's in charge of:
 * - creating [ViewModel]s
 * - storing [ViewModel]s in the [viewModelStore] of this class
 * - retrieving [ViewModel] copies from the [viewModelStore] of this class
 *
 */
internal class ScopedViewModelStoreOwner : ViewModelStoreOwner {

    private val viewModelStore = ViewModelStore()

    override fun getViewModelStore(): ViewModelStore = viewModelStore

    fun clear() {
        viewModelStore.clear()
    }
}