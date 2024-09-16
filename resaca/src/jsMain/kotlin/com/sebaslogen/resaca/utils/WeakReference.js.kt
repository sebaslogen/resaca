package com.sebaslogen.resaca.utils

import kotlin.experimental.ExperimentalNativeApi

@ExperimentalNativeApi // This must be propagated from the underlying native implementation.
internal actual class WeakReference<T : Any> actual constructor(referred: T) {
    private var reference: WeakRef<T>? = WeakRef(referred)

    actual fun get(): T? {
        return reference?.deref()
    }

    actual fun clear() {
        reference = null
    }
}

// https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/WeakRef
private external class WeakRef<T>(value: T) {
    fun deref(): T?
}