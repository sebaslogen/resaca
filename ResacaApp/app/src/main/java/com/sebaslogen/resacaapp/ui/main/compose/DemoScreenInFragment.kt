package com.sebaslogen.resacaapp.ui.main.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sebaslogen.resaca.compose.rememberScoped
import com.sebaslogen.resaca.compose.rememberScopedViewModel
import com.sebaslogen.resacaapp.ui.main.data.FakeRepo
import com.sebaslogen.resacaapp.ui.main.data.FakeScopedViewModel

@Composable
fun DemoScreenInFragment(clickListener: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DemoNotScopedObjectComposable()
        DemoScopedObjectComposable()
        DemoScopedViewModelComposable()
        FragmentTwoButton(clickListener)
    }
}

@Composable
fun DemoNotScopedObjectComposable() {
    Box(modifier = Modifier.padding(5.dp)) {
        DemoComposable(FakeRepo(), "FakeRepo")
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

@Composable
fun FragmentTwoButton(clickListener: () -> Unit) {
    Box(
        Modifier
            .padding(vertical = 20.dp)
    ) {
        Button(onClick = clickListener) {
            Text("CLick to navigate to nested fragment")
        }
    }

}
