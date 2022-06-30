package com.sebaslogen.resacaapp.ui.main

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sebaslogen.resacaapp.ui.main.compose.*
import com.sebaslogen.resacaapp.ui.main.ui.theme.ResacaAppTheme
import dagger.hilt.android.AndroidEntryPoint

const val rememberScopedDestination = "rememberScopedDestination"
const val hiltViewModelScopedDestination = "hiltViewModelScopedDestination"


@AndroidEntryPoint // This annotation is required for Hilt to work anywhere inside this Activity
class ComposeActivity : ComponentActivity() {

    companion object {
        const val START_DESTINATION = "START_DESTINATION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ResacaAppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val startDestination = intent.extras?.getString(START_DESTINATION) ?: rememberScopedDestination
                    ScreensWithNavigation(startDestination = startDestination)
                }
            }
        }
    }
}

@Composable
fun ScreensWithNavigation(navController: NavHostController = rememberNavController(), startDestination: String = rememberScopedDestination) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(rememberScopedDestination) {
            ComposeScreenWithRememberScoped(navController)
        }
        composable(hiltViewModelScopedDestination) {
            ComposeScreenWithViewModelScoped(navController)
        }
    }
}

@Composable
private fun ComposeScreenWithRememberScoped(navController: NavHostController) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DemoNotScopedObjectComposable()
        DemoScopedObjectComposable()
        DemoScopedViewModelComposable()
        DemoScopedParametrizedViewModelComposable()
        NavigationButtons(navController)
    }
}

@Composable
private fun ComposeScreenWithViewModelScoped(navController: NavHostController) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DemoNotScopedObjectComposable()
        DemoScopedObjectComposable()
        DemoScopedInjectedViewModelComposable()
        Text(
            modifier = Modifier.padding(8.dp),
            text = "The Hilt ViewModel below will be shown in light mode and garbage collected in dark mode"
        )
        // The Hilt Injected ViewModel is only shown in light mode, to demo how the ViewModel is properly garbage collected in a different config (dark mode)
        if (!isSystemInDarkTheme()) {
            DemoScopedInjectedViewModelComposable()
        }
        NavigationButtons(navController)
    }
}

@Composable
fun NavigationButtons(navController: NavHostController) {
    Button(modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        onClick = { navController.navigate(rememberScopedDestination) }) {
        Text(text = "Push rememberScoped destination")
    }
    Button(modifier = Modifier
        .padding(vertical = 4.dp)
        .testTag("hiltViewModelScopedDestinationTestTag"),
        onClick = { navController.navigate(hiltViewModelScopedDestination) }) {
        Text(text = "Push Hilt hiltViewModelScoped destination")
    }
    val activity = (LocalContext.current as? Activity)
    Button(modifier = Modifier.padding(vertical = 4.dp),
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