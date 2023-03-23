package com.sebaslogen.resacaapp

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.utils.ComposeTestUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ComposeFragmentBackstackNavigationTests : ComposeTestUtils {

    @get:Rule
    override val composeTestRule = createComposeRule()

    private val fragmentComposeContainerTag = "FragmentComposeContentTestTag"

    @Test
    fun `given MainActivity with Composables in a Fragment, when I navigate to nested Fragment and back, then the scoped objects are the same`() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity: MainActivity ->

                // Given the Activity shows a screen with scoped objects inside a Fragment
                printComposeUiTreeToLog(fragmentComposeContainerTag)
                // Find the scoped text fields and grab their texts
                val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag(tag = "FakeRepo Scoped", parentTestTag = fragmentComposeContainerTag)
                val initialFakeScopedViewModelText =
                    retrieveTextFromNodeWithTestTag(tag = "FakeScopedViewModel Scoped", parentTestTag = fragmentComposeContainerTag)

                // When I navigate to a nested fragment and back to initial screen
                activity.navigateToFragmentTwo()
                activity.onBackPressedDispatcher.onBackPressed()
                printComposeUiTreeToLog(fragmentComposeContainerTag)


                // Then the scoped objects on the first screen are still the same
                onNodeWithTestTag(tag = "FakeRepo Scoped", parentTestTag = fragmentComposeContainerTag)
                    .assertTextEquals(initialFakeScopedRepoText)
                onNodeWithTestTag(tag = "FakeScopedViewModel Scoped", parentTestTag = fragmentComposeContainerTag)
                    .assertTextEquals(initialFakeScopedViewModelText)
            }
        }
    }

    @Test
    fun `given MainActivity with Composables in a Fragment, when I navigate to nested Fragment and back, then the NOT scoped object changes`() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity: MainActivity ->

                // Given the Activity shows a screen with scoped objects
                printComposeUiTreeToLog(fragmentComposeContainerTag)
                // Find the NOT scoped text field and grab its text
                val initialFakeRepoText = retrieveTextFromNodeWithTestTag(tag = "FakeRepo Not scoped", parentTestTag = fragmentComposeContainerTag)

                // When I navigate to a nested fragment and back to initial screen
                activity.navigateToFragmentTwo()
                activity.onBackPressedDispatcher.onBackPressed()
                printComposeUiTreeToLog(fragmentComposeContainerTag)

                // Then the text of the NOT scoped object is different from the original one because it's a new object
                onNodeWithTestTag(tag = "FakeRepo Not scoped", parentTestTag = fragmentComposeContainerTag)
                    .assert(hasTextExactly(initialFakeRepoText).not())
            }
        }
    }
}