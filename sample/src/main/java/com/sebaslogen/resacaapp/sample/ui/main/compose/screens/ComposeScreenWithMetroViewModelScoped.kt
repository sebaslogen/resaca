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
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoNotScopedObjectComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedMetroInjectedObjectComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedMetroInjectedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedMetroInjectedViewModelWithClearDelayComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedMetroSimpleInjectedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedObjectComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedSecondMetroInjectedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.showSingleScopedViewModel

@Composable
fun ComposeScreenWithMetroViewModelScoped(navController: NavHostController) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DemoNotScopedObjectComposable()
        DemoScopedObjectComposable()
        DemoScopedMetroInjectedObjectComposable()
        DemoScopedMetroSimpleInjectedViewModelComposable()
        Text(
            modifier = Modifier.padding(8.dp),
            text = "The Metro ViewModel below will be shown in light mode and garbage collected in dark mode"
        )
        // The Metro Injected ViewModel is only shown in light mode, to demo how the ViewModel is properly garbage collected in a different config (dark mode)
        if (showSingleScopedViewModel ?: !isSystemInDarkTheme()) {
            DemoScopedMetroInjectedViewModelComposable()
            DemoScopedMetroInjectedViewModelWithClearDelayComposable()
        }
        DemoScopedSecondMetroInjectedViewModelComposable()
        NavigationButtons(navController)
    }
}
