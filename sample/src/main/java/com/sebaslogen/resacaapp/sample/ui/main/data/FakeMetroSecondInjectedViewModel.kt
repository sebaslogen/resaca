package com.sebaslogen.resacaapp.sample.ui.main.data

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Inject
import java.util.concurrent.atomic.AtomicInteger

/**
 * This is a second fake [ViewModel] with dependencies that will be injected by the Metro DI framework.
 *
 * @param viewModelsClearedCounter Is a counter to inform the providers of this parameter that this ViewModel has been correctly cleared
 */
class FakeMetroSecondInjectedViewModel @Inject constructor(
    private val viewModelsClearedCounter: AtomicInteger
) : ViewModel() {

    override fun onCleared() {
        println("FakeMetroSecondInjectedViewModel.onCleared()")
        viewModelsClearedCounter.incrementAndGet()
        super.onCleared()
    }
}
