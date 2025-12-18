@file:OptIn(ResacaPackagePrivate::class)

package com.sebaslogen.resaca.koin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.sebaslogen.resaca.KeyInScopeResolver
import com.sebaslogen.resaca.ScopeKeyWithResolver
import com.sebaslogen.resaca.ScopedViewModelContainer
import com.sebaslogen.resaca.ScopedViewModelContainer.ExternalKey
import com.sebaslogen.resaca.ScopedViewModelContainer.InternalKey
import com.sebaslogen.resaca.ScopedViewModelOwner
import com.sebaslogen.resaca.generateKeysAndObserveLifecycle
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import org.koin.compose.getKoin
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope
import org.koin.viewmodel.factory.KoinViewModelFactory

/**
 * Return a [ViewModel] provided by a Koin [ViewModelProvider.Factory] and a [ViewModelProvider].
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone
 * and the [key] is not present anymore in [keyInScopeResolver] or the [keyInScopeResolver] itself leaves Composition.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
 *
 * The returned [ViewModel] is provided by the [ViewModelProvider] using a Koin [ViewModelProvider.Factory] and a [ViewModelStore].
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
 * @param qualifier Koin qualifier to help qualify a component, like named qualifiers.
 * @param scope Koin scope.
 * @param parameters for instance building injection. These can be used for assisted injection.
 */
@OptIn(KoinInternalApi::class)
@Composable
public inline fun <reified T : ViewModel, K : Any> koinViewModelScoped(
    key: K,
    noinline keyInScopeResolver: KeyInScopeResolver<K>,
    qualifier: Qualifier? = null,
    scope: Scope = getKoin().scopeRegistry.rootScope,
    noinline parameters: ParametersDefinition? = null,
): T {
    val scopeKeyWithResolver: ScopeKeyWithResolver<K> = remember(key, keyInScopeResolver) { ScopeKeyWithResolver(key, keyInScopeResolver) }
    return koinViewModelScoped(
        key = scopeKeyWithResolver,
        qualifier = qualifier,
        scope = scope,
        parameters = parameters
    )
}

/**
 * Return a [ViewModel] provided by a Koin [ViewModelProvider.Factory] and a [ViewModelProvider].
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
 *
 * The returned [ViewModel] is provided by the [ViewModelProvider] using a Koin [ViewModelProvider.Factory] and a [ViewModelStore].
 * The [ViewModel] will be created and stored by the [ViewModelProvider] in the [ViewModelStore].
 * The [ScopedViewModelOwner] will be the object stored in the [ScopedViewModelContainer] and
 * the [ScopedViewModelContainer] will be in charge of keeping the [ScopedViewModelOwner] and its [ViewModel] in memory for as long as needed.
 *
 * Internally, a key will be generated for this [ScopedViewModelOwner] in the Compose tree and if a [ScopedViewModelOwner] is present
 * for this key in the [ScopedViewModelContainer], then it will be used to invoke [ViewModelProvider] to return an existing [ViewModel],
 * instead of creating a new [ScopedViewModelOwner] that produces a new [ViewModel] instance when the keys don't match.
 *
 * @param key Key to track the version of the [ViewModel]. Changing [key] between compositions will produce and store a new [ViewModel].
 * @param qualifier Koin qualifier to help qualify a component, like named qualifiers.
 * @param scope Koin scope.
 * @param parameters for instance building injection. These can be used for assisted injection.
 */
@OptIn(KoinInternalApi::class)
@Composable
public inline fun <reified T : ViewModel> koinViewModelScoped(
    key: Any? = null,
    qualifier: Qualifier? = null,
    scope: Scope = getKoin().scopeRegistry.rootScope,
    noinline parameters: ParametersDefinition? = null,
): T {

    val (scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: InternalKey, externalKey: ExternalKey) =
        generateKeysAndObserveLifecycle(key = key)

    // The object will be built the first time and retrieved in next calls or recompositions
    return scopedViewModelContainer.getOrBuildViewModel(
        modelClass = T::class,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        factory = KoinViewModelFactory(
            kClass = T::class,
            scope = scope,
            qualifier = qualifier,
            params = parameters
        ),
    )
}
