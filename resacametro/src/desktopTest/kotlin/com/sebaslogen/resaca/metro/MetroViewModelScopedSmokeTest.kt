@file:OptIn(ExperimentalTestApi::class)

package com.sebaslogen.resaca.metro

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.zacsweers.metro.Provider
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Smoke test for [metroViewModelScoped] verifying the wrapper hands off to the same `getOrBuildViewModel` codepath
 * tested in `:resaca`. We pass a hand-rolled [ViewModelProvider.Factory] directly (rather than wiring up the full
 * Metro DI graph) — this is sufficient to exercise the wrapper's plumbing.
 */
internal class MetroViewModelScopedSmokeTest {

    internal class TrackingViewModel : ViewModel()

    private class FakeOwner : ViewModelStoreOwner, LifecycleOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
        override val lifecycle: Lifecycle = LifecycleRegistry(this)
    }

    private val factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T =
            TrackingViewModel() as T
    }

    /** Concrete [MetroViewModelFactory] for the LocalMetroViewModelFactory default-resolution test. */
    private val metroFactory = object : MetroViewModelFactory() {
        override val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>> = mapOf(
            TrackingViewModel::class to Provider { TrackingViewModel() }
        )
    }

    @Test
    fun `metroViewModelScoped returns the same VM across recomposition`() = runComposeUiTest {
        val owner = FakeOwner()
        var version by mutableStateOf(0)
        val captures = mutableListOf<TrackingViewModel>()

        setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides owner,
                LocalLifecycleOwner provides owner
            ) {
                Column {
                    val vm = metroViewModelScoped<TrackingViewModel>(factory = factory)
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
        captures.forEach { assertSame(first, it, "Same VM should be reused across recomposition") }
    }

    @Test
    fun `metroViewModelScoped resolves the default factory from LocalMetroViewModelFactory`() = runComposeUiTest {
        // Regression guard for the default `factory = LocalMetroViewModelFactory.current` parameter added in commit
        // b7d89b7. When the caller omits `factory`, the wrapper must read it from the CompositionLocal — a
        // regression in that branch (e.g. wrong CompositionLocal, or the Metro library changing the binding) would
        // otherwise slip through CI silently because every other call site passes a factory explicitly.
        val owner = FakeOwner()
        var observedVm: TrackingViewModel? = null

        setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides owner,
                LocalLifecycleOwner provides owner,
                LocalMetroViewModelFactory provides metroFactory
            ) {
                Column {
                    val vm = metroViewModelScoped<TrackingViewModel>() // factory parameter omitted → uses CompositionLocal
                    SideEffect { observedVm = vm }
                    Text("rendered")
                }
            }
        }

        waitForIdle()
        assertNotNull(observedVm, "VM should have been resolved via LocalMetroViewModelFactory")
    }

    @Test
    fun `metroViewModelScoped returns a new VM when the key changes`() = runComposeUiTest {
        val owner = FakeOwner()
        var key by mutableStateOf("a")
        val captures = mutableListOf<TrackingViewModel>()

        setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides owner,
                LocalLifecycleOwner provides owner
            ) {
                Column {
                    val vm = metroViewModelScoped<TrackingViewModel>(key = key, factory = factory)
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
