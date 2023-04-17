package com.sebaslogen.resacaapp.sample.utils

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.text.AnnotatedString
import org.junit.Before
import org.robolectric.shadows.ShadowLog

interface ComposeTestUtils {

    val composeTestRule: ComposeContentTestRule

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out // Redirect Logcat to console output to read printToLog Compose debug messages
    }


    // Helper functions //

    fun printComposeUiTreeToLog(testTag: String? = null) {
        if (testTag.isNullOrEmpty()) {
            composeTestRule.onRoot().printToLog("TAG")
        } else {
            composeTestRule.onNodeWithTag(testTag).printToLog("TAG")
        }
    }

    fun onNodeWithTestTag(tag: String, parentTestTag: String? = null, assertDisplayed: Boolean = true) =
        if (parentTestTag != null) {
            composeTestRule.onAllNodesWithTag(tag)
                .filterToOne(hasParent(hasTestTag(parentTestTag))).apply {
                    if (assertDisplayed) assertIsDisplayed()
                }
        } else {
            composeTestRule.onNodeWithTag(tag).apply {
                if (assertDisplayed) assertIsDisplayed()
            }
        }

    @Suppress("UNCHECKED_CAST")
    fun retrieveTextFromNodeWithTestTag(tag: String, parentTestTag: String? = null): String =
        (onNodeWithTestTag(tag, parentTestTag)
            .fetchSemanticsNode().config
            .first { it.key.name == "Text" }
            .value as List<AnnotatedString>).first().toString()
}