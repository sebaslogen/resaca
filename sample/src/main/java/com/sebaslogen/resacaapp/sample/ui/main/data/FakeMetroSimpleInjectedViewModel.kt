package com.sebaslogen.resacaapp.sample.ui.main.data

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Inject
import java.util.concurrent.atomic.AtomicInteger

/**
 * This is a fake [ViewModel] with dependencies that will be injected by the Metro DI framework.
 * This is a simple [ViewModel] because all dependencies can be provided by the Metro DI graph.
 *
 * @param repository Sample of a common dependency on a project's object created by a DI framework
 * @param viewModelsClearedCounter Is a counter to inform the providers of this parameter that this ViewModel has been correctly cleared
 */
class FakeMetroSimpleInjectedViewModel @Inject constructor(
    private val repository: FakeInjectedRepo,
    private val viewModelsClearedCounter: AtomicInteger
) : ViewModel() {

    override fun onCleared() {
        println("FakeMetroSimpleInjectedViewModel.onCleared() without SSH")
        viewModelsClearedCounter.incrementAndGet()
        super.onCleared()
    }
}
