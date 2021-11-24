package com.sebaslogen.resaca.compose

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.sebaslogen.resaca.ScopedViewModel
import com.sebaslogen.resaca.ScopedViewModelContainer
import com.sebaslogen.resaca.ScopedViewModelContainer.Key
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
    val viewModelContainer: ScopedViewModelContainer = viewModel()
    val key = Key(rememberSaveable { Random.nextInt() })
    val scopedObject: T = viewModelContainer.getOrBuildObject(key, builder)

    DisposableEffect(key) { // Remove reference to object from ScopedViewModelContainer so it can be garbage collected
        onDispose { viewModelContainer.onDisposedFromComposition(key) }
    }
    return scopedObject
}

/**
 * Return an object created with the provided [builder] function
 * and store this object in the [ScopedViewModelContainer] that will keep this
 * object in memory as long as needed
 * A key will be generated for this object in the Compose tree and if an object
 * is present in [ScopedViewModelContainer] for this key it will be returned instead of calling [builder]
 */
@Composable
fun <T : ScopedViewModel> rememberScopedViewModel(builder: (() -> T)): T {
    val viewModelContainer: ScopedViewModelContainer = viewModel()
    val key = Key(rememberSaveable { Random.nextInt() })
    val scopedViewModel: T = viewModelContainer.getOrBuildScopedViewModel(key, builder)

    DisposableEffect(key) { // Remove reference to ScopedViewModel from ScopedViewModelContainer so it can be garbage collected and the CoroutineScope cancelled
        onDispose { viewModelContainer.onDisposedFromComposition(key) }
    }

    return scopedViewModel
}

/**
 * Observe [NavBackStackEntry]'s (destination) Lifecycle in [ScopedViewModelContainer] to detect screen resumed/shown/destroyed
 * We can detect objects stored in [ScopedViewModelContainer] that are missing after this resume and dispose them after a delay
 */
@Composable
@SuppressLint("ComposableNaming")
fun NavBackStackEntry.installScopedViewModelContainer() {
    val viewModelContainer: ScopedViewModelContainer = viewModel()
    lifecycle.addObserver(viewModelContainer)
}

/**
 * Observe Fragment's (Compose-container) Lifecycle in [ScopedViewModelContainer] to detect screen resumed/shown/destroyed
 * We can detect objects stored in [ScopedViewModelContainer] that are missing after this resume and dispose them after a delay
 */
@Composable
@SuppressLint("ComposableNaming")
fun Fragment.installScopedViewModelContainer() {
    val viewModelContainer: ScopedViewModelContainer = viewModel()
    lifecycle.addObserver(viewModelContainer)
}

/**
 * Observe Activity's (Compose-container) Lifecycle in [ScopedViewModelContainer] to detect screen resumed/shown/destroyed
 * We can detect objects stored in [ScopedViewModelContainer] that are missing after this resume and dispose them after a delay
 */
@Composable
@SuppressLint("ComposableNaming")
fun ComponentActivity.installScopedViewModelContainer() {
    val viewModelContainer: ScopedViewModelContainer = viewModel()
    lifecycle.addObserver(viewModelContainer)
}