@file:Suppress("NOTHING_TO_INLINE")

package com.sebaslogen.resaca

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Return an object created with the provided [builder] function and store this object
 * in the [ScopedViewModelContainer] which will keep this object in memory for as long as needed,
 * and until the requester Composable is permanently gone.
 * This means, it retains the object across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
 *
 * Internally, an extra key will be generated for this object in the Compose tree and if an object is present
 * for this key in the [ScopedViewModelContainer], then it will be returned instead of calling the [builder].
 *
 * @param key Key to track the version of the store object. Changing [key] between compositions will produce and remember a new value by calling [builder].
 * @param builder Factory function to produce a new value that will be remembered.
 */
@Composable
fun <T : Any> rememberScoped(key: Any? = null, builder: @DisallowComposableCalls () -> T): T {
    require(key !is Function0<*>) { "The Key for rememberScoped should not be a lambda" }

    val (scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: String, externalKey: ScopedViewModelContainer.ExternalKey) =
        generateKeysAndObserveLifecycle(key)

    // The object will be built the first time and retrieved in next calls or recompositions
    return scopedViewModelContainer.getOrBuildObject(
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        builder = builder
    )
}

/**
 * Return a [ViewModel] provided by the default [ViewModelProvider.Factory] and a [ViewModelProvider].
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
 *
 * The [ViewModel] will be created and stored by the [ViewModelProvider] using a default [ViewModelProvider.Factory] and a [ViewModelStore].
 * The [ScopedViewModelOwner] will be the object stored in the [ScopedViewModelContainer] and
 * the [ScopedViewModelContainer] will be in charge of keeping the [ScopedViewModelOwner] and its [ViewModel] in memory for as long as needed.
 *
 * Internally, an extra key will be generated for this [ScopedViewModelOwner] in the Compose tree and if a [ScopedViewModelOwner] is present
 * for this key in the [ScopedViewModelContainer], then it will be used to invoke [ViewModelProvider] to return an existing [ViewModel],
 * instead of creating a new [ScopedViewModelOwner] that produces a new [ViewModel] instance when the keys don't match.
 *
 * @param key Key to track the version of the [ViewModel]. Changing [key] between compositions will produce and remember a new [ViewModel].
 * @param defaultArguments A [Bundle] containing all the default arguments that will be provided to the [ViewModel].
 */
@Composable
inline fun <reified T : ViewModel> viewModelScoped(key: Any? = null, defaultArguments: Bundle = Bundle.EMPTY): T {
    require(key !is Function0<*>) { "The Key for viewModelScoped should not be a lambda" }

    val (scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: String, externalKey: ScopedViewModelContainer.ExternalKey) =
        generateKeysAndObserveLifecycle(key)

    val viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    // The object will be built the first time and retrieved in next calls or recompositions
    return scopedViewModelContainer.getOrBuildViewModel(
        modelClass = T::class.java,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        defaultArguments = defaultArguments
    )
}

/**
 * Return a [ViewModel] provided by the [builder] and a [ViewModelProvider].
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
 *
 * The [ViewModel] will be created and stored by the [ViewModelProvider] using the [builder] and a [ViewModelStore].
 * The [ScopedViewModelOwner] will be the object stored in the [ScopedViewModelContainer] and
 * the [ScopedViewModelContainer] will be in charge of keeping the [ScopedViewModelOwner] and its [ViewModel] in memory for as long as needed.
 *
 * Internally, an extra key will be generated for this [ScopedViewModelOwner] in the Compose tree and if a [ScopedViewModelOwner] is present
 * for this key in the [ScopedViewModelContainer], then it will be used to invoke [ViewModelProvider] to return an existing [ViewModel],
 * instead of creating a new [ScopedViewModelOwner] that produces a new [ViewModel] instance when the keys don't match.
 *
 * @param key Key to track the version of the [ViewModel]. Changing [key] between compositions will produce and remember a new [ViewModel].
 * @param defaultArguments A [Bundle] containing all the default arguments that will be provided to the [ViewModel].
 * @param builder Factory function to produce a new [ViewModel] that will be remembered.
 */
