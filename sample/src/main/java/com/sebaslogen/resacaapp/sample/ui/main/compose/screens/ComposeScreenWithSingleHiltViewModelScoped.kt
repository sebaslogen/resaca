package com.sebaslogen.resacaapp.sample.ui.main.compose.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sebaslogen.resacaapp.sample.ui.main.NavigationButtons
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedHiltInjectedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.showSingleScopedViewModel

/**
 * This Screen is only used in automated tests
 */
@Composable
fun ComposeScreenWithSingleHiltViewModelScoped(navController: NavHostController) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = "The ViewModel below will be shown in light mode and garbage collected in dark mode"
        )
        // The ViewModel is only shown in light mode, to demo how the ViewModel is properly garbage collected in a different config (dark mode)
        if (showSingleScopedViewModel ?: !isSystemInDarkTheme()) {
            DemoScopedHiltInjectedViewModelComposable()
        }
        NavigationButtons(navController)
    }
}