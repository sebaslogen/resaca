package com.sebaslogen.resacaapp.ui.main.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.sebaslogen.resaca.compose.rememberScoped
import com.sebaslogen.resacaapp.ui.main.data.FakeRepo
import com.sebaslogen.resacaapp.ui.main.data.FakeScopedViewModel

/**
 * The objective of these Composables is to instantiate
 * fake business logic objects ([FakeRepo] or [FakeScopedViewModel]) and
 * to represent on the screen their unique memory location by rendering:
 * - the object's toString representation in a [Text] Composable
 * - a unique color for the object's instance using [objectToColorInt] as background
 * - a semi-unique emoji for the object's instance (limited to list of emojis available in [emojis])
 */

/**
 * Wraps the [DemoComposable] with a red border to indicate its content is not scoped
 */
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
    val fakeScopedVM: FakeScopedViewModel = rememberScoped { FakeScopedViewModel() }
    DemoComposable(inputObject = fakeScopedVM, objectType = "FakeScopedViewModel", scoped = true)
}

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