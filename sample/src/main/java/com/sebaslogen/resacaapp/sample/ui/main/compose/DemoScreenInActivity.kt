package com.sebaslogen.resacaapp.sample.ui.main.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DemoScreenInActivity(clickListener: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DemoScopedObjectComposable()
        DemoScopedViewModelComposable()
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
