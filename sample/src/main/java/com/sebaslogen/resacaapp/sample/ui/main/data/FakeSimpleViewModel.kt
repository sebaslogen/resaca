package com.sebaslogen.resacaapp.sample.ui.main.data

import androidx.lifecycle.ViewModel
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import java.util.concurrent.atomic.AtomicInteger

/**
 * Simple [ViewModel] with no constructor parameters, usable with the default [ViewModelProvider.Factory].
 * Used to test `viewModelScoped` overloads that don't require a builder/factory.
 */
class FakeSimpleViewModel : ViewModel() {
    private val viewModelsClearedCounter: AtomicInteger = viewModelsClearedGloballySharedCounter

    override fun onCleared() {
        println("FakeSimpleViewModel.onCleared()")
        viewModelsClearedCounter.incrementAndGet()
        super.onCleared()
    }
}
