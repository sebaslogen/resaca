package com.sebaslogen.resacaapp.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.rememberKeysInScope
import com.sebaslogen.resaca.viewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeSimpleViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.NumberContainer
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds


/**
 * Tests for `viewModelScoped(keyInScopeResolver, key, clearDelay)` overload (no builder, default factory).
 * This exercises the overload that delegates to the default-factory `viewModelScoped(key, clearDelay)`,
 * wrapping the key in a `ScopeKeyWithResolver`.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ClearDelayViewModelScopedNoBuilderWithKeysTest : ComposeTestUtils {
    init {
        callFromTestInit()
    }

    override fun callFromTestInit() {
        preTestInitializationToEmptyComposeDestination()
    }

    @get:Rule
    override val composeTestRule = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // region Helper composable

    /**
     * Sets up a LazyColumn where each item uses `viewModelScoped(keyInScopeResolver, key, clearDelay)` (no builder).
     * This uses [FakeSimpleViewModel] which has a no-arg constructor compatible with the default factory.
     */
    @Composable
    private fun TestLazyColumnWithDefaultFactoryClearDelayAndKeys(
        items: SnapshotStateList<NumberContainer>,
        height: Dp
    ) {
        Box(modifier = Modifier.size(width = 200.dp, height = height)) {
            val listItems: SnapshotStateList<NumberContainer> = remember { items }
            val keys = rememberKeysInScope(inputListOfKeys = listItems)
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(items = listItems, key = { it.number }) { item ->
                    Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                        val simpleVM: FakeSimpleViewModel = viewModelScoped(
                            keyInScopeResolver = keys,
                            key = item,
                            clearDelay = 5.seconds
                        )
                        DemoComposable(inputObject = simpleVM, objectType = "FakeSimpleViewModel $item", scoped = true)
                    }
                }
            }
        }
    }

    /**
     * Sets up a LazyColumn where each item uses `viewModelScoped(keyInScopeResolver, key)` (no builder, no clearDelay).
     * This exercises the default `clearDelay = null` parameter on the no-builder overload.
     */
    @Composable
    private fun TestLazyColumnWithDefaultFactoryNoClearDelayAndKeys(
        items: SnapshotStateList<NumberContainer>,
        height: Dp
    ) {
        Box(modifier = Modifier.size(width = 200.dp, height = height)) {
            val listItems: SnapshotStateList<NumberContainer> = remember { items }
            val keys = rememberKeysInScope(inputListOfKeys = listItems)
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(items = listItems, key = { it.number }) { item ->
                    Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                        val simpleVM: FakeSimpleViewModel = viewModelScoped(
                            keyInScopeResolver = keys,
                            key = item
                        )
                        DemoComposable(inputObject = simpleVM, objectType = "FakeSimpleViewModel $item", scoped = true)
                    }
                }
            }
        }
    }

    // endregion

    // region Tests for viewModelScoped(keyInScopeResolver, key) without clearDelay (exercises clearDelay = null default)

    @Test
    fun `when item with default-factory and keyInScope but no clearDelay scrolls off-screen, its VM is NOT cleared`() = runTest {
        // Given a LazyColumn where all items use viewModelScoped(keyInScopeResolver, key) with no builder and no clearDelay
        val listItems = (1..10).toList().map { NumberContainer(it) }.toMutableStateList()
        var height by mutableStateOf(1000.dp)
        val textTitle = "Test text no delay"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                TestLazyColumnWithDefaultFactoryNoClearDelayAndKeys(listItems, height)
            }
        }
        printComposeUiTreeToLog()

        // When height shrinks so only item 1 is visible
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        height = 100.dp
        onNodeWithTestTag("FakeSimpleViewModel 1 Scoped").assertExists()
        advanceTimeBy(1000) // Advance time to allow disposal processing
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then no ViewModels are cleared — keyInScopeResolver keeps them alive
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "Expected 0 ViewModels cleared (keyInScopeResolver keeps items alive), but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }

    // endregion

    // region Tests for viewModelScoped(keyInScopeResolver, key, clearDelay) with explicit clearDelay

    @Test
    fun `when item with default-factory clearDelay and keyInScope scrolls off-screen, its VM is NOT cleared`() = runTest {
        // Given a LazyColumn where all items use viewModelScoped(keyInScopeResolver, key, clearDelay) with no builder
        val listItems = (1..10).toList().map { NumberContainer(it) }.toMutableStateList()
        var height by mutableStateOf(1000.dp)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                TestLazyColumnWithDefaultFactoryClearDelayAndKeys(listItems, height)
            }
        }
        printComposeUiTreeToLog()

        // When height shrinks so only item 1 is visible
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        height = 100.dp
        onNodeWithTestTag("FakeSimpleViewModel 1 Scoped").assertExists()
        advanceTimeBy(10_000) // Wait a long time past clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then no ViewModels are cleared — keyInScopeResolver keeps them alive
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "Expected 0 ViewModels cleared (keyInScopeResolver keeps items alive), but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }

    @Test
    fun `when item is removed from list with default-factory clearDelay and keyInScope, its VM is cleared after delay`() = runTest {
        // Given a LazyColumn where items use viewModelScoped(keyInScopeResolver, key, clearDelay) with no builder
        val items = (1..5).toList().map { NumberContainer(it) }.toMutableStateList()
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                TestLazyColumnWithDefaultFactoryClearDelayAndKeys(items, height = 500.dp)
            }
        }
        printComposeUiTreeToLog()

        // When item 1 is removed (key no longer in scope)
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        items.removeAt(0)
        onNodeWithTestTag("FakeSimpleViewModel 2 Scoped").assertExists()

        // Before clearDelay: not cleared
        advanceTimeBy(3000)
        val midAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        assert(midAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "Expected no VMs cleared before delay, but count changed from $initialAmountOfViewModelsCleared to $midAmountOfViewModelsCleared"
        }

        // After clearDelay: cleared
        advanceTimeBy(2100) // total 5100ms > 5s
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "Expected 1 VM cleared after delay, but count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }

    @Test
    fun `when entire list is disposed with default-factory clearDelay and keyInScope, all VMs are eventually cleared`() = runTest {
        // Given a LazyColumn with items
        val totalItems = 5
        val items = (1..totalItems).toList().map { NumberContainer(it) }.toMutableStateList()
        var shown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                if (shown) {
                    TestLazyColumnWithDefaultFactoryClearDelayAndKeys(items, height = 500.dp)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the entire list is disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        shown = false
        composeTestRule.onNodeWithText(textTitle).assertExists()
        advanceTimeBy(5100) // Wait past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then all VMs are eventually cleared
        assert(finalAmountOfViewModelsCleared >= initialAmountOfViewModelsCleared + totalItems) {
            "Expected all $totalItems VMs cleared, but count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }
}
