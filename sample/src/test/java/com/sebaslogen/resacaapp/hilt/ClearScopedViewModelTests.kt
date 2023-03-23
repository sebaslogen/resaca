package com.sebaslogen.resacaapp.hilt

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
import com.sebaslogen.resacaapp.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.ui.main.ScreensWithNavigation
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedInjectedViewModelComposable
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedSecondInjectedViewModelComposable
import com.sebaslogen.resacaapp.ui.main.hiltViewModelScopedDestination
import com.sebaslogen.resacaapp.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.utils.MainDispatcherRule
import com.sebaslogen.resacaapp.viewModelsClearedGloballySharedCounter
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
                    "was not higher that the amount before navigating ($initialAmountOfViewModelsCleared)"
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
                    DemoScopedInjectedViewModelComposable()
                }
            }
        }
        printComposeUiTreeToLog()

        // When one Composable with a scoped ViewModel is not part of composition anymore and disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        printComposeUiTreeToLog() // Required to trigger recomposition
        onNodeWithTestTag("Hilt FakeInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
        advanceTimeBy(6000) // Advance more than 5 seconds to pass the disposal delay on ScopedViewModelContainer
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
                    DemoScopedInjectedViewModelComposable()
                    if (composablesShown) {
                        DemoScopedSecondInjectedViewModelComposable()
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
                DemoScopedInjectedViewModelComposable(viewModelKey)
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


    @Test
    fun `when I switch from light mode to night mode, then the one and only scoped ViewModel that's only used in light mode is gone`() = runTest {
        // Given the starting screen with ViewModel scoped that is ONLY shown in light mode
        composeTestRule.activity.setContent {
            Text("Demo text")
            if (!isSystemInDarkTheme()) {
                DemoScopedInjectedViewModelComposable()
            }
        }
        printComposeUiTreeToLog()
        // Find the scoped text fields and grab their texts
        retrieveTextFromNodeWithTestTag("Hilt FakeInjectedViewModel Scoped")
        advanceTimeBy(1000) // Give time to the ObserveLifecycleWithScopedViewModelContainer to execute lifecycle.addObserver on main thread

        // When I change to night mode and apply the configuration change by recreating the Activity
        RuntimeEnvironment.setQualifiers("+night") // This triggers activity re-creation
        composeTestRule.activity.setContent { // Almost empty screen in night mode
            Text("Demo text")
            if (!isSystemInDarkTheme()) {
                DemoScopedInjectedViewModelComposable()
            }
        }
        printComposeUiTreeToLog()

        // When one Composable with a scoped ViewModel is not part of composition anymore and disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        advanceTimeBy(6000) // Advance more than 5 seconds to pass the disposal delay on ScopedViewModelContainer
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the Hilt Injected ViewModel disappears because it was only available in light mode
        onNodeWithTestTag("Hilt FakeInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
        // And the scoped ViewModel is cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of FakeInjectedViewModel that were cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "was not higher that the amount before disposal ($initialAmountOfViewModelsCleared)"
        }
    }
}