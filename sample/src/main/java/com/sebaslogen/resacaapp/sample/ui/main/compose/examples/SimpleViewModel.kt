package com.sebaslogen.resacaapp.sample.ui.main.compose.examples

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import com.sebaslogen.resaca.core.viewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeScopedViewModel

/**
 * Create a simple [ViewModel] with resaca's [viewModelScoped] function.
 * This [ViewModel] has no external parameters/dependencies required by the constructor.
 * In this case the [SavedStateHandle] required by [FakeScopedViewModel] will be provided by the default [SavedStateViewModelFactory]
 */
@Composable
fun DemoScopedViewModelComposable(key: String? = null) {
    val fakeScopedVM: FakeScopedViewModel = viewModelScoped(key = key)
    DemoComposable(inputObject = fakeScopedVM, objectType = "FakeScopedViewModel", scoped = true)
}