@file:OptIn(ResacaPackagePrivate::class)

package com.sebaslogen.resaca.metro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.sebaslogen.resaca.KeyInScopeResolver
import com.sebaslogen.resaca.ScopeKeyWithResolver
import com.sebaslogen.resaca.ScopedViewModelContainer
import com.sebaslogen.resaca.ScopedViewModelContainer.ExternalKey
import com.sebaslogen.resaca.ScopedViewModelContainer.InternalKey
import com.sebaslogen.resaca.ScopedViewModelOwner
import com.sebaslogen.resaca.addViewModelKey
import com.sebaslogen.resaca.generateKeysAndObserveLifecycle
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import com.sebaslogen.resaca.utils.getCanonicalNameKey
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlin.time.Duration

/**
 * Return a [ViewModel] provided by a Metro [ViewModelProvider.Factory] and a [ViewModelProvider].
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone
 * and the [key] is not present anymore in [keyInScopeResolver] or the [keyInScopeResolver] itself leaves Composition.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
 *
 * The returned [ViewModel] is provided by the [ViewModelProvider] using a Metro [ViewModelProvider.Factory] and a [ViewModelStore].
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
 * @param clearDelay The delay after which the [ViewModel] will be cleared from memory.
 * @param factory Metro [ViewModelProvider.Factory] to create the [ViewModel] (e.g. obtained from your Metro @DependencyGraph).
 *              Defaults to [LocalMetroViewModelFactory], in this case the factory should be provided with a CompositionLocalProvider, see https://zacsweers.github.io/metro/latest/metrox-viewmodel-compose/#2-provide-localmetroviewmodelfactory
 */
@Composable
public inline fun <reified T : ViewModel, K : Any> metroViewModelScoped(
    key: K,
    noinline keyInScopeResolver: KeyInScopeResolver<K>,
    clearDelay: Duration? = null,
    factory: ViewModelProvider.Factory = LocalMetroViewModelFactory.current,
): T {
    val scopeKeyWithResolver: ScopeKeyWithResolver<K> = remember(key, keyInScopeResolver) { ScopeKeyWithResolver(key, keyInScopeResolver) }
    return metroViewModelScoped(
        key = scopeKeyWithResolver,
        clearDelay = clearDelay,
        factory = factory,
    )
}

/**
 * Return a [ViewModel] provided by a Metro [ViewModelProvider.Factory] and a [ViewModelProvider].
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
 *
 * The returned [ViewModel] is provided by the [ViewModelProvider] using a Metro [ViewModelProvider.Factory] and a [ViewModelStore].
 * The [ViewModel] will be created and stored by the [ViewModelProvider] in the [ViewModelStore].
 * The [ScopedViewModelOwner] will be the object stored in the [ScopedViewModelContainer] and
 * the [ScopedViewModelContainer] will be in charge of keeping the [ScopedViewModelOwner] and its [ViewModel] in memory for as long as needed.
 *
 * Internally, a key will be generated for this [ScopedViewModelOwner] in the Compose tree and if a [ScopedViewModelOwner] is present
 * for this key in the [ScopedViewModelContainer], then it will be used to invoke [ViewModelProvider] to return an existing [ViewModel],
 * instead of creating a new [ScopedViewModelOwner] that produces a new [ViewModel] instance when the keys don't match.
 *
 * @param key Key to track the version of the [ViewModel]. Changing [key] between compositions will produce and store a new [ViewModel].
 * @param clearDelay The delay after which the [ViewModel] will be cleared from memory.
 * @param factory Metro [ViewModelProvider.Factory] to create the [ViewModel] (e.g. obtained from your Metro @DependencyGraph).
 *              Defaults to [LocalMetroViewModelFactory], in this case the factory should be provided with a CompositionLocalProvider, see https://zacsweers.github.io/metro/latest/metrox-viewmodel-compose/#2-provide-localmetroviewmodelfactory
 */
@Composable
public inline fun <reified T : ViewModel> metroViewModelScoped(
    key: Any? = null,
    clearDelay: Duration? = null,
    factory: ViewModelProvider.Factory = LocalMetroViewModelFactory.current,
): T {

    val (scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: InternalKey, externalKey: ExternalKey) =
        generateKeysAndObserveLifecycle(key = key)

    // The object will be built the first time and retrieved in next calls or recompositions
    return scopedViewModelContainer.getOrBuildViewModel(
        modelClass = T::class,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        clearDelay = clearDelay,
        factory = factory,
    )
}

