package com.sebaslogen.resaca.compose

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.sebaslogen.resaca.ScopedViewModelContainer
import com.sebaslogen.resaca.ScopedViewModelContainer.Key
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random


/**
 * Return an object created with the provided [builder] function
 * and store this object in the [ScopedViewModelContainer] that will keep this
 * object in memory as long as needed
 * A key will be generated for this object in the Compose tree and if an object
 * is present in [ScopedViewModelContainer] for this key it will be returned instead of calling [builder]
 */
@Composable
fun <T : Any> rememberScoped(builder: (() -> T)): T {
    val scopedViewModelContainer: ScopedViewModelContainer = viewModel()
    ObserverLifecycleWithScopedViewModelContainer(scopedViewModelContainer)
    val key = Key(rememberSaveable { Random.nextInt() })
    val scopedObject: T = scopedViewModelContainer.getOrBuildObject(key, builder)

    DisposableEffect(key) { // Remove reference to object from ScopedViewModelContainer so it can be garbage collected
        onDispose { scopedViewModelContainer.onDisposedFromComposition(key) }
    }
    return scopedObject
}

/**
 * Observe the lifecycle of this navigation destination in [ScopedViewModelContainer] and detect screen resumed/paused/destroyed
 * With this observer we can detect when an object (stored in [ScopedViewModelContainer]) is missing on the screen
 * after the screen is resumed and then we can finally dispose the object after a delay
 *
 * The current navigation destination that owns the lifecycle can be either a:
 * - [NavBackStackEntry] ScopedViewModelContainer will live in the scope of this Nav-graph destination
 * - [Fragment] ScopedViewModelContainer will live in the scope of this Fragment
 * - [Activity] ScopedViewModelContainer will live in the scope of this Activity
 *
 * Note: the addObserver call needs to run on main thread because there is a thread check in [LifecycleRegistry.addObserver]
 */
@Composable
private fun ObserverLifecycleWithScopedViewModelContainer(scopedViewModelContainer: ScopedViewModelContainer) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle, scopedViewModelContainer) {
        launch(Dispatchers.Main) {
            lifecycle.addObserver(scopedViewModelContainer)
        }
    }
}