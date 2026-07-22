package com.sebaslogen.resacaapp.sample

import android.content.Intent
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.sebaslogen.resaca.COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import com.sebaslogen.resacaapp.sample.ui.main.Nav3ViewModelsActivity
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Reproduces and guards against https://github.com/sebaslogen/resaca/issues/385:
 * `hiltViewModelScoped` (and, by extension, `viewModelScoped`/`rememberScoped`) must retain their
 * ViewModel while the requesting destination is paused but still present on the Nav3 back stack,
 * e.g. RouteB while RouteC (pushed on top of it) is visible.
 *
 * This is only guaranteed when `NavDisplay`'s `entryDecorators` include
 * `rememberViewModelStoreNavEntryDecorator()`. Without it, all destinations share a single
 * Activity-wide `ScopedViewModelContainer`, and its foreground/background tracking gets confused by
 * Nav3 giving every entry its own `LifecycleOwner`: the incoming entry's `ON_RESUME` races with the
 * outgoing entry's `ON_PAUSE` on the same shared state, so the outgoing entry's ViewModel is cleared
 * immediately instead of surviving on the back stack.
 */
@OptIn(ResacaPackagePrivate::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class ComposeNav3ViewModelRetentionTests : ComposeTestUtils {

    @get:Rule
    override val composeTestRule = createEmptyComposeRule()

    @Test
    fun whenViewModelStoreDecoratorIsPresent_thenTheScopedViewModelSurvivesOnTheBackStack() {
        ActivityScenario.launch<Nav3ViewModelsActivity>(
            Intent(ApplicationProvider.getApplicationContext(), Nav3ViewModelsActivity::class.java)
                .putExtra(Nav3ViewModelsActivity.INCLUDE_VIEW_MODEL_STORE_DECORATOR, true)
        ).use {
            // Given RouteB with its own hiltViewModelScoped ViewModel
            composeTestRule.waitForIdle()
            onNodeWithTestTag("Nav3 Button").performClick()
            composeTestRule.waitForIdle()
            val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

            // When I navigate to RouteC, which pushes RouteB down (but not off) the back stack
            onNodeWithTestTag("Nav3 RouteB To RouteC Button").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS * 2 * 1000) // Wait for any (incorrect) deferred disposal to run

            // Then RouteB's ViewModel must NOT have been cleared while it's merely paused on the back stack
            val amountOfViewModelsClearedWhileOnBackStack = viewModelsClearedGloballySharedCounter.get()
            assert(amountOfViewModelsClearedWhileOnBackStack == initialAmountOfViewModelsCleared) {
                "Expected no ViewModels to be cleared while RouteB is still on the back stack, " +
                        "but the cleared count went from $initialAmountOfViewModelsCleared to $amountOfViewModelsClearedWhileOnBackStack"
            }

            // And when I navigate back to RouteB, which pops RouteC off the back stack for good
            onNodeWithTestTag("Nav3 RouteC Back Button").performClick()
            composeTestRule.waitForIdle()

            // Then the same RouteB ViewModel instance is shown.
            // Only RouteC's own ViewModel is cleared at this point (correctly, since RouteC left the back stack for good),
            // RouteB's ViewModel (the one under test) must not add to that count.
            assert(retrieveTextFromNodeWithTestTag("Nav3 Text") == "Route B id: 123 ")
            assert(viewModelsClearedGloballySharedCounter.get() == initialAmountOfViewModelsCleared + 1)
        }
    }

    @Test
    fun whenViewModelStoreDecoratorIsMissing_thenTheScopedViewModelIsClearedWhileStillOnTheBackStack() {
        ActivityScenario.launch<Nav3ViewModelsActivity>(
            Intent(ApplicationProvider.getApplicationContext(), Nav3ViewModelsActivity::class.java)
                .putExtra(Nav3ViewModelsActivity.INCLUDE_VIEW_MODEL_STORE_DECORATOR, false)
        ).use {
            // Given RouteB with its own hiltViewModelScoped ViewModel
            composeTestRule.waitForIdle()
            onNodeWithTestTag("Nav3 Button").performClick()
            composeTestRule.waitForIdle()
            val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

            // When I navigate to RouteC, which pushes RouteB down (but not off) the back stack
            onNodeWithTestTag("Nav3 RouteB To RouteC Button").performClick()
            composeTestRule.waitForIdle()
            Thread.sleep(COMPOSITION_RESUMED_TIMEOUT_IN_SECONDS * 2 * 1000) // Wait for the deferred disposal to run

            // Then RouteB's ViewModel is incorrectly cleared, even though RouteB is still on the back stack.
            // This is the bug reported in https://github.com/sebaslogen/resaca/issues/385
            val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
            assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
                "Expected RouteB's ViewModel to be incorrectly cleared (reproducing issue #385) when " +
                        "rememberViewModelStoreNavEntryDecorator() is missing from entryDecorators, but the cleared " +
                        "count went from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
            }
        }
    }
}
