package com.sebaslogen.resacaapp.sample.ui.main.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeRepo
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeScopedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.ui.theme.emojis

/**
 * The objective of these Composables is to instantiate
 * fake business logic objects (like [FakeRepo] or [FakeScopedViewModel]) and
 * to represent on the screen their unique memory location by rendering:
 * - the object's toString representation in a [Text] Composable
 * - a unique color for the object's instance using [objectToColorInt] as background
 * - a semi-unique emoji for the object's instance (limited to list of emojis available in [emojis])
 */

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
        val objectAddress = remember(inputObject) { objectToShortStringWithoutPackageName(inputObject).replaceBeforeLast("@", "") }
        Text(
            modifier = Modifier
                .testTag("$objectType $scopedBannerText") // Semantics used for automated tests to find this node
                .background(Color(objectToColorInt(inputObject)))
                .padding(vertical = 18.dp, horizontal = 8.dp)
                .weight(1f)
                .fillMaxHeight(),
            text = buildAnnotatedString {
                append(
                    AnnotatedString("Composable hosting a \n", spanStyle = SpanStyle(fontWeight = FontWeight.Light))
                )
                append(
                    AnnotatedString(objectType, spanStyle = SpanStyle(fontWeight = FontWeight.Bold))
                )
                append(
                    AnnotatedString(
                        " with address: ",
                        spanStyle = SpanStyle(fontWeight = FontWeight.Light, fontSize = 11.sp)
                    )
                )
                append(
                    AnnotatedString(
                        objectAddress,
                        spanStyle = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    )
                )
            }
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
