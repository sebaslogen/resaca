package com.sebaslogen.resaca

import com.sebaslogen.resaca.utils.ResacaPackagePrivate

@ResacaPackagePrivate
public const val COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS: Long = 1

/**
 * This class allows a specific platform to handle the lifecycle of objects stored in [ScopedViewModelContainer].
 * For example, in Android, this class is used to wait for the first frame after Activity resumes and properly handle changes after configuration changes.
 */
public expect class PlatformLifecycleHandler() {
    public fun onResumed()
    public fun onDestroyed()
    public suspend fun awaitBeforeDisposing(inForeground: Boolean)
}


public class DefaultPlatformLifecycleHandler() {
    public fun onResumed() {}
    public fun onDestroyed() {}
    public suspend fun awaitBeforeDisposing(inForeground: Boolean) {}
}