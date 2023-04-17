package com.sebaslogen.resacaapp.sample.ui.main.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.lifecycle.*
import com.sebaslogen.resaca.hilt.hiltViewModelScoped
import com.sebaslogen.resaca.rememberScoped
import com.sebaslogen.resaca.viewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.data.*
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedViewModel.Companion.MY_ARGS_KEY
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter


/**
 * The objective of these Composables is to instantiate
 * fake business logic objects ([FakeRepo] or [FakeScopedViewModel]) and
 * to represent on the screen their unique memory location by rendering:
 * - the object's toString representation in a [Text] Composable
 * - a unique color for the object's instance using [objectToColorInt] as background
 * - a semi-unique emoji for the object's instance (limited to list of emojis available in [emojis])
 */

/**
 * Wraps the [DemoComposable] with a red border to indicate its content is not scoped
 */
@Composable
fun DemoNotScopedObjectComposable() {
    Box(
        modifier = Modifier
            .padding(top = 2.dp, bottom = 12.dp, start = 8.dp, end = 8.dp)
            .border(width = 4.dp, color = Color.Red)
    ) {
        DemoComposable(inputObject = FakeRepo(), objectType = "FakeRepo", scoped = false)
    }
}

@Composable
fun DemoScopedObjectComposable(key: String? = null, fakeRepoInstance: FakeRepo = FakeRepo()) {
    val fakeRepo: FakeRepo = rememberScoped(key = key) { fakeRepoInstance }
    DemoComposable(inputObject = fakeRepo, objectType = "FakeRepo", scoped = true)
}

/**
 * Create a [ViewModel] with the [viewModelScoped] function and with no external parameters/dependencies required by the constructor.
 * In this case the [SavedStateHandle] required by [FakeScopedViewModel] will be provided by the default [SavedStateViewModelFactory]
 */
@Composable
fun DemoScopedViewModelComposable(key: String? = null) {
    val fakeScopedVM: FakeScopedViewModel = viewModelScoped(key = key)
    DemoComposable(inputObject = fakeScopedVM, objectType = "FakeScopedViewModel", scoped = true)
}

/**
 * Create a [ViewModel] with the [viewModelScoped] function and use a provided builder with parameters/dependencies required by the constructor
 * Note: This is useful for frameworks like Koin or other way of providing dependencies
 */
@Composable
fun DemoScopedParametrizedViewModelComposable(
    viewModelInstance: FakeScopedViewModel = FakeScopedViewModel(stateSaver = SavedStateHandle(mapOf(MY_ARGS_KEY to 0))),
    key: String? = null
) {
    val fakeScopedParametrizedVM: FakeScopedViewModel = viewModelScoped(key = key) { viewModelInstance }
    DemoComposable(inputObject = fakeScopedParametrizedVM, objectType = "FakeScopedParametrizedViewModel", scoped = true)
}

@Composable
fun DemoScopedInjectedViewModelComposable(key: String? = null, fakeInjectedViewModelId: Int = 666) {
    val fakeInjectedVM: FakeInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use hiltViewModelScoped
            FakeInjectedViewModel(
                stateSaver = SavedStateHandle(),
                repository = FakeInjectedRepo(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter
            )
        } else {
            hiltViewModelScoped(key = key, defaultArguments = bundleOf(MY_ARGS_KEY to fakeInjectedViewModelId))
        }
    DemoComposable(inputObject = fakeInjectedVM, objectType = "Hilt FakeInjectedViewModel", scoped = true)
}

@Composable
fun DemoScopedSecondInjectedViewModelComposable() {
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