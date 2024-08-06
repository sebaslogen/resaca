package com.sebaslogen.resaca.core

import androidx.core.bundle.Bundle
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras


/**
 * Combine the existing arguments present in the receiver's [CreationExtras] under the key [DEFAULT_ARGS_KEY] with the [defaultArguments] parameter.
 * When the no arguments are present just add them.
 */
public fun CreationExtras.addDefaultArguments(defaultArguments: Bundle): CreationExtras =
    if (defaultArguments.isEmpty()) {
        this
    } else {
        MutableCreationExtras(this).apply {
            val combinedBundle = (get(DEFAULT_ARGS_KEY) ?: Bundle()).apply { putAll(defaultArguments) }
            set(DEFAULT_ARGS_KEY, combinedBundle)
        }
    }

public fun Bundle.toCreationExtras(
    viewModelStoreOwner: ViewModelStoreOwner
): CreationExtras =
    if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
        viewModelStoreOwner.defaultViewModelCreationExtras
    } else {
        CreationExtras.Empty
    }.addDefaultArguments(this)