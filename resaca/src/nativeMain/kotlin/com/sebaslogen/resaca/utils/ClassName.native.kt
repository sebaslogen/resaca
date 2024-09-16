package com.sebaslogen.resaca.utils

import kotlin.reflect.KClass

internal actual fun <T: Any> KClass<T>.getClassName(): String? = this.qualifiedName