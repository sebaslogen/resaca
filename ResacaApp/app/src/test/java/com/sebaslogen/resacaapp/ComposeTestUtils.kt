package com.sebaslogen.resacaapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.text.AnnotatedString
import org.junit.Before
import org.robolectric.shadows.ShadowLog

interface ComposeTestUtils {

    abstract val composeTestRule: ComposeContentTestRule

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out // Redirect Logcat to console output to read printToLog Compose debug messages
    }


    // Helper functions //

    fun printComposeUiTreeToLog() {
        composeTestRule.onRoot().printToLog("TAG")
    }

    fun onNodeWithContentDescription(description: String) = composeTestRule.onNodeWithContentDescription(description, substring = true)

    fun retrieveTextFromNodeWithContentDescription(description: String): String {
        val textField = onNodeWithContentDescription(description).assertIsDisplayed()
        return (textField.fetchSemanticsNode().config.first { it.key.name == "Text" }.value as List<AnnotatedString>).first().toString()
    }
}