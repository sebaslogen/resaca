package com.sebaslogen.resacaapp.sample.ui.main.compose.examples

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sebaslogen.resaca.viewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeScopedViewModel

/**
 * Create a [ViewModel] with the [viewModelScoped] function and use a provided builder
 * with parameters/dependencies required by the constructor.
 * Note: This is useful for frameworks like Koin and other ways of providing dependencies.
 */
@Composable
fun DemoScopedParametrizedViewModelComposable(
    viewModelInstance: FakeScopedViewModel = FakeScopedViewModel(stateSaver = SavedStateHandle(), viewModelId = 0),
    key: String? = null
) {
    val fakeScopedParametrizedVM: FakeScopedViewModel = viewModelScoped(key = key) { viewModelInstance }
    DemoComposable(inputObject = fakeScopedParametrizedVM, objectType = "FakeScopedParametrizedViewModel", scoped = true)
}
