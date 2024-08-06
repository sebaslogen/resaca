package com.sebaslogen.resacaapp.sample.ui.main.compose.examples

import androidx.compose.runtime.Composable
import com.sebaslogen.resaca.core.rememberScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeRepo


/**
 * Instantiate a simple object (no constructor dependencies and therefore no injection) and retain it with resaca's [rememberScoped] function.
 */
@Composable
fun DemoScopedObjectComposable(key: String? = null, fakeRepoInstance: FakeRepo = FakeRepo()) {
    val fakeRepo: FakeRepo = rememberScoped(key = key) { fakeRepoInstance }
    DemoComposable(inputObject = fakeRepo, objectType = "FakeRepo", scoped = true)
}