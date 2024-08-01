package com.sebaslogen.resaca

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sebaslogen.resaca.core.KeyInScopeResolver

/**
 * Remember a collection of keys in the current composition scope to be used in the returned [KeyInScopeResolver].
 * When a new list of keys is provided, the new list is used inside the [KeyInScopeResolver] and
 * the same [KeyInScopeResolver] is returned.
 *
 * The objective of [KeyInScopeResolver] is to be used as a parameter in [rememberScoped]
 * together with a key contained in the [inputListOfKeys] provided to this function.
 * This allows the user of this library to extend the scope of the objects associated with the [inputListOfKeys].
 *
 * Placing this [rememberKeysInScope] call in a scope larger than where [rememberScoped] is called (e.g. outside of a LazyColumn),
 * allows objects scoped with [rememberScoped] (e.g. inside an item in the column) to be kept in memory for longer than
 * the composition where they were created (e.g. when items in the LazyColumn are scrolled outside of the View port).
 * In this case, objects will be disposed of when both:
 * - Composable with [rememberScoped] is disposed of and
 * - [rememberKeysInScope] is disposed of, or the key used in [rememberScoped] is no longer part of the [inputListOfKeys].
 *
 * @param inputListOfKeys The list of keys to be remembered in the current composition scope and used to extend the scope of [rememberScoped] calls.
 * @return A [KeyInScopeResolver] that can be used as a parameter in [rememberScoped] together with a key to extend the scope of the remembered object.
 */
@Composable
public fun <T : Any> rememberKeysInScope(inputListOfKeys: Collection<T>): KeyInScopeResolver<T> {

    val keysList: MutableList<T> = rememberScoped { inputListOfKeys.toMutableList() } // Create our own keys container

    val keyScopedResolver: KeyInScopeResolver<T> = rememberScoped { { key: T -> keysList.contains(key) } } // Resolves if a key is contained in the list

    val scopedViewModelContainer: ScopedViewModelContainer = viewModel() // Get the container of scoped objects

    if (inputListOfKeys.toList() != keysList.toList()) { // Update our keys container if the input list changes
        keysList.clear()
        keysList.addAll(inputListOfKeys)
    }

    DisposableEffect(keyScopedResolver, scopedViewModelContainer) {
        onDispose {
            keysList.clear() // The keys in this list are no longer in scope and should be cleared here and in the container
            scopedViewModelContainer.onDisposedFromComposition(keyScopedResolver)
        }
    }

    return keyScopedResolver
}