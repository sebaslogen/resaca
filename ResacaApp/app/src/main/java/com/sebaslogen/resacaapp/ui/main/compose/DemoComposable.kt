package com.sebaslogen.resacaapp.ui.main.compose

import androidx.annotation.ColorInt
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sebaslogen.resaca.compose.rememberScoped
import com.sebaslogen.resacaapp.ui.main.data.FakeRepo
import com.sebaslogen.resacaapp.ui.main.data.FakeScopedViewModel
import com.sebaslogen.resacaapp.ui.main.toHexString
import com.sebaslogen.resacaapp.ui.main.ui.theme.emojis

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
        val scopedBannerText = if (scoped) "Scoped" else "Not scoped"
        Text(scopedBannerText, textAlign = TextAlign.Center, modifier = Modifier.rotate(-90f))

        val objectAddressName = remember { objectToShortStringWithoutPackageName(inputObject) }
        Text(
            modifier = Modifier
                .background(Color(objectToColorInt(inputObject)))
                .padding(vertical = 18.dp, horizontal = 8.dp)
                .weight(1f)
                .fillMaxHeight(),
            text = "Composable that uses \n$objectType with address:\n$objectAddressName"
        )

        val objectAddressEmoji = remember { objectToEmoji(inputObject) }
        Text(
            modifier = Modifier
                .padding(vertical = 18.dp, horizontal = 4.dp)
                .fillMaxHeight(),
            fontSize = 30.sp,
            text = objectAddressEmoji
        )
    }
}

@ColorInt
private fun objectToColorInt(inputObject: Any): Int =
    android.graphics.Color.parseColor(
        "#9F" + inputObject
            .hashCode()
            .toHexString()
            .substring(0..5)
    )

private fun objectToEmoji(inputObject: Any): String = emojis[inputObject.hashCode() % emojis.size]

private fun objectToShortStringWithoutPackageName(inputObject: Any): String =
    inputObject.toString().removePrefix("com.sebaslogen.resacaapp.ui.main.data.")