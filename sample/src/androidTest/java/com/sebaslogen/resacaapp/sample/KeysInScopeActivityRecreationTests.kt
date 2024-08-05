package com.sebaslogen.resacaapp.sample

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.sebaslogen.resaca.core.COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.viewModelScopedWithKeysDestination
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class KeysInScopeActivityRecreationTests : ComposeTestUtils {

    private lateinit var scenario: ActivityScenario<ComposeActivity>

    @get:Rule
    override val composeTestRule = createEmptyComposeRule()

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(
            Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
                putExtra(ComposeActivity.START_DESTINATION, viewModelScopedWithKeysDestination)
            })
    }

    @Test
    fun givenAListOfKeysAndALazyListOfScopedViewModelsScrolledToTheMiddle_whenActivityRecreatesAndIScrollBackToTop_thenTheOriginalScopedViewModelIsStillPresent() {
        // Given the starting screen with ViewModels scoped
        composeTestRule.waitForIdle()
        // Find the scoped text fields and grab their texts
        val initialFakeScopedViewModelText = retrieveTextFromNodeWithTestTag("FakeScopedViewModel 1 Scoped")
        printComposeUiTreeToLog()
        // Scroll away from first item
        composeTestRule.onNodeWithTag("LazyList").performScrollToIndex(50)

        // When we trigger a configuration change by recreating the Activity and scroll back to the top
        scenario.recreate()
        printComposeUiTreeToLog()
        Thread.sleep(COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS * 1000) // Wait for the ViewModel to be cleared
        composeTestRule.onNodeWithTag("LazyList").performScrollToIndex(0)

        // Then the scoped ViewModel disappears
        onNodeWithTestTag("FakeScopedViewModel 1 Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedViewModelText)
    }
}