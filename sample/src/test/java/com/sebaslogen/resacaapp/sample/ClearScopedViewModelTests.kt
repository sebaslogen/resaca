package com.sebaslogen.resacaapp.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.sample.ui.main.ScreensWithNavigation
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedParametrizedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeScopedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.rememberScopedDestination
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.utils.MainDispatcherRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
    override val composeTestRule = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var navController: NavHostController


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Scenarios to test clear after screen closed (i.e. when the ScopedViewModelContainer clears all ViewModels) //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `when I navigate to nested screen and back, then the 2 scoped ViewModels of the second screen are cleared`() = runTest {

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
        withContext(Dispatchers.Main) {
            advanceTimeBy(100) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
        }
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped ViewModel from the second screen is cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 2) {
            "The amount of FakeScopedViewModels that were cleared after back navigation ($finalAmountOfViewModelsCleared) " +
                    "was not two numbers higher that the amount before navigating ($initialAmountOfViewModelsCleared)"
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
        advanceTimeBy(100) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then both scoped ViewModels are cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 2) {
            "The amount of FakeScopedViewModels that were cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "was not two numbers higher that the amount before the Composables were disposed ($initialAmountOfViewModelsCleared)"
        }
    }

    @Test
    fun `given two sibling Composables with the same ViewModel instance scoped to them, when one Composable is disposed, then the ViewModel is NOT cleared`() =
        runTest {

            // Given the starting screen with two scoped ViewModels sharing the same ViewModel instance
            var firstComposableShown by mutableStateOf(true)
            var secondComposableShown by mutableStateOf(true)
            val textTitle = "Test text"
            val viewModelInstance = FakeScopedViewModel(stateSaver = SavedStateHandle(mapOf(FakeScopedViewModel.MY_ARGS_KEY to 0)))
            composeTestRule.setContent {
                Column {
                    Text(textTitle)
                    if (firstComposableShown) {
                        DemoScopedParametrizedViewModelComposable(viewModelInstance)
                    }
                    if (secondComposableShown) {
                        DemoScopedParametrizedViewModelComposable(viewModelInstance)
                    }
                }
            }
            printComposeUiTreeToLog()

            // When one Composable with a scoped ViewModel is not part of composition anymore and disposed
            val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
            firstComposableShown = false // Trigger disposal
            composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
            advanceTimeBy(100) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
            printComposeUiTreeToLog()
            val firstAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

            // Then the scoped ViewModel is NOT cleared
            assert(firstAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
                "The amount of FakeScopedViewModels that were cleared after disposal ($firstAmountOfViewModelsCleared) " +
                        "is not the same as the initial the amount before the Composable was disposed ($initialAmountOfViewModelsCleared)"
            }

            // When one Composable with a scoped ViewModel is not part of composition anymore and disposed
            secondComposableShown = false // Trigger disposal
            composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
            advanceTimeBy(100) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
            printComposeUiTreeToLog()
            val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

            // Then the scoped ViewModel is NOT cleared
            assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
                "The amount of FakeScopedViewModels that were cleared after disposal ($finalAmountOfViewModelsCleared) " +
                        "is not the same as the initial the amount plus one (one cleared) before the Composable was disposed ($initialAmountOfViewModelsCleared)"
            }
        }

    /////////////////////////////////////////////////////
    // Scenarios to test clear on ViewModel key change //
    /////////////////////////////////////////////////////

    @Test
    fun `when the keys associated with the ViewModels change, then the old scoped ViewModels are cleared`() = runTest {

        // Given the starting screen with two scoped ViewModels
        var viewModelKey by mutableStateOf("initial key")
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                DemoScopedViewModelComposable(viewModelKey)
                DemoScopedParametrizedViewModelComposable(key = viewModelKey)
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

        // Then both old scoped ViewModels are cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 2) {
            "The amount of FakeScopedViewModels that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                    "was not two numbers higher that the amount before the key change ($initialAmountOfViewModelsCleared)"
        }
    }
}