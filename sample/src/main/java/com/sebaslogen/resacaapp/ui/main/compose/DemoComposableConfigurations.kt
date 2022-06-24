package com.sebaslogen.resacaapp.ui.main.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.sebaslogen.resaca.hilt.hiltViewModelScoped
import com.sebaslogen.resaca.rememberScoped
import com.sebaslogen.resaca.viewModelScoped
import com.sebaslogen.resacaapp.ui.main.data.FakeInjectedViewModel
import com.sebaslogen.resacaapp.ui.main.data.FakeRepo
import com.sebaslogen.resacaapp.ui.main.data.FakeScopedViewModel


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
fun DemoScopedObjectComposable() {
    val fakeRepo: FakeRepo = rememberScoped { FakeRepo() }
    DemoComposable(inputObject = fakeRepo, objectType = "FakeRepo", scoped = true)
}

/**
 * Create a [ViewModel] with the [viewModelScoped] function and with no external parameters/dependencies required by the constructor
 * Note: To use Koin or other way of providing dependencies you can use rememberScoped like this:
 *        val fakeScopedVM: FakeScopedViewModel = rememberScoped { FakeScopedViewModel(viewModelsClearedCounter) }
 */
@Composable
fun DemoScopedViewModelComposable() {
    val fakeScopedVM: FakeScopedViewModel = viewModelScoped()
    DemoComposable(inputObject = fakeScopedVM, objectType = "FakeScopedViewModel", scoped = true)
}

@Composable
fun DemoScopedInjectedViewModelComposable() {
    val fakeInjectedVM: FakeInjectedViewModel = hiltViewModelScoped()
    DemoComposable(inputObject = fakeInjectedVM, objectType = "Hilt FakeInjectedViewModel", scoped = true)
}