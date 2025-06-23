package com.sebaslogen.resacaapp.sample.ui.main.data

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicInteger

/**
 * This is a fake [ViewModel] with dependencies that will be injected by a DI framework (e.g. Hilt or Koin),
 * and a dependency [viewModelId] that will be provided by pseudo assisted injection via [stateSaver].
 *
 * @param stateSaver A dependency provided by the Android and DI frameworks to save and restore state in a [Bundle]
 * @param repository Sample of a common dependency on a project's object created by a DI framework.
 * @param viewModelsClearedCounter Is a counter to inform the providers of this parameter that this ViewModel has been correctly cleared
 */
@HiltViewModel(assistedFactory = FakeInjectedViewModel.FakeInjectedViewModelFactory::class)
class FakeInjectedViewModel @AssistedInject constructor(
    private val stateSaver: SavedStateHandle,
    private val repository: FakeInjectedRepo,
    private val viewModelsClearedCounter: AtomicInteger,
    @Assisted val viewModelId: Int
) : ViewModel() {

    override fun onCleared() {
        println("FakeInjectedViewModel.onCleared() with SSH: $stateSaver")
        viewModelsClearedCounter.incrementAndGet()
        super.onCleared()
    }

    @AssistedFactory
    interface FakeInjectedViewModelFactory {
        fun create(viewModelId: Int): FakeInjectedViewModel
    }
}
