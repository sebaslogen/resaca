package com.sebaslogen.resacaapp

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.ui.main.ScreensWithNavigation
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedParametrizedViewModelComposable
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedViewModelComposable
import com.sebaslogen.resacaapp.ui.main.data.FakeScopedViewModel
import com.sebaslogen.resacaapp.ui.main.hiltViewModelScopedDestination
import com.sebaslogen.resacaapp.ui.main.rememberScopedDestination
import com.sebaslogen.resacaapp.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ClearScopedViewModelTests : ComposeTestUtils {

    @get:Rule
    override val composeTestRule = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    lateinit var navController: NavHostController


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Scenarios to test clear after screen closed (i.e. when the ScopedViewModelContainer clears all ViewModels) //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `when I navigate to nested screen and back, then the 2 scoped ViewModels of the second screen are cleared`() {

        // Given the starting screen with scoped ViewModels
        composeTestRule.setContent {
            navController = rememberNavController()
            ScreensWithNavigation(navController = navController)
        }
        printComposeUiTreeToLog()

        // When I navigate to a nested screen with a scoped ViewModel and back to initial screen
        navController.navigate(rememberScopedDestination)
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        printComposeUiTreeToLog()
        navController.popBackStack()
        printComposeUiTreeToLog() // This seems to be needed to trigger recomposition
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped ViewModel from the second screen is cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 2) {
            "The amount of FakeScopedViewModels that where cleared after back navigation ($finalAmountOfViewModelsCleared) " +
                    "was not two numbers higher that the amount before navigating ($initialAmountOfViewModelsCleared)"
        }
    }

    @Test
    fun `when I navigate to nested screen and back, then the Hilt scoped ViewModel of the second screen is cleared`() {

        // Given the starting screen with Hilt injected ViewModel scoped
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(ComposeActivity.START_DESTINATION, hiltViewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

                // When I navigate to a nested screen with a Hilt scoped ViewModel and back to initial screen
                onNodeWithTestTag("hiltViewModelScopedDestinationTestTag").performClick()
                printComposeUiTreeToLog()
                activity.onBackPressed()
                printComposeUiTreeToLog()
                val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

                // Then the Hilt scoped ViewModel from the second screen is cleared
                assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
                    "The amount of FakeInjectedViewModels that where cleared after back navigation ($finalAmountOfViewModelsCleared) " +
                            "was not two numbers higher that the amount before navigating ($initialAmountOfViewModelsCleared)"
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Scenarios to test clear on owner Composable scope disposed (Composable not part of the composition anymore) //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `when the Composable that creates the ViewModel is disposed, then the scoped ViewModel is cleared`() = runTest {

        // Given the starting screen with two scoped ViewModels
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    DemoScopedViewModelComposable()
                    DemoScopedParametrizedViewModelComposable()
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composables with scoped ViewModels are not part of composition anymore and disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(6000) // Advance more than 5 seconds to pass the disposal delay on ScopedViewModelContainer
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then both scoped ViewModels are cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 2) {
            "The amount of FakeScopedViewModels that where cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "was not two numbers higher that the amount before the Composables were disposed ($initialAmountOfViewModelsCleared)"
        }
    }


    @Test
    fun `given two sibling Composables with the same ViewModel instance scoped to them, when one Composable is disposed, then the ViewModel is NOT cleared`() =
        runTest {

            // Given the starting screen with two scoped ViewModels sharing the same ViewModel instance
            var composablesShown by mutableStateOf(true)
            val textTitle = "Test text"
            val viewModelInstance = FakeScopedViewModel(viewModelsClearedGloballySharedCounter)
            composeTestRule.setContent {
                Column {
                    Text(textTitle)
                    DemoScopedParametrizedViewModelComposable(viewModelInstance)
                    if (composablesShown) {
                        DemoScopedParametrizedViewModelComposable(viewModelInstance)
                    }
                }
            }
            printComposeUiTreeToLog()

            // When one Composables with a scoped ViewModel is not part of composition anymore and disposed
            val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
            composablesShown = false // Trigger disposal
            composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
            advanceTimeBy(6000) // Advance more than 5 seconds to pass the disposal delay on ScopedViewModelContainer
            printComposeUiTreeToLog()
            val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

            // Then the scoped ViewModels is NOT cleared
            assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
                "The amount of FakeScopedViewModels that where cleared after disposal ($finalAmountOfViewModelsCleared) " +
                        "0, the initial the amount before the Composables were disposed ($initialAmountOfViewModelsCleared)"
            }
        }


    @Test
    fun `when two Composables request the same Hilt ViewModel, then both Composables get the same ViewModel instance`() {

    }
}