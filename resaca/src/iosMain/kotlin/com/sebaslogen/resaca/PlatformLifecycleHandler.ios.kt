package com.sebaslogen.resaca

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