package com.sebaslogen.resacaapp.sample.metro

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.sebaslogen.resaca.COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.metroSingleViewModelScopedDestination
import com.sebaslogen.resacaapp.sample.ui.main.metroViewModelScopedDestination
import com.sebaslogen.resacaapp.sample.ui.main.showSingleScopedViewModel
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ResacaPackagePrivate::class)
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
                putExtra(ComposeActivity.START_DESTINATION, metroViewModelScopedDestination)
            })
    }

    @Test
    fun whenISwitchFromLightModeToNightMode_thenTheOneAndOnlyMetroScopedViewModelThatSOnlyUsedInLightModeIsGoneAndTheRestStay() {
        // Given the starting screen with Metro ViewModel scoped that is ONLY shown in light mode
        composeTestRule.waitForIdle()
        // Find the scoped text fields and grab their texts
        val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")
        retrieveTextFromNodeWithTestTag("Metro FakeMetroInjectedViewModel Scoped")
        printComposeUiTreeToLog()

        // When I change to night mode and apply the configuration change by recreating the Activity
        showSingleScopedViewModel = false // This is a fake night-mode change but it will remove Composable after Activity re-creation
        scenario.recreate()
        printComposeUiTreeToLog()

        // Then the scoped object is still the same but the Metro Injected ViewModel disappears
        // Then the text of the NOT scoped object is different from the original one because it's a new object
        onNodeWithTestTag("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
        onNodeWithTestTag("Metro FakeMetroInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
    }

    @Test
    fun whenTheActivityIsRecreated_thenTheScopedObjectAndMetroInjectedScopedViewModelRemainTheSame() {
        // Given the starting screen with Metro injected ViewModel scoped
        composeTestRule.waitForIdle()
        // Find the scoped text fields and grab their texts
        val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")
        composeTestRule.onNodeWithTag("Metro FakeMetroSecondInjectedViewModel Scoped").performScrollTo() // Scroll to the node that might be off-screen
        val initialMetroFakeScopedViewModelText = retrieveTextFromNodeWithTestTag("Metro FakeMetroSecondInjectedViewModel Scoped")
        printComposeUiTreeToLog()

        // When I change to night mode and apply the configuration change by recreating the Activity
        showSingleScopedViewModel = false // This is a fake night-mode change but it will remove Composable after Activity re-creation
        scenario.recreate()
        printComposeUiTreeToLog()

        // Then the scoped objects are still the same
        composeTestRule.onNodeWithTag("Metro FakeMetroSecondInjectedViewModel Scoped").performScrollTo() // Scroll to the node that might be off-screen
        onNodeWithTestTag("FakeRepo Scoped", assertDisplayed = false) // After scrolling down, FakeRepo might be off-screen now
            .assertTextEquals(initialFakeScopedRepoText)
        onNodeWithTestTag("Metro FakeMetroSecondInjectedViewModel Scoped").assertIsDisplayed().assertTextEquals(initialMetroFakeScopedViewModelText)
    }

    @Test
    fun whenISwitchFromLightModeToNightMode_thenTheOneAndOnlyMetroScopedViewModelThatSOnlyUsedInLightModeIsGone() {
        // Given the starting screen with ViewModel scoped that is ONLY shown in light mode
        scenario = ActivityScenario.launch(
            Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
                putExtra(ComposeActivity.START_DESTINATION, metroSingleViewModelScopedDestination)
            })
        scenario.recreate()
        composeTestRule.waitForIdle()
        // Find the scoped text fields and grab their texts
        retrieveTextFromNodeWithTestTag("Metro FakeMetroInjectedViewModel Scoped")
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        printComposeUiTreeToLog()

        // When I change to night mode and apply the configuration change by recreating the Activity
        showSingleScopedViewModel = false // This is a fake night-mode change but it will remove Composable after Activity re-creation
        scenario.recreate()
        printComposeUiTreeToLog()
        Thread.sleep(COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS * 2 * 1000) // Wait for the ViewModel to be cleared
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped ViewModel disappears
        onNodeWithTestTag("Metro FakeMetroInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of FakeMetroInjectedViewModel that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                    "was not higher that the amount before the key change ($initialAmountOfViewModelsCleared)"
        }
    }
}
