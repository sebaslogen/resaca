package com.sebaslogen.resacaapp.sample.hilt

import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.hilt.hiltViewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedViewModel
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config


@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class AssistedInjectionTest : ComposeTestUtils {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComposeActivity>()

    @Test
    fun `when an id is provided to hiltViewModelScoped for ViewModel creation, then the created scoped ViewModel contains this id`() {

        // When the starting screen has a scoped Hilt ViewModel that uses an id
        val fakeInjectedViewModelId = 555
        var fakeInjectedViewModel: FakeInjectedViewModel? = null
        composeTestRule.activity.setContent {
            fakeInjectedViewModel = hiltViewModelScoped(defaultArguments = bundleOf(FakeInjectedViewModel.MY_ARGS_KEY to fakeInjectedViewModelId))
            DemoComposable(inputObject = fakeInjectedViewModel!!, objectType = "FakeInjectedViewModel", scoped = true)
        }
        printComposeUiTreeToLog()

        // Then the id of the scoped ViewModel is the same that was provided at injection time
        assert(fakeInjectedViewModel?.viewModelId == fakeInjectedViewModelId) {
            "The id of the scoped ViewModel ${fakeInjectedViewModel?.viewModelId} does not match the provided id at injection time $fakeInjectedViewModelId"
        }
    }
}