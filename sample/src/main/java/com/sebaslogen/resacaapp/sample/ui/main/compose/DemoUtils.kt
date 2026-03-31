package com.sebaslogen.resacaapp.sample.ui.main.compose

import android.annotation.SuppressLint
import androidx.annotation.ColorInt
import androidx.compose.runtime.Composable
import androidx.core.graphics.toColorInt
import androidx.lifecycle.SavedStateHandle
import com.sebaslogen.resacaapp.sample.ui.main.toHexString
import com.sebaslogen.resacaapp.sample.ui.main.ui.theme.emojis


/**
 * Convert the object's [hashCode] into a color
 */
@ColorInt
internal fun objectToColorInt(inputObject: Any): Int =
    ("#9F" + inputObject
        .hashCode()
        .toHexString()
        .padStart(6, 'A')
        .substring(0..5)).toColorInt()

internal fun objectToEmoji(inputObject: Any): String = emojis[inputObject.hashCode() % emojis.size]

internal fun objectToShortStringWithoutPackageName(inputObject: Any): String =
    inputObject.toString().replaceBeforeLast(".", "").removePrefix(".")

@SuppressLint("VisibleForTests")
@Composable
internal fun savedStateHandleForPreviewsTesting(): SavedStateHandle = SavedStateHandle()
