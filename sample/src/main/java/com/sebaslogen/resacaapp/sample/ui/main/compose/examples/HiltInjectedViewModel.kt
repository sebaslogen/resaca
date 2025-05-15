package com.sebaslogen.resacaapp.sample.ui.main.compose.examples

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sebaslogen.resaca.hilt.hiltViewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeAssistedInjectionViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedRepo
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeSecondInjectedViewModel
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter

/**
 * Create a [ViewModel] with resaca's [hiltViewModelScoped] function to let
 * Hilt provide the [ViewModel] with all the required dependencies and
 * Resaca handle the lifecycle of the provided [ViewModel].
 *
 * Assisted Injection (i.e. provide some dependencies manually to the [ViewModel] constructor and let Hilt provide the rest)
 * is supported using the [hiltViewModelScoped] defaultArguments parameter and a [Bundle] of parameters,
 * for more info and instructions see https://github.com/sebaslogen/resaca/blob/main/resacahilt/README.md#pseudo-assisted-injection-support
 */
@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedHiltInjectedViewModelComposable(key: String? = null, fakeInjectedViewModelId: Int = 666) {
    val fakeInjectedVM: FakeInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use hiltViewModelScoped
            FakeInjectedViewModel(
                stateSaver = SavedStateHandle(),
                repository = FakeInjectedRepo(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter
            )
        } else {
            hiltViewModelScoped(key = key, defaultArguments = bundleOf(FakeInjectedViewModel.MY_ARGS_KEY to fakeInjectedViewModelId))
        }
    DemoComposable(inputObject = fakeInjectedVM, objectType = "Hilt FakeInjectedViewModel", scoped = true)
}

@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedSecondHiltInjectedViewModelComposable() {
    val fakeSecondInjectedVM: FakeSecondInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use hiltViewModelScoped
            FakeSecondInjectedViewModel(
                stateSaver = SavedStateHandle(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter
            )
        } else {
            hiltViewModelScoped()
        }
    DemoComposable(inputObject = fakeSecondInjectedVM, objectType = "Hilt FakeSecondInjectedViewModel", scoped = true)
}

@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedHiltAssistedInjectionViewModelComposable(key: String? = null, fakeInjectedViewModelId: Int = 666) {
    val fakeInjectedVM: FakeAssistedInjectionViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use hiltViewModelScoped
            FakeAssistedInjectionViewModel(
                stateSaver = SavedStateHandle(),
                repository = FakeInjectedRepo(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter,
                viewModelId = fakeInjectedViewModelId
            )
        } else {
            hiltViewModelScoped(key = key) { factory: FakeAssistedInjectionViewModel.FakeAssistedInjectionViewModelFactory ->
                factory.create(
                    viewModelId = fakeInjectedViewModelId
                )
            }
        }
    DemoComposable(inputObject = fakeInjectedVM, objectType = "Hilt FakeAssistedInjectionViewModel", scoped = true)
}
