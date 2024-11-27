package com.sebaslogen.resacaapp.sample.ui.main.data

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sebaslogen.resacaapp.sample.ui.main.compose.objectToShortStringWithoutPackageName
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeScopedViewModel.Companion.MY_ARGS_KEY
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import java.util.concurrent.atomic.AtomicInteger

/**
 * ViewModel used to test the lifecycle of the app and the library.
 * The [viewModelId] field is pseudo injected by the DI framework using the [MY_ARGS_KEY] key and the default parameters in the [stateSaver].
 *
 * @param stateSaver A dependency provided by the Android and DI frameworks to save and restore state in a [Bundle]
 */
class FakeScopedViewModel(private val stateSaver: SavedStateHandle) : ViewModel() {
    init {
        println("stateSaver: $stateSaver")
    }

    companion object {
        const val MY_ARGS_KEY = "MY_ARGS_KEY"
    }

    val viewModelId = stateSaver.get<Int>(MY_ARGS_KEY)

    /**
     * Counter to track that this ViewModel has been correctly cleared
     */
    private val viewModelsClearedCounter: AtomicInteger = viewModelsClearedGloballySharedCounter

    /**
     * Memory address of the ViewModel instance to debug name on the screen
     */
    val memoryAddress: String = objectToShortStringWithoutPackageName(this).replaceBeforeLast("@", "")

    override fun onCleared() {
        println("Sebas FakeScopedViewModel.onCleared() with SSH: $stateSaver")
        viewModelsClearedCounter.incrementAndGet()
        super.onCleared()
    }
}