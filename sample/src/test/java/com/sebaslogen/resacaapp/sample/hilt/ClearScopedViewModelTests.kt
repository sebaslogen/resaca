package com.sebaslogen.resacaapp.sample.hilt

import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedHiltInjectedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedSecondHiltInjectedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.hiltViewModelScopedDestination
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.utils.MainDispatcherRule
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ClearScopedViewModelTests : ComposeTestUtils {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComposeActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var navController: NavHostController

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Scenarios to test clear after screen closed (i.e. when the ScopedViewModelContainer clears all ViewModels) //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `when I navigate to nested screen and back, then the Hilt scoped ViewModels of the second screen are cleared`() {
        // Given the starting screen with Hilt injected ViewModel scoped
        composeTestRule.activity.setContent {
            navController = rememberNavController()
            ScreensWithNavigation(navController = navController, startDestination = hiltViewModelScopedDestination)
        }
        printComposeUiTreeToLog()
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // When I navigate to a nested screen with a Hilt scoped ViewModel and back to initial screen
        navController.navigate(hiltViewModelScopedDestination)
        printComposeUiTreeToLog()
        navController.popBackStack()
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the Hilt scoped ViewModel from the second screen is cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 2) {
            "The amount of FakeInjectedViewModels that were cleared after back navigation ($finalAmountOfViewModelsCleared) " +
                    "was not as high as expected compared to the amount before navigating ($initialAmountOfViewModelsCleared)"
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Scenarios to test clear on owner Composable scope disposed (Composable not part of the composition anymore) //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `when the Composable that creates the Hilt ViewModel is disposed, then the scoped ViewModel is cleared`() = runTest {

        // Given the starting screen with a scoped Hilt ViewModel
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    DemoScopedHiltInjectedViewModelComposable()
                }
            }
        }
        printComposeUiTreeToLog()

        // When one Composable with a scoped ViewModel is not part of composition anymore and disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        printComposeUiTreeToLog() // Required to trigger recomposition
        onNodeWithTestTag("Hilt FakeInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
        advanceTimeBy(100) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped ViewModel is cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of FakeInjectedViewModel that were cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "was not higher that the amount before disposal ($initialAmountOfViewModelsCleared)"
        }
    }

    @Test
    fun `given two sibling Composables with two instances of the same Hilt ViewModel, when one Composable is disposed, the other ViewModel is NOT cleared`() =
        runTest {

            // Given the starting screen with two scoped ViewModels sharing the same ViewModel instance
            var composablesShown by mutableStateOf(true)
            val textTitle = "Test text"
            composeTestRule.activity.setContent {
                Column {
                    Text(textTitle)
                    DemoScopedHiltInjectedViewModelComposable()
                    if (composablesShown) {
                        DemoScopedHiltInjectedViewModelComposable()
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
    fun `given two sibling Composables with different Hilt ViewModels, when one Composable is disposed, then only one ViewModel is cleared`() =
        runTest {

            // Given the starting screen with two scoped ViewModels sharing the same ViewModel instance
            var composablesShown by mutableStateOf(true)
            val textTitle = "Test text"
            composeTestRule.activity.setContent {
                Column {
                    Text(textTitle)
                    DemoScopedHiltInjectedViewModelComposable()
                    if (composablesShown) {
                        DemoScopedSecondHiltInjectedViewModelComposable()
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
    fun `when the key associated with the Hilt ViewModel changes, then the old scoped ViewModel is cleared`() {

        // Given the starting screen with a Hilt scoped ViewModel
        var viewModelKey by mutableStateOf("initial key")
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            Column {
                Text(textTitle)
                DemoScopedHiltInjectedViewModelComposable(viewModelKey)
            }
        }
        printComposeUiTreeToLog()

        // When the key changes
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        viewModelKey = "new key" // Trigger disposal
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the Hilt scoped ViewModel is cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of FakeScopedViewModels that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                    "was not two numbers higher that the amount before the key change ($initialAmountOfViewModelsCleared)"
        }
    }
}