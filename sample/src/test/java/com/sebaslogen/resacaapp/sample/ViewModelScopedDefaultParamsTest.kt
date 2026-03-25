package com.sebaslogen.resacaapp.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.viewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeSimpleViewModel
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Tests for `viewModelScoped<T>()` with default parameters (no key, no clearDelay).
 * This exercises the overload at ScopedMemoizers.kt lines 133-148 where both
 * `key: Any? = null` and `clearDelay: Duration? = null` use their defaults.
 * It also transitively covers ScopedViewModelUtils.getOrBuildViewModel clearDelay = null (line 45).
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ViewModelScopedDefaultParamsTest : ComposeTestUtils {
    init {
        callFromTestInit()
    }

    override fun callFromTestInit() {
        preTestInitializationToEmptyComposeDestination()
    }

    @get:Rule
    override val composeTestRule = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `when composable with no-arg viewModelScoped is shown, a ViewModel is created`() {
        // Given a Composable that uses viewModelScoped with no arguments (default key and clearDelay)
        composeTestRule.setContent {
            Column {
                val simpleVM: FakeSimpleViewModel = viewModelScoped()
                DemoComposable(inputObject = simpleVM, objectType = "FakeSimpleViewModel", scoped = true)
            }
        }
        printComposeUiTreeToLog()

        // Then the ViewModel is created and displayed
        onNodeWithTestTag("FakeSimpleViewModel Scoped").assertExists()
    }

    @Test
    fun `when composable with no-arg viewModelScoped is disposed, the ViewModel IS cleared`() = runTest {
        // Given a Composable that uses viewModelScoped with no arguments
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    val simpleVM: FakeSimpleViewModel = viewModelScoped()
                    DemoComposable(inputObject = simpleVM, objectType = "FakeSimpleViewModel", scoped = true)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable is disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(100) // Advance time to allow clear call to be processed
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped ViewModel is cleared (no clearDelay means immediate disposal)
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of ViewModels cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "was not higher than the amount before disposal ($initialAmountOfViewModelsCleared)"
        }
    }
}
