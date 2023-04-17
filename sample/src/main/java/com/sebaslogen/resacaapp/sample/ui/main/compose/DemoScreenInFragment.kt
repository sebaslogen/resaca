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
fun DemoScreenInFragment(clickListener: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DemoNotScopedObjectComposable()
        DemoScopedObjectComposable()
        DemoScopedViewModelComposable()
        FragmentTwoButton(clickListener)
    }
}

@Composable
fun FragmentTwoButton(clickListener: () -> Unit) {
    Box(
        Modifier
            .padding(vertical = 12.dp, horizontal = 4.dp)
    ) {
        Button(onClick = clickListener) {
            Text("Click to navigate to nested fragment")
        }
    }

}
