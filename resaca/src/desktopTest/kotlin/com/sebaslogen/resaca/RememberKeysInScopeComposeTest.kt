@file:OptIn(ExperimentalTestApi::class, ResacaPackagePrivate::class)

package com.sebaslogen.resaca

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import kotlin.test.Test
import kotlin.test.assertSame

/**
 * Compose-runtime smoke test for [rememberKeysInScope] verifying it integrates correctly with the @Composable
 * [rememberScoped] entry point. Disposal semantics are covered by the unit tests in
 * `ScopedViewModelContainerLifecycleTest`; this test only exercises the composition wiring.
 */
internal class RememberKeysInScopeComposeTest {

    private class FakeOwner : ViewModelStoreOwner, LifecycleOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
        override val lifecycle: Lifecycle = LifecycleRegistry(this)
    }

    @Test
    fun `keys still in the list keep their objects alive across recomposition`() = runComposeUiTest {
        val owner = FakeOwner()
        var keys by mutableStateOf(listOf("x", "y"))
        val builtByKey = mutableMapOf<String, Any>()

        setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides owner,
                LocalLifecycleOwner provides owner
            ) {
                Column {
                    val resolver = rememberKeysInScope(keys)
                    keys.forEach { k ->
                        val obj = rememberScoped(key = k, keyInScopeResolver = resolver) {
                            Any().also { builtByKey.putIfAbsent(k, it) }
                        }
                        @Suppress("UNUSED_EXPRESSION") obj
                    }
                    Text("keys=$keys")
                }
            }
        }

        waitForIdle()
        val originalX = builtByKey.getValue("x")
        val originalY = builtByKey.getValue("y")

        runOnIdle { keys = listOf("y", "x") }
        waitForIdle()

        assertSame(originalX, builtByKey.getValue("x"))
        assertSame(originalY, builtByKey.getValue("y"))
    }
}
