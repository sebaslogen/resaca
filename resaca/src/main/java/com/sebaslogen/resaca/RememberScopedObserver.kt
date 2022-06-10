package com.sebaslogen.resaca.compose

import androidx.compose.runtime.RememberObserver
import com.sebaslogen.resaca.ScopedViewModelContainer

/**
 * When an object of this class is remembered in a composition (i.e. as part of a Composable function's body),
 * then it can track when it is being disposed and notify its [scopedViewModelContainer] (to possibly remove and clean up scoped objects associated with it).
 *
 * Disposition of this object can be triggered either by:
 * [onAbandoned] callback: Called when this object is returned by the callback to remember but is not successfully remembered by a composition.
 * [onForgotten] callback: when the object is not part of composition anymore.
 *
 * @param scopedViewModelContainer the container that stores the object remembered together with this [RememberScopedObserver]
 * @param containerKey the key to find the object remembered together with this [RememberScopedObserver] inside the provided [scopedViewModelContainer]
 */
class RememberScopedObserver(
    private val scopedViewModelContainer: ScopedViewModelContainer,
    private val containerKey: String
) : RememberObserver {

    private fun onDisposed() {
        scopedViewModelContainer.onDisposedFromComposition(containerKey)
    }

    override fun onAbandoned() {
        onDisposed()
    }

    override fun onForgotten() {
        onDisposed()
    }

    override fun onRemembered() {
        // no op
    }
}