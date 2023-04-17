package com.sebaslogen.resacaapp.sample.ui.main.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DemoComposable(
    inputObject: Any,
    objectType: String,
    scoped: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical label for the Scoping of the input object
        val scopedBannerText = if (scoped) "Scoped" else "Not scoped"
        Text(scopedBannerText, textAlign = TextAlign.Center, modifier = Modifier.rotate(-90f))

        // Text representation of the input object
        val objectAddressName = remember(inputObject) { objectToShortStringWithoutPackageName(inputObject) }
        Text(
            modifier = Modifier
                .testTag("$objectType $scopedBannerText") // Semantics used for automated tests to find this node
                .background(Color(objectToColorInt(inputObject)))
                .padding(vertical = 18.dp, horizontal = 8.dp)
                .weight(1f)
                .fillMaxHeight(),
            text = "Composable that uses \n$objectType with address:\n$objectAddressName"
        )

        // Emoji representation of the input object
        val objectAddressEmoji = remember(inputObject) { objectToEmoji(inputObject) }
        Text(
            modifier = Modifier
                .padding(vertical = 18.dp, horizontal = 4.dp)
                .fillMaxHeight(),
            fontSize = 30.sp,
            text = objectAddressEmoji
        )
    }
}
