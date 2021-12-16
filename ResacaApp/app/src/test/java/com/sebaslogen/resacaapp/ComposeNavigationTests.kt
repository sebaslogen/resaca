package com.sebaslogen.resacaapp

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.sebaslogen.resacaapp.ui.main.ScreensWithNavigation
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
class ComposeNavigationTests : ComposeTestUtils {

    @get:Rule
    override val composeTestRule = createComposeRule()

    lateinit var navController: NavHostController

    @Test
    fun `when I navigate to nested screen and back, then the scoped objects are the same`() {

        // Given the starting screen with scoped objects
        composeTestRule.setContent {
            navController = rememberNavController()
            ScreensWithNavigation(navController = navController)
        }
        printComposeUiTreeToLog()
        // Find the scoped text fields and grab their texts
        val initialFakeScopedRepoText = retrieveTextFromNodeWithContentDescription("FakeRepo Scoped")
        val initialFakeScopedViewModelText = retrieveTextFromNodeWithContentDescription("FakeScopedViewModel Scoped")

        // When I navigate to a nested screen and back to initial screen
        navController.navigate("first")
        printComposeUiTreeToLog()
        navController.popBackStack()
        printComposeUiTreeToLog()

        // Then the scoped objects on the first screen are still the same
        onNodeWithContentDescription("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
        onNodeWithContentDescription("FakeScopedViewModel Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedViewModelText)
    }


    @Test
    fun `when I navigate to nested screen and back, then the not scoped object changes`() {

        // Given the starting screen with scoped objects
        composeTestRule.setContent {
            navController = rememberNavController()
            ScreensWithNavigation(navController = navController)
        }
        printComposeUiTreeToLog()
        // Find the NOT scoped text field and grab its text
        val initialFakeRepoText = retrieveTextFromNodeWithContentDescription("FakeRepo Not scoped")

        // When I navigate to a nested screen and back to initial screen
        navController.navigate("first")
        printComposeUiTreeToLog()
        navController.popBackStack()
        printComposeUiTreeToLog()

        // Then the text of the not scoped object is different from the original one because it's a new object
        onNodeWithContentDescription("FakeRepo Not scoped").assertIsDisplayed().assert(hasTextExactly(initialFakeRepoText).not())
    }
}