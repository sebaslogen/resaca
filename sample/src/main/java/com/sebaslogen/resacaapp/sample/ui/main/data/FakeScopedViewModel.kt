package com.sebaslogen.resacaapp.sample.ui.main.data

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sebaslogen.resacaapp.sample.ui.main.compose.objectToShortStringWithoutPackageName
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicInteger

/**
 * ViewModel used to test the lifecycle of the app and the library.
 * The [viewModelId] field is injected by the DI framework using assisted injection with [FakeScopedViewModel.FakeScopedViewModelFactory].
 *
 * @param stateSaver A dependency provided by the Android and DI frameworks to save and restore state in a [Bundle]
 */
@HiltViewModel(assistedFactory = FakeScopedViewModel.FakeScopedViewModelFactory::class)
class FakeScopedViewModel @AssistedInject constructor(
    private val stateSaver: SavedStateHandle,
    @Assisted val viewModelId: Int
) : ViewModel() {

    /**
     * Counter to track that this ViewModel has been correctly cleared
     */
    private val viewModelsClearedCounter: AtomicInteger = viewModelsClearedGloballySharedCounter

    /**
     * Memory address of the ViewModel instance to debug name on the screen
     */
    val memoryAddress: String = objectToShortStringWithoutPackageName(this).replaceBeforeLast("@", "")

    override fun onCleared() {
        println("FakeScopedViewModel.onCleared() with SSH: $stateSaver")
        viewModelsClearedCounter.incrementAndGet()
        super.onCleared()
    }

    @AssistedFactory
    interface FakeScopedViewModelFactory {
        fun create(viewModelId: Int): FakeScopedViewModel
    }
}
