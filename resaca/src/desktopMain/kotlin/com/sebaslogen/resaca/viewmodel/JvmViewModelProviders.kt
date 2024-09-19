package com.sebaslogen.resaca.viewmodel

import androidx.lifecycle.ViewModel


/**
 * [JvmViewModelProviders] provides common helper functionalities.
 *
 * Kotlin Multiplatform does not support expect class with default implementation yet, so we
 * extracted the common logic used by all platforms to this internal class.
 *
 * @see <a href="https://youtrack.jetbrains.com/issue/KT-20427">KT-20427</a>
 */
internal object JvmViewModelProviders {

    /**
     * Creates a new [ViewModel] instance using the no-args constructor if available, otherwise
     * throws a [RuntimeException].
     */
    @Suppress("DocumentExceptions")
    fun <T : ViewModel> createViewModel(modelClass: Class<T>): T =
        try {
            modelClass.getDeclaredConstructor().newInstance()
        } catch (e: NoSuchMethodException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: InstantiationException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        }
}
