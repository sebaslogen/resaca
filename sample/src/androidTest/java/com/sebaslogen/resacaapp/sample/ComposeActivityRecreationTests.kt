package com.sebaslogen.resacaapp.sample

import android.content.Intent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.sebaslogen.resaca.COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.showSingleScopedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.viewModelScopedDestination
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
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
        showSingleScopedViewModel = null
        scenario = ActivityScenario.launch(
            Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
                putExtra(ComposeActivity.START_DESTINATION, viewModelScopedDestination)
            })
    }

    @Test
    fun whenActivityRecreates_thenTheOneAndOnlyScopedViewModelThatSOnlyUsedInLightModeIsGone() {
        // Given the starting screen with ViewModel scoped that is ONLY shown in light mode
        composeTestRule.waitForIdle()
        // Find the scoped text fields and grab their texts
        retrieveTextFromNodeWithTestTag("FakeScopedViewModel Scoped")
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        printComposeUiTreeToLog()

        // When I change to night mode and apply the configuration change by recreating the Activity
        showSingleScopedViewModel = false // This is a fake night-mode change but it will remove Composable after Activity re-creation
        scenario.recreate()
        printComposeUiTreeToLog()
        Thread.sleep(COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS * 1000) // Wait for the ViewModel to be cleared
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped ViewModel disappears
        onNodeWithTestTag("FakeInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of FakeInjectedViewModel that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                "was not higher that the amount before the key change ($initialAmountOfViewModelsCleared)"
        }
    }
}