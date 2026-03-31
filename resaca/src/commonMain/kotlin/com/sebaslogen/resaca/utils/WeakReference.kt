package com.sebaslogen.resaca.utils

import kotlin.experimental.ExperimentalNativeApi

@ExperimentalNativeApi // This must be propagated from the underlying native implementation.
internal expect class WeakReference<T : Any>(referred: T) {
    public fun get(): T?
    public fun clear()
}
