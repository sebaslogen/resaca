package com.sebaslogen.resaca

/**
 * This class allows a specific platform to handle the lifecycle of objects stored in [ScopedViewModelContainer].
 * For example, in Android, this class is used to wait for the first frame after Activity resumes and properly handle changes after configuration changes.
 */
public actual class PlatformLifecycleHandler actual constructor() {
    actual fun onResumed() {
    }

    actual fun onDestroyed() {
    }

    actual suspend fun awaitBeforeDisposing(inForeground: Boolean) {
    }
}