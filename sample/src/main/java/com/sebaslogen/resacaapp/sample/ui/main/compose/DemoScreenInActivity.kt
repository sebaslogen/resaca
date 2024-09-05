package com.sebaslogen.resacaapp.sample.ui.main.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedObjectComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedViewModelComposable

@Composable
fun DemoScreenInActivity(clickListener: () -> Unit) {
    var contentKey by rememberSaveable { mutableStateOf(false) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DemoScopedObjectComposable()
        DemoScopedViewModelComposable(key = contentKey.toString())
        Button(onClick = { contentKey = !contentKey }) {
            Text("Get a new instance of the FakeScopedViewModel")
        }
        ComposeActivityButton(clickListener)
    }
}

@Composable
fun ComposeActivityButton(clickListener: () -> Unit) {
    Box(
        Modifier
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Button(onClick = clickListener) {
            Text("Click to navigate to a full Compose Activity with Compose navigation")
        }
    }
}
