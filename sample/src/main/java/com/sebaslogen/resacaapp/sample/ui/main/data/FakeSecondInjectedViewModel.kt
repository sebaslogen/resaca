package com.sebaslogen.resacaapp.sample.ui.main.data

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * This is a fake [ViewModel] with dependencies that will be injected by Hilt.
 *
 * @param stateSaver A dependency provided by the Android and DI frameworks to save and restore state in a [Bundle]
 * @param viewModelsClearedCounter Is a counter to inform the providers of this parameter that this ViewModel has been correctly cleared
 */
@HiltViewModel
class FakeSecondInjectedViewModel @Inject constructor(
    private val stateSaver: SavedStateHandle,
    private val viewModelsClearedCounter: AtomicInteger
) : ViewModel() {

    override fun onCleared() {
        println("FakeSecondInjectedViewModel.onCleared() with SSH: $stateSaver")
        viewModelsClearedCounter.incrementAndGet()
        super.onCleared()
    }
}