package com.sebaslogen.resacaapp.sample.koin

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.ScreensWithNavigation
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedKoinInjectedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedSecondKoinInjectedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.emptyDestination
import com.sebaslogen.resacaapp.sample.ui.main.koinViewModelScopedDestination
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

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ClearScopedViewModelTests : ComposeTestUtils {
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

    private lateinit var navController: NavHostController

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Scenarios to test clear after screen closed (i.e. when the ScopedViewModelContainer clears all ViewModels) //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `when I navigate to nested screen and back, then the Koin scoped ViewModels of the second screen are cleared`() = runTest {
        // Given the starting screen with Koin injected ViewModel scoped
        composeTestRule.activity.setContent {
            KoinContext {
                navController = rememberNavController()
                ScreensWithNavigation(navController = navController, startDestination = emptyDestination)
            }
        }
        printComposeUiTreeToLog()
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // When I navigate to a nested screen with a Koin scoped ViewModel and back to initial screen
        navController.navigate(koinViewModelScopedDestination)
        printComposeUiTreeToLog()
        withContext(Dispatchers.Main) {
            advanceTimeBy(100) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
        }
        navController.popBackStack()
        printComposeUiTreeToLog()
        withContext(Dispatchers.Main) {
            advanceTimeBy(100) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
        }
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the Koin scoped ViewModel from the second screen is cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 4) {
            "The amount of FakeInjectedViewModels that were cleared after back navigation ($finalAmountOfViewModelsCleared) " +
                    "was not as high as expected compared to the amount before navigating ($initialAmountOfViewModelsCleared)"
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Scenarios to test clear on owner Composable scope disposed (Composable not part of the composition anymore) //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `when the Composable that creates the Koin ViewModel is disposed, then the scoped ViewModel is cleared`() = runTest {

        // Given the starting screen with a scoped Koin ViewModel
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            KoinContext {
                Column {
                    Text(textTitle)
                    if (composablesShown) {
                        DemoScopedKoinInjectedViewModelComposable()
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When one Composable with a scoped ViewModel is not part of composition anymore and disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        printComposeUiTreeToLog() // Required to trigger recomposition
        onNodeWithTestTag("Koin FakeInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
        advanceTimeBy(100) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped ViewModel is cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of FakeInjectedViewModel that were cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "was not one higher that the amount before disposal ($initialAmountOfViewModelsCleared)"
        }
    }

    @Test
    fun `given two sibling Composables with two instances of the same Koin ViewModel, when one Composable is disposed, the other ViewModel is NOT cleared`() =
        runTest {

            // Given the starting screen with two scoped ViewModels sharing the same ViewModel instance
            var composablesShown by mutableStateOf(true)
            val textTitle = "Test text"
            composeTestRule.activity.setContent {
                KoinContext {
                    Column {
                        Text(textTitle)
                        DemoScopedKoinInjectedViewModelComposable()
                        if (composablesShown) {
                            DemoScopedKoinInjectedViewModelComposable()
                        }
                    }
                }
            }
            printComposeUiTreeToLog()

            // When one Composable with a scoped ViewModel is not part of composition anymore and disposed
            val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
            composablesShown = false // Trigger disposal
            composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
            advanceTimeBy(100) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
            printComposeUiTreeToLog()
            val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

            // Then one scoped ViewModel is cleared
            assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
                "The amount of FakeSecondInjectedViewModel that were cleared after disposal ($finalAmountOfViewModelsCleared) " +
                        "is not higher than the initial the amount before the Composable was disposed ($initialAmountOfViewModelsCleared)"
            }
        }

    @Test
    fun `given two sibling Composables with different Koin ViewModels, when one Composable is disposed, then only one ViewModel is cleared`() =
        runTest {

            // Given the starting screen with two scoped ViewModels sharing the same ViewModel instance
            var composablesShown by mutableStateOf(true)
            val textTitle = "Test text"
            composeTestRule.activity.setContent {
                KoinContext {
                    Column {
                        Text(textTitle)
                        DemoScopedKoinInjectedViewModelComposable()
                        if (composablesShown) {
                            DemoScopedSecondKoinInjectedViewModelComposable()
                        }
                    }
                }
            }
            printComposeUiTreeToLog()

            // When one Composable with a scoped ViewModel is not part of composition anymore and disposed
            val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
            composablesShown = false // Trigger disposal
            composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
            advanceTimeBy(100) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
            printComposeUiTreeToLog()
            val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

            // Then one scoped ViewModel is cleared
            assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
                "The amount of FakeSecondInjectedViewModel that were cleared after disposal ($finalAmountOfViewModelsCleared) " +
                        "is not higher than the initial the amount before the Composable was disposed ($initialAmountOfViewModelsCleared)"
            }
        }

    /////////////////////////////////////////////////////
    // Scenarios to test clear on ViewModel key change //
    /////////////////////////////////////////////////////

    @Test
    fun `when the key associated with the Koin ViewModel changes, then the old scoped ViewModel is cleared`() = runTest {

        // Given the starting screen with a Koin scoped ViewModel
        var viewModelKey by mutableStateOf("initial key")
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            KoinContext {
                Column {
                    Text(textTitle)
                    DemoScopedKoinInjectedViewModelComposable(viewModelKey)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the key changes
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        viewModelKey = "new key" // Trigger disposal
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        withContext(Dispatchers.Main) {
            advanceTimeBy(100) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
        }
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the Koin scoped ViewModel is cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of FakeScopedViewModels that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                    "was not two numbers higher that the amount before the key change ($initialAmountOfViewModelsCleared)"
        }
    }
}