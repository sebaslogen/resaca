package com.sebaslogen.resacaapp.sample.utils

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.compose.ui.text.AnnotatedString
import com.sebaslogen.resacaapp.sample.ui.main.showSingleScopedViewModel
import org.junit.After
import org.junit.Before

interface ComposeTestUtils {

    val composeTestRule: ComposeTestRule

    @Before
    fun internalSetUp() {
        showSingleScopedViewModel = null
    }

    @After
    fun internalTearDown() {
        showSingleScopedViewModel = null
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