/**
 * Return a [ViewModel] provided by a Metro [ViewModelProvider.Factory] and a [ViewModelProvider] with custom [CreationExtras].
 * This overload supports Metro assisted injection by allowing custom [CreationExtras] to be passed to the [ViewModelProvider.Factory].
 *
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone
 * and the [key] is not present anymore in [keyInScopeResolver] or the [keyInScopeResolver] itself leaves Composition.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
 *
 * The returned [ViewModel] is provided by the [ViewModelProvider] using a Metro [ViewModelProvider.Factory] and a [ViewModelStore].
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
 * @param clearDelay The delay after which the [ViewModel] will be cleared from memory.
 * @param factory Metro [ViewModelProvider.Factory] to create the [ViewModel] (e.g. obtained from your Metro @DependencyGraph).
 *              Defaults to [LocalMetroViewModelFactory], in this case the factory should be provided with a CompositionLocalProvider, see https://zacsweers.github.io/metro/latest/metrox-viewmodel-compose/#2-provide-localmetroviewmodelfactory
 * @param creationExtras Custom [CreationExtras] to pass additional data to the factory for assisted injection.
 */
@Composable
public inline fun <reified T : ViewModel, K : Any> metroViewModelScoped(
    key: K,
    noinline keyInScopeResolver: KeyInScopeResolver<K>,
    clearDelay: Duration? = null,
    factory: ViewModelProvider.Factory = LocalMetroViewModelFactory.current,
    creationExtras: CreationExtras,
): T {
    val scopeKeyWithResolver: ScopeKeyWithResolver<K> = remember(key, keyInScopeResolver) { ScopeKeyWithResolver(key, keyInScopeResolver) }
    return metroViewModelScoped(
        key = scopeKeyWithResolver,
        clearDelay = clearDelay,
        factory = factory,
        creationExtras = creationExtras,
    )
}

/**
 * Return a [ViewModel] provided by a Metro [ViewModelProvider.Factory] and a [ViewModelProvider] with custom [CreationExtras].
 * This overload supports Metro assisted injection by allowing custom [CreationExtras] to be passed to the [ViewModelProvider.Factory].
 *
 * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone.
 * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
 *
 * The returned [ViewModel] is provided by the [ViewModelProvider] using a Metro [ViewModelProvider.Factory] and a [ViewModelStore].
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
 *             metroViewModelScoped(
 *                 factory = metroGraph.viewModelFactory,
 *                 creationExtras = myCustomExtras
 *             )
 *
 * @param key Key to track the version of the [ViewModel]. Changing [key] between compositions will produce and store a new [ViewModel].
 * @param clearDelay The delay after which the [ViewModel] will be cleared from memory.
 * @param factory Metro [ViewModelProvider.Factory] to create the [ViewModel] (e.g. obtained from your Metro @DependencyGraph).
 *              Defaults to [LocalMetroViewModelFactory], in this case the factory should be provided with a CompositionLocalProvider, see https://zacsweers.github.io/metro/latest/metrox-viewmodel-compose/#2-provide-localmetroviewmodelfactory
 * @param creationExtras Custom [CreationExtras] to pass additional data to the factory for assisted injection.
 */
@Composable
public inline fun <reified T : ViewModel> metroViewModelScoped(
    key: Any? = null,
    clearDelay: Duration? = null,
    factory: ViewModelProvider.Factory = LocalMetroViewModelFactory.current,
    creationExtras: CreationExtras,
): T {
    val (scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: InternalKey, externalKey: ExternalKey) =
        generateKeysAndObserveLifecycle(key = key)

    val viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }

    val viewModelKey = T::class.getCanonicalNameKey(positionalMemoizationKey, externalKey)
    val creationExtrasWithViewModelKey = creationExtras.addViewModelKey(viewModelKey)

    // The object will be built the first time and retrieved in next calls or recompositions
    return scopedViewModelContainer.getOrBuildViewModel(
        modelClass = T::class,
        positionalMemoizationKey = positionalMemoizationKey,
        externalKey = externalKey,
        clearDelay = clearDelay,
        factory = factory,
        viewModelStoreOwner = viewModelStoreOwner,
        creationExtras = creationExtrasWithViewModelKey
    )
}
