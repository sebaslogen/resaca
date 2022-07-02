package com.sebaslogen.resacaapp

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
import com.sebaslogen.resacaapp.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.ui.main.ScreensWithNavigation
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedInjectedViewModelComposable
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedParametrizedViewModelComposable
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedViewModelComposable
import com.sebaslogen.resacaapp.ui.main.data.FakeScopedViewModel
import com.sebaslogen.resacaapp.ui.main.hiltViewModelScopedDestination
import com.sebaslogen.resacaapp.ui.main.rememberScopedDestination
import com.sebaslogen.resacaapp.utils.MainDispatcherRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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
        composeTestRule.setContent {
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
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of FakeInjectedViewModels that where cleared after back navigation ($finalAmountOfViewModelsCleared) " +
                    "was not higher that the amount before navigating ($initialAmountOfViewModelsCleared)"
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Scenarios to test clear on owner Composable scope disposed (Composable not part of the composition anymore) //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `when the Composables that create the ViewModels are disposed, then the scoped ViewModels are cleared`() = runTest {

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
    fun `when the Composable that creates the Hilt ViewModel is disposed, then the scoped ViewModel is cleared`() = runTest {

        // Given the starting screen with a scoped Hilt ViewModel
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    DemoScopedInjectedViewModelComposable()
                }
            }
        }
        printComposeUiTreeToLog()

        // When one Composable with a scoped ViewModel is not part of composition anymore and disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(6000) // Advance more than 5 seconds to pass the disposal delay on ScopedViewModelContainer
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped ViewModel is cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of FakeInjectedViewModel that where cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "was not higher that the amount before disposal ($initialAmountOfViewModelsCleared)"
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

            // When one Composable with a scoped ViewModel is not part of composition anymore and disposed
            val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
            composablesShown = false // Trigger disposal
            composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
            advanceTimeBy(6000) // Advance more than 5 seconds to pass the disposal delay on ScopedViewModelContainer
            printComposeUiTreeToLog()
            val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

            // Then the scoped ViewModel is NOT cleared
            assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
                "The amount of FakeScopedViewModels that where cleared after disposal ($finalAmountOfViewModelsCleared) " +
                        "is not the same as the initial the amount before the Composable was disposed ($initialAmountOfViewModelsCleared)"
            }
        }


    @Test
    fun `given two sibling Composables with the same Hilt ViewModel instance, when one Composable is disposed, then the ViewModel is NOT cleared`() =
        runTest {

            // Given the starting screen with two scoped ViewModels sharing the same ViewModel instance
            var composablesShown by mutableStateOf(true)
            val textTitle = "Test text"
            composeTestRule.setContent {
                Column {
                    Text(textTitle)
                    DemoScopedInjectedViewModelComposable()
                    if (composablesShown) {
                        DemoScopedInjectedViewModelComposable()
                    }
                }
            }
            printComposeUiTreeToLog()

            // When one Composable with a scoped ViewModel is not part of composition anymore and disposed
            val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
            composablesShown = false // Trigger disposal
            composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
            advanceTimeBy(6000) // Advance more than 5 seconds to pass the disposal delay on ScopedViewModelContainer
            printComposeUiTreeToLog()
            val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

            // Then the scoped ViewModel is NOT cleared
            assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
                "The amount of FakeInjectedViewModel that where cleared after disposal ($finalAmountOfViewModelsCleared) " +
                        "is not the same as the initial the amount before the Composable was disposed ($initialAmountOfViewModelsCleared)"
            }
        }
}