package com.sebaslogen.resaca.core

import androidx.lifecycle.ViewModelProvider

/**
 * Singleton to mimic [ViewModelProvider.NewInstanceFactory] instance in that library.
 */
internal object ViewModelNewInstanceFactory {
    val instance: ViewModelProvider.Factory = TODO() //ViewModelProvider.NewInstanceFactory()
}