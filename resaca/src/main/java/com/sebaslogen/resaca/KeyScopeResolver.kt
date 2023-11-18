package com.sebaslogen.resaca

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel


public typealias KeyInScopeResolver<K> = (key: K) -> Boolean

public data class ScopeKeyWithResolver<K : Any>(val key: K, val keyInScopeResolver: KeyInScopeResolver<K>) {
    internal fun isKeyInScope(): Boolean = keyInScopeResolver(key)
}

@Composable
public fun <T : Any> keyInScopeResolverFor(inputListOfKeys: Collection<T>): KeyInScopeResolver<T> {

    val keysList: MutableList<T> = remember { inputListOfKeys.toMutableList() } // Create our own keys container

    val keyScopedResolver: KeyInScopeResolver<T> = remember { { key: T -> keysList.contains(key) } } // Resolves if a key is contained in the list

    val scopedViewModelContainer: ScopedViewModelContainer = viewModel() // Get the container of scoped objects

    if (inputListOfKeys.toList() != keysList.toList()) { // Update our keys container if the input list changes
        keysList.clear()
        keysList.addAll(inputListOfKeys)
    }

    DisposableEffect(keyScopedResolver, scopedViewModelContainer) {
        onDispose {
            keysList.clear()
            scopedViewModelContainer.onDisposedFromComposition(keyScopedResolver)
        }
    }

    return keyScopedResolver
}