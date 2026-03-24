package com.sebaslogen.resacaapp.sample.ui.main.compose.examples

import androidx.compose.runtime.Composable
import com.sebaslogen.resaca.rememberScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeRepo
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds


/**
 * Instantiate a simple object (no constructor dependencies and therefore no injection) and retain it with resaca's [rememberScoped] function.
 */
@Composable
fun DemoScopedObjectComposable(key: String? = null, fakeRepoInstance: FakeRepo = FakeRepo()) {
    val fakeRepo: FakeRepo = rememberScoped(key = key) { fakeRepoInstance }
    DemoComposable(inputObject = fakeRepo, objectType = "FakeRepo", scoped = true)
}

/**
 * Instantiate a simple object and retain it with resaca's [rememberScoped] function with a [clearDelay].
 * The object will only be cleared after the [clearDelay] has passed since the Composable was disposed.
 */
@Composable
fun DemoScopedObjectWithClearDelayComposable(
    key: String? = null,
    clearDelay: Duration = 5.seconds,
    fakeRepoInstance: FakeRepo = FakeRepo()
) {
    val fakeRepo: FakeRepo = rememberScoped(key = key, clearDelay = clearDelay) { fakeRepoInstance }
    DemoComposable(inputObject = fakeRepo, objectType = "FakeRepo with clearDelay", scoped = true)
}
