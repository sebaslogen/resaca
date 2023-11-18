package com.sebaslogen.resaca.koin

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.sebaslogen.resaca.ScopedViewModelContainer
import com.sebaslogen.resaca.ScopedViewModelContainer.ExternalKey
import com.sebaslogen.resaca.ScopedViewModelContainer.InternalKey
import com.sebaslogen.resaca.ScopedViewModelOwner
import com.sebaslogen.resaca.generateKeysAndObserveLifecycle
import org.koin.androidx.viewmodel.factory.KoinViewModelFactory
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope


/**
 * Return a [ViewModel] provided by a Koin [ViewModelProvider.Factory] and a [ViewModelProvider].
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
@Composable
public inline fun <reified T : ViewModel> koinViewModelScoped(
    key: Any? = null,
    qualifier: Qualifier? = null,
    scope: Scope = GlobalContext.get().scopeRegistry.rootScope,
    noinline parameters: ParametersDefinition? = null,
    defaultArguments: Bundle = Bundle.EMPTY
): T {

    val (scopedViewModelContainer: ScopedViewModelContainer, positionalMemoizationKey: InternalKey, externalKey: ExternalKey) =
        generateKeysAndObserveLifecycle(key = key)

    // The object will be built the first time and retrieved in next calls or recompositions
    return scopedViewModelContainer.getOrBuildViewModel(
        modelClass = T::class.java,
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
