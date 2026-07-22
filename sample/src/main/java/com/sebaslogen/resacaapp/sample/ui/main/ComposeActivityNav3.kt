package com.sebaslogen.resacaapp.sample.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sebaslogen.resaca.hilt.hiltViewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.ui.theme.ResacaAppTheme
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicInteger

/**
 * Passing navigation arguments to a Hilt injected ViewModel
 *
 * - ViewModelStoreNavEntryDecorator ensures that ViewModels are scoped to the NavEntry, so that a
 *   [ScopedViewModelContainer][com.sebaslogen.resaca.ScopedViewModelContainer] (and therefore any
 *   `viewModelScoped`/`hiltViewModelScoped` ViewModel) is retained while its destination stays on the
 *   back stack. Without it, all destinations share the Activity-wide ViewModelStore and ViewModels can
 *   be cleared as soon as their Composable leaves composition, even while their destination is still on
 *   the back stack. See [Nav3ViewModelsActivity.INCLUDE_VIEW_MODEL_STORE_DECORATOR].
 * - Assisted injection is used to construct ViewModels with the navigation key
 */

@Serializable
sealed class Screen : NavKey {
    @Serializable
    data object RouteA : Screen()

    @Serializable
    data class RouteB(val id: String) : Screen()

    @Serializable
    data class RouteC(val id: String) : Screen()
}

@AndroidEntryPoint
class Nav3ViewModelsActivity : ComponentActivity() {

    companion object {
        /**
         * Intent extra to toggle whether [rememberViewModelStoreNavEntryDecorator] is added to
         * [NavDisplay]'s `entryDecorators`. Defaults to `true` (the correct/recommended setup).
         * Set to `false` to reproduce https://github.com/sebaslogen/resaca/issues/385.
         */
        const val INCLUDE_VIEW_MODEL_STORE_DECORATOR = "INCLUDE_VIEW_MODEL_STORE_DECORATOR"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val includeViewModelStoreDecorator = intent.getBooleanExtra(INCLUDE_VIEW_MODEL_STORE_DECORATOR, true)
        setContent {
            ResacaAppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colors.background
                ) {
                    val backStack = rememberNavBackStack(Screen.RouteA)
                    val entryDecorators: List<NavEntryDecorator<NavKey>> = if (includeViewModelStoreDecorator) {
                        listOf(rememberSaveableStateHolderNavEntryDecorator(), rememberViewModelStoreNavEntryDecorator())
                    } else {
                        listOf(rememberSaveableStateHolderNavEntryDecorator())
                    }

                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryDecorators = entryDecorators,
                        entryProvider = entryProvider {
                            entry<Screen.RouteA> {
                                Button(
                                    modifier = Modifier.testTag("Nav3 Button"),
                                    onClick = { backStack.add(Screen.RouteB("123")) }
                                ) {
                                    Text("Click to navigate to route B")
                                }
                            }
                            entry<Screen.RouteB> { key ->
                                val viewModel = hiltViewModelScoped { factory: Nav3ViewModel.Factory ->
                                    factory.create(key.id)
                                }
                                Column {
                                    Text(
                                        modifier = Modifier.testTag("Nav3 Text"),
                                        text = "Route B id: ${viewModel.navKey} "
                                    )
                                    Button(
                                        modifier = Modifier.testTag("Nav3 RouteB To RouteC Button"),
                                        onClick = { backStack.add(Screen.RouteC("456")) }
                                    ) {
                                        Text("Click to navigate to route C")
                                    }
                                }
                            }
                            entry<Screen.RouteC> { key ->
                                val viewModel = hiltViewModelScoped { factory: Nav3ViewModel.Factory ->
                                    factory.create(key.id)
                                }
                                Column {
                                    Text("Route C id: ${viewModel.navKey} ")
                                    Button(
                                        modifier = Modifier.testTag("Nav3 RouteC Back Button"),
                                        onClick = { backStack.removeLastOrNull() }
                                    ) {
                                        Text("Back")
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@HiltViewModel(assistedFactory = Nav3ViewModel.Factory::class)
class Nav3ViewModel @AssistedInject constructor(
    @Assisted val navKey: String
) : ViewModel() {

    /**
     * Counter to track that this ViewModel has been correctly cleared
     */
    private val viewModelsClearedCounter: AtomicInteger = viewModelsClearedGloballySharedCounter

    override fun onCleared() {
        viewModelsClearedCounter.incrementAndGet()
        super.onCleared()
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: String): Nav3ViewModel
    }
}
