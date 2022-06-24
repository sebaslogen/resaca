package com.sebaslogen.resaca.hilt

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.HiltViewModelFactory
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import com.sebaslogen.resaca.ScopedViewModelContainer
import com.sebaslogen.resaca.rememberScoped
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel

/**
 * Returns an existing [HiltViewModel] (an annotated [ViewModel]) for a particular type [T] or creates a new one.
 * The scope is handled by [rememberScoped] to store and retrieve the requested [ViewModel].
 *
 * If a [ViewModel] of type [T] cannot be found in the [ScopedViewModelContainer],
 * then the [ViewModelProvider.Factory] from Hilt will be used to create one.
 *
 * Note: There is no support for keys in this method because Hilt instances are singletons, so the same object will always be returned once created.
 * Support for keys in the Hilt library is still WIP. See https://github.com/google/dagger/issues/2328
 */
@Composable
inline fun <reified T : ViewModel> hiltViewModelScoped(): T {
    val viewModel: T = rememberScoped {
        hiltViewModelFactoryWithRestoration()
    }
    return viewModel
}

@Composable
@PublishedApi
internal inline fun <reified T : ViewModel> hiltViewModelFactoryWithRestoration(): T =
    tryToRestoreScopedViewModel() // The requested ViewModel already exists and it's probably a singleton, return the one stored in our container
        ?: createHiltViewModelFactory().create(T::class.java) // Otherwise create one using a factory

/**
 * In case the ViewModel that we will try to create already exists in the container,
 * then we retrieve it to reuse it,
 * because Hilt doesn't support more than one [ViewModel] for the same type or a key per [ViewModel].
 */
@Composable
@PublishedApi
internal inline fun <reified T : ViewModel> tryToRestoreScopedViewModel(): T? {
    val scopedViewModelContainer: ScopedViewModelContainer = viewModel()
    return scopedViewModelContainer.getFirstViewModelWithTypeOrNull()
}

/**
 * Create a factory based on the back-stack-entry in case the Navigation library is used for this Composable
 * Otherwise, assume the container Fragment or Activity is properly annotated with @[AndroidEntryPoint]
 * and use its factory
 */
@Composable
@PublishedApi
internal fun createHiltViewModelFactory(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
): ViewModelProvider.Factory =
    if (viewModelStoreOwner is NavBackStackEntry) {
        HiltViewModelFactory(
            context = LocalContext.current,
            navBackStackEntry = viewModelStoreOwner
        )
    } else {
        // Use the default factory provided by the ViewModelStoreOwner
        // and assume it is an @AndroidEntryPoint annotated fragment or activity
        if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
            viewModelStoreOwner.defaultViewModelProviderFactory
        } else {
            ViewModelProvider.NewInstanceFactory.instance
        }
    }
