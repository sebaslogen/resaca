@file:OptIn(ResacaPackagePrivate::class)

package com.sebaslogen.resaca.koin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
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
import org.koin.core.parameter.ParametersHolder
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope
import kotlin.reflect.KClass


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
 * @param defaultArguments A [Bundle] containing all the default arguments that will be provided to the [ViewModel]. VM still has to manually read these values.
 */
@OptIn(KoinInternalApi::class)
@Deprecated("Use koinViewModelScoped without \"defaultArguments: Bundle\" instead")
@Composable
public inline fun <reified T : ViewModel, K : Any> koinViewModelScoped(
    key: K,
    noinline keyInScopeResolver: KeyInScopeResolver<K>,
    qualifier: Qualifier? = null,
    scope: Scope = getKoin().scopeRegistry.rootScope,
    noinline parameters: ParametersDefinition? = null,
    defaultArguments: Bundle
): T {
    val scopeKeyWithResolver: ScopeKeyWithResolver<K> = remember(key, keyInScopeResolver) { ScopeKeyWithResolver(key, keyInScopeResolver) }
    return koinViewModelScoped(
        key = scopeKeyWithResolver,
        qualifier = qualifier,
        scope = scope,
        parameters = parameters,
        defaultArguments = defaultArguments
    )
}
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
        parameters = parameters,
        defaultArguments = Bundle()
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
 * @param defaultArguments A [Bundle] containing all the default arguments that will be provided to the [ViewModel]. VM still has to manually read these values.
 */
@OptIn(KoinInternalApi::class)
@Deprecated("Use koinViewModelScoped without \"defaultArguments: Bundle\" instead")
@Composable
public inline fun <reified T : ViewModel> koinViewModelScoped(
    key: Any? = null,
    qualifier: Qualifier? = null,
    scope: Scope = getKoin().scopeRegistry.rootScope,
    noinline parameters: ParametersDefinition? = null,
    defaultArguments: Bundle
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
        defaultArguments = defaultArguments
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
        defaultArguments = Bundle()
    )
}

/**
 * Note: The two classes below are not yet KMP compatible in Koin 4.0.0-RC1,
 * once Koin uses the KMP version of the AndroidX libraries, these classes will be removed.
 * TODO: Remove these classes once Koin uses AndroidX KMP compatible libs
 */

/**
 * ViewModelProvider.Factory for Koin instances resolution
 * @see ViewModelProvider.Factory
 */
@PublishedApi
internal class KoinViewModelFactory(
    private val kClass: KClass<out ViewModel>,
    private val scope: Scope,
    private val qualifier: Qualifier? = null,
    private val params: ParametersDefinition? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val koinParams = KoinParametersHolder(params, extras)
        return scope.get(kClass, qualifier) { koinParams }
    }
}

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal class KoinParametersHolder(
    initialValues: ParametersDefinition? = null,
    private val extras: CreationExtras,
) : ParametersHolder(initialValues?.invoke()?.values?.toMutableList() ?: mutableListOf()) {

    override fun <T> elementAt(i: Int, clazz: KClass<*>): T {
        return createSavedStateHandleOrElse(clazz) { super.elementAt(i, clazz) }
    }

    override fun <T> getOrNull(clazz: KClass<*>): T? {
        return createSavedStateHandleOrElse(clazz) { super.getOrNull(clazz) }
    }

    private fun <T> createSavedStateHandleOrElse(clazz: KClass<*>, block: () -> T): T {
        return if (clazz == SavedStateHandle::class) {
            extras.createSavedStateHandle() as T
        } else block()
    }
}
