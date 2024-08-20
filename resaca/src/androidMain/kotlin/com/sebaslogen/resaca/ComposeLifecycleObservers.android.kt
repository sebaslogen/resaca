package com.sebaslogen.resaca

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

@Composable
@PublishedApi
internal actual fun ObserveComposableContainerLifecycle(scopedViewModelContainer: ScopedViewModelContainer) {
    // Observe state of configuration changes when disposing
    val context = LocalContext.current
    DisposableEffect(context) {
        onDispose {
            scopedViewModelContainer.setIsChangingConfiguration(context.findActivity().isChangingConfigurations)
        }
    }
}

internal fun Context.findActivity(): Activity {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) {
            return ctx
        }
        ctx = ctx.baseContext
    }
    throw IllegalStateException(
        "Expected an activity context for detecting configuration changes for a NavBackStackEntry but instead found: $ctx"
    )
}