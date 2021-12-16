package com.sebaslogen.resacaapp

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ActivityScenario
import com.sebaslogen.resacaapp.ui.main.ComposeActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(instrumentedPackages = ["androidx.loader.content"])
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
                val initialFakeScopedRepoText = retrieveTextFromNodeWithContentDescription("FakeRepo Scoped")
                val initialFakeScopedViewModelText = retrieveTextFromNodeWithContentDescription("FakeScopedViewModel Scoped")

                // When recreate the activity
                activity.recreate()
                printComposeUiTreeToLog()

                // Then the scoped objects on the first screen are still the same
                onNodeWithContentDescription("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
                onNodeWithContentDescription("FakeScopedViewModel Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedViewModelText)
            }
        }
    }

    @Test
    fun `when the activity is recreated, then the not scoped object changes`() {
        ActivityScenario.launch(ComposeActivity::class.java).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->

                // Given the Activity shows a screen with scoped objects
                printComposeUiTreeToLog()
                // Find the NOT scoped text field and grab its text
                val initialFakeRepoText = retrieveTextFromNodeWithContentDescription("FakeRepo Not scoped")

                // When recreate the activity
                activity.recreate()
                printComposeUiTreeToLog()

                // Then the text of the not scoped object is different from the original one because it's a new object
                onNodeWithContentDescription("FakeRepo Not scoped").assertIsDisplayed().assert(hasTextExactly(initialFakeRepoText).not())
            }
        }
    }
}