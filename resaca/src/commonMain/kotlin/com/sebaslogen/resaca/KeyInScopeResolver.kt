package com.sebaslogen.resaca

/**
 * A function that given a key returns true if the key is in scope, false otherwise.
 * The default implementation is to check if the key is contained in the list of keys in scope.
 * See [rememberKeysInScope] for implementation details.
 */
public typealias KeyInScopeResolver<K> = (key: K) -> Boolean

/**
 * This class is used to store a [key] and its associated [KeyInScopeResolver] to,
 * at any point in time, be able to determine if the [key] is still in scope with the help of the [keyInScopeResolver].
 */
public data class ScopeKeyWithResolver<K : Any>(val key: K, val keyInScopeResolver: KeyInScopeResolver<K>) {
    public fun isKeyInScope(): Boolean = keyInScopeResolver(key)
}