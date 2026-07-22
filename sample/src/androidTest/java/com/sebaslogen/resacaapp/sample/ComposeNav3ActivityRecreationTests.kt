package com.sebaslogen.resacaapp.sample

import android.content.Intent
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.sebaslogen.resaca.utils.ResacaPackagePrivate
import com.sebaslogen.resacaapp.sample.ui.main.Nav3ViewModelsActivity
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ResacaPackagePrivate::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class ComposeNav3ActivityRecreationTests : ComposeTestUtils {

    @get:Rule
    override val composeTestRule = createEmptyComposeRule()

    @Test
    fun whenActivityRecreates_thenTheNavigationRouteIsPersisted() {
        ActivityScenario.launch<Nav3ViewModelsActivity>(
            Intent(ApplicationProvider.getApplicationContext(), Nav3ViewModelsActivity::class.java)
        ).use { scenario ->
            // Given the starting screen
            composeTestRule.waitForIdle()
            onNodeWithTestTag("Nav3 Button").performClick()

            // When I navigate to RouteB and verify it's displayed
            composeTestRule.waitForIdle()
            val routeText = "Route id: 123 "
            assert(retrieveTextFromNodeWithTestTag("Nav3 Text") == routeText)

            // And I recreate the Activity
            scenario.recreate()
            composeTestRule.waitForIdle()

            // Then the navigation route is still RouteB
            assert(retrieveTextFromNodeWithTestTag("Nav3 Text") == routeText)
        }
    }
}
