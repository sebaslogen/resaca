package com.sebaslogen.resacaapp.sample.koin

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
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedKoinInjectedViewModelWithClearDelayComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedSecondKoinInjectedViewModelWithClearDelayComposable
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
import org.koin.compose.KoinContext
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
    fun `when Koin composable with clearDelay is disposed, the ViewModel is NOT cleared before delay expires`() = runTest {

        // Given the starting screen with a scoped Koin ViewModel with clearDelay
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            @Suppress("DEPRECATION")
            KoinContext {
                Column {
                    Text(textTitle)
                    if (composablesShown) {
                        DemoScopedKoinInjectedViewModelWithClearDelayComposable(clearDelay = 2.seconds)
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable with scoped Koin ViewModel is not part of composition anymore and disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        printComposeUiTreeToLog() // Required to trigger recomposition
        onNodeWithTestTag("Koin FakeInjectedViewModel with clearDelay Scoped", assertDisplayed = false).assertDoesNotExist()
        advanceTimeBy(100) // Advance time but NOT past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped Koin ViewModel is NOT yet cleared because clearDelay has not expired
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "The amount of ViewModels that were cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "should be the same as before disposal ($initialAmountOfViewModelsCleared) because clearDelay has not expired"
        }
    }

    @Test
    fun `when Koin composable with clearDelay is disposed, the ViewModel IS cleared after delay expires`() = runTest {

        // Given the starting screen with a scoped Koin ViewModel with clearDelay
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            @Suppress("DEPRECATION")
            KoinContext {
                Column {
                    Text(textTitle)
                    if (composablesShown) {
                        DemoScopedKoinInjectedViewModelWithClearDelayComposable(clearDelay = 2.seconds)
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable with scoped Koin ViewModel is not part of composition anymore and disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        printComposeUiTreeToLog() // Required to trigger recomposition
        onNodeWithTestTag("Koin FakeInjectedViewModel with clearDelay Scoped", assertDisplayed = false).assertDoesNotExist()
        advanceTimeBy(2100) // Advance time past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped Koin ViewModel IS cleared because clearDelay has expired
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of ViewModels that were cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "was not higher than the amount before disposal ($initialAmountOfViewModelsCleared)"
        }
    }

    @Test
    fun `when Koin composable returns before clearDelay expires, the ViewModel disposal is cancelled`() = runTest {

        // Given the starting screen with a scoped Koin ViewModel with clearDelay
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            @Suppress("DEPRECATION")
            KoinContext {
                Column {
                    Text(textTitle)
                    if (composablesShown) {
                        DemoScopedKoinInjectedViewModelWithClearDelayComposable(clearDelay = 2.seconds)
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable is disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        printComposeUiTreeToLog() // Required to trigger recomposition
        advanceTimeBy(100) // Advance some time but NOT past the clearDelay
        printComposeUiTreeToLog()

        // And the Composable returns to the composition before the clearDelay expires
        composablesShown = true
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(2100) // Advance time past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped Koin ViewModel is NOT cleared because the disposal was cancelled
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "The amount of ViewModels that were cleared ($finalAmountOfViewModelsCleared) " +
                    "should be the same as before disposal ($initialAmountOfViewModelsCleared) because disposal was cancelled"
        }
    }

    @Test
    fun `when key changes on Koin VM with clearDelay, the old ViewModel is cleared immediately (clearDelay does not apply to key changes)`() = runTest {

        // Given the starting screen with a Koin scoped ViewModel with clearDelay
        var viewModelKey by mutableStateOf("initial key")
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            @Suppress("DEPRECATION")
            KoinContext {
                Column {
                    Text(textTitle)
                    DemoScopedKoinInjectedViewModelWithClearDelayComposable(key = viewModelKey, clearDelay = 2.seconds)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the key changes
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        viewModelKey = "new key" // Trigger disposal of old ViewModel
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        withContext(Dispatchers.Main) {
            advanceTimeBy(100) // Advance time to allow clear call to be processed
        }
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the old scoped Koin ViewModel IS cleared immediately because key changes bypass clearDelay
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of ViewModels that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                    "was not higher than the amount before key change ($initialAmountOfViewModelsCleared)"
        }
    }

    // region Simple koinViewModelScoped(key, clearDelay) — no parameters

    @Test
    fun `when simple Koin VM with clearDelay is disposed, the ViewModel is NOT cleared before delay expires`() = runTest {

        // Given the starting screen with a simple Koin ViewModel with clearDelay (no parameters)
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            @Suppress("DEPRECATION")
            KoinContext {
                Column {
                    Text(textTitle)
                    if (composablesShown) {
                        DemoScopedSecondKoinInjectedViewModelWithClearDelayComposable(clearDelay = 2.seconds)
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable is disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false
        printComposeUiTreeToLog()
        onNodeWithTestTag("Koin FakeSecondInjectedViewModel with clearDelay Scoped", assertDisplayed = false).assertDoesNotExist()
        advanceTimeBy(100) // Advance time but NOT past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped Koin ViewModel is NOT yet cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "The amount of ViewModels cleared ($finalAmountOfViewModelsCleared) " +
                    "should be the same as before disposal ($initialAmountOfViewModelsCleared) because clearDelay has not expired"
        }
    }

    @Test
    fun `when simple Koin VM with clearDelay is disposed, the ViewModel IS cleared after delay expires`() = runTest {

        // Given the starting screen with a simple Koin ViewModel with clearDelay (no parameters)
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            @Suppress("DEPRECATION")
            KoinContext {
                Column {
                    Text(textTitle)
                    if (composablesShown) {
                        DemoScopedSecondKoinInjectedViewModelWithClearDelayComposable(clearDelay = 2.seconds)
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable is disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false
        printComposeUiTreeToLog()
        onNodeWithTestTag("Koin FakeSecondInjectedViewModel with clearDelay Scoped", assertDisplayed = false).assertDoesNotExist()
        advanceTimeBy(2100) // Advance time past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped Koin ViewModel IS cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of ViewModels cleared ($finalAmountOfViewModelsCleared) " +
                    "was not higher than before disposal ($initialAmountOfViewModelsCleared)"
        }
    }

    // endregion
}
