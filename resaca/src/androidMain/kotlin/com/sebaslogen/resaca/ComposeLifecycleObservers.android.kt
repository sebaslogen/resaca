package com.sebaslogen.resaca

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember

@Composable
@PublishedApi
internal actual fun ObserveComposableContainerLifecycle(scopedViewModelContainer: ScopedViewModelContainer) {
    // Observe state of configuration changes when disposing
    val activity = LocalActivity.current
        ?: throw IllegalStateException("Expected an Activity for detecting configuration changes for a NavBackStackEntry but instead found null")
    remember(activity) {
        object : RememberObserver {
            private fun onRemoved() {
                scopedViewModelContainer.setIsChangingConfiguration(activity.isChangingConfigurations)
            }

            override fun onAbandoned() {
                onRemoved()
            }

            override fun onForgotten() {
                onRemoved()
            }

            override fun onRemembered() {
                // no-op
            }
        }
    }
}