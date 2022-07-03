package com.sebaslogen.resacaapp

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedObjectComposable
import com.sebaslogen.resacaapp.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ClearRememberScopedObjectTests : ComposeTestUtils {

    @get:Rule
    override val composeTestRule = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `when the Composables that remembers a Closeable is disposed of, then the scoped Closeable is cleared`() = runTest {

        // Given the starting screen with one scoped Closeable
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    DemoScopedObjectComposable()
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composables with scoped Closeable is not part of composition anymore and disposed
        val initialAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(6000) // Advance more than 5 seconds to pass the disposal delay on ScopedViewModelContainer
        printComposeUiTreeToLog()
        val finalAmountOfCloseableClosed = closeableClosedGloballySharedCounter.get()

        // Then the scoped Closeable is cleared
        assert(finalAmountOfCloseableClosed == initialAmountOfCloseableClosed + 1) {
            "The amount of FakeRepo that where closed after disposal ($finalAmountOfCloseableClosed) " +
                    "was not higher that the amount before the Composable was disposed ($initialAmountOfCloseableClosed)"
        }
    }
}