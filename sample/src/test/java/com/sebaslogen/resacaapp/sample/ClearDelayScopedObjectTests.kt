package com.sebaslogen.resacaapp.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedObjectWithClearDelayComposable
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ClearDelayScopedObjectTests : ComposeTestUtils {
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

    @Test
    fun `when composable with clearDelay is disposed, the scoped Closeable is NOT cleared before delay expires`() = runTest {

        // Given the starting screen with one scoped Closeable with clearDelay
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    DemoScopedObjectWithClearDelayComposable(clearDelay = 2.seconds)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable with scoped Closeable is not part of composition anymore and disposed
        val initialAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(100) // Advance time but NOT past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()

        // Then the scoped Closeable is NOT yet cleared because clearDelay has not expired
        assert(finalAmountOfCloseableClosed == initialAmountOfCloseableClosed) {
            "The amount of FakeRepo that were closed after disposal ($finalAmountOfCloseableClosed) " +
                    "should be the same as before disposal ($initialAmountOfCloseableClosed) because clearDelay has not expired"
        }
    }

    @Test
    fun `when composable with clearDelay is disposed, the scoped Closeable IS cleared after delay expires`() = runTest {

        // Given the starting screen with one scoped Closeable with clearDelay
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    DemoScopedObjectWithClearDelayComposable(clearDelay = 2.seconds)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable with scoped Closeable is not part of composition anymore and disposed
        val initialAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(2100) // Advance time past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()

        // Then the scoped Closeable IS cleared because clearDelay has expired
        assert(finalAmountOfCloseableClosed == initialAmountOfCloseableClosed + 1) {
            "The amount of FakeRepo that were closed after disposal ($finalAmountOfCloseableClosed) " +
                    "was not higher than the amount before disposal ($initialAmountOfCloseableClosed)"
        }
    }

    @Test
    fun `when composable returns to composition before clearDelay expires, the scoped Closeable disposal is cancelled`() = runTest {

        // Given the starting screen with one scoped Closeable with clearDelay
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    DemoScopedObjectWithClearDelayComposable(clearDelay = 2.seconds)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable is disposed
        val initialAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(100) // Advance some time but NOT past the clearDelay
        printComposeUiTreeToLog()

        // And the Composable returns to the composition before the clearDelay expires
        composablesShown = true
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(2100) // Advance time past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()

        // Then the scoped Closeable is NOT cleared because the disposal was cancelled
        assert(finalAmountOfCloseableClosed == initialAmountOfCloseableClosed) {
            "The amount of FakeRepo that were closed ($finalAmountOfCloseableClosed) " +
                    "should be the same as before disposal ($initialAmountOfCloseableClosed) because disposal was cancelled"
        }
    }

    @Test
    fun `when key changes with clearDelay, the old scoped Closeable is cleared immediately (clearDelay does not apply to key changes)`() = runTest {

        // Given the starting screen with a scoped Closeable with clearDelay
        var closeableKey by mutableStateOf("initial key")
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                DemoScopedObjectWithClearDelayComposable(key = closeableKey, clearDelay = 2.seconds)
            }
        }
        printComposeUiTreeToLog()

        // When the key changes
        val initialAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()
        closeableKey = "new key" // Trigger disposal of old object
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(100) // Advance time to allow clear call to be processed
        printComposeUiTreeToLog()
        val finalAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()

        // Then the old scoped Closeable IS cleared immediately because key changes bypass clearDelay
        assert(finalAmountOfCloseableClosed == initialAmountOfCloseableClosed + 1) {
            "The amount of FakeRepo that were closed after key change ($finalAmountOfCloseableClosed) " +
                    "was not higher than the amount before key change ($initialAmountOfCloseableClosed)"
        }
    }
}
