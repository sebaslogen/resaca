package com.sebaslogen.resacaapp.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.rememberKeysInScope
import com.sebaslogen.resaca.viewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedRepo
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeScopedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeSimpleInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.NumberContainer
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class KeyInScopeResolverTest : ComposeTestUtils {
    init {
        callFromTestInit()
    }

    override fun callFromTestInit() {
        preTestInitializationToEmptyComposeDestination()
    }

    @get:Rule
    override val composeTestRule = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `Given a long lazy list when the first item is not visible anymore, then its manually created ViewModel remains in the container and it's not cleared`() =
        runTest {
            // Given the starting screen with long lazy list of scoped objects remembering their keys
            val listItems = (1..1000).toList().map { NumberContainer(it) }
            var height by mutableStateOf(1000.dp)
            composeTestRule.setContent {
                Box(modifier = Modifier.size(width = 200.dp, height = height)) {
                    val keys = rememberKeysInScope(inputListOfKeys = listItems)
                    LazyColumn(modifier = Modifier.fillMaxHeight()) {
                        items(items = listItems, key = { it.number }) { item ->
                            Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                                val fakeScopedVM: FakeSimpleInjectedViewModel = viewModelScoped(key = item, keyInScopeResolver = keys) {
                                    FakeSimpleInjectedViewModel(
                                        repository = FakeInjectedRepo(),
                                        viewModelsClearedCounter = viewModelsClearedGloballySharedCounter
                                    )
                                }
                                DemoComposable(inputObject = fakeScopedVM, objectType = "FakeSimpleInjectedViewModel $item", scoped = true)
                            }
                        }
                    }
                }
            }
            printComposeUiTreeToLog()

            // When the size of the content changes and only one item fits on the screen
            val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
            height = 150.dp // Trigger recomposition
            onNodeWithTestTag("FakeSimpleInjectedViewModel 1 Scoped").assertExists() // Required to trigger recomposition
            advanceTimeBy(1000) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
            printComposeUiTreeToLog()

            // Then no scoped ViewModels are cleared
            val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
            onNodeWithTestTag("FakeSimpleInjectedViewModel 1 Scoped").assertExists()
            onNodeWithTestTag("FakeSimpleInjectedViewModel 5 Scoped", assertDisplayed = false).assertIsNotDisplayed() // Required to trigger recomposition
            assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
                "The amount of FakeSimpleInjectedViewModels that were cleared after change ($finalAmountOfViewModelsCleared) " +
                        "was not the same that the amount before the change ($initialAmountOfViewModelsCleared). It should be 0"
            }
        }

    @Test
    fun `Given a long lazy list when the first item is not visible anymore, then its ViewModel remains in the container and it's not cleared`() = runTest {

        // Given the starting screen with long lazy list of scoped objects remembering their keys
        val listItems = (1..1000).toList().map { NumberContainer(it) }
        var height by mutableStateOf(1000.dp)
        composeTestRule.setContent {
            Box(modifier = Modifier.size(width = 200.dp, height = height)) {
                val keys = rememberKeysInScope(inputListOfKeys = listItems)
                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(items = listItems, key = { it.number }) { item ->
                        Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                            val fakeScopedVM: FakeScopedViewModel = viewModelScoped(key = item, keyInScopeResolver = keys) {
                                FakeScopedViewModel(
                                    stateSaver = it,
                                    viewModelId = item.number
                                )
                            }
                            DemoComposable(inputObject = fakeScopedVM, objectType = "FakeScopedViewModel $item", scoped = true)
                        }
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When the size of the content changes and only one item fits on the screen
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        height = 150.dp // Trigger recomposition
        onNodeWithTestTag("FakeScopedViewModel 1 Scoped").assertExists() // Required to trigger recomposition
        advanceTimeBy(1000) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
        printComposeUiTreeToLog()

        // Then no scoped ViewModels are cleared
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        onNodeWithTestTag("FakeScopedViewModel 1 Scoped").assertExists()
        onNodeWithTestTag("FakeScopedViewModel 5 Scoped", assertDisplayed = false).assertIsNotDisplayed() // Required to trigger recomposition
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "The amount of FakeScopedViewModels that were cleared after change ($finalAmountOfViewModelsCleared) " +
                    "was not the same that the amount before the change ($initialAmountOfViewModelsCleared). It should be 0"
        }
    }

    @Test
    fun `Given a long lazy list when the an item is removed from the list, then its ViewModel is cleared from the container`() = runTest {

        // Given the starting screen with long lazy list of scoped objects remembering their keys
        val items: SnapshotStateList<NumberContainer> = (1..1000).toList().map { NumberContainer(it) }.toMutableStateList()
        composeTestRule.setContent {
            Box(modifier = Modifier.size(width = 200.dp, height = 1000.dp)) {
                val listItems: SnapshotStateList<NumberContainer> = remember { items }
                val keys = rememberKeysInScope(inputListOfKeys = listItems)
                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(items = listItems, key = { it.number }) { item ->
                        Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                            val fakeScopedVM: FakeScopedViewModel = viewModelScoped(key = item, keyInScopeResolver = keys) {
                                FakeScopedViewModel(
                                    stateSaver = it,
                                    viewModelId = item.number
                                )
                            }
                            DemoComposable(inputObject = fakeScopedVM, objectType = "FakeScopedViewModel $item", scoped = true)
                        }
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When the content of the list changes
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        items.removeAt(0) // Trigger recomposition
        onNodeWithTestTag("FakeScopedViewModel 2 Scoped").assertExists() // Required to trigger recomposition
        advanceTimeBy(1000) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
        printComposeUiTreeToLog()

        // Then one scoped ViewModel is cleared
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        onNodeWithTestTag("FakeScopedViewModel 2 Scoped").assertExists()
        onNodeWithTestTag("FakeScopedViewModel 1 Scoped", assertDisplayed = false).assertIsNotDisplayed() // Required to trigger recomposition
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of FakeScopedViewModels that were cleared after keys list change ($finalAmountOfViewModelsCleared) " +
                    "was not one more that the amount before the keys list change ($initialAmountOfViewModelsCleared). It should be 1"
        }
    }

    @Test
    fun `Given a long lazy list when the whole list with the keyInScope is disposed of, then its ViewModel is cleared from the container`() = runTest {

        // Given the starting screen with long lazy list of scoped objects remembering their keys
        val totalScopedViewModels = 7
        val listItems = (1..totalScopedViewModels).toList().map { NumberContainer(it) }
        var shown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                Box(modifier = Modifier.size(width = 200.dp, height = 1000.dp)) {
                    if (shown) {
                        val keys = rememberKeysInScope(inputListOfKeys = listItems)
                        LazyColumn(modifier = Modifier.fillMaxHeight()) {
                            items(items = listItems, key = { it.number }) { item ->
                                Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                                    val fakeScopedVM: FakeScopedViewModel = viewModelScoped(key = item, keyInScopeResolver = keys) {
                                        FakeScopedViewModel(
                                            stateSaver = it,
                                            viewModelId = item.number
                                        )
                                    }
                                    DemoComposable(inputObject = fakeScopedVM, objectType = "FakeScopedViewModel $item", scoped = true)
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
        onNodeWithTestTag("FakeScopedViewModel 2 Scoped").assertExists() // Required to trigger recomposition
        shown = false// Trigger recomposition
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(1000) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
        printComposeUiTreeToLog()

        // Then all scoped ViewModels are cleared
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + totalScopedViewModels) {
            "The amount of FakeScopedViewModels that were cleared after change ($finalAmountOfViewModelsCleared) " +
                    "was not one more that the amount before the change ($initialAmountOfViewModelsCleared)."
        }
    }

    @Test
    fun `Given a ViewModel scoped with a key removed from KeyInScopeResolver but not from Compose, then the ViewModel is not cleared from the container`() = runTest {

        // Given the starting screen with a key scoped ViewModel remembering it with a KeyInScopeResolver
        var shown by mutableStateOf(true)
        val textTitle = "Test text"
        val viewModelKey = "My key"
        val inputListOfKeys: SnapshotStateList<String> = mutableStateListOf(viewModelKey)
        composeTestRule.setContent {
            Column {
                Text(textTitle)
                if (shown) {
                    LaunchedEffect(Unit) {
                        if (!inputListOfKeys.contains(viewModelKey)) inputListOfKeys.add(viewModelKey)
                    }
                } else {
                    LaunchedEffect(Unit) {
                        inputListOfKeys.clear()
                    }
                }
                val keys = rememberKeysInScope(inputListOfKeys = inputListOfKeys)
                val fakeScopedVM: FakeScopedViewModel = viewModelScoped(key = viewModelKey, keyInScopeResolver = keys) {
                    FakeScopedViewModel(
                        stateSaver = it,
                        viewModelId = 666
                    )
                }
                DemoComposable(inputObject = fakeScopedVM, objectType = "FakeScopedViewModel $viewModelKey", scoped = true)
            }
        }
        printComposeUiTreeToLog()

        // When the list of keys is emptied
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        onNodeWithTestTag("FakeScopedViewModel My key Scoped").assertExists() // Required to trigger recomposition
        shown = false// Trigger recomposition
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(1000) // Advance time to allow clear call on ScopedViewModelContainer to be processed before querying the counter
        printComposeUiTreeToLog()

        // Then no scoped ViewModel is cleared
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "The amount of FakeScopedViewModels that were cleared after change ($finalAmountOfViewModelsCleared) " +
                "was different that the amount before the change ($initialAmountOfViewModelsCleared). 0 was expected."
        }
    }
}
