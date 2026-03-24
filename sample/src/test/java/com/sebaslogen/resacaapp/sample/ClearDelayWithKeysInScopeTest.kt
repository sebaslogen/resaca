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
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeScopedViewModel
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
 * Tests for the interaction between clearDelay (item 1) and keyInScopeResolver (items 2+)
 * in a LazyColumn, mirroring the behavior in ComposeScreenWithSingleViewModelScopedWithKeys.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ClearDelayWithKeysInScopeTest : ComposeTestUtils {
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
     * Sets up a LazyColumn where:
     * - Item 1 uses viewModelScoped with clearDelay = 5.seconds (no keyInScopeResolver)
     * - Items 2+ use viewModelScoped with keyInScopeResolver (no clearDelay)
     */
    @Composable
    private fun TestLazyColumnWithClearDelayAndKeys(
        items: SnapshotStateList<NumberContainer>,
        height: Dp
    ) {
        Box(modifier = Modifier.size(width = 200.dp, height = height)) {
            val listItems: SnapshotStateList<NumberContainer> = remember { items }
            val keys = rememberKeysInScope(inputListOfKeys = listItems)
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(items = listItems, key = { it.number }) { item ->
                    Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                        val fakeScopedVM: FakeScopedViewModel = if (item.number == 1) {
                            viewModelScoped(key = item, clearDelay = 5.seconds) {
                                FakeScopedViewModel(stateSaver = it, viewModelId = item.number)
                            }
                        } else {
                            viewModelScoped(key = item, keyInScopeResolver = keys) {
                                FakeScopedViewModel(stateSaver = it, viewModelId = item.number)
                            }
                        }
                        DemoComposable(inputObject = fakeScopedVM, objectType = "FakeScopedViewModel $item", scoped = true)
                    }
                }
            }
        }
    }

    // endregion

    @Test
    fun `when item 1 with clearDelay is removed from list, its ViewModel is NOT cleared before 5 seconds`() = runTest {
        // Given a LazyColumn where item 1 uses clearDelay=5s
        val items = (1..10).toList().map { NumberContainer(it) }.toMutableStateList()
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                TestLazyColumnWithClearDelayAndKeys(items, height = 1000.dp)
            }
        }
        printComposeUiTreeToLog()

        // When item 1 is removed from the list (triggers disposal of item 1's composable)
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        items.removeAt(0) // Remove NumberContainer(1) from the list
        onNodeWithTestTag("FakeScopedViewModel 2 Scoped").assertExists() // Required to trigger recomposition
        advanceTimeBy(3000) // Advance time but NOT past the 5s clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then item 1's ViewModel is NOT yet cleared because clearDelay has not expired
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "Expected no ViewModels cleared before clearDelay expires, but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }

    @Test
    fun `when item 1 with clearDelay is removed from list, its ViewModel IS cleared after 5 seconds`() = runTest {
        // Given a LazyColumn where item 1 uses clearDelay=5s
        val items = (1..10).toList().map { NumberContainer(it) }.toMutableStateList()
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                TestLazyColumnWithClearDelayAndKeys(items, height = 1000.dp)
            }
        }
        printComposeUiTreeToLog()

        // When item 1 is removed from the list and we wait past 5s
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        items.removeAt(0) // Remove NumberContainer(1) from the list
        onNodeWithTestTag("FakeScopedViewModel 2 Scoped").assertExists() // Required to trigger recomposition
        advanceTimeBy(5100) // Advance time past the 5s clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then item 1's ViewModel IS cleared (only 1 VM — items 2+ are retained by keyInScopeResolver)
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "Expected exactly 1 ViewModel cleared after clearDelay expired, but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }

    @Test
    fun `when items 2+ with keyInScopeResolver scroll off-screen, their ViewModels are NOT cleared even after a long time`() = runTest {
        // Given a LazyColumn where items 2+ use keyInScopeResolver
        val listItems = (1..10).toList().map { NumberContainer(it) }.toMutableStateList()
        var height by mutableStateOf(1000.dp) // All items visible initially
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                TestLazyColumnWithClearDelayAndKeys(listItems, height)
            }
        }
        printComposeUiTreeToLog()

        // When height shrinks so only item 1 is visible (items 2+ go off-screen)
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        height = 100.dp // Only item 1 fits (100dp item in 100dp container)
        onNodeWithTestTag("FakeScopedViewModel 1 Scoped").assertExists() // Required to trigger recomposition
        advanceTimeBy(10_000) // Wait a very long time
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then no ViewModels are cleared — items 2+ are kept alive by keyInScopeResolver,
        // and item 1 is still in composition
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "Expected 0 ViewModels cleared (keyInScopeResolver keeps items 2+ alive), but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }

    @Test
    fun `when composable with clearDelay returns to composition before delay expires, disposal is cancelled`() = runTest {
        // Given a single composable with viewModelScoped + clearDelay=5s
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    val item = NumberContainer(1)
                    val fakeScopedVM: FakeScopedViewModel = viewModelScoped(key = item, clearDelay = 5.seconds) {
                        FakeScopedViewModel(stateSaver = it, viewModelId = item.number)
                    }
                    DemoComposable(inputObject = fakeScopedVM, objectType = "FakeScopedViewModel $item", scoped = true)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the composable is disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(3000) // Advance some time but NOT past the clearDelay
        printComposeUiTreeToLog()

        // And the composable returns to the composition before the clearDelay expires
        composablesShown = true
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(5100) // Wait past the original clearDelay deadline
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the disposal was cancelled — no ViewModels cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "Expected disposal to be cancelled when composable returned to composition, but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }

    @Test
    fun `when entire list is disposed, items 2+ are cleared immediately but item 1 waits for clearDelay`() = runTest {
        // Given a LazyColumn with all items visible
        val totalItems = 7
        val items = (1..totalItems).toList().map { NumberContainer(it) }.toMutableStateList()
        var shown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                if (shown) {
                    TestLazyColumnWithClearDelayAndKeys(items, height = 1000.dp)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the entire list is disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        shown = false
        composeTestRule.onNodeWithText(textTitle).assertExists()
        advanceTimeBy(100) // Short time — clearDelay for item 1 has NOT expired
        printComposeUiTreeToLog()
        val midAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then items 2+ (with keyInScopeResolver that was also disposed) are cleared,
        // but item 1 (with clearDelay) is NOT yet cleared
        val itemsWithKeyInScope = totalItems - 1 // items 2 through totalItems
        assert(midAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + itemsWithKeyInScope) {
            "Expected $itemsWithKeyInScope ViewModels cleared immediately (items 2+), but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $midAmountOfViewModelsCleared"
        }

        // When we wait past the clearDelay
        advanceTimeBy(5100)
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then item 1 is also cleared — all VMs are now cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + totalItems) {
            "Expected all $totalItems ViewModels cleared after clearDelay expired, but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }
}
