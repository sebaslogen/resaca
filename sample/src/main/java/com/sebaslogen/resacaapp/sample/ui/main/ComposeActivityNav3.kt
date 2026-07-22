package com.sebaslogen.resacaapp.sample.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.sebaslogen.resaca.hilt.hiltViewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.ui.theme.ResacaAppTheme
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.Serializable

/**
 * Passing navigation arguments to a Hilt injected ViewModel
 *
 * - ViewModelStoreNavEntryDecorator ensures that ViewModels are scoped to the NavEntry
 * - Assisted injection is used to construct ViewModels with the navigation key
 */

@Serializable
sealed class Screen : NavKey {
    @Serializable
    data object RouteA : Screen()
    @Serializable
    data class RouteB(val id: String) : Screen()
}

@AndroidEntryPoint
class InjectedViewModelsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResacaAppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .safeDrawingPadding(),
                    color = MaterialTheme.colors.background
                ) {
                    val backStack = rememberNavBackStack(Screen.RouteA)

                    NavDisplay(
                        backStack = backStack,
                        onBack = { backStack.removeLastOrNull() },
                        entryDecorators = listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        entryProvider = entryProvider {
                            entry<Screen.RouteA> {
                                Button(
                                    modifier = Modifier.testTag("Nav3 Button"),
                                    onClick = {
                                        backStack.add(Screen.RouteB("123"))
                                    }
                                ) {
                                    Text("Click to navigate")
                                }
                            }
                            entry<Screen.RouteB> { key ->
                                val viewModel = hiltViewModelScoped { factory: RouteBViewModel.Factory ->
                                    factory.create(key)
                                }
                                ScreenB(viewModel = viewModel)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScreenB(viewModel: RouteBViewModel) {
    Text(
        modifier = Modifier.testTag("Nav3 Text"),
        text = "Route id: ${viewModel.navKey.id} "
    )
}

@HiltViewModel(assistedFactory = RouteBViewModel.Factory::class)
class RouteBViewModel @AssistedInject constructor(
    @Assisted val navKey: Screen.RouteB
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(navKey: Screen.RouteB): RouteBViewModel
    }
}
