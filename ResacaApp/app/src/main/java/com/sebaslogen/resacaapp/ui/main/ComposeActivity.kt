package com.sebaslogen.resacaapp.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sebaslogen.resaca.compose.installScopedViewModelContainer
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedObjectComposable
import com.sebaslogen.resacaapp.ui.main.compose.DemoScopedViewModelComposable
import com.sebaslogen.resacaapp.ui.main.ui.theme.ResacaAppTheme

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResacaAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    ScreensWithNavigation()
                }
            }
        }
    }
}

@Composable
fun ScreensWithNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "first") {
        composable("first") {
            it.installScopedViewModelContainer()
            DemoScopedObjectComposable()
            DemoScopedViewModelComposable()
            NavigationButtons(navController)
        }
        composable("second") {
            it.installScopedViewModelContainer()
            DemoScopedObjectComposable()
            DemoScopedViewModelComposable()
            NavigationButtons(navController)
        }
    }
}

@Composable
fun NavigationButtons(navController: NavHostController) {
    Button(onClick = { navController.navigate("first") }) {
        Text(text = "Push first destination")
    }
    Button(onClick = { navController.navigate("second") }) {
        Text(text = "Push second destination")
    }
    Button(onClick = { navController.popBackStack() }) {
        Text(text = "Go to BACK")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ResacaAppTheme {
        ScreensWithNavigation()
    }
}