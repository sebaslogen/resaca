package com.sebaslogen.resaca.cmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.remember


// TODO: What to do with keyInScopeResolver?
//  - Move to common module
//  - Remove from iOS (and also from CMP
//  - Implement new version in CMP
//@Composable
//public fun <T : Any, K : Any> rememberScoped(key: K, keyInScopeResolver: KeyInScopeResolver<K>, builder: @DisallowComposableCalls () -> T): T {
//    val scopeKeyWithResolver: ScopeKeyWithResolver<K> = remember(key, keyInScopeResolver) { ScopeKeyWithResolver(key, keyInScopeResolver) }
//    return rememberScoped(key = scopeKeyWithResolver, builder = builder)
//}
@Composable
public expect fun <T : Any, K : Any> rememberScopedMP(key: K): T

@Composable
public expect fun <T : Any> rememberScoped(key: Any? = null, builder: @DisallowComposableCalls () -> T): T

