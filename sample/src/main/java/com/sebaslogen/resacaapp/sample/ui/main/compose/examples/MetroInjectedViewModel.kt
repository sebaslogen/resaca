package com.sebaslogen.resacaapp.sample.ui.main.compose.examples

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.ViewModel
import com.sebaslogen.resaca.metro.metroViewModelScoped
import com.sebaslogen.resaca.rememberScoped
import com.sebaslogen.resacaapp.sample.ResacaSampleApp
import com.sebaslogen.resacaapp.sample.di.metro.MetroSampleViewModelFactory
import com.sebaslogen.resacaapp.sample.di.metro.createMetroAssistedFactory
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedRepo
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeMetroInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeMetroSecondInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeMetroSimpleInjectedViewModel
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Create a [FakeInjectedRepo] object with Resaca's [rememberScoped] function to let
 * Metro provide the [FakeInjectedRepo] with all the required dependencies and let
 * Resaca handle the lifecycle of the provided [FakeInjectedRepo].
 */
@Composable
fun DemoScopedMetroInjectedObjectComposable(key: String? = null) {
    val fakeInjectedRepo: FakeInjectedRepo = rememberScoped(key = key) { ResacaSampleApp.metroGraph.let { FakeInjectedRepo() } }
    DemoComposable(inputObject = fakeInjectedRepo, objectType = "Metro FakeInjectedRepo", scoped = true)
}

/**
 * Create a [ViewModel] with Resaca's [metroViewModelScoped] function to let
 * Metro provide the [ViewModel] with all the required dependencies and let
 * Resaca handle the lifecycle of the provided [ViewModel].
 */
@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedMetroSimpleInjectedViewModelComposable(key: String? = null) {
    val fakeInjectedVM: FakeMetroSimpleInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use metroViewModelScoped
            FakeMetroSimpleInjectedViewModel(
                repository = FakeInjectedRepo(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter
            )
        } else {
            metroViewModelScoped(
                key = key,
                factory = MetroSampleViewModelFactory(ResacaSampleApp.metroGraph),
            )
        }
    DemoComposable(inputObject = fakeInjectedVM, objectType = "Metro FakeMetroSimpleInjectedViewModel", scoped = true)
}

/**
 * Create a [ViewModel] with Resaca's [metroViewModelScoped] function to let
 * Metro provide the [ViewModel] with all the required dependencies and
 * Resaca handle the lifecycle of the provided [ViewModel].
 *
 * Assisted Injection is supported using Metro's @AssistedFactory and [createMetroAssistedFactory].
 */
@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedMetroInjectedViewModelComposable(key: String? = null, fakeInjectedViewModelId: Int = 666) {
    val fakeInjectedVM: FakeMetroInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use metroViewModelScoped
            FakeMetroInjectedViewModel(
                repository = FakeInjectedRepo(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter,
                viewModelId = fakeInjectedViewModelId
            )
        } else {
            metroViewModelScoped(
                key = key,
                factory = createMetroAssistedFactory(ResacaSampleApp.metroGraph, fakeInjectedViewModelId),
            )
        }
    DemoComposable(inputObject = fakeInjectedVM, objectType = "Metro FakeMetroInjectedViewModel", scoped = true)
}

/**
 * Create a [ViewModel] with Resaca's [metroViewModelScoped] function.
 * All parameters for this [FakeMetroSecondInjectedViewModel] are provided by Metro.
 */
@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedSecondMetroInjectedViewModelComposable() {
    val fakeSecondInjectedVM: FakeMetroSecondInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use metroViewModelScoped
            FakeMetroSecondInjectedViewModel(
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter
            )
        } else {
            metroViewModelScoped(
                factory = MetroSampleViewModelFactory(ResacaSampleApp.metroGraph),
            )
        }
    DemoComposable(inputObject = fakeSecondInjectedVM, objectType = "Metro FakeMetroSecondInjectedViewModel", scoped = true)
}

/**
 * Create a [ViewModel] with Resaca's [metroViewModelScoped] function with a [clearDelay].
 * The [ViewModel] will only be cleared after the [clearDelay] has passed since the Composable was disposed.
 */
@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedMetroInjectedViewModelWithClearDelayComposable(
    key: String? = null,
    clearDelay: Duration = 5.seconds,
    fakeInjectedViewModelId: Int = 666
) {
    val fakeInjectedVM: FakeMetroInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use metroViewModelScoped
            FakeMetroInjectedViewModel(
                repository = FakeInjectedRepo(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter,
                viewModelId = fakeInjectedViewModelId
            )
        } else {
            metroViewModelScoped(
                key = key,
                clearDelay = clearDelay,
                factory = createMetroAssistedFactory(ResacaSampleApp.metroGraph, fakeInjectedViewModelId),
            )
        }
    DemoComposable(inputObject = fakeInjectedVM, objectType = "Metro FakeMetroInjectedViewModel with clearDelay", scoped = true)
}

/**
 * Create a [ViewModel] with Resaca's simple [metroViewModelScoped] function with a [clearDelay].
 * This exercises the overload that uses a non-assisted factory.
 * The [ViewModel] will only be cleared after the [clearDelay] has passed since the Composable was disposed.
 */
@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedSecondMetroInjectedViewModelWithClearDelayComposable(
    key: String? = null,
    clearDelay: Duration = 5.seconds,
) {
    val fakeSecondInjectedVM: FakeMetroSecondInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use metroViewModelScoped
            FakeMetroSecondInjectedViewModel(
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter
            )
        } else {
            metroViewModelScoped(key = key, clearDelay = clearDelay, factory = MetroSampleViewModelFactory(ResacaSampleApp.metroGraph))
        }
    DemoComposable(inputObject = fakeSecondInjectedVM, objectType = "Metro FakeMetroSecondInjectedViewModel with clearDelay", scoped = true)
}
