package com.sebaslogen.resacaapp.ui.main.data

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * This is a fake [ViewModel] with dependencies that will be injected by Hilt.
 * @param stateSaver A dependency provided by the Android and Hilt frameworks to save and restore state in a [Bundle]
 * @param repository Sample of a common dependency on a project's object created by Hilt
 * @param viewModelsClearedCounter Is a counter to inform the providers of this parameter that this ViewModel has been correctly cleared
 */
@HiltViewModel
class FakeInjectedViewModel @Inject constructor(
    private val stateSaver: SavedStateHandle,
    private val repository: FakeInjectedRepo,
    private val viewModelsClearedCounter: AtomicInteger
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        viewModelsClearedCounter.incrementAndGet()
    }
}