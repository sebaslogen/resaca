package com.sebaslogen.resaca.utils

import kotlin.reflect.KClass

internal expect fun <T: Any> KClass<T>.getClassName(): String?