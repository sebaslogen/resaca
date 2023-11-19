package com.sebaslogen.resacaapp.sample.utils

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.text.AnnotatedString
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.emptyDestination
import com.sebaslogen.resacaapp.sample.ui.main.showSingleScopedViewModel
import org.junit.After
import org.junit.Before
import org.koin.core.context.stopKoin
import org.robolectric.shadows.ShadowLog

interface ComposeTestUtils {

    val composeTestRule: ComposeContentTestRule

    /**
     * Require the implementation to call this function from the test class' init block that runs
     * before the Activity is created by some Compose test rules.
     */
    fun callFromTestInit()

    /**
     * By default we use a completely empty Compose destination to set the Compose content explicitly on each test.
     */
    fun preTestInitializationToEmptyComposeDestination() {
        ComposeActivity.defaultDestination = emptyDestination
        showSingleScopedViewModel = null
    }

    @Before
    @Throws(Exception::class)
    fun setUp() {
        ShadowLog.stream = System.out // Redirect Logcat to console output to read printToLog Compose debug messages
    }

    @After
    fun tearDown() {
        stopKoin()
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