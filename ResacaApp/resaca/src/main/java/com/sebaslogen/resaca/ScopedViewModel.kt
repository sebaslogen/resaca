package com.sebaslogen.resaca

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * [ScopedViewModel] is a [ViewModel] that should NOT be created nor used in combination with [ViewModelProvider]
 * Instead, children of this class should be stored and retrieved with a [ScopedViewModelContainer]
 */
abstract class ScopedViewModel : ViewModel() {
}