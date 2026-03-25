package com.sebaslogen.resacaapp.sample.hilt

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.hilt.hiltViewModelScoped
import com.sebaslogen.resaca.rememberKeysInScope
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeSecondInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.NumberContainer
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.utils.MainDispatcherRule
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config


/**
 * Tests for `hiltViewModelScoped(key, keyInScopeResolver)` overload WITHOUT creationCallback.
 * This exercises the overload at resacahilt/ScopedMemoizers.kt lines 55-63 which uses
 * `@HiltViewModel` with standard `@Inject` constructor (no assisted injection).
 * Uses [FakeSecondInjectedViewModel] which has a `@HiltViewModel @Inject` constructor.
 */
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class HiltViewModelScopedNoCallbackWithKeysTest : ComposeTestUtils {
    init {
        callFromTestInit()
    }

    override fun callFromTestInit() {
        preTestInitializationToEmptyComposeDestination()
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComposeActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // region Helper composable

    @Composable
    private fun TestHiltLazyColumnNoCallbackWithKeys(
        items: SnapshotStateList<NumberContainer>,
        height: Dp
    ) {
        Box(modifier = Modifier.size(width = 200.dp, height = height)) {
            val listItems: SnapshotStateList<NumberContainer> = remember { items }
            val keys = rememberKeysInScope(inputListOfKeys = listItems)
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(items = listItems, key = { it.number }) { item ->
                    Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                        val fakeScopedVM: FakeSecondInjectedViewModel = hiltViewModelScoped(
                            key = item,
                            keyInScopeResolver = keys
                        )
                        DemoComposable(inputObject = fakeScopedVM, objectType = "FakeSecondInjectedViewModel $item", scoped = true)
                    }
                }
            }
        }
    }

    // endregion

    @Test
    fun `when item scrolls off-screen with hiltViewModelScoped no callback and keyInScope, its VM is NOT cleared`() = runTest {
        // Given a LazyColumn where items use hiltViewModelScoped(key, keyInScopeResolver) with no creationCallback
        val listItems = (1..10).toList().map { NumberContainer(it) }.toMutableStateList()
        var height by mutableStateOf(1000.dp)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            Column {
                Text(textTitle)
                TestHiltLazyColumnNoCallbackWithKeys(listItems, height)
            }
        }
        printComposeUiTreeToLog()

        // When height shrinks so only item 1 is visible
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        height = 150.dp // Trigger recomposition
        onNodeWithTestTag("FakeSecondInjectedViewModel 1 Scoped").assertExists()
        advanceTimeBy(1000) // Advance time to allow disposal processing
        printComposeUiTreeToLog()

        // Then no scoped ViewModels are cleared — keyInScopeResolver keeps them alive
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        onNodeWithTestTag("FakeSecondInjectedViewModel 1 Scoped").assertExists()
        onNodeWithTestTag("FakeSecondInjectedViewModel 5 Scoped", assertDisplayed = false).assertIsNotDisplayed()
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "Expected 0 ViewModels cleared (keyInScopeResolver keeps items alive), but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }

    @Test
    fun `when item is removed from list with hiltViewModelScoped no callback and keyInScope, its VM IS cleared`() = runTest {
        // Given a LazyColumn where items use hiltViewModelScoped(key, keyInScopeResolver) with no creationCallback
        val items: SnapshotStateList<NumberContainer> = (1..1000).toList().map { NumberContainer(it) }.toMutableStateList()
        composeTestRule.activity.setContent {
            Box(modifier = Modifier.size(width = 200.dp, height = 1000.dp)) {
                val listItems: SnapshotStateList<NumberContainer> = remember { items }
                val keys = rememberKeysInScope(inputListOfKeys = listItems)
                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(items = listItems, key = { it.number }) { item ->
                        Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                            val fakeScopedVM: FakeSecondInjectedViewModel = hiltViewModelScoped(
                                key = item,
                                keyInScopeResolver = keys
                            )
                            DemoComposable(inputObject = fakeScopedVM, objectType = "FakeSecondInjectedViewModel $item", scoped = true)
                        }
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When item 1 is removed from the list
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        items.removeAt(0) // Trigger recomposition
        onNodeWithTestTag("FakeSecondInjectedViewModel 2 Scoped").assertExists()
        advanceTimeBy(1000) // Advance time to allow clear call to be processed
        printComposeUiTreeToLog()

        // Then one scoped ViewModel is cleared
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        onNodeWithTestTag("FakeSecondInjectedViewModel 2 Scoped").assertExists()
        onNodeWithTestTag("FakeSecondInjectedViewModel 1 Scoped", assertDisplayed = false).assertIsNotDisplayed()
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "Expected 1 VM cleared after item removal, but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }

    @Test
    fun `when entire list is disposed with hiltViewModelScoped no callback and keyInScope, all VMs are cleared`() = runTest {
        // Given a LazyColumn with items
        val totalScopedViewModels = 7
        val listItems = (1..totalScopedViewModels).toList().map { NumberContainer(it) }
        var shown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            Column {
                Text(textTitle)
                Box(modifier = Modifier.size(width = 200.dp, height = 1000.dp)) {
                    if (shown) {
                        val keys = rememberKeysInScope(inputListOfKeys = listItems)
                        LazyColumn(modifier = Modifier.fillMaxHeight()) {
                            items(items = listItems, key = { it.number }) { item ->
                                Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                                    val fakeScopedVM: FakeSecondInjectedViewModel = hiltViewModelScoped(
                                        key = item,
                                        keyInScopeResolver = keys
                                    )
                                    DemoComposable(
                                        inputObject = fakeScopedVM,
                                        objectType = "FakeSecondInjectedViewModel $item",
                                        scoped = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When the whole list and its keys are disposed of
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        onNodeWithTestTag("FakeSecondInjectedViewModel 2 Scoped").assertExists()
        shown = false // Trigger recomposition
        composeTestRule.onNodeWithText(textTitle).assertExists()
        advanceTimeBy(1000) // Advance time to allow clear calls to be processed
        printComposeUiTreeToLog()

        // Then all scoped ViewModels are cleared
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + totalScopedViewModels) {
            "Expected all $totalScopedViewModels VMs cleared, but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }
}
