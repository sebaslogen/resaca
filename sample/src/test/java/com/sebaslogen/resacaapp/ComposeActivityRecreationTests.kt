package com.sebaslogen.resacaapp

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.ui.main.ComposeActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ComposeActivityRecreationTests : ComposeTestUtils {

    @get:Rule
    override val composeTestRule = createComposeRule()

    @Test
    fun `when the activity is recreated, then the scoped objects are the same`() {
        ActivityScenario.launch(ComposeActivity::class.java).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->

                // Given the Activity shows a screen with scoped objects
                printComposeUiTreeToLog()
                // Find the scoped text fields and grab their texts
                val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")
                val initialFakeScopedViewModelText = retrieveTextFromNodeWithTestTag("FakeScopedViewModel Scoped")

                // When we recreate the activity
                activity.recreate()
                printComposeUiTreeToLog()

                // Then the scoped objects on the first screen are still the same
                onNodeWithTestTag("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
                onNodeWithTestTag("FakeScopedViewModel Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedViewModelText)
            }
        }
    }

    @Test
    fun `when the activity is recreated, then the NOT scoped object changes`() {
        ActivityScenario.launch(ComposeActivity::class.java).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->

                // Given the Activity shows a screen with scoped objects
                printComposeUiTreeToLog()
                // Find the NOT scoped text field and grab its text
                val initialFakeRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Not scoped")

                // When we recreate the activity
                activity.recreate()
                printComposeUiTreeToLog()

                // Then the text of the NOT scoped object is different from the original one because it's a new object
                onNodeWithTestTag("FakeRepo Not scoped").assertIsDisplayed().assert(hasTextExactly(initialFakeRepoText).not())
            }
        }
    }
}