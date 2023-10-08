package com.sebaslogen.resacaapp.sample.hilt

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.sebaslogen.resaca.COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.hiltSingleViewModelScopedDestination
import com.sebaslogen.resacaapp.sample.ui.main.hiltViewModelScopedDestination
import com.sebaslogen.resacaapp.sample.ui.main.showSingleScopedViewModel
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ComposeActivityRecreationTests : ComposeTestUtils {

    private lateinit var scenario: ActivityScenario<ComposeActivity>

    @get:Rule
    override val composeTestRule = createEmptyComposeRule()

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(
            Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
                putExtra(ComposeActivity.START_DESTINATION, hiltViewModelScopedDestination)
            })
    }

    @Test
    fun whenISwitchFromLightModeToNightMode_thenTheOneAndOnlyHiltScopedViewModelThatSOnlyUsedInLightModeIsGoneAndTheRestStay() {
        // Given the starting screen with Hilt ViewModel scoped that is ONLY shown in light mode
        composeTestRule.waitForIdle()
        // Find the scoped text fields and grab their texts
        val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")
        retrieveTextFromNodeWithTestTag("Hilt FakeInjectedViewModel Scoped")
        printComposeUiTreeToLog()

        // When I change to night mode and apply the configuration change by recreating the Activity
        showSingleScopedViewModel = false // This is a fake night-mode change but it will remove Composable after Activity re-creation
        scenario.recreate()
        printComposeUiTreeToLog()

        // Then the scoped object is still the same but the Hilt Injected ViewModel disappears
        // Then the text of the NOT scoped object is different from the original one because it's a new object
        onNodeWithTestTag("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
        onNodeWithTestTag("Hilt FakeInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
    }

    @Test
    fun whenTheActivityIsRecreated_thenTheScopedObjectAndHiltInjectedScopedViewModelRemainTheSame() {
        // Given the starting screen with Hilt injected ViewModel scoped
        composeTestRule.waitForIdle()
        // Find the scoped text fields and grab their texts
        val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")
        val initialHiltFakeScopedViewModelText = retrieveTextFromNodeWithTestTag("Hilt FakeSecondInjectedViewModel Scoped")
        printComposeUiTreeToLog()

        // When I change to night mode and apply the configuration change by recreating the Activity
        showSingleScopedViewModel = false // This is a fake night-mode change but it will remove Composable after Activity re-creation
        scenario.recreate()
        printComposeUiTreeToLog()

        // Then the scoped objects are still the same
        onNodeWithTestTag("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
        onNodeWithTestTag("Hilt FakeSecondInjectedViewModel Scoped").assertIsDisplayed().assertTextEquals(initialHiltFakeScopedViewModelText)
    }

    @Test
    fun whenISwitchFromLightModeToNightMode_thenTheOneAndOnlyHiltScopedViewModelThatSOnlyUsedInLightModeIsGone() {
        // Given the starting screen with ViewModel scoped that is ONLY shown in light mode
        scenario = ActivityScenario.launch(
            Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
                putExtra(ComposeActivity.START_DESTINATION, hiltSingleViewModelScopedDestination)
            })
        scenario.recreate()
        composeTestRule.waitForIdle()
        // Find the scoped text fields and grab their texts
        retrieveTextFromNodeWithTestTag("Hilt FakeInjectedViewModel Scoped")
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        printComposeUiTreeToLog()

        // When I change to night mode and apply the configuration change by recreating the Activity
        showSingleScopedViewModel = false // This is a fake night-mode change but it will remove Composable after Activity re-creation
        scenario.recreate()
        printComposeUiTreeToLog()
        Thread.sleep(COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS * 1000) // Wait for the ViewModel to be cleared
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped ViewModel disappears
        onNodeWithTestTag("Hilt FakeInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of FakeInjectedViewModel that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                    "was not higher that the amount before the key change ($initialAmountOfViewModelsCleared)"
        }
    }
}