package com.sebaslogen.resacaapp.sample.ui.main.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedHiltInjectedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedObjectComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedParametrizedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedSecondHiltInjectedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedViewModelComposable


@Preview
@Composable
fun TestDemoNotScopedObjectComposable() {
    DemoNotScopedObjectComposable()
}

@Preview
@Composable
fun TestDemoScopedObjectComposable() {
    DemoScopedObjectComposable()
}

@Preview
@Composable
fun TestDemoScopedViewModelComposable() {
    DemoScopedViewModelComposable()
}

@Preview
@Composable
fun TestDemoScopedParametrizedViewModelComposable() {
    DemoScopedParametrizedViewModelComposable()
}

@Preview
@Composable
fun TestDemoScopedInjectedViewModelComposable() {
    DemoScopedHiltInjectedViewModelComposable()
}

@Preview
@Composable
fun TestDemoScopedSecondInjectedViewModelComposable() {
    DemoScopedSecondHiltInjectedViewModelComposable()
}
