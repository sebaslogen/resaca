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
    NavHost(navController = navController, startDestination = "first") {
        composable("first") {
            ComposeScreenWithNavigation(navController)
        }
        composable("second") {
            ComposeScreenWithNavigation(navController)
        }
    }
}

// TODO: docs: Hilt App -> Activity/Fragment Hilt -> VM Hilt + Inject -> Constructor Inject

@Composable
private fun ComposeScreenWithNavigation(navController: NavHostController) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DemoNotScopedObjectComposable()
        DemoScopedObjectComposable()
        DemoScopedViewModelComposable()
        if (!isSystemInDarkTheme()) { // TODO: Add this for demo and fix tests
            DemoScopedInjectedViewModelComposable("0")
            DemoScopedInjectedViewModelComposable("1") // Injecting the same type twice will reuse the existing instance
        }
        NavigationButtons(navController)
    }
}

@Composable
fun NavigationButtons(navController: NavHostController) {
    Button(modifier = Modifier.padding(vertical = 4.dp),
        onClick = { navController.navigate("first") }) {
        Text(text = "Push first destination")
    }
    Button(modifier = Modifier.padding(vertical = 4.dp),
        onClick = { navController.navigate("second") }) {
        Text(text = "Push second destination")
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