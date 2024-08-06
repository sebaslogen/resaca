package com.sebaslogen.resaca.core.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlin.reflect.KClass

internal actual object DefaultViewModelProviderFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
        throw UnsupportedOperationException(
            "`Factory.create(String, CreationExtras)` is not implemented. You may need to " +
                    "override the method and provide a custom implementation. Note that using " +
                    "`Factory.create(String)` is not supported and considered an error."
        )
}