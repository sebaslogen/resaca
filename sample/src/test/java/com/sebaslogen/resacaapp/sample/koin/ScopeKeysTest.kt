package com.sebaslogen.resacaapp.sample.koin

import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.koin.koinViewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedViewModel
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinContext
import org.koin.core.parameter.parametersOf


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ScopeKeysTest : ComposeTestUtils {
    init {
        callFromTestInit()
    }

    override fun callFromTestInit() {
        preTestInitializationToEmptyComposeDestination()
    }

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComposeActivity>()

    @Test
    fun `when the key used for koinViewModelScoped changes, then the scoped ViewModel is different`() {

        // Given the starting screen with scoped object that uses a key
        composeTestRule.activity.setContent {
            KoinContext {
                var myKey by remember { mutableStateOf(false) }
                val fakeInjectedViewModel: FakeInjectedViewModel =
                    koinViewModelScoped(
                        key = myKey,
                        defaultArguments = bundleOf(FakeInjectedViewModel.MY_ARGS_KEY to 123),
                        parameters = { parametersOf(viewModelsClearedGloballySharedCounter) }
                    )
                DemoComposable(inputObject = fakeInjectedViewModel, objectType = "FakeInjectedViewModel", scoped = true)
                Button(modifier = Modifier.testTag("Button"),
                    onClick = { myKey = !myKey }) {
                    Text("Click to change")
                }
            }
        }
        printComposeUiTreeToLog()
        // Find the scoped text field and grab its text
        val initialFakeInjectedViewModelText = retrieveTextFromNodeWithTestTag("FakeInjectedViewModel Scoped")

        // When I click a button to change the state and key of the scoped ViewModel
        onNodeWithTestTag("Button").performClick()
        printComposeUiTreeToLog()

        // Then the text of the scoped ViewModel is different from the original one because it's a new ViewModel after changing the key
        onNodeWithTestTag("FakeInjectedViewModel Scoped").assertIsDisplayed()
            .assert(hasTextExactly(initialFakeInjectedViewModelText).not()) { "The text and address of the scoped ViewModel didn't change after key changed" }
    }
}