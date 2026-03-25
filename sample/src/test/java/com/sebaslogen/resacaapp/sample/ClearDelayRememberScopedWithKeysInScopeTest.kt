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
import com.sebaslogen.resaca.rememberScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeRepo
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
 * Tests for the `rememberScoped(key, keyInScopeResolver, clearDelay, builder)` overload
 * that exercises the combined clearDelay + keyInScopeResolver path.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ClearDelayRememberScopedWithKeysInScopeTest : ComposeTestUtils {
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
     * Sets up a LazyColumn where each item uses `rememberScoped(key, keyInScopeResolver, clearDelay, builder)`.
     */
    @Composable
    private fun TestLazyColumnWithClearDelayAndKeysForObjects(
        items: SnapshotStateList<NumberContainer>,
        height: Dp
    ) {
        Box(modifier = Modifier.size(width = 200.dp, height = height)) {
            val listItems: SnapshotStateList<NumberContainer> = remember { items }
            val keys = rememberKeysInScope(inputListOfKeys = listItems)
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(items = listItems, key = { it.number }) { item ->
                    Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                        val fakeRepo: FakeRepo = rememberScoped(
                            key = item,
                            keyInScopeResolver = keys,
                            clearDelay = 5.seconds
                        ) { FakeRepo() }
                        DemoComposable(inputObject = fakeRepo, objectType = "FakeRepo $item", scoped = true)
                    }
                }
            }
        }
    }

    // endregion

    @Test
    fun `when item with clearDelay and keyInScope scrolls off-screen, its object is NOT cleared because keyInScope keeps it alive`() = runTest {
        // Given a LazyColumn where all items use clearDelay + keyInScopeResolver
        val listItems = (1..10).toList().map { NumberContainer(it) }.toMutableStateList()
        var height by mutableStateOf(1000.dp) // All items visible initially
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                TestLazyColumnWithClearDelayAndKeysForObjects(listItems, height)
            }
        }
        printComposeUiTreeToLog()

        // When height shrinks so only item 1 is visible (items 2+ go off-screen)
        val initialAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()
        height = 100.dp // Only item 1 fits
        onNodeWithTestTag("FakeRepo 1 Scoped").assertExists() // Required to trigger recomposition
        advanceTimeBy(10_000) // Wait a very long time
        printComposeUiTreeToLog()
        val finalAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()

        // Then no objects are cleared because keyInScopeResolver keeps them alive
        assert(finalAmountOfCloseableClosed == initialAmountOfCloseableClosed) {
            "Expected 0 objects closed (keyInScopeResolver keeps items alive), but " +
                    "closed count changed from $initialAmountOfCloseableClosed to $finalAmountOfCloseableClosed"
        }
    }

    @Test
    fun `when entire list with clearDelay and keyInScope is disposed, objects are cleared after delay expires`() = runTest {
        // Given a LazyColumn with clearDelay + keyInScopeResolver items
        val totalItems = 5
        val items = (1..totalItems).toList().map { NumberContainer(it) }.toMutableStateList()
        var shown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                if (shown) {
                    TestLazyColumnWithClearDelayAndKeysForObjects(items, height = 500.dp)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the entire list is disposed
        val initialAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()
        shown = false
        composeTestRule.onNodeWithText(textTitle).assertExists()

        // After a short time (before clearDelay) — objects should NOT yet be cleared because clearDelay applies
        advanceTimeBy(100)
        printComposeUiTreeToLog()
        val midAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()

        // The clearDelay has not expired yet so objects should still be alive
        assert(midAmountOfCloseableClosed == initialAmountOfCloseableClosed) {
            "Expected 0 objects closed before clearDelay expires, but " +
                    "closed count changed from $initialAmountOfCloseableClosed to $midAmountOfCloseableClosed"
        }

        // After the clearDelay expires, all objects should be cleared
        advanceTimeBy(5100) // Wait past the 5s clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()
        assert(finalAmountOfCloseableClosed >= initialAmountOfCloseableClosed + totalItems) {
            "Expected at least $totalItems objects closed after clearDelay expires, but " +
                    "closed count changed from $initialAmountOfCloseableClosed to $finalAmountOfCloseableClosed"
        }
    }

    @Test
    fun `when item is removed from list with clearDelay and keyInScope, its object is cleared because key is no longer in scope`() = runTest {
        // Given a LazyColumn where items use clearDelay + keyInScopeResolver
        val items = (1..5).toList().map { NumberContainer(it) }.toMutableStateList()
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                TestLazyColumnWithClearDelayAndKeysForObjects(items, height = 500.dp)
            }
        }
        printComposeUiTreeToLog()

        // When item 1 is removed from the list (key removed from scope)
        val initialAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()
        items.removeAt(0) // Remove NumberContainer(1) — key no longer in scope
        onNodeWithTestTag("FakeRepo 2 Scoped").assertExists() // Trigger recomposition
        advanceTimeBy(5100) // Advance past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()

        // Then the removed item's object is cleared
        assert(finalAmountOfCloseableClosed >= initialAmountOfCloseableClosed + 1) {
            "Expected at least 1 object closed when key removed from scope, but " +
                    "closed count changed from $initialAmountOfCloseableClosed to $finalAmountOfCloseableClosed"
        }
    }
}
