package com.sebaslogen.resacaapp.sample

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.rememberScopedDestination
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class MemoryLeakTests : ComposeTestUtils {
    init {
        callFromTestInit()
    }

    override fun callFromTestInit() {
        ComposeActivity.defaultDestination = rememberScopedDestination // This is needed to reset the destination to the default one on the release app
    }

    @get:Rule
    override val composeTestRule = createComposeRule()

    @Test
    fun `given ComposeActivity with Composables in a nested Navigation Composable, when the activity is recreated, then the original ComposeActivity object is garbage collected`() {
        val referenceQueue = ReferenceQueue<ComposeActivity>()
        var weakActivityReference: WeakReference<ComposeActivity>? = null
        // Given I create the Activity
        val scenario = ActivityScenario.launch(ComposeActivity::class.java)
        composeTestRule.waitForIdle()
        scenario.onActivity { activity: ComposeActivity ->

            // And we grab a WeakReference to the Activity
            weakActivityReference = WeakReference(activity, referenceQueue)
        }
        printComposeUiTreeToLog()

        // And I click "Navigate to rememberScoped" to get to a nested screen in the same Activity
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("Navigate to rememberScoped").performClick()
        printComposeUiTreeToLog()
        composeTestRule.waitForIdle()

        // When we recreate the activity
        scenario.recreate()
        composeTestRule.waitForIdle()

        // And trigger Garbage Collection to make sure old ComposeActivity is collected
        Runtime.getRuntime().gc()
        printComposeUiTreeToLog()

        // Then the original Activity object is garbage collected
        val polledReferenceQueue = referenceQueue.poll()
        assertNotNull(polledReferenceQueue, "The object is still alive (or GC hasn't run yet).")
        assertNotNull(weakActivityReference, "WeakReference container for initial ComposeActivity should not be null because it was created")
        assertNull(weakActivityReference?.get(), "Initial ComposeActivity should have been garbage collected but it wasn't, so it's leaking")
    }
}
