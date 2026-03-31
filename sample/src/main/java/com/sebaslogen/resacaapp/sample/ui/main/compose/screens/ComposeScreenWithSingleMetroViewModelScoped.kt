package com.sebaslogen.resacaapp.sample.ui.main.compose.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.sebaslogen.resacaapp.sample.ui.main.NavigationButtons
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedMetroInjectedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.showSingleScopedViewModel

@Composable
fun ComposeScreenWithSingleMetroViewModelScoped(navController: NavHostController) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // The Metro Injected ViewModel is only shown in light mode, to demo how the ViewModel is properly garbage collected in a different config (dark mode)
        if (showSingleScopedViewModel ?: !isSystemInDarkTheme()) {
            DemoScopedMetroInjectedViewModelComposable()
        }
        NavigationButtons(navController)
    }
}
