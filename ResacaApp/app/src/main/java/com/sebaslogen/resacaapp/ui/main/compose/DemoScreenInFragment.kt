package com.sebaslogen.resacaapp.ui.main.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.sebaslogen.resaca.compose.rememberScoped
import com.sebaslogen.resaca.compose.rememberScopedViewModel
import com.sebaslogen.resacaapp.ui.main.data.FakeRepo
import com.sebaslogen.resacaapp.ui.main.data.FakeScopedViewModel

@Composable
fun DemoScreenInFragment() {
    Column {
        DemoScopedObjectComposable()
        DemoScopedViewModelComposable()
    }
}

@Composable
fun DemoScopedObjectComposable() {
    val fakeRepo: FakeRepo = rememberScoped { FakeRepo() }
    DemoComposable(fakeRepo, "FakeRepo")
}

@Composable
fun DemoScopedViewModelComposable() {
    val fakeScopedVM: FakeScopedViewModel = rememberScopedViewModel { FakeScopedViewModel() }
    DemoComposable(fakeScopedVM, "FakeScopedViewModel")
}
