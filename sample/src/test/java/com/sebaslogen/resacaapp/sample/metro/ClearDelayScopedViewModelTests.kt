package com.sebaslogen.resacaapp.sample.metro

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedMetroInjectedViewModelWithClearDelayComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedSecondMetroInjectedViewModelWithClearDelayComposable
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.utils.MainDispatcherRule
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ClearDelayScopedViewModelTests : ComposeTestUtils {
    init {
        callFromTestInit()
    }

    override fun callFromTestInit() {
        preTestInitializationToEmptyComposeDestination()
    }

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComposeActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `when Metro composable with clearDelay is disposed, the ViewModel is NOT cleared before delay expires`() = runTest {

        // Given the starting screen with a scoped Metro ViewModel with clearDelay
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    DemoScopedMetroInjectedViewModelWithClearDelayComposable(clearDelay = 2.seconds)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable with scoped Metro ViewModel is not part of composition anymore and disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        printComposeUiTreeToLog() // Required to trigger recomposition
        onNodeWithTestTag("Metro FakeMetroInjectedViewModel with clearDelay Scoped", assertDisplayed = false).assertDoesNotExist()
        advanceTimeBy(100) // Advance time but NOT past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped Metro ViewModel is NOT yet cleared because clearDelay has not expired
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "The amount of ViewModels that were cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "should be the same as before disposal ($initialAmountOfViewModelsCleared) because clearDelay has not expired"
        }
    }

    @Test
    fun `when Metro composable with clearDelay is disposed, the ViewModel IS cleared after delay expires`() = runTest {

        // Given the starting screen with a scoped Metro ViewModel with clearDelay
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    DemoScopedMetroInjectedViewModelWithClearDelayComposable(clearDelay = 2.seconds)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable with scoped Metro ViewModel is not part of composition anymore and disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        printComposeUiTreeToLog() // Required to trigger recomposition
        onNodeWithTestTag("Metro FakeMetroInjectedViewModel with clearDelay Scoped", assertDisplayed = false).assertDoesNotExist()
        advanceTimeBy(2100) // Advance time past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped Metro ViewModel IS cleared because clearDelay has expired
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of ViewModels that were cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "was not higher than the amount before disposal ($initialAmountOfViewModelsCleared)"
        }
    }

    @Test
    fun `when Metro composable returns before clearDelay expires, the ViewModel disposal is cancelled`() = runTest {

        // Given the starting screen with a scoped Metro ViewModel with clearDelay
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    DemoScopedMetroInjectedViewModelWithClearDelayComposable(clearDelay = 2.seconds)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable is removed from composition
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        printComposeUiTreeToLog()
        advanceTimeBy(100) // Advance time but NOT past the clearDelay

        // And then the Composable returns to composition before clearDelay expires
        composablesShown = true
        printComposeUiTreeToLog()
        advanceTimeBy(3000) // Advance time past what would have been the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped Metro ViewModel is NOT cleared because it returned before clearDelay expired
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "The amount of ViewModels that were cleared ($finalAmountOfViewModelsCleared) " +
                    "should be the same as before ($initialAmountOfViewModelsCleared) because the Composable returned before clearDelay expired"
        }
    }

    @Test
    fun `when Metro simple composable with clearDelay is disposed, the ViewModel IS cleared after delay expires`() = runTest {

        // Given the starting screen with a scoped Metro ViewModel with clearDelay
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    DemoScopedSecondMetroInjectedViewModelWithClearDelayComposable(clearDelay = 2.seconds)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable with scoped Metro ViewModel is not part of composition anymore and disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        printComposeUiTreeToLog() // Required to trigger recomposition
        onNodeWithTestTag("Metro FakeMetroSecondInjectedViewModel with clearDelay Scoped", assertDisplayed = false).assertDoesNotExist()
        advanceTimeBy(2100) // Advance time past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped Metro ViewModel IS cleared because clearDelay has expired
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of ViewModels that were cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "was not higher than the amount before disposal ($initialAmountOfViewModelsCleared)"
        }
    }
}
