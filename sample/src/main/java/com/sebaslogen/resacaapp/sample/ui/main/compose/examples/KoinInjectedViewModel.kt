package com.sebaslogen.resacaapp.sample.ui.main.compose.examples

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sebaslogen.resaca.rememberScoped
import com.sebaslogen.resaca.viewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedRepo
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeSimpleInjectedViewModel
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import org.koin.mp.KoinPlatform.getKoin

/**
 * Create a [FakeInjectedRepo] object with Resaca's [rememberScoped] function to let
 * Koin provide the [FakeInjectedRepo] with all the required dependencies and let
 * Resaca handle the lifecycle of the provided [FakeInjectedRepo].
 */
@Composable
fun DemoScopedKoinInjectedObjectComposable(key: String? = null) {
    val fakeInjectedRepo: FakeInjectedRepo = rememberScoped(key = key) { getKoin().get() }
    DemoComposable(inputObject = fakeInjectedRepo, objectType = "Koin FakeInjectedRepo", scoped = true)
}

/**
 * Create a [ViewModel] with Resaca's [viewModelScoped] function to let
 * Koin provide the [ViewModel] with all the required dependencies and let
 * Resaca handle the lifecycle of the provided [ViewModel].
 *
 * Assisted Injection is out-of-the-box supported by Koin using parameters.
 *
 * Note: This [FakeSimpleInjectedViewModel] does not depend on the [SavedStateHandle] to be injected
 * and therefore no special [koinViewModelScoped] function is required for Koin injection of [ViewModel].
 */
@Composable
fun DemoScopedKoinSimpleInjectedViewModelComposable(key: String? = null) {
    val fakeInjectedVM: FakeSimpleInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use viewModelScoped
            FakeSimpleInjectedViewModel(
                repository = FakeInjectedRepo(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter
            )
        } else {
            viewModelScoped(key = key) { getKoin().get() }
        }
    DemoComposable(inputObject = fakeInjectedVM, objectType = "Koin FakeSimpleInjectedViewModel", scoped = true)
}