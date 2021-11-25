package com.sebaslogen.resacaapp.ui.main.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sebaslogen.resacaapp.ui.main.toHexString


@Composable
fun DemoComposable(
    inputObject: Any,
    objectType: String,
    scoped: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        val scopedBannerText = if (scoped) "Scoped" else "Not scoped"
        Text(scopedBannerText, textAlign = TextAlign.Center, modifier = Modifier.rotate(-90f))
        Box(
            Modifier
                .padding(vertical = 2.dp)
                .background(
                    Color(
                        android.graphics.Color.parseColor(
                            "#" + inputObject
                                .hashCode()
                                .toHexString()
                                .substring(0..5)
                        )
                    )
                )
        ) {
            Text(
                modifier = Modifier.padding(vertical = 10.dp, horizontal = 5.dp),
                text = "Composable that uses \n$objectType with address:\n$inputObject"
            )
        }
    }
}