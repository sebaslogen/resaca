package com.sebaslogen.resacaapp.ui.main.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


@Preview
@Composable
fun testDemoNotScopedObjectComposable() {
    DemoNotScopedObjectComposable()
}

@Preview
@Composable
fun testDemoScopedObjectComposable() {
    DemoScopedObjectComposable()
}

@Preview
@Composable
fun testDemoScopedViewModelComposable() {
    DemoScopedViewModelComposable()
}

@Preview
@Composable
fun testDemoScopedParametrizedViewModelComposable() {
    DemoScopedParametrizedViewModelComposable()
}
