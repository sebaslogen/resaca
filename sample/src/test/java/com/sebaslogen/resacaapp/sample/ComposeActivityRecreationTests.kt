package com.sebaslogen.resacaapp.sample

import android.content.Intent
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity.Companion.START_DESTINATION
import com.sebaslogen.resacaapp.sample.ui.main.rememberScopedDestination
import com.sebaslogen.resacaapp.sample.ui.main.showSingleScopedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.viewModelScopedDestination
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@OptIn(ResacaPackagePrivate::class)
@RunWith(AndroidJUnit4::class)
class ComposeActivityRecreationTests : ComposeTestUtils {
    init {
        callFromTestInit()
    }

    override fun callFromTestInit() {
        ComposeActivity.defaultDestination = rememberScopedDestination // This is needed to reset the destination to the default one on the release app
    }

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

    @Test
    fun `when I switch from light mode to night mode, then the one and only scoped ViewModel that's only used in light mode is gone`() {
        // Given the starting screen with ViewModel scoped that is ONLY shown in light mode
        showSingleScopedViewModel = true
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(START_DESTINATION, viewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                // Find the scoped text fields and grab their texts
                retrieveTextFromNodeWithTestTag("FakeScopedViewModel Scoped")
                val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
                printComposeUiTreeToLog()

                // When I change to night mode and apply the configuration change by recreating the Activity
                showSingleScopedViewModel = false // This is a fake night-mode change but it will remove Composable after Activity re-creation
                activity.recreate()
                printComposeUiTreeToLog()
                Thread.sleep(COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS * 2 * 1000) // Wait for the ViewModel to be cleared
                printComposeUiTreeToLog() // Second print is needed to push the main thread forward
                val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

                // Then the scoped ViewModel disappears
                onNodeWithTestTag("FakeScopedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
                assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
                    "The amount of FakeScopedViewModel that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                        "was not higher that the amount before the key change ($initialAmountOfViewModelsCleared)"
                }
            }
        }
    }

    @Test
    fun `given Activity is recreated while navigating to a new destination, when I navigate back then the scoped ViewModel is not cleared`() {
        // Given the starting screen with a single ViewModel scoped
        showSingleScopedViewModel = true
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(START_DESTINATION, viewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                // Find the scoped text fields and grab their texts
                val initialFakeScopedViewModelText = retrieveTextFromNodeWithTestTag("FakeScopedViewModel Scoped")
                val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
                printComposeUiTreeToLog()

                // When I change to night mode and apply the configuration change by recreating the Activity
                onNodeWithTestTag("Navigate to ViewModelScoped D&N").performClick()
                activity.recreate()
                printComposeUiTreeToLog()
                Thread.sleep(COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS * 2 * 1000) // Wait for the ViewModel to be cleared
                printComposeUiTreeToLog() // Second print is needed to push the main thread forward

                // And then I navigate back to the first screen
                onNodeWithTestTag("Back").performClick()
                printComposeUiTreeToLog() // Second print is needed to push the main thread forward
                val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

                // Then the scoped ViewModel disappears

                onNodeWithTestTag("FakeScopedViewModel Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedViewModelText)
                assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
                    "The amount of FakeScopedViewModel that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                        "was not higher that the amount before the key change ($initialAmountOfViewModelsCleared)"
                }
            }
        }
    }
}