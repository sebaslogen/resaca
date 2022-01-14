package com.sebaslogen.resacaapp

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
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.compose.rememberScoped
import com.sebaslogen.resacaapp.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.ui.main.data.FakeRepo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScopeKeysTest : ComposeTestUtils {

    @get:Rule
    override val composeTestRule = createComposeRule()

    @Test
    fun `when the key used for rememberScoped changes, then the scoped object is different`() {

        // Given the starting screen with scoped object that uses a key
        composeTestRule.setContent {
            var myKey by remember { mutableStateOf(false) }
            val fakeRepo: FakeRepo = rememberScoped(key = myKey) { FakeRepo() }
            DemoComposable(inputObject = fakeRepo, objectType = "FakeRepo", scoped = true)
            Button(modifier = Modifier.testTag("Button"),
                onClick = { myKey = !myKey }) {
                Text("Click to change")
            }
        }
        printComposeUiTreeToLog()
        // Find the scoped text field and grab its text
        val initialFakeScopedRepoText = retrieveTextFromNodeWithTestTag("FakeRepo Scoped")

        // When I click a button to change the state and key of the rememberScoped object
        onNodeWithTestTag("Button").performClick()
        printComposeUiTreeToLog()

        // Then the text of the scoped object is different from the original one because it's a new object after changing the key
        onNodeWithTestTag("FakeRepo Scoped").assertIsDisplayed()
            .assert(hasTextExactly(initialFakeScopedRepoText).not())
    }
}