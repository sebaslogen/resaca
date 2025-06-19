package com.sebaslogen.resaca

import androidx.core.bundle.Bundle
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModelProvider.Companion.VIEW_MODEL_KEY
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedState
import androidx.savedstate.SavedStateWriter
import androidx.savedstate.savedState


/**
 * Combine the existing arguments present in the receiver's [CreationExtras] under the key [DEFAULT_ARGS_KEY] with the [defaultArguments] parameter.
 * When the no arguments are present just add them.
 */
private fun CreationExtras.addDefaultArguments(defaultArguments: Bundle): CreationExtras =
    if (defaultArguments.isEmpty()) {
        this
    } else {
        MutableCreationExtras(this).apply {
            val initialExtras: SavedState = get(DEFAULT_ARGS_KEY) ?: savedState(emptyMap())
            val combinedExtras = savedState {
                putAll(initialExtras)
                defaultArguments.keySet().forEach { key ->
                    if (key != null) put(key, defaultArguments.get(key))
                }
            }
            set(DEFAULT_ARGS_KEY, combinedExtras)
        }
    }

private fun SavedStateWriter.put(key: String, inputObject: Any?) {
    when (inputObject) {
        is Boolean -> putBoolean(key, inputObject)
        is Char -> putChar(key, inputObject)
        is Int -> putInt(key, inputObject)
        is Long -> putLong(key, inputObject)
        is Float -> putFloat(key, inputObject)
        is Double -> putDouble(key, inputObject)
        is String -> putString(key, inputObject)
        is CharSequence -> putCharSequence(key, inputObject)
        is BooleanArray -> putBooleanArray(key, inputObject)
        is CharArray -> putCharArray(key, inputObject)
        is IntArray -> putIntArray(key, inputObject)
        is LongArray -> putLongArray(key, inputObject)
        is FloatArray -> putFloatArray(key, inputObject)
        is DoubleArray -> putDoubleArray(key, inputObject)
        is List<*> -> {
            when (inputObject.firstOrNull()) {
                is Int -> putIntList(key, inputObject as List<Int>)
                is CharSequence -> putCharSequenceList(key, inputObject as List<CharSequence>)
                is String -> putStringList(key, inputObject as List<String>)
                else -> putNull(key)
            }
        }

        is Array<*> -> {
            when (inputObject.firstOrNull()) {
                is String -> putStringArray(key, inputObject as Array<String>)
                is CharSequence -> putCharSequenceArray(key, inputObject as Array<CharSequence>)
                else -> putNull(key)
            }
        }

        else -> putNull(key) // We can't parse the type so we map to null :(
    }
}

internal fun Bundle.toCreationExtras(
    viewModelStoreOwner: ViewModelStoreOwner
): CreationExtras =
    if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
        viewModelStoreOwner.defaultViewModelCreationExtras
    } else {
        CreationExtras.Empty
    }.addDefaultArguments(this)

/**
 * This is a helper function to add the [viewModelKey] to the [CreationExtras] if it is not already present.
 * The [viewModelKey] is, among others, used to create a [SavedStateHandle] for the [ViewModel].
 */
internal fun CreationExtras.addViewModelKey(viewModelKey: String): CreationExtras =
    MutableCreationExtras(this).apply {
        if (get(VIEW_MODEL_KEY) == null) {
            set(VIEW_MODEL_KEY, viewModelKey)
        }
    }
