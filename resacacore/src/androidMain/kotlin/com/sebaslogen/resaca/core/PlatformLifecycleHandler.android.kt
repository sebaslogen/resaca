package com.sebaslogen.resaca.core

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume

public actual class PlatformLifecycleHandler {
    public actual fun onResumed() {
        compositionResumedTimeout.countDown() // Signal that the first composition after resume is happening
    }

    public actual fun onDestroyed() {
        compositionResumedTimeout.countDown() // Clear any pending waiting latch
        compositionResumedTimeout = CountDownLatch(1) // Start a new latch for the next time this ViewModel is used after resume
    }


    /**
     * Handler to post work to the main thread and used to wait for the first frame after Activity resumes,
     * when this happens it is safe to continue with scheduled disposal of objects that are
     * not required after configuration change.
     */
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Lock to wait for the first composition after Activity resumes.
     * This is apparently only required in automated tests.
     */
    private var compositionResumedTimeout = CountDownLatch(1)




    /**
     * Await for the next frame when the Activity is resumed.
     * See these blog posts for more info:
     * - https://blog.p-y.wtf/whilesubscribed5000
     * - https://developer.squareup.com/blog/a-journey-on-the-android-main-thread-lifecycle-bits/
     *
     * In a nutshell: Any work posted to the main thread while the UI is in the background
     * will be scheduled to be executed after the next frame when the Activity is resumed.
     * If the Activity never comes back, then the work will be cancelled and
     * the FrameCallback will be removed thanks to the coroutine scope cancellation.
     */
    private suspend fun awaitChoreographerFramePostFrontOfQueue() {
        val localCoroutineScope = CoroutineScope(coroutineContext)
        suspendCancellableCoroutine { continuation ->
            val frameCallback = Choreographer.FrameCallback {
                handler.postAtFrontOfQueue { // This needs to be posted and run right after Activity resumes
                    localCoroutineScope.launch {
                        withContext(Dispatchers.IO) { // This needs to be done in IO because it's a blocking call
                            // This extra wait is needed to make sure Composition happens after resume on automated tests
                            compositionResumedTimeout.await(COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)
                        }
                        handler.post {
                            if (!continuation.isCompleted) {
                                continuation.resume(Unit)
                            }
                        }
                    }
                }
            }
            Choreographer.getInstance().postFrameCallback(frameCallback)

            continuation.invokeOnCancellation {
                Choreographer.getInstance().removeFrameCallback(frameCallback)
            }
        }
    }

    public actual suspend fun awaitBeforeDisposing(inForeground: Boolean) {
        if (!inForeground) awaitChoreographerFramePostFrontOfQueue() // When in background, wait for the next frame when the Activity is resumed
    }
}