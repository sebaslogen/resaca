package com.sebaslogen.resacaapp.sample.ui.main.data

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

/**
 * This is a fake [ViewModel] with dependencies that will be injected by a DI framework (e.g. Hilt or Koin).
 * This is a simple [ViewModel] because it does not depend on the [SavedStateHandle] to be injected. Therefore, all
 * dependencies can be provided by a DI framework and don't depend on platform objects.
 *
 * @param repository Sample of a common dependency on a project's object created by a DI framework
 * @param viewModelsClearedCounter Is a counter to inform the providers of this parameter that this ViewModel has been correctly cleared
 */
@HiltViewModel
class FakeSimpleInjectedViewModel @Inject constructor(
    private val repository: FakeInjectedRepo,
    private val viewModelsClearedCounter: AtomicInteger
) : ViewModel() {

    override fun onCleared() {
        println("FakeSimpleInjectedViewModel.onCleared() without SSH")
        viewModelsClearedCounter.incrementAndGet()
        super.onCleared()
    }
}