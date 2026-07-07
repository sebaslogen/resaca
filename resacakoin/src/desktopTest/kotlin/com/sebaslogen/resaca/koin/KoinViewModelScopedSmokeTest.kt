@file:OptIn(ExperimentalTestApi::class)
@file:Suppress("DEPRECATION") // KoinApplication(application = ...) overload is preferred for test brevity

package com.sebaslogen.resaca.koin

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import org.koin.compose.KoinApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Smoke test for [koinViewModelScoped] verifying the wrapper hands off to the same `getOrBuildViewModel` codepath
 * tested in `:resaca`. We don't re-test the underlying state machine — we only assert that:
 * 1. The Koin factory is invoked when the wrapper is first composed.
 * 2. The same VM is returned across recompositions.
 * 3. Changing the key triggers a new VM construction via the Koin factory.
 */
internal class KoinViewModelScopedSmokeTest {

    internal class TrackingViewModel : ViewModel()

    private class FakeOwner : ViewModelStoreOwner, LifecycleOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
        override val lifecycle: Lifecycle = LifecycleRegistry(this)
    }

    private val module = module {
        viewModelOf(::TrackingViewModel)
    }

    @AfterTest
    fun cleanup() {
        // KoinApplication composable starts/stops the Koin context per composition; nothing global to clear.
    }

    @Test
    fun `koinViewModelScoped returns the same VM across recomposition`() = runComposeUiTest {
        val owner = FakeOwner()
        var version by mutableStateOf(0)
        val captures = mutableListOf<TrackingViewModel>()

        setContent {
            KoinApplication(application = { modules(module) }) {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides owner,
                    LocalLifecycleOwner provides owner
                ) {
                    Column {
                        val vm = koinViewModelScoped<TrackingViewModel>()
                        @Suppress("UNUSED_EXPRESSION") version
                        SideEffect { captures += vm }
                        Text("v=$version")
                    }
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
        captures.forEach { assertSame(first, it, "Same VM should be reused across recomposition") }
    }

    @Test
    fun `koinViewModelScoped returns a new VM when the key changes`() = runComposeUiTest {
        val owner = FakeOwner()
        var key by mutableStateOf("a")
        val captures = mutableListOf<TrackingViewModel>()

        setContent {
            KoinApplication(application = { modules(module) }) {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner provides owner,
                    LocalLifecycleOwner provides owner
                ) {
                    Column {
                        val vm = koinViewModelScoped<TrackingViewModel>(key = key)
                        SideEffect { captures += vm }
                        Text("k=$key")
                    }
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
