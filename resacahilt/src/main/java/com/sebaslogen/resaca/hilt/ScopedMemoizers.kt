@file:Suppress("NOTHING_TO_INLINE")
@file:OptIn(ResacaPackagePrivate::class)

package com.sebaslogen.resaca.hilt

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import com.sebaslogen.resaca.KeyInScopeResolver
import com.sebaslogen.resaca.ScopeKeyWithResolver
import com.sebaslogen.resaca.ScopedViewModelContainer
import com.sebaslogen.resaca.ScopedViewModelContainer.ExternalKey
import com.sebaslogen.resaca.ScopedViewModelContainer.InternalKey
import com.sebaslogen.resaca.ScopedViewModelOwner
import com.sebaslogen.resaca.generateKeysAndObserveLifecycle
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.lifecycle.withCreationCallback


/**
 * Return a [ViewModel] (annotated with [HiltViewModel]) provided by a Hilt [ViewModelProvider.Factory] and a [ViewModelProvider].
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone
 * and the [key] is not present anymore in [keyInScopeResolver] or the [keyInScopeResolver] itself leaves Composition.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
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
 * @param keyInScopeResolver A function that uses [key] to determine if the ViewModel should be kept in memory even after it's no longer part of the composition.
 * @param defaultArguments A [Bundle] containing all the default arguments that will be provided to the [ViewModel].
 */
@Deprecated("Use hiltViewModelScoped without \"defaultArguments: Bundle\" instead")
@Composable
public inline fun <reified T : ViewModel, K : Any> hiltViewModelScoped(
    key: K,
    noinline keyInScopeResolver: KeyInScopeResolver<K>,
    defaultArguments: Bundle
): T {
    val scopeKeyWithResolver: ScopeKeyWithResolver<K> = remember(key, keyInScopeResolver) { ScopeKeyWithResolver(key, keyInScopeResolver) }
    return hiltViewModelScoped(key = scopeKeyWithResolver, defaultArguments = defaultArguments)
}
/**
 * Return a [ViewModel] (annotated with [HiltViewModel]) provided by a Hilt [ViewModelProvider.Factory] and a [ViewModelProvider].
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone
 * and the [key] is not present anymore in [keyInScopeResolver] or the [keyInScopeResolver] itself leaves Composition.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
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
 * @param keyInScopeResolver A function that uses [key] to determine if the ViewModel should be kept in memory even after it's no longer part of the composition.
 */
@Composable
public inline fun <reified T : ViewModel, K : Any> hiltViewModelScoped(
    key: K,
    noinline keyInScopeResolver: KeyInScopeResolver<K>,
): T {
    val scopeKeyWithResolver: ScopeKeyWithResolver<K> = remember(key, keyInScopeResolver) { ScopeKeyWithResolver(key, keyInScopeResolver) }
    return hiltViewModelScoped(key = scopeKeyWithResolver, defaultArguments = Bundle.EMPTY)
}

/**
 * Return a [ViewModel] (annotated with [HiltViewModel]) provided by a Hilt [ViewModelProvider.Factory] and a [ViewModelProvider].
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
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
 * @param defaultArguments A [Bundle] containing all the default arguments that will be provided to the [ViewModel].
 */
@Deprecated("Use hiltViewModelScoped without \"defaultArguments: Bundle\" instead")
@Composable
public inline fun <reified T : ViewModel> hiltViewModelScoped(key: Any? = null, defaultArguments: Bundle): T {
    val (scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: InternalKey, externalKey: ExternalKey) =
        generateKeysAndObserveLifecycle(key = key)

    val viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    // The object will be built the first time and retrieved in next calls or recompositions
    return scopedViewModelContainer.getOrBuildViewModel(
        modelClass = T::class,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        factory = createHiltViewModelFactory(viewModelStoreOwner),
        viewModelStoreOwner = viewModelStoreOwner,
        defaultArguments = defaultArguments
    )
}

/**
 * Return a [ViewModel] (annotated with [HiltViewModel]) provided by a Hilt [ViewModelProvider.Factory] and a [ViewModelProvider].
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
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
public inline fun <reified T : ViewModel> hiltViewModelScoped(key: Any? = null): T {
    val (scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: InternalKey, externalKey: ExternalKey) =
        generateKeysAndObserveLifecycle(key = key)

    val viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    // The object will be built the first time and retrieved in next calls or recompositions
    return scopedViewModelContainer.getOrBuildViewModel(
        modelClass = T::class,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        factory = createHiltViewModelFactory(viewModelStoreOwner),
        viewModelStoreOwner = viewModelStoreOwner,
        defaultArguments = Bundle.EMPTY
    )
}

/**
 * Return a [ViewModel] (annotated with [HiltViewModel]) provided by a custom factory (see [AssistedFactory]),
 * a Hilt [ViewModelProvider.Factory] and a [ViewModelProvider].
 * The [AssistedFactory] will be used to create the [ViewModel] with one or more parameter provided outside of Hilt/dependency injection.
 * For more documentation about Hilt assisted injection see https://dagger.dev/hilt/view-model#assisted-injection
 *
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
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
 * Usage example:
 * val myViewModel: MyViewModel =
 *             hiltViewModelScoped(key = key) { factory: MyViewModelFactory ->
 *                 factory.create(
 *                     myViewModelId = someIdAvailableInMyComposable
 *                 )
 *             }
 *
 * @param key Key to track the version of the [ViewModel]. Changing [key] between compositions will produce and store a new [ViewModel].
 * @param creationCallback A callback to pass [ViewModel] creation [Assisted] parameters to Hilt using your [AssistedFactory].
 */
@Composable
public inline fun <reified VM : ViewModel, reified VMF> hiltViewModelScoped(key: Any? = null, noinline creationCallback: (VMF) -> VM): VM {
    val (scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: InternalKey, externalKey: ExternalKey) =
        generateKeysAndObserveLifecycle(key = key)

    val viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    val defaultCreationExtras =
        if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) viewModelStoreOwner.defaultViewModelCreationExtras else CreationExtras.Empty
    val creationExtras = defaultCreationExtras.withCreationCallback(creationCallback)

    // The object will be built the first time and retrieved in next calls or recompositions
    return scopedViewModelContainer.getOrBuildViewModel(
        modelClass = VM::class,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        factory = createHiltViewModelFactory(viewModelStoreOwner),
        viewModelStoreOwner = viewModelStoreOwner,
        creationExtras = creationExtras
    )
}

/**
 * Create a factory based on the back-stack-entry in case the Navigation library is used for this Composable
 * Otherwise, assume the container Fragment or Activity is properly annotated with @[AndroidEntryPoint]
 * and use its factory, if all else fails then use the default factory.
 */
@Composable
public inline fun createHiltViewModelFactory(viewModelStoreOwner: ViewModelStoreOwner): ViewModelProvider.Factory? =
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
