package com.sebaslogen.resaca.core

public actual class PlatformLifecycleHandler {
    public actual fun onResumed() {
        // No-op
    }

    public actual fun onDestroyed() {
        // No-op
    }

    public actual suspend fun awaitBeforeDisposing(inForeground: Boolean) {
        // No-op
    }
}