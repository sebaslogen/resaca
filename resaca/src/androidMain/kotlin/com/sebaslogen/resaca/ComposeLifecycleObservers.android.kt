package com.sebaslogen.resaca

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavViewModelStoreProvider
import java.lang.reflect.Field

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
    val viewModelStores = getViewModelStores()
    if (viewModelStores == null) {
        // Use a different observer when not using Compose Navigation with a NavHost, default to just Activity recreation
        ObserveComposableContainerLifecycleWithoutComposeNavigation(scopedViewModelContainer)
        return
    }
    val totalViewModelStoresWhenDestinationIsCreatedInNavHost = viewModelStores.size

    // Observe state of configuration changes when disposing
    val activity = LocalActivity.current
        ?: throw IllegalStateException("Expected an Activity for detecting configuration changes for a NavBackStackEntry but instead found null")
    remember(activity) {
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
                        totalViewModelStoresWhenDestinationIsCreatedInNavHost == viewModelStores.size
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
 * Get the NavBackStackEntry from the current LifecycleOwner to access the ViewModelStoreProvider and ViewModelStores
 * to count the number of ViewModelStores when the destination was created in the NavHost.
 *
 * In the callback after the Activity is recreated, we can check if the number of ViewModelStores is the same as when the destination was created.
 * When the number matches we are still on top of the back stack and the destination is back in the foreground.
 * When the number differs, it means the destination is not the top of the back stack and we should NOT dispose any scoped objects yet. Only after resuming.
 */
@SuppressLint("RestrictedApi")
@Composable
private fun getViewModelStores(): Map<String, ViewModelStore>? {
    val current = LocalLifecycleOwner.current
    try {
        val navBackEntry = current as? NavBackStackEntry ?: return null // No-op if not a NavBackStackEntry, aka if not using Compose Navigation with a NavHost
        val navViewModelStoreProviderField: Field = navBackEntry.javaClass.getDeclaredField("viewModelStoreProvider")
        navViewModelStoreProviderField.isAccessible = true // Make the field accessible to read
        val navViewModelStoreProvider: NavViewModelStoreProvider = navViewModelStoreProviderField.get(navBackEntry) as? NavViewModelStoreProvider ?: return null
        val viewModelStoresField: Field = navViewModelStoreProvider.javaClass.getDeclaredField("viewModelStores")
        viewModelStoresField.isAccessible = true // Make the field accessible to read
        return viewModelStoresField.get(navViewModelStoreProvider) as? Map<String, ViewModelStore>
    } catch (_: Exception) {
        return null
    }
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
