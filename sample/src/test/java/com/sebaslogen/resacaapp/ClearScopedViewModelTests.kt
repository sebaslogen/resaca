package com.sebaslogen.resacaapp

import android.content.Intent
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.ui.main.ScreensWithNavigation
import com.sebaslogen.resacaapp.ui.main.hiltViewModelScopedDestination
import com.sebaslogen.resacaapp.ui.main.rememberScopedDestination
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClearScopedViewModelTests : ComposeTestUtils {

    @get:Rule
    override val composeTestRule = createComposeRule()

    lateinit var navController: NavHostController

    @Test
    fun `when I navigate to nested screen and back, then the 2 scoped ViewModels of the second screen are cleared`() {

        // Given the starting screen with scoped objects
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
            "The amount of FakeScopedViewModel(s) that where cleared after back navigation ($finalAmountOfViewModelsCleared) " +
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
                    "The amount of FakeInjectedViewModel(s) that where cleared after back navigation ($finalAmountOfViewModelsCleared) " +
                            "was not two numbers higher that the amount before navigating ($initialAmountOfViewModelsCleared)"
                }
            }
        }
    }
}