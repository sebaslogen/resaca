package com.sebaslogen.resacaapp.sample.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sebaslogen.resacaapp.sample.ui.main.compose.screens.ComposeScreenWithHiltViewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.screens.ComposeScreenWithKoinViewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.screens.ComposeScreenWithRememberScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.screens.ComposeScreenWithSingleHiltViewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.screens.ComposeScreenWithSingleKoinViewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.screens.ComposeScreenWithSingleViewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.screens.ComposeScreenWithSingleViewModelScopedWithKeys
import com.sebaslogen.resacaapp.sample.ui.main.ui.theme.ResacaAppTheme
import dagger.hilt.android.AndroidEntryPoint
import org.koin.compose.KoinContext

const val emptyDestination = "emptyDestination"
const val rememberScopedDestination = "rememberScopedDestination"
const val viewModelScopedDestination = "viewModelScopedDestination"
const val viewModelScopedWithKeysDestination = "viewModelScopedWithKeysDestination"
const val hiltViewModelScopedDestination = "hiltViewModelScopedDestination"
const val hiltSingleViewModelScopedDestination = "hiltSingleViewModelScopedDestination"
const val koinViewModelScopedDestination = "koinViewModelScopedDestination"
const val koinSingleViewModelScopedDestination = "koinSingleViewModelScopedDestination"

/**
 * This global boolean is only used in automated tests to fake
 * the configuration change + activity re-creation + composable gone from composition.
 */
var showSingleScopedViewModel: Boolean? = null

@AndroidEntryPoint // This annotation is required for Hilt to work anywhere inside this Activity
class ComposeActivity : ComponentActivity() {

    companion object {
        const val START_DESTINATION = "START_DESTINATION"

        /**
         * Global for testing purposes
         */
        var defaultDestination = rememberScopedDestination
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ResacaAppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val startDestination = intent.extras?.getString(START_DESTINATION) ?: defaultDestination
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .safeDrawingPadding()
                    ) {
                        IconButton( // Recreate Activity on Refresh button pressed to test scoped objects
                            modifier = Modifier.align(Alignment.End),
                            onClick = { recreate() }
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Recreate Activity")
                        }
                        ScreensWithNavigation(startDestination = startDestination)
                    }
                }
            }
        }
    }
}

@Composable
fun ScreensWithNavigation(navController: NavHostController = rememberNavController(), startDestination: String = rememberScopedDestination) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(emptyDestination) {
            Text(text = "Empty destination")
        }
        composable(rememberScopedDestination) {
            ComposeScreenWithRememberScoped(navController)
        }
        composable(viewModelScopedDestination) {
            ComposeScreenWithSingleViewModelScoped(navController)
        }
        composable(viewModelScopedWithKeysDestination) {
            ComposeScreenWithSingleViewModelScopedWithKeys(navController)
        }
        composable(hiltViewModelScopedDestination) {
            ComposeScreenWithHiltViewModelScoped(navController)
        }
        composable(koinViewModelScopedDestination) {
            @Suppress("DEPRECATION")
            KoinContext { // This is required to use Koin in ActivityScenario Robolectric tests, see ComposeActivityRecreationTests.kt
                ComposeScreenWithKoinViewModelScoped(navController)
            }
        }
        composable(hiltSingleViewModelScopedDestination) { // This destination is only used in automated tests
            ComposeScreenWithSingleHiltViewModelScoped(navController)
        }
        composable(koinSingleViewModelScopedDestination) { // This destination is only used in automated tests
            @Suppress("DEPRECATION")
            KoinContext { // This is required to use Koin in ActivityScenario Robolectric tests, see ComposeActivityRecreationTests.kt
                ComposeScreenWithSingleKoinViewModelScoped(navController)
            }
        }
    }
}

/**
 * Group of navigation buttons to navigate to different screens
 */
@Composable
fun NavigationButtons(navController: NavHostController) {
    Button(
        modifier = Modifier
            .padding(top = 16.dp, bottom = 2.dp)
            .testTag("Navigate to rememberScoped"),
        onClick = { navController.navigate(rememberScopedDestination) }) {
        Text(text = "Push rememberScoped destination")
    }
    Button(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .testTag("Navigate to ViewModelScoped D&N"),
        onClick = { navController.navigate(viewModelScopedDestination) }) {
        Text(text = "Push ViewModelScoped dest. with day/night")
    }
    Button(
        modifier = Modifier.padding(vertical = 2.dp),
        onClick = { navController.navigate(viewModelScopedWithKeysDestination) }) {
        Text(text = "Push ViewModelScoped dest. with LazyColumn")
    }
    Button(
        modifier = Modifier.padding(vertical = 2.dp),
        onClick = { navController.navigate(hiltViewModelScopedDestination) }) {
        Text(text = "Push Hilt ViewModelScoped destination")
    }
    Button(
        modifier = Modifier.padding(vertical = 2.dp),
        onClick = { navController.navigate(koinViewModelScopedDestination) }) {
        Text(text = "Push Koin ViewModelScoped destination")
    }
    val activity = LocalActivity.current
    Button(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .testTag("Back"),
        onClick = {
            if (!navController.popBackStack()) activity?.finish()
        }) {
        Text(text = "go back")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ResacaAppTheme {
        ScreensWithNavigation()
    }
}
