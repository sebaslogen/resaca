package com.sebaslogen.resacaapp.ui.main.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@Composable
fun DemoScopedViewModelComposable() {
    val fakeScopedVM: FakeScopedViewModel = rememberScopedViewModel { FakeScopedViewModel() }
    DemoComposable(inputObject = fakeScopedVM, objectType = "FakeScopedViewModel", scoped = true)
}

@Composable
fun FragmentTwoButton(clickListener: () -> Unit) {
    Box(
        Modifier
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Button(onClick = clickListener) {
            Text("CLick to navigate to nested fragment")
        }
    }

}
