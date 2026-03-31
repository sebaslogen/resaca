package com.sebaslogen.resacaapp.sample.metro

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity.Companion.START_DESTINATION
import com.sebaslogen.resacaapp.sample.ui.main.metroSingleViewModelScopedDestination
import com.sebaslogen.resacaapp.sample.ui.main.metroViewModelScopedDestination
import com.sebaslogen.resacaapp.sample.ui.main.showSingleScopedViewModel
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
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
        preTestInitializationToEmptyComposeDestination()
    }

    @get:Rule
    override val composeTestRule = createComposeRule()

    @Test
    fun `when I switch from light mode to night mode, then the Metro injected scoped ViewModel that's only used in light mode is gone`() {
        // Given the starting screen with Metro ViewModel scoped that is ONLY shown in light mode
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(START_DESTINATION, metroViewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                // Find the scoped text fields and grab their texts
                val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")
                retrieveTextFromNodeWithTestTag("Metro FakeMetroInjectedViewModel Scoped")
                printComposeUiTreeToLog()

                // When I change to night mode and apply the configuration change by recreating the Activity
                showSingleScopedViewModel = false // This is a fake night-mode change but it will remove Composable after Activity re-creation
                activity.recreate()
                printComposeUiTreeToLog()

                // Then the scoped object is still the same but the Metro Injected ViewModel disappears
                onNodeWithTestTag("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
                onNodeWithTestTag("Metro FakeMetroInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
            }
        }
    }

    @Test
    fun `when the activity is recreated, then the scoped object and Metro injected scoped ViewModel remain the same`() {

        // Given the starting screen with Metro injected ViewModel scoped
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(START_DESTINATION, metroViewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                printComposeUiTreeToLog()
                // Find the scoped text fields and grab their texts
                val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")
                val initialMetroFakeScopedViewModelText = retrieveTextFromNodeWithTestTag("Metro FakeMetroInjectedViewModel Scoped")

                // When we recreate the activity
                activity.recreate()
                printComposeUiTreeToLog()

                // Then the scoped objects are still the same
                onNodeWithTestTag("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
                onNodeWithTestTag("Metro FakeMetroInjectedViewModel Scoped").assertIsDisplayed()
                    .assertTextEquals(initialMetroFakeScopedViewModelText)
            }
        }
    }

    @Test
    fun `when I switch from light mode to night mode, then the one and only Metro scoped ViewModel that's only used in light mode is gone`() {
        // Given the starting screen with ViewModel scoped that is ONLY shown in light mode
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(START_DESTINATION, metroSingleViewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                // Find the scoped text fields and grab their texts
                retrieveTextFromNodeWithTestTag("Metro FakeMetroInjectedViewModel Scoped")
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
                onNodeWithTestTag("Metro FakeMetroInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
                assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
                    "The amount of FakeMetroInjectedViewModel that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                        "was not higher that the amount before the key change ($initialAmountOfViewModelsCleared)"
                }
            }
        }
    }

    @Test
    fun `given Activity is recreated while navigating to a new destination, when I navigate back then the Metro scoped ViewModel is not cleared`() {
        // Given the starting screen with a single ViewModel scoped
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(START_DESTINATION, metroSingleViewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                // Find the scoped text fields and grab their texts
                val initialFakeInjectedViewModelText = retrieveTextFromNodeWithTestTag("Metro FakeMetroInjectedViewModel Scoped")
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

                // Then the scoped ViewModel persists
                onNodeWithTestTag("Metro FakeMetroInjectedViewModel Scoped").assertIsDisplayed()
                    .assertTextEquals(initialFakeInjectedViewModelText)
                assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
                    "The amount of FakeMetroInjectedViewModel that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                        "was not higher that the amount before the key change ($initialAmountOfViewModelsCleared)"
                }
            }
        }
    }
}
