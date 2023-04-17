package com.sebaslogen.resacaapp.sample.ui.main.data

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import java.util.concurrent.atomic.AtomicInteger

/**
 * ViewModel used to test the lifecycle of the app and the library.
 * @param stateSaver A dependency provided by the Android and Hilt frameworks to save and restore state in a [Bundle]
 */
class FakeScopedViewModel(private val stateSaver: SavedStateHandle) : ViewModel() {

    companion object {
        const val MY_ARGS_KEY = "MY_ARGS_KEY"
    }

    /**
     * Counter to track that this ViewModel has been correctly cleared
     */
    private val viewModelsClearedCounter: AtomicInteger = viewModelsClearedGloballySharedCounter
    val viewModelId = stateSaver.get<Int>(MY_ARGS_KEY)

    override fun onCleared() {
        super.onCleared()
        viewModelsClearedCounter.incrementAndGet()
    }
}