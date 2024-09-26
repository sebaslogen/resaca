package com.sebaslogen.resaca

import androidx.compose.runtime.RememberObserver
import com.sebaslogen.resaca.ScopedViewModelContainer.InternalKey

/**
 * When an object of this class is remembered in a composition (i.e. as part of a Composable function's body),
 * then it can track when it is being disposed and notify its [scopedViewModelContainer] (to possibly remove and clean up scoped objects associated with it).
 *
 * Disposition of this object can be triggered either by:
 * [onAbandoned] callback: Called when this object is returned by the callback to remember but is not successfully remembered by a composition.
 * [onForgotten] callback: when the object is not part of composition anymore.
 *
 * @param scopedViewModelContainer the container that stores the object remembered together with this [RememberScopedObserver]
 * @param positionalMemoizationKey the key to find the object remembered together with this [RememberScopedObserver] inside the provided [scopedViewModelContainer]
 */
public class RememberScopedObserver(
    private val scopedViewModelContainer: ScopedViewModelContainer,
    private val positionalMemoizationKey: InternalKey
) : RememberObserver {

    private fun onRemoved() {
        scopedViewModelContainer.onRemovedFromComposition(positionalMemoizationKey)
    }

    override fun onAbandoned() {
        onRemoved()
    }

    override fun onForgotten() {
        onRemoved()
    }

    override fun onRemembered() {
        // no op
    }
}