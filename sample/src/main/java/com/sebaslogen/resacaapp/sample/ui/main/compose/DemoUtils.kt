package com.sebaslogen.resacaapp.sample.ui.main.compose

import androidx.annotation.ColorInt
import com.sebaslogen.resacaapp.sample.ui.main.toHexString
import com.sebaslogen.resacaapp.sample.ui.main.ui.theme.emojis


/**
 * Convert the object's [hashCode] into a color
 */
@ColorInt
internal fun objectToColorInt(inputObject: Any): Int =
    android.graphics.Color.parseColor(
        "#9F" + inputObject
            .hashCode()
            .toHexString()
            .padStart(6, 'A')
            .substring(0..5)
    )

internal fun objectToEmoji(inputObject: Any): String = emojis[inputObject.hashCode() % emojis.size]

internal fun objectToShortStringWithoutPackageName(inputObject: Any): String =
    inputObject.toString().removePrefix("com.sebaslogen.resacaapp.ui.main.data.")