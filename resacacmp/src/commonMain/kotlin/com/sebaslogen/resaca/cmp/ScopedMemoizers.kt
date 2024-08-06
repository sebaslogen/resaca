package com.sebaslogen.resaca.cmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.core.bundle.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.sebaslogen.resaca.core.KeyInScopeResolver


// TODO: How to implement keyInScopeResolver in iOS?
//  - Remove from iOS (and also from CMP?)
//  - Implement new version in CMP somehow???
//  - Move all resaca to code/KMP and just re-use the same code in iOS and CMP

/**
 * Return an object created with the provided [builder] function and store this object
 * in the [ScopedViewModelContainer] which will keep this object in memory for as long as needed,
 * and until the requester Composable is permanently gone and the [key] is not present
 * anymore in [keyInScopeResolver] or the [keyInScopeResolver] itself leaves Composition.
 * This means, it retains the object across recompositions, during configuration changes, and
 * also when the container Fragment or Compose Navigation destination goes into the backstack.
 *
 * Internally, an extra key will be generated for this object in the Compose tree and if an object is present
 * for this key in the [ScopedViewModelContainer], then it will be returned instead of calling the [builder].
 *
 * @param key Key to track the version of the stored object. Changing [key] between compositions will produce and remember a new value by calling [builder].
 * @param keyInScopeResolver A function that uses [key] to determine if the object should be kept in memory even after it's no longer part of the composition.
 * @param builder Factory function to produce a new value that will be remembered.
 */
@Composable
public expect fun <T : Any, K : Any> rememberScoped(key: K, keyInScopeResolver: KeyInScopeResolver<K>, builder: @DisallowComposableCalls () -> T): T

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
 * @param key Key to track the version of the stored object. Changing [key] between compositions will produce and remember a new value by calling [builder].
 * @param builder Factory function to produce a new value that will be remembered.
 */
@Composable
public expect fun <T : Any> rememberScoped(key: Any? = null, builder: @DisallowComposableCalls () -> T): T

///**
// * Return a [ViewModel] provided by the default [ViewModelProvider.Factory] and a [ViewModelProvider].
// * The [ViewModel] will be kept in memory for as long as needed, and until the requester Composable is permanently gone
// * and the [key] is not present anymore in [keyInScopeResolver] or the [keyInScopeResolver] itself leaves Composition.
// * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
// * also when the container Fragment or Compose Navigation destination goes into the backstack.
// *
// * The [ViewModel] will be created and stored by the [ViewModelProvider] using a default [ViewModelProvider.Factory] and a [ViewModelStore].
// * The [ScopedViewModelOwner] will be the object stored in the [ScopedViewModelContainer] and
// * the [ScopedViewModelContainer] will be in charge of keeping the [ScopedViewModelOwner] and its [ViewModel] in memory for as long as needed.
// *
// * Internally, an extra key will be generated for this [ScopedViewModelOwner] in the Compose tree and if a [ScopedViewModelOwner] is present
// * for this key in the [ScopedViewModelContainer], then it will be used to invoke [ViewModelProvider] to return an existing [ViewModel],
// * instead of creating a new [ScopedViewModelOwner] that produces a new [ViewModel] instance when the keys don't match.
// *
// * @param key Key to track the version of the [ViewModel]. Changing [key] between compositions will produce and remember a new [ViewModel].
// * @param keyInScopeResolver A function that uses [key] to determine if the ViewModel should be kept in memory even after it's no longer part of the composition.
// * @param defaultArguments A [Bundle] containing all the default arguments that will be provided to the [ViewModel].
// */
//@Composable
//public expect inline fun <reified T : ViewModel, K : Any> viewModelScoped(
//    key: K,
//    noinline keyInScopeResolver: KeyInScopeResolver<K>,
//    defaultArguments: Bundle = Bundle()
//): T

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
public expect inline fun <reified T : ViewModel> viewModelScoped(key: Any? = null, defaultArguments: Bundle = Bundle()): T

///**
// * Return a [ViewModel] provided by the [builder] and a [ViewModelProvider].
// * The [ViewModel] will keep in memory for as long as needed, and until the requester Composable is permanently gone
// * and the [key] is not present anymore in [keyInScopeResolver] or the [keyInScopeResolver] itself leaves Composition.
// * This means, it retains the [ViewModel] across recompositions, during configuration changes, and
// * also when the container Fragment or Compose Navigation destination goes into the backstack.
// *
// * The [ViewModel] will be created and stored by the [ViewModelProvider] using the [builder] and a [ViewModelStore].
// * The [ScopedViewModelOwner] will be the object stored in the [ScopedViewModelContainer] and
// * the [ScopedViewModelContainer] will be in charge of keeping the [ScopedViewModelOwner] and its [ViewModel] in memory for as long as needed.
// *
// * Internally, an extra key will be generated for this [ScopedViewModelOwner] in the Compose tree and if a [ScopedViewModelOwner] is present
// * for this key in the [ScopedViewModelContainer], then it will be used to invoke [ViewModelProvider] to return an existing [ViewModel],
// * instead of creating a new [ScopedViewModelOwner] that produces a new [ViewModel] instance when the keys don't match.
// *
// * @param key Key to track the version of the [ViewModel]. Changing [key] between compositions will produce and remember a new [ViewModel].
// * @param keyInScopeResolver A function that uses [key] to determine if the ViewModel should be kept in memory even after it's no longer part of the composition.
// * @param defaultArguments A [Bundle] containing all the default arguments that will be provided to the [ViewModel].
// * @param builder Factory function to produce a new [ViewModel] that will be remembered.
// */
//@Composable
//public expect inline fun <reified T : ViewModel, K : Any> viewModelScoped(
//    key: K,
//    noinline keyInScopeResolver: KeyInScopeResolver<K>,
//    defaultArguments: Bundle = Bundle(),
//    noinline builder: @DisallowComposableCalls () -> T
//): T

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
public expect inline fun <reified T : ViewModel> viewModelScoped(
    key: Any? = null,
    defaultArguments: Bundle = Bundle(),
    noinline builder: @DisallowComposableCalls () -> T
): T




//@Composable
//public expect fun ObserveComposableContainerLifecycle(scopedViewModelContainer: ScopedViewModelContainer)