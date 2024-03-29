package com.sebaslogen.resacaapp.sample

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.sample.ui.main.ScreensWithNavigation
import com.sebaslogen.resacaapp.sample.ui.main.rememberScopedDestination
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ComposeNavigationTests : ComposeTestUtils {
    init {
        callFromTestInit()
    }

    override fun callFromTestInit() {
        preTestInitializationToEmptyComposeDestination()
    }

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
        val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")
        val initialFakeScopedViewModelText = retrieveTextFromNodeWithTestTag("FakeScopedViewModel Scoped")
        val initialFakeScopedParametrizedViewModelText = retrieveTextFromNodeWithTestTag(tag = "FakeScopedParametrizedViewModel Scoped")

        // When I navigate to a nested screen and back to initial screen
        navController.navigate(rememberScopedDestination)
        printComposeUiTreeToLog()
        navController.popBackStack()
        printComposeUiTreeToLog()

        // Then the scoped objects on the first screen are still the same
        onNodeWithTestTag("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
        onNodeWithTestTag("FakeScopedViewModel Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedViewModelText)
        onNodeWithTestTag("FakeScopedParametrizedViewModel Scoped").assertTextEquals(initialFakeScopedParametrizedViewModelText)
    }

    @Test
    fun `when I navigate to nested screen and back, then the NOT scoped object changes`() {

        // Given the starting screen with scoped objects
        composeTestRule.setContent {
            navController = rememberNavController()
            ScreensWithNavigation(navController = navController)
        }
        printComposeUiTreeToLog()
        // Find the NOT scoped text field and grab its text
        val initialFakeRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Not scoped")

        // When I navigate to a nested screen and back to initial screen
        navController.navigate(rememberScopedDestination)
        printComposeUiTreeToLog()
        navController.popBackStack()
        printComposeUiTreeToLog()

        // Then the text of the NOT scoped object is different from the original one because it's a new object
        onNodeWithTestTag("FakeRepo Not scoped").assertIsDisplayed().assert(hasTextExactly(initialFakeRepoText).not())
    }
}