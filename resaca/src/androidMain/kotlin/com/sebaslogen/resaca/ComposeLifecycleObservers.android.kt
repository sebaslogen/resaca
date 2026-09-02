@file:OptIn(ResacaPackagePrivate::class)

package com.sebaslogen.resaca

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import com.sebaslogen.resaca.utils.ResacaPackagePrivate

/**
 * Amount returned when the number of [ViewModelStore]s held by the NavController could not be read.
 * It never matches a real amount of stores, so a scoped object is kept instead of disposed when in doubt.
 */
private const val UNKNOWN_AMOUNT_OF_VIEW_MODEL_STORES = -1

/**
 * Observe the lifecycle of a Composable container to detect when it is being disposed
 * and provide a callback to check if the current NavDestination (when using Compose Navigation with a NavHost)
 * is at the top of the back stack, i.e. it's resumed and back in the foreground.
 *
 * This callback is useful in case the container Activity is recreated due to configuration change
 * and after the first frame post recreation we need want to check if the destination is the same as the one that was resumed.
 * This signals the destination holding our scoped objects is back in the foreground and objects that were not requested again
 * should be disposed of and garbage collected.
 *
 * @param scopedViewModelContainer the container that stores the object remembered together with this [RememberScopedObserver]
 */
@Composable
@PublishedApi
internal actual fun ObserveComposableContainerLifecycle(scopedViewModelContainer: ScopedViewModelContainer) {
    val navBackStackEntry = LocalLifecycleOwner.current as? NavBackStackEntry
    val countViewModelStoresInNavHost = remember(navBackStackEntry) { navBackStackEntry?.let(::viewModelStoresCounter) }
    if (countViewModelStoresInNavHost == null) {
        // Use a different observer when not using Compose Navigation with a NavHost, default to just Activity recreation
        ObserveComposableContainerLifecycleWithoutComposeNavigation(scopedViewModelContainer)
        return
    }

    // Observe state of configuration changes when disposing
    val activity = LocalActivity.current
        ?: throw IllegalStateException("Expected an Activity for detecting configuration changes for a NavBackStackEntry but instead found null")
    remember(activity) {
        val totalViewModelStoresWhenDestinationIsCreatedInNavHost = countViewModelStoresInNavHost()
        object : RememberObserver {
            /**
             * When the destination is removed from the composition, we can check if the destination is still in the foreground.
             *
             * In this callback, after the Activity is recreated, we can check if
             * the number of ViewModelStores is the same as when the destination was created.
             * When the number matches we are still on top of the back stack and the destination is back in the foreground.
             * When the number differs, it means the destination is not the top of the back stack and
             * we should NOT dispose any scoped objects yet. Only after resuming.
             */
            private fun onRemoved() {
                if (activity.isChangingConfigurations) {
                    scopedViewModelContainer.setShouldBeReturningToForeground {
                        totalViewModelStoresWhenDestinationIsCreatedInNavHost == countViewModelStoresInNavHost()
                    }
                }
            }

            override fun onAbandoned() {
                onRemoved()
            }

            override fun onForgotten() {
                onRemoved()
            }

            override fun onRemembered() {
                // no-op
            }
        }
    }
}


/**
 * Build a function that reads how many [ViewModelStore]s the NavController hosting this [navBackEntry] currently holds,
 * or null when that amount cannot be read, e.g. because the internals of Navigation changed again.
 *
 * The stores are owned by an object that outlives Activity recreation (a [androidx.lifecycle.ViewModel] scoped to the NavHost),
 * which is why the returned function keeps reporting the current amount across a configuration change,
 * unlike the [NavBackStackEntry] that produced it.
 *
 * The amount of stores is not part of the public Navigation API, so it is read reflectively and two internal layouts are supported:
 * - Navigation 2.9.x and older: `NavBackStackEntry.viewModelStoreProvider` is a `NavControllerViewModel` owning a `viewModelStores` map.
 * - Navigation 2.10.0 and newer: `NavBackStackEntry.viewModelStoreProvider` is a `NavViewModelStoreProviderImpl` that delegates to an
 *   `androidx.lifecycle.viewmodel.ViewModelStoreProvider`, whose lazily created `StateHolder` owns an `entries` map.
 */
@SuppressLint("RestrictedApi")
private fun viewModelStoresCounter(navBackEntry: NavBackStackEntry): (() -> Int)? {
    // Access an androidx.navigation.NavViewModelStoreProvider
    val viewModelStoreProvider: Any = navBackEntry.readDeclaredField("viewModelStoreProvider") ?: return null

    // Navigation 2.9.x and older
    (viewModelStoreProvider.readDeclaredField("viewModelStores") as? Map<*, *>)?.let { stores -> return { stores.size } }

    // Navigation 2.10.0 and newer
    val delegatedStoreProvider: Any = viewModelStoreProvider.readDeclaredField("provider") ?: return null
    val counter = { delegatedStoreProvider.countViewModelStoresInStateHolder() }
    return counter.takeIf { it() != UNKNOWN_AMOUNT_OF_VIEW_MODEL_STORES }
}

/**
 * Count the entries held by the `StateHolder` of an `androidx.lifecycle.viewmodel.ViewModelStoreProvider`.
 * The amount is never cached because the `StateHolder` keeps track of the stores of all the destinations in the NavHost over time.
 */
private fun Any.countViewModelStoresInStateHolder(): Int =
    invokeNoArgumentsMethod("getStateHolder")
        ?.invokeNoArgumentsMethod("getEntries")
        ?.invokeNoArgumentsMethod("getSize") as? Int
        ?: UNKNOWN_AMOUNT_OF_VIEW_MODEL_STORES

private fun Any.readDeclaredField(name: String): Any? = try {
    javaClass.getDeclaredField(name).apply { isAccessible = true }.get(this)
} catch (_: Exception) {
    null
}

private fun Any.invokeNoArgumentsMethod(name: String): Any? = try {
    // The method can be declared by the class itself (and be private) or inherited from a public parent class
    val method = runCatching { javaClass.getDeclaredMethod(name) }.getOrElse { javaClass.getMethod(name) }
    method.apply { isAccessible = true }.invoke(this)
} catch (_: Exception) {
    null
}


@Composable
private fun ObserveComposableContainerLifecycleWithoutComposeNavigation(scopedViewModelContainer: ScopedViewModelContainer) {
    // Observe state of configuration changes when disposing
    val activity = LocalActivity.current
        ?: throw IllegalStateException("Expected an Activity for detecting configuration changes for a NavBackStackEntry but instead found null")
    remember(activity) {
        object : RememberObserver {
            /**
             * When the destination is removed from the composition, we can check if the destination is still in the foreground.
             *
             * We assume that after Activity recreation due to configuration change happens
             * if we wait for the first frame (see [ScopedViewModelContainer.scheduleToDispose])
             * and the scoped object was not requested again in the composition,
             * then the scoped object is not in Composition anymore and it should be disposed of.
             */
            private fun onRemoved() {
                val shouldBeReturningToForeground = activity.isChangingConfigurations
                scopedViewModelContainer.setShouldBeReturningToForeground {
                    shouldBeReturningToForeground
                }
            }

            override fun onAbandoned() {
                onRemoved()
            }

            override fun onForgotten() {
                onRemoved()
            }

            override fun onRemembered() {
                // no-op
            }
        }
    }
}
