package com.sebaslogen.resacaapp.sample.ui.main.data

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import java.util.concurrent.atomic.AtomicInteger

/**
 * This is a fake [ViewModel] with dependencies that will be injected by the Metro DI framework,
 * and a dependency [viewModelId] that will be provided by assisted injection.
 *
 * @param repository Sample of a common dependency on a project's object created by a DI framework.
 * @param viewModelsClearedCounter Is a counter to inform the providers of this parameter that this ViewModel has been correctly cleared
 * @param viewModelId Is a dependency that will be provided by assisted injection.
 */
@AssistedInject
class FakeMetroInjectedViewModel(
    private val repository: FakeInjectedRepo,
    private val viewModelsClearedCounter: AtomicInteger,
    @Assisted val viewModelId: Int
) : ViewModel() {

    override fun onCleared() {
        println("FakeMetroInjectedViewModel.onCleared()")
        viewModelsClearedCounter.incrementAndGet()
        super.onCleared()
    }

    @AssistedFactory
    fun interface FakeMetroInjectedViewModelFactory {
        fun create(viewModelId: Int): FakeMetroInjectedViewModel
    }
}
