package com.sebaslogen.resaca

import androidx.lifecycle.ViewModelProvider

/**
 * Singleton to mimic [ViewModelProvider.NewInstanceFactory] instance in that library.
 */
internal object ViewModelNewInstanceFactory {
    val instance: ViewModelProvider.NewInstanceFactory = ViewModelProvider.NewInstanceFactory()
}