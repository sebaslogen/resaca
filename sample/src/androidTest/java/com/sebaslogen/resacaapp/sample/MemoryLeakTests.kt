package com.sebaslogen.resacaapp.sample

import android.content.Intent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.rememberScopedDestination
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.ref.WeakReference
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class MemoryLeakTests : ComposeTestUtils {

    private lateinit var scenario: ActivityScenario<ComposeActivity>

    @get:Rule
    override val composeTestRule = createEmptyComposeRule()

    @Before
    fun setUp() {
        scenario = ActivityScenario.launch(
            Intent(ApplicationProvider.getApplicationContext(), ComposeActivity::class.java).apply {
                putExtra(ComposeActivity.START_DESTINATION, rememberScopedDestination)
            })
    }

    @Test
    fun givenComposeActivityWithComposablesInANestedNavigationComposable_whenTheActivityIsRecreated_thenTheOriginalComposeActivityObjectIsGarbageCollected() {
        var weakActivityReference: WeakReference<ComposeActivity>? = null
        // Given I create the Activity
        composeTestRule.waitForIdle()
        scenario.onActivity { activity: ComposeActivity ->

            // And we grab a WeakReference to the Activity
            weakActivityReference = WeakReference(activity)
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
        Thread.sleep(100)
        printComposeUiTreeToLog()

        // Then the original Activity object is garbage collected
        assertNotNull(weakActivityReference, "WeakReference container for initial ComposeActivity should not be null because it was created")
        assertNull(weakActivityReference?.get(), "Initial ComposeActivity should have been garbage collected but it wasn't, so it's leaking")
    }
}
