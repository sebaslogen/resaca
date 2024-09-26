package com.sebaslogen.resacaapp.sample

import android.content.Intent
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.ScreensWithNavigation
import com.sebaslogen.resacaapp.sample.ui.main.rememberScopedDestination
import com.sebaslogen.resacaapp.sample.ui.main.viewModelScopedDestination
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

    @Test
    fun `when I navigate to nested screen, produce an activity recreation and navigate back, then the scoped objects are the same`() {
        // Given the starting screen with ViewModel scoped
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(ComposeActivity.START_DESTINATION, viewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                // Find the scoped text fields and grab their texts
                val initialFakeScopedViewModelText = retrieveTextFromNodeWithTestTag("FakeScopedViewModel Scoped")
                printComposeUiTreeToLog()

                // When I navigate to a second screen I apply the configuration change by recreating the Activity
                onNodeWithTestTag("Navigate to rememberScoped").performClick()
                printComposeUiTreeToLog()
                val newScenario: ActivityScenario<ComposeActivity> = scenario.recreate()
                Thread.sleep(COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS * 2 * 1000) // Wait for the ViewModel to be cleared
                printComposeUiTreeToLog() // Print is needed to push the main thread forward

                // And then I navigate back to the first screen
                newScenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
                printComposeUiTreeToLog() // Print is needed to push the main thread forward

                // Then the scoped ViewModel is still the same
                onNodeWithTestTag("FakeScopedViewModel Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedViewModelText)
            }
        }
    }
}