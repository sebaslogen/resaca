package com.sebaslogen.resaca.cmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import com.sebaslogen.resaca.rememberScoped as rememberScopedResaca

@Composable
public actual fun <T : Any, K : Any> rememberScopedMP(key: K): T {
    TODO("Not yet implemented")
}

@Composable
public actual fun <T : Any> rememberScoped(key: Any?, builder: @DisallowComposableCalls () -> T): T =
    rememberScopedResaca(key, builder)