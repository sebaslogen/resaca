package com.sebaslogen.resaca

public const val COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS: Long = 1

expect public class PlatformLifecycleHandler() {
    public fun onResumed()
    public fun onDestroyed()
    public suspend fun awaitBeforeDisposing(inForeground: Boolean)
}