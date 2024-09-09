package com.sebaslogen.resaca

/**
 * There is no need to handle the lifecycle of objects in iOS, so this class is a no-op.
 */
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