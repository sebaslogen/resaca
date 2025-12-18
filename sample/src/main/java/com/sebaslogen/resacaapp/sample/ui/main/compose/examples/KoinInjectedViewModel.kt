package com.sebaslogen.resacaapp.sample.ui.main.compose.examples

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sebaslogen.resaca.koin.koinViewModelScoped
import com.sebaslogen.resaca.rememberScoped
import com.sebaslogen.resaca.viewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedRepo
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeScopedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeSecondInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeSimpleInjectedViewModel
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import org.koin.core.parameter.parametersOf
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
@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedKoinSimpleInjectedViewModelComposable(key: String? = null) {
    val fakeInjectedVM: FakeSimpleInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use viewModelScoped
            FakeSimpleInjectedViewModel(
                repository = FakeInjectedRepo(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter
            )
        } else {
            viewModelScoped(key = key) { getKoin().get() } // Koin assisted injection is possible using parameters: getKoin().get { parametersOf(myThing) } }
        }
    DemoComposable(inputObject = fakeInjectedVM, objectType = "Koin FakeSimpleInjectedViewModel", scoped = true)
}

/**
 * Create a [ViewModel] with Resaca's [viewModelScoped] function to let
 * Koin provide the [ViewModel] with all the required dependencies and
 * Resaca handle the lifecycle of the provided [ViewModel].
 *
 * Assisted Injection is out-of-the-box supported by Koin using parameters.
 *
 * Note: This [FakeScopedViewModel] depends on the [SavedStateHandle] to be injected
 * and we can use the [SavedStateHandle] provided by resaca in the [viewModelScoped] builder lambda,
 * instead of using the [koinViewModelScoped] function.
 */
@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedKoinParametrizedInjectedViewModelComposable(key: String? = null) {
    val viewModelId = 666
    val fakeScopedVM: FakeScopedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use viewModelScoped
            FakeScopedViewModel(
                stateSaver = SavedStateHandle(),
                viewModelId = viewModelId
            )
        } else {
            viewModelScoped(key = key) { savedStateHandle: SavedStateHandle ->
                getKoin().get() { parametersOf(savedStateHandle, viewModelId) }
            } // Koin assisted injection is possible using parameters: getKoin().get { parametersOf(myThing) } }
        }
    DemoComposable(inputObject = fakeScopedVM, objectType = "Koin FakeScopedViewModel", scoped = true)
}

/**
 * Create a [ViewModel] with Resaca's [koinViewModelScoped] function to let
 * Koin provide the [ViewModel] with all the required dependencies and
 * Resaca handle the lifecycle of the provided [ViewModel].
 *
 * Assisted Injection is out-of-the-box supported by Koin using parameters.
 *
 * Note: [koinViewModelScoped] is required for [FakeInjectedViewModel] because it depends on the [SavedStateHandle].
 */
@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedKoinInjectedViewModelComposable(key: String? = null, fakeInjectedViewModelId: Int = 666) {
    val fakeInjectedVM: FakeInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use viewModelScoped
            FakeInjectedViewModel(
                stateSaver = SavedStateHandle(),
                repository = FakeInjectedRepo(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter,
                viewModelId = fakeInjectedViewModelId
            )
        } else {
            koinViewModelScoped(
                key = key,
                parameters = { parametersOf(viewModelsClearedGloballySharedCounter, fakeInjectedViewModelId) }
            )
        }
    DemoComposable(inputObject = fakeInjectedVM, objectType = "Koin FakeInjectedViewModel", scoped = true)
}

/**
 * Create a [ViewModel] with Resaca's [koinViewModelScoped] function to let
 * Koin provide the [ViewModel] with all the required dependencies and
 * Resaca handle the lifecycle of the provided [ViewModel].
 *
 * All parameters for this [FakeSecondInjectedViewModel] are provided by Koin, including the [SavedStateHandle].
 *
 * Note: [koinViewModelScoped] is required for [FakeSecondInjectedViewModel] because it depends on the [SavedStateHandle].
 */
@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedSecondKoinInjectedViewModelComposable() {
    val fakeSecondInjectedVM: FakeSecondInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use viewModelScoped
            FakeSecondInjectedViewModel(
                stateSaver = SavedStateHandle(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter
            )
        } else {
            koinViewModelScoped()
        }
    DemoComposable(inputObject = fakeSecondInjectedVM, objectType = "Koin FakeSecondInjectedViewModel", scoped = true)
}
