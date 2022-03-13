package com.sebaslogen.resacaapp.ui.main.data

import androidx.lifecycle.ViewModel
import java.util.concurrent.atomic.AtomicInteger

/**
 * ViewModel used to test the lifecycle of the app and the library.
 * @param viewModelsClearedCounter Is a counter to inform the providers of this parameter that this ViewModel has been correctly cleared
 */
class FakeScopedViewModel(private val viewModelsClearedCounter: AtomicInteger) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        viewModelsClearedCounter.incrementAndGet()
    }
}