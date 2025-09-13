package com.sebaslogen.resaca

import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider.Companion.VIEW_MODEL_KEY
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras


internal fun ViewModelStoreOwner.getCreationExtras(
): CreationExtras =
    if (this is HasDefaultViewModelProviderFactory) {
        this.defaultViewModelCreationExtras
    } else {
        CreationExtras.Empty
    }

/**
 * This is a helper function to add the [viewModelKey] to the [CreationExtras] if it is not already present.
 * The [viewModelKey] is, among others, used to create a [SavedStateHandle] for the [ViewModel].
 */
internal fun CreationExtras.addViewModelKey(viewModelKey: String): CreationExtras =
    MutableCreationExtras(this).apply {
        if (get(VIEW_MODEL_KEY) == null) {
            set(VIEW_MODEL_KEY, viewModelKey)
        }
    }
