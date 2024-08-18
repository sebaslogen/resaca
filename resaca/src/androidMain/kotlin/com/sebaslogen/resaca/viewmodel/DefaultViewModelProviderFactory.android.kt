package com.sebaslogen.resaca.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

/**
 * Default implementation of [ViewModelProvider.Factory] that creates a new instance of a ViewModel using the default constructor.
 */
internal actual object DefaultViewModelProviderFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
        JvmViewModelProviders.createViewModel(modelClass.java)
}