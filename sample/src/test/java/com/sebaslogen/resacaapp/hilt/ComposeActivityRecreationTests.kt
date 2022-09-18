package com.sebaslogen.resacaapp.hilt

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.ui.main.ComposeActivity.Companion.START_DESTINATION
import com.sebaslogen.resacaapp.ui.main.hiltViewModelScopedDestination
import com.sebaslogen.resacaapp.utils.ComposeTestUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(AndroidJUnit4::class)
class ComposeActivityRecreationTests : ComposeTestUtils {

    @get:Rule
    override val composeTestRule = createComposeRule()

    @Test
    fun `when I switch from light mode to night mode, then the Hilt injected scoped ViewModel that's only used in light mode is gone`() {

        // Given the starting screen with Hilt injected ViewModel scoped that is ONLY shown in light mode
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(START_DESTINATION, hiltViewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                printComposeUiTreeToLog()
                // Find the scoped text fields and grab their texts
                val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")
                retrieveTextFromNodeWithTestTag("Hilt FakeInjectedViewModel Scoped")

                // When I change to night mode and apply the configuration change by recreating the Activity
                RuntimeEnvironment.setQualifiers("+night")
                activity.recreate()
                printComposeUiTreeToLog()

                // Then the scoped object is still the same but the Hilt Injected ViewModel disappears
                // Then the text of the NOT scoped object is different from the original one because it's a new object
                onNodeWithTestTag("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
                onNodeWithTestTag("Hilt FakeInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
            }
        }
    }

    @Test
    fun `when the activity is recreated, then the scoped object and Hilt injected scoped ViewModel remain the same`() {

        // Given the starting screen with Hilt injected ViewModel scoped
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(START_DESTINATION, hiltViewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                printComposeUiTreeToLog()
                // Find the scoped text fields and grab their texts
                val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")
                val initialHiltFakeScopedViewModelText = retrieveTextFromNodeWithTestTag("Hilt FakeInjectedViewModel Scoped")

                // When we recreate the activity
                activity.recreate()
                printComposeUiTreeToLog()

                // Then the scoped objects are still the same
                onNodeWithTestTag("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
                onNodeWithTestTag("Hilt FakeInjectedViewModel Scoped").assertIsDisplayed().assertTextEquals(initialHiltFakeScopedViewModelText)
            }
        }
    }
}