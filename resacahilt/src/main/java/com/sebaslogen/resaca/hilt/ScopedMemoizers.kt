@file:Suppress("NOTHING_TO_INLINE")

package com.sebaslogen.resaca.hilt

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import com.sebaslogen.resaca.ScopedViewModelContainer
import com.sebaslogen.resaca.ScopedViewModelOwner
import com.sebaslogen.resaca.generateKeysAndObserveLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 * Return a [ViewModel] (annotated with [HiltViewModel]) provided by a Hilt [ViewModelProvider.Factory] and a [ViewModelProvider].
 *
 * The returned [ViewModel] is provided by the [ViewModelProvider] using a Hilt [ViewModelProvider.Factory] and a [ViewModelStore].
 * The [ViewModel] will be created and stored by the [ViewModelProvider] in the [ViewModelStore].
 * The [ScopedViewModelOwner] will be the object stored in the [ScopedViewModelContainer] and
 * the [ScopedViewModelContainer] will be in charge of keeping the [ScopedViewModelOwner] and its [ViewModel] in memory for as long as needed.
 *
 * Internally, a key will be generated for this [ScopedViewModelOwner] in the Compose tree and if a [ScopedViewModelOwner] is present
 * for this key in the [ScopedViewModelContainer], then it will be used to invoke [ViewModelProvider] to return an existing [ViewModel],
 * instead of creating a new [ScopedViewModelOwner] that produces a new [ViewModel] instance when the keys don't match.
 *
 * @param key Key to track the version of the [ViewModel]. Changing [key] between compositions will produce and store a new [ViewModel].
 */
@Composable
inline fun <reified T : ViewModel> hiltViewModelScoped(key: Any? = null): T {
    val (scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: String, externalKey: ScopedViewModelContainer.ExternalKey) =
        generateKeysAndObserveLifecycle(key = key)

    val viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    // The object will be built the first time and retrieved in next calls or recompositions
    return scopedViewModelContainer.getOrBuildHiltViewModel(
        modelClass = T::class.java,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        factory = createHiltViewModelFactory(viewModelStoreOwner),
        viewModelStoreOwner = viewModelStoreOwner
    )
}

/**
 * Create a factory based on the back-stack-entry in case the Navigation library is used for this Composable
 * Otherwise, assume the container Fragment or Activity is properly annotated with @[AndroidEntryPoint]
 * and use its factory, if all else fails then use the default factory.
 */
@Composable
fun createHiltViewModelFactory(viewModelStoreOwner: ViewModelStoreOwner): ViewModelProvider.Factory? =
    if (viewModelStoreOwner is NavBackStackEntry) {
        HiltViewModelFactory(
            context = LocalContext.current,
            navBackStackEntry = viewModelStoreOwner
        )
    } else {
        // Use the default factory provided by the ViewModelStoreOwner
        // and assume it is an @AndroidEntryPoint annotated fragment or activity
        null
    }
