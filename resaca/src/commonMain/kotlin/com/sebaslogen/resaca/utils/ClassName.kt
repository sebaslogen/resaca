package com.sebaslogen.resaca.utils

import kotlin.reflect.KClass

internal expect fun <T : Any> KClass<T>.getClassName(): String?

internal fun <T : Any> KClass<T>.getCanonicalNameKey(key: String): String {
    val canonicalName = this.getClassName() ?: throw IllegalArgumentException("Local and anonymous classes can not be ViewModels")
    return "$canonicalName:$key"
}