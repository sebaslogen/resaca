package com.sebaslogen.resacaapp.sample

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.viewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeScopedViewModel
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
    override val composeTestRule = createComposeRule()

    @Test
    fun `when an id is provided to viewModelScoped for ViewModel creation, then the created scoped ViewModel contains this id`() {

        // When the starting screen has a scoped Hilt ViewModel that uses an id
        val fakeScopedViewModelId = 555
        var fakeScopedViewModel: FakeScopedViewModel? = null
        composeTestRule.setContent {
            fakeScopedViewModel = viewModelScoped(defaultArguments = bundleOf(FakeScopedViewModel.MY_ARGS_KEY to fakeScopedViewModelId))
            DemoComposable(inputObject = fakeScopedViewModel!!, objectType = "FakeScopedViewModel", scoped = true)
        }
        printComposeUiTreeToLog()

        // Then the id of the scoped ViewModel is the same that was provided at injection time
        assert(fakeScopedViewModel?.viewModelId == fakeScopedViewModelId) {
            "The id of the scoped ViewModel ${fakeScopedViewModel?.viewModelId} does not match the provided id at injection time $fakeScopedViewModelId"
        }
    }
}