package com.sebaslogen.resacaapp.sample.metro

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sebaslogen.resaca.metro.metroViewModelScoped
import com.sebaslogen.resaca.rememberKeysInScope
import com.sebaslogen.resacaapp.sample.ResacaSampleApp
import com.sebaslogen.resacaapp.sample.di.metro.MetroSampleViewModelFactory
import com.sebaslogen.resacaapp.sample.di.metro.createMetroAssistedFactory
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeMetroInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeMetroSecondInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeMetroSimpleInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.NumberContainer
import com.sebaslogen.resacaapp.sample.utils.ComposeTestUtils
import com.sebaslogen.resacaapp.sample.utils.MainDispatcherRule
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

/**
 * Tests for the [metroViewModelScoped] overloads that accept [CreationExtras].
 * These cover overloads 3 and 4 of ScopedMemoizers.kt:
 * - `metroViewModelScoped(key, clearDelay, factory, creationExtras)` (no keyInScopeResolver)
 * - `metroViewModelScoped(key, keyInScopeResolver, clearDelay, factory, creationExtras)` (with keyInScopeResolver)
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class CreationExtrasScopedViewModelTests : ComposeTestUtils {
    init {
        callFromTestInit()
    }

    override fun callFromTestInit() {
        preTestInitializationToEmptyComposeDestination()
    }

    @get:Rule
    override val composeTestRule = createAndroidComposeRule<ComposeActivity>()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Scenarios for metroViewModelScoped(key, clearDelay, factory, creationExtras) — overload without keyInScopeResolver //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `when metroViewModelScoped is called with creationExtras, then a ViewModel is created and displayed`() {

        // Given a Composable that uses metroViewModelScoped with creationExtras
        composeTestRule.activity.setContent {
            val simpleVM: FakeMetroSimpleInjectedViewModel = metroViewModelScoped(
                factory = MetroSampleViewModelFactory(ResacaSampleApp.metroGraph),
                creationExtras = CreationExtras.Empty,
            )
            DemoComposable(inputObject = simpleVM, objectType = "Metro SimpleVM CreationExtras", scoped = true)
        }
        printComposeUiTreeToLog()

        // Then the ViewModel is displayed
        onNodeWithTestTag("Metro SimpleVM CreationExtras Scoped").assertIsDisplayed()
    }

    @Test
    fun `when metroViewModelScoped with creationExtras and key is recomposed, the same ViewModel is returned`() {

        // Given a Composable that uses metroViewModelScoped with creationExtras and a key
        composeTestRule.activity.setContent {
            var counter by remember { mutableStateOf(0) }
            val simpleVM: FakeMetroSimpleInjectedViewModel = metroViewModelScoped(
                key = "stableKey",
                factory = MetroSampleViewModelFactory(ResacaSampleApp.metroGraph),
                creationExtras = CreationExtras.Empty,
            )
            DemoComposable(inputObject = simpleVM, objectType = "Metro SimpleVM CreationExtras", scoped = true)
            Button(
                modifier = Modifier.testTag("Recompose"),
                onClick = { counter++ }) {
                Text("Recompose $counter")
            }
        }
        printComposeUiTreeToLog()
        val initialText = retrieveTextFromNodeWithTestTag("Metro SimpleVM CreationExtras Scoped")

        // When I trigger a recomposition
        onNodeWithTestTag("Recompose").performClick()
        printComposeUiTreeToLog()

        // Then the ViewModel text/address is still the same (same instance)
        onNodeWithTestTag("Metro SimpleVM CreationExtras Scoped").assertIsDisplayed()
            .assert(hasTextExactly(initialText)) { "The scoped ViewModel should be the same instance after recomposition" }
    }

    @Test
    fun `when metroViewModelScoped with creationExtras key changes, then a different ViewModel is created`() {

        // Given a Composable that uses metroViewModelScoped with creationExtras and a changeable key
        composeTestRule.activity.setContent {
            var myKey by remember { mutableStateOf(false) }
            val fakeInjectedViewModel: FakeMetroInjectedViewModel = metroViewModelScoped(
                key = myKey,
                factory = createMetroAssistedFactory(ResacaSampleApp.metroGraph, 123),
                creationExtras = CreationExtras.Empty,
            )
            DemoComposable(inputObject = fakeInjectedViewModel, objectType = "Metro InjectedVM CreationExtras", scoped = true)
            Button(
                modifier = Modifier.testTag("ChangeKey"),
                onClick = { myKey = !myKey }) {
                Text("Click to change key")
            }
        }
        printComposeUiTreeToLog()
        val initialText = retrieveTextFromNodeWithTestTag("Metro InjectedVM CreationExtras Scoped")

        // When the key changes
        onNodeWithTestTag("ChangeKey").performClick()
        printComposeUiTreeToLog()

        // Then a different ViewModel is created
        onNodeWithTestTag("Metro InjectedVM CreationExtras Scoped").assertIsDisplayed()
            .assert(hasTextExactly(initialText).not()) { "The ViewModel should be different after key change" }
    }

    @Test
    fun `when the Composable using metroViewModelScoped with creationExtras is disposed, then the ViewModel is cleared`() = runTest {

        // Given a Composable that uses metroViewModelScoped with creationExtras
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    val simpleVM: FakeMetroSimpleInjectedViewModel = metroViewModelScoped(
                        factory = MetroSampleViewModelFactory(ResacaSampleApp.metroGraph),
                        creationExtras = CreationExtras.Empty,
                    )
                    DemoComposable(inputObject = simpleVM, objectType = "Metro SimpleVM CreationExtras", scoped = true)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable is disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false // Trigger disposal
        composeTestRule.onNodeWithText(textTitle).assertExists() // Required to trigger recomposition
        advanceTimeBy(100) // Advance time to allow clear call on ScopedViewModelContainer to be processed
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped ViewModel is cleared
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of ViewModels cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "was not one higher than the amount before disposal ($initialAmountOfViewModelsCleared)"
        }
    }

    @Test
    fun `when metroViewModelScoped with creationExtras and clearDelay is disposed, ViewModel is NOT cleared before delay`() = runTest {

        // Given a Composable that uses metroViewModelScoped with creationExtras and clearDelay
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    val fakeInjectedVM: FakeMetroInjectedViewModel = metroViewModelScoped(
                        clearDelay = 2.seconds,
                        factory = createMetroAssistedFactory(ResacaSampleApp.metroGraph, 999),
                        creationExtras = CreationExtras.Empty,
                    )
                    DemoComposable(inputObject = fakeInjectedVM, objectType = "Metro InjectedVM CreationExtras Delay", scoped = true)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable is disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false
        printComposeUiTreeToLog()
        onNodeWithTestTag("Metro InjectedVM CreationExtras Delay Scoped", assertDisplayed = false).assertDoesNotExist()
        advanceTimeBy(100) // Advance time but NOT past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped ViewModel is NOT yet cleared because clearDelay has not expired
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "The amount of ViewModels cleared ($finalAmountOfViewModelsCleared) " +
                    "should be the same as before disposal ($initialAmountOfViewModelsCleared) because clearDelay has not expired"
        }
    }

    @Test
    fun `when metroViewModelScoped with creationExtras and clearDelay is disposed, ViewModel IS cleared after delay`() = runTest {

        // Given a Composable that uses metroViewModelScoped with creationExtras and clearDelay
        var composablesShown by mutableStateOf(true)
        val textTitle = "Test text"
        composeTestRule.activity.setContent {
            Column {
                Text(textTitle)
                if (composablesShown) {
                    val fakeInjectedVM: FakeMetroInjectedViewModel = metroViewModelScoped(
                        clearDelay = 2.seconds,
                        factory = createMetroAssistedFactory(ResacaSampleApp.metroGraph, 999),
                        creationExtras = CreationExtras.Empty,
                    )
                    DemoComposable(inputObject = fakeInjectedVM, objectType = "Metro InjectedVM CreationExtras Delay", scoped = true)
                }
            }
        }
        printComposeUiTreeToLog()

        // When the Composable is disposed
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        composablesShown = false
        printComposeUiTreeToLog()
        onNodeWithTestTag("Metro InjectedVM CreationExtras Delay Scoped", assertDisplayed = false).assertDoesNotExist()
        advanceTimeBy(2100) // Advance time past the clearDelay
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then the scoped ViewModel IS cleared because clearDelay has expired
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of ViewModels cleared after disposal ($finalAmountOfViewModelsCleared) " +
                    "was not one higher than before disposal ($initialAmountOfViewModelsCleared)"
        }
    }

    @Test
    fun `when metroViewModelScoped is called with MutableCreationExtras, then a ViewModel is created successfully`() {

        // Given a Composable that uses metroViewModelScoped with custom MutableCreationExtras
        val customExtras = MutableCreationExtras()
        composeTestRule.activity.setContent {
            val secondVM: FakeMetroSecondInjectedViewModel = metroViewModelScoped(
                factory = MetroSampleViewModelFactory(ResacaSampleApp.metroGraph),
                creationExtras = customExtras,
            )
            DemoComposable(inputObject = secondVM, objectType = "Metro SecondVM MutableExtras", scoped = true)
        }
        printComposeUiTreeToLog()

        // Then the ViewModel is displayed
        onNodeWithTestTag("Metro SecondVM MutableExtras Scoped").assertIsDisplayed()
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Scenarios for metroViewModelScoped(key, keyInScopeResolver, clearDelay, factory, creationExtras) — with keyInScopeResolver //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `when metroViewModelScoped with keyInScopeResolver and creationExtras scrolls off-screen, the ViewModel is NOT cleared`() = runTest {

        // Given a LazyColumn where items use metroViewModelScoped(key, keyInScopeResolver, factory, creationExtras)
        val listItems = (1..10).toList().map { NumberContainer(it) }.toMutableStateList()
        var height by mutableStateOf(1000.dp)
        composeTestRule.activity.setContent {
            Box(modifier = Modifier.size(width = 200.dp, height = height)) {
                val items: SnapshotStateList<NumberContainer> = remember { listItems }
                val keys = rememberKeysInScope(inputListOfKeys = items)
                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(items = items, key = { it.number }) { item ->
                        Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                            val vm: FakeMetroInjectedViewModel = metroViewModelScoped(
                                key = item,
                                keyInScopeResolver = keys,
                                factory = createMetroAssistedFactory(ResacaSampleApp.metroGraph, item.number),
                                creationExtras = CreationExtras.Empty,
                            )
                            DemoComposable(inputObject = vm, objectType = "Metro InjectedVM KeyScope $item", scoped = true)
                        }
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When height shrinks so only item 1 is visible
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        height = 100.dp
        onNodeWithTestTag("Metro InjectedVM KeyScope 1 Scoped").assertExists()
        advanceTimeBy(1000)
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then no ViewModels are cleared — keyInScopeResolver keeps them alive
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "Expected 0 ViewModels cleared (keyInScopeResolver keeps items alive), but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }

    @Test
    fun `when an item is removed from list using metroViewModelScoped with keyInScopeResolver and creationExtras, the ViewModel is cleared`() = runTest {

        // Given a LazyColumn where items use metroViewModelScoped(key, keyInScopeResolver, factory, creationExtras)
        val items: SnapshotStateList<NumberContainer> = (1..1000).toList().map { NumberContainer(it) }.toMutableStateList()
        composeTestRule.activity.setContent {
            Box(modifier = Modifier.size(width = 200.dp, height = 1000.dp)) {
                val listItems: SnapshotStateList<NumberContainer> = remember { items }
                val keys = rememberKeysInScope(inputListOfKeys = listItems)
                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(items = listItems, key = { it.number }) { item ->
                        Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                            val vm: FakeMetroInjectedViewModel = metroViewModelScoped(
                                key = item,
                                keyInScopeResolver = keys,
                                factory = createMetroAssistedFactory(ResacaSampleApp.metroGraph, item.number),
                                creationExtras = CreationExtras.Empty,
                            )
                            DemoComposable(inputObject = vm, objectType = "Metro InjectedVM KeyScope $item", scoped = true)
                        }
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When the first item is removed from the list
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        items.removeAt(0) // Trigger recomposition
        onNodeWithTestTag("Metro InjectedVM KeyScope 2 Scoped").assertExists()
        advanceTimeBy(1000)
        printComposeUiTreeToLog()

        // Then one scoped ViewModel is cleared
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + 1) {
            "The amount of ViewModels cleared after item removal ($finalAmountOfViewModelsCleared) " +
                    "was not one higher than the amount before removal ($initialAmountOfViewModelsCleared)"
        }
    }

    @Test
    fun `when the whole list with keyInScopeResolver and creationExtras is disposed, all ViewModels are cleared`() = runTest {

        // Given a LazyColumn where items use metroViewModelScoped(key, keyInScopeResolver, factory, creationExtras)
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
                                    val vm: FakeMetroInjectedViewModel = metroViewModelScoped(
                                        key = item,
                                        keyInScopeResolver = keys,
                                        factory = createMetroAssistedFactory(ResacaSampleApp.metroGraph, item.number),
                                        creationExtras = CreationExtras.Empty,
                                    )
                                    DemoComposable(inputObject = vm, objectType = "Metro InjectedVM KeyScope $item", scoped = true)
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
        onNodeWithTestTag("Metro InjectedVM KeyScope 2 Scoped").assertExists()
        shown = false // Trigger recomposition
        composeTestRule.onNodeWithText(textTitle).assertExists()
        advanceTimeBy(1000)
        printComposeUiTreeToLog()

        // Then all scoped ViewModels are cleared
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared + totalScopedViewModels) {
            "The amount of ViewModels cleared ($finalAmountOfViewModelsCleared) " +
                    "was not $totalScopedViewModels higher than before disposal ($initialAmountOfViewModelsCleared)"
        }
    }

    @Test
    fun `when metroViewModelScoped with keyInScopeResolver, clearDelay, and creationExtras scrolls off-screen, ViewModel is NOT cleared`() = runTest {

        // Given a LazyColumn where items use metroViewModelScoped(key, keyInScopeResolver, clearDelay, factory, creationExtras)
        val listItems = (1..10).toList().map { NumberContainer(it) }.toMutableStateList()
        var height by mutableStateOf(1000.dp)
        composeTestRule.activity.setContent {
            Box(modifier = Modifier.size(width = 200.dp, height = height)) {
                val items: SnapshotStateList<NumberContainer> = remember { listItems }
                val keys = rememberKeysInScope(inputListOfKeys = items)
                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(items = items, key = { it.number }) { item ->
                        Box(modifier = Modifier.size(width = 200.dp, height = 100.dp)) {
                            val vm: FakeMetroInjectedViewModel = metroViewModelScoped(
                                key = item,
                                keyInScopeResolver = keys,
                                clearDelay = 5.seconds,
                                factory = createMetroAssistedFactory(ResacaSampleApp.metroGraph, item.number),
                                creationExtras = CreationExtras.Empty,
                            )
                            DemoComposable(inputObject = vm, objectType = "Metro InjectedVM KeyScopeDelay $item", scoped = true)
                        }
                    }
                }
            }
        }
        printComposeUiTreeToLog()

        // When height shrinks so only item 1 is visible
        val initialAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()
        height = 100.dp
        onNodeWithTestTag("Metro InjectedVM KeyScopeDelay 1 Scoped").assertExists()
        advanceTimeBy(10_000) // Wait a very long time
        printComposeUiTreeToLog()
        val finalAmountOfViewModelsCleared = viewModelsClearedGloballySharedCounter.get()

        // Then no ViewModels are cleared — keyInScopeResolver keeps them alive even past clearDelay
        assert(finalAmountOfViewModelsCleared == initialAmountOfViewModelsCleared) {
            "Expected 0 ViewModels cleared (keyInScopeResolver keeps items alive), but " +
                    "cleared count changed from $initialAmountOfViewModelsCleared to $finalAmountOfViewModelsCleared"
        }
    }
}
