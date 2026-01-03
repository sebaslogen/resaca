package com.sebaslogen.resaca.utils

import com.sebaslogen.resaca.ScopedViewModelContainer.ExternalKey
import com.sebaslogen.resaca.ScopedViewModelContainer.InternalKey
import com.sebaslogen.resaca.plus
import kotlin.reflect.KClass

internal expect fun <T : Any> KClass<T>.getClassName(): String?

internal fun <T : Any> KClass<T>.getCanonicalNameKey(key: String): String {
    val canonicalName = this.getClassName() ?: throw IllegalArgumentException("Local and anonymous classes can not be ViewModels")
    return "$canonicalName:$key"
}

@ResacaPackagePrivate
public fun <T : Any> KClass<T>.getCanonicalNameKey(internalKey: InternalKey, externalKey: ExternalKey): String =
    this.getCanonicalNameKey(internalKey + externalKey)
