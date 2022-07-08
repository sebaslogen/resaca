package com.sebaslogen.resacaapp.ui.main.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


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
    DemoScopedInjectedViewModelComposable()
}

@Preview
@Composable
fun TestDemoScopedSecondInjectedViewModelComposable() {
    DemoScopedSecondInjectedViewModelComposable()
}
