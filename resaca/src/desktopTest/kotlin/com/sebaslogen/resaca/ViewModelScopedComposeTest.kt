@file:OptIn(ExperimentalTestApi::class, ResacaPackagePrivate::class)

package com.sebaslogen.resaca

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

internal class ViewModelScopedComposeTest {

    private class FakeOwner : ViewModelStoreOwner, LifecycleOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
        override val lifecycle: Lifecycle = LifecycleRegistry(this)
    }

    internal class TrackingViewModel : ViewModel()

    @Test
    fun `viewModelScoped returns the same VM across recomposition`() = runComposeUiTest {
        val owner = FakeOwner()
        var version by mutableStateOf(0)
        val captures = mutableListOf<TrackingViewModel>()

        setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides owner,
                LocalLifecycleOwner provides owner
            ) {
                Column {
                    // No-builder overload — uses DefaultViewModelProviderFactory and avoids the SavedStateHandle path
                    // which would require LocalSavedStateRegistryOwner.
                    val vm = viewModelScoped<TrackingViewModel>()
                    @Suppress("UNUSED_EXPRESSION") version
                    SideEffect { captures += vm }
                    Text("v=$version")
                }
            }
        }

        waitForIdle()
        runOnIdle { version = 1 }
        waitForIdle()
        runOnIdle { version = 2 }
        waitForIdle()

        assertTrue(captures.size >= 3, "Expected at least 3 compositions, got ${captures.size}")
        val first = captures.first()
        captures.forEach { assertSame(first, it) }
    }

    @Test
    fun `viewModelScoped returns a new VM when the key changes`() = runComposeUiTest {
        val owner = FakeOwner()
        var key by mutableStateOf("a")
        val captures = mutableListOf<TrackingViewModel>()

        setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides owner,
                LocalLifecycleOwner provides owner
            ) {
                Column {
                    val vm = viewModelScoped<TrackingViewModel>(key = key)
                    SideEffect { captures += vm }
                    Text("k=$key")
                }
            }
        }

        waitForIdle()
        runOnIdle { key = "b" }
        waitForIdle()

        assertTrue(captures.size >= 2)
        assertNotSame(captures.first(), captures.last(), "A new VM should be built when the key changes")
    }
}
