package com.sebaslogen.resaca.hilt

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.*
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
 * The [ViewModelProvider] and the [ViewModelStore] will be created and managed by the [ScopedViewModelOwner].
 * The [ScopedViewModelOwner] will be the object stored in the [ScopedViewModelContainer] and
 * the [ScopedViewModelContainer] will be in charge of keeping the [ScopedViewModelOwner] and its [ViewModel] in memory for as long as needed.
 *
 * Internally, a key will be generated for this [ScopedViewModelOwner] in the Compose tree and if a [ScopedViewModelOwner] is present
 * for this key in the [ScopedViewModelContainer], then it will be used to invoke [ViewModelProvider] to return an existing [ViewModel],
 * instead of creating a new [ScopedViewModelOwner] that produces a new [ViewModel] instance when the keys don't match.
 *
 * Note: There is no support for keys in this method because Hilt instances are singletons in the container scope (Activity/Fragment/Nav. destination),
 *      so the same object will always be returned once created and until disposal of the Composables using it.
 *      Support for keys in the Hilt library is still WIP. See https://github.com/google/dagger/issues/2328
 */
@Composable
inline fun <reified T : ViewModel> hiltViewModelScoped(): T {
    val (scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: String, externalKey: ScopedViewModelContainer.ExternalKey) =
        generateKeysAndObserveLifecycle(key = null)

    // The object will be built the first time and retrieved in next calls or recompositions
    return scopedViewModelContainer.getOrBuildViewModel(
        modelClass = T::class.java,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        factory = createHiltViewModelFactory()
    )
}

/**
 * Create a factory based on the back-stack-entry in case the Navigation library is used for this Composable
 * Otherwise, assume the container Fragment or Activity is properly annotated with @[AndroidEntryPoint]
 * and use its factory, if all else fails then use the default factory.
 */
@Composable
@PublishedApi
internal fun createHiltViewModelFactory(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
): ViewModelProvider.Factory =
    if (viewModelStoreOwner is NavBackStackEntry) {
        HiltViewModelFactory(
            context = LocalContext.current,
            navBackStackEntry = viewModelStoreOwner
        )
    } else {
        // Use the default factory provided by the ViewModelStoreOwner
        // and assume it is an @AndroidEntryPoint annotated fragment or activity
        if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
            viewModelStoreOwner.defaultViewModelProviderFactory
        } else {
            ViewModelProvider.NewInstanceFactory.instance
        }
    }