@Composable
inline fun <reified T : ViewModel> viewModelScoped(
    key: Any? = null,
    defaultArguments: Bundle = Bundle.EMPTY,
    noinline builder: @DisallowComposableCalls () -> T
): T {
    require(key !is Function0<*>) { "The Key for viewModelScoped should not be a lambda" }

    val (scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: String, externalKey: ScopedViewModelContainer.ExternalKey) =
        generateKeysAndObserveLifecycle(key)

    // The object will be built the first time and retrieved in next calls or recompositions
    return scopedViewModelContainer.getOrBuildViewModel(
        modelClass = T::class.java,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        defaultArguments = defaultArguments,
        builder = builder
    )
}

@Composable
fun generateKeysAndObserveLifecycle(key: Any?): Triple<ScopedViewModelContainer, String, ScopedViewModelContainer.ExternalKey> {
    val scopedViewModelContainer: ScopedViewModelContainer = viewModel()

    // This key will be used to identify, retrieve and remove the stored object in the ScopedViewModelContainer
    // across recompositions and configuration changes
    val positionalMemoizationKey: String = rememberSaveable { UUID.randomUUID().toString() }
    // The external key will be used to track and store new versions of the object, based on [key] input parameter
    val externalKey: ScopedViewModelContainer.ExternalKey = ScopedViewModelContainer.ExternalKey.from(key)

    ObserveLifecycles(scopedViewModelContainer, positionalMemoizationKey)

    return Triple(scopedViewModelContainer, positionalMemoizationKey, externalKey)
}

@Composable
@PublishedApi
internal inline fun ObserveLifecycles(scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: String) {
    // Observe this destination's lifecycle to detect screen resumed/paused/destroyed
    // and remember or forget this object correctly from the container (so it can be garbage collected when needed)
    ObserveLifecycleWithScopedViewModelContainer(scopedViewModelContainer)
    // Observe the lifecycle of this Composable to detect disposal (with onAbandoned & onForgotten)
    // and remember or forget this object correctly from the container (so it can be garbage collected when needed)
    ObserveComposableDisposal(positionalMemoizationKey, scopedViewModelContainer)
}

/**
 * Observe the lifecycle of this Composable to detect disposal (with onAbandoned & onForgotten)
 * This function creates an observer that notifies the [scopedViewModelContainer] when composition is abandoned or forgotten,
 * and then this observer is remembered to connect the observer's callbacks to the lifecycle of this Composable
 */
@Composable
@PublishedApi
internal inline fun ObserveComposableDisposal(positionalMemoizationKey: String, scopedViewModelContainer: ScopedViewModelContainer) {
    remember(positionalMemoizationKey) { RememberScopedObserver(scopedViewModelContainer, positionalMemoizationKey) }
}

/**
 * Observe the lifecycle of this navigation destination in [ScopedViewModelContainer] and detect screen resumed/paused/destroyed.
 * With this observer we can detect when an object (stored in [ScopedViewModelContainer]) is missing on the screen
 * after the screen is resumed and then we can finally dispose the object after a delay
 *
 * The current navigation destination that owns the lifecycle can be either a:
 * - [NavBackStackEntry] ScopedViewModelContainer will live in the scope of this Nav-graph destination
 * - [Fragment] ScopedViewModelContainer will live in the scope of this Fragment
 * - [Activity] ScopedViewModelContainer will live in the scope of this Activity
 *
 * Note: the addObserver call needs to run on main thread because there is a thread check in [LifecycleRegistry.addObserver].
 * Note2: Adding the same observer [scopedViewModelContainer] twice to the lifecycle has no effect
 */
@Composable
@PublishedApi
internal fun ObserveLifecycleWithScopedViewModelContainer(scopedViewModelContainer: ScopedViewModelContainer) {
    // Observe state of configuration changes when disposing
    val context = LocalContext.current
    DisposableEffect(context) {
        onDispose {
            scopedViewModelContainer.setChangingConfigurationState(context.findActivity().isChangingConfigurations)
        }
    }

    // Observe general lifecycle events (resume, pause, destroy, etc.)
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    // Use LaunchedEffect to make sure we have a coroutine scope to run on main-thread
    // and to add the observer again every time the lifecycle or the ScopedViewModelContainer change
    LaunchedEffect(lifecycle, scopedViewModelContainer) {
        launch(Dispatchers.Main) {
            lifecycle.addObserver(scopedViewModelContainer)
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