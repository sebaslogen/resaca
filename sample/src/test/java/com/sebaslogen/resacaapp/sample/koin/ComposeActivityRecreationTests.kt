package com.sebaslogen.resacaapp.sample.koin

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity.Companion.START_DESTINATION
import com.sebaslogen.resacaapp.sample.ui.main.koinSingleViewModelScopedDestination
import com.sebaslogen.resacaapp.sample.ui.main.koinViewModelScopedDestination
import com.sebaslogen.resacaapp.sample.ui.main.showSingleScopedViewModel
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
    fun `when I switch from light mode to night mode, then the Koin injected scoped ViewModel that's only used in light mode is gone`() {
        // Given the starting screen with Koin ViewModel scoped that is ONLY shown in light mode
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(START_DESTINATION, koinViewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                // Find the scoped text fields and grab their texts
                val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")
                retrieveTextFromNodeWithTestTag("Koin FakeInjectedViewModel Scoped")
                printComposeUiTreeToLog()

                // When I change to night mode and apply the configuration change by recreating the Activity
                showSingleScopedViewModel = false // This is a fake night-mode change but it will remove Composable after Activity re-creation
                activity.recreate()
                printComposeUiTreeToLog()

                // Then the scoped object is still the same but the Koin Injected ViewModel disappears
                // Then the text of the NOT scoped object is different from the original one because it's a new object
                onNodeWithTestTag("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
                onNodeWithTestTag("Koin FakeInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
            }
        }
    }

    @Test
    fun `when the activity is recreated, then the scoped object and Koin injected scoped ViewModel remain the same`() {

        // Given the starting screen with Koin injected ViewModel scoped
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(START_DESTINATION, koinViewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                printComposeUiTreeToLog()
                // Find the scoped text fields and grab their texts
                val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")
                val initialKoinFakeScopedViewModelText = retrieveTextFromNodeWithTestTag("Koin FakeInjectedViewModel Scoped")

                // When we recreate the activity
                activity.recreate()
                printComposeUiTreeToLog()

                // Then the scoped objects are still the same
                onNodeWithTestTag("FakeRepo Scoped").assertIsDisplayed().assertTextEquals(initialFakeScopedRepoText)
                onNodeWithTestTag("Koin FakeInjectedViewModel Scoped").assertIsDisplayed().assertTextEquals(initialKoinFakeScopedViewModelText)
            }
        }
    }

    @Test
    fun `when I switch from light mode to night mode, then the one and only Koin scoped ViewModel that's only used in light mode is gone`() {
        // Given the starting screen with ViewModel scoped that is ONLY shown in light mode
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(START_DESTINATION, koinSingleViewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                // Find the scoped text fields and grab their texts
                retrieveTextFromNodeWithTestTag("Koin FakeInjectedViewModel Scoped")
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
                onNodeWithTestTag("Koin FakeInjectedViewModel Scoped", assertDisplayed = false).assertDoesNotExist()
                assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
                    "The amount of FakeInjectedViewModel that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                        "was not higher that the amount before the key change ($initialAmountOfViewModelsCleared)"
                }
            }
        }
    }

    @Test
    fun `given Activity is recreated while navigating to a new destination, when I navigate back then the Koin scoped ViewModel is not cleared`() {
        // Given the starting screen with a single ViewModel scoped
        val launchIntent = Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
            putExtra(START_DESTINATION, koinSingleViewModelScopedDestination)
        }
        ActivityScenario.launch<ComposeActivity>(launchIntent).use { scenario ->
            scenario.onActivity { activity: ComposeActivity ->
                // Find the scoped text fields and grab their texts
                val initialFakeInjectedViewModelText = retrieveTextFromNodeWithTestTag("Koin FakeInjectedViewModel Scoped")
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

                onNodeWithTestTag("Koin FakeInjectedViewModel Scoped").assertIsDisplayed().assertTextEquals(initialFakeInjectedViewModelText)
                assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
                    "The amount of FakeInjectedViewModel that were cleared after key change ($finalAmountOfViewModelsCleared) " +
                        "was not higher that the amount before the key change ($initialAmountOfViewModelsCleared)"
                }
            }
        }
    }
}