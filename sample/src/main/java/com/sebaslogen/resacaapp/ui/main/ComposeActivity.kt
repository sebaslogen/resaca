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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sebaslogen.resacaapp.ui.main.compose.DemoNotScopedObjectComposable
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedInjectedViewModelComposable
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedObjectComposable
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedViewModelComposable
import com.sebaslogen.resacaapp.ui.main.ui.theme.ResacaAppTheme
import dagger.hilt.android.AndroidEntryPoint

const val rememberScopedDestination = "rememberScopedDestination"
const val viewModelScopedDestination = "viewModelScopedDestination"

@AndroidEntryPoint
class ComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResacaAppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    ScreensWithNavigation()
                }
            }
        }
    }
}

@Composable
fun ScreensWithNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = rememberScopedDestination) {
        composable(rememberScopedDestination) {
            ComposeScreenWithRememberScoped(navController)
        }
        composable(viewModelScopedDestination) {
            ComposeScreenWithViewModelScoped(navController)
        }
    }
}

// TODO: docs: Hilt App -> Activity/Fragment Hilt -> VM Hilt + Inject -> Constructor Inject

@Composable
private fun ComposeScreenWithRememberScoped(navController: NavHostController) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DemoNotScopedObjectComposable()
        DemoScopedObjectComposable()
        DemoScopedViewModelComposable()
        NavigationButtons(navController)
    }
}

@Composable
private fun ComposeScreenWithViewModelScoped(navController: NavHostController) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DemoNotScopedObjectComposable()
        // The Hilt Injected ViewModel is only shown in light mode, to demo how the ViewModel is properly garbage collected in a different config (dark mode)
        if (!isSystemInDarkTheme()) { // TODO: Fix tests
            DemoScopedInjectedViewModelComposable()
        }
        NavigationButtons(navController)
    }
}

@Composable
fun NavigationButtons(navController: NavHostController) {
    Button(modifier = Modifier.padding(vertical = 4.dp),
        onClick = { navController.navigate(rememberScopedDestination) }) {
        Text(text = "Push rememberScoped destination")
    }
    Button(modifier = Modifier.padding(vertical = 4.dp),
        onClick = { navController.navigate(viewModelScopedDestination) }) {
        Text(text = "Push Hilt viewModelScoped destination")
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