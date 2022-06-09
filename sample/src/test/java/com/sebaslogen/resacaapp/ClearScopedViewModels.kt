package com.sebaslogen.resacaapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.ui.main.ScreensWithNavigation
import com.sebaslogen.resacaapp.ui.main.rememberScopedDestination
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClearScopedViewModels : ComposeTestUtils {

    @get:Rule
    override val composeTestRule = createComposeRule()

    lateinit var navController: NavHostController

    @Test
    fun `when I navigate to nested screen and back, then the scoped ViewModel of the second screen is cleared`() {

        // Given the starting screen with scoped objects
        composeTestRule.setContent {
            navController = rememberNavController()
            ScreensWithNavigation(navController = navController)
        }
        printComposeUiTreeToLog()

        // When I navigate to a nested screen with a scoped ViewModel and back to initial screen
        navController.navigate(rememberScopedDestination)
        val initialAmountOfViewModelsCleared = viewModelsClearedCounter.get()
        printComposeUiTreeToLog()
        navController.popBackStack()
        printComposeUiTreeToLog() // This seems to be needed to trigger recomposition
        val finalAmountOfViewModelsCleared = viewModelsClearedCounter.get()

        // Then the scoped ViewModel from the second screen is cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of FakeScopedViewModel(s) that where cleared after back navigation ($finalAmountOfViewModelsCleared) " +
                    "was not one number higher that the amount before navigating ($initialAmountOfViewModelsCleared)"
        }
    }
}