package com.sebaslogen.resacaapp.sample.ui.main.data

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicInteger

/**
 * This is a fake [ViewModel] with dependencies that will be injected by a DI framework (e.g. Hilt or Koin),
 * and a dependency [viewModelId] that will be provided by assisted injection via [CreationExtras] callback.
 *
 * @param stateSaver A dependency provided by the Android and DI frameworks to save and restore state in a [Bundle]
 * @param repository Sample of a common dependency on a project's object created by a DI framework.
 * @param viewModelsClearedCounter Is a counter to inform the providers of this parameter that this ViewModel has been correctly cleared
 * @param viewModelId Is a dependency that will be provided by assisted injection via [CreationExtras] callback.
 */
@HiltViewModel(assistedFactory = FakeAssistedInjectionViewModel.FakeAssistedInjectionViewModelFactory::class)
class FakeAssistedInjectionViewModel @AssistedInject constructor(
    private val stateSaver: SavedStateHandle,
    private val repository: FakeInjectedRepo,
    private val viewModelsClearedCounter: AtomicInteger,
    @Assisted private val viewModelId: Int
) : ViewModel() {

    override fun onCleared() {
        println("FakeAssistedInjectionViewModel.onCleared() with SSH: $stateSaver")
        viewModelsClearedCounter.incrementAndGet()
        super.onCleared()
    }

    @AssistedFactory
    interface FakeAssistedInjectionViewModelFactory {
        fun create(viewModelId: Int): FakeAssistedInjectionViewModel
    }
}