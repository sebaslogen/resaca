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
import com.sebaslogen.resaca.core.rememberKeysInScope
import com.sebaslogen.resaca.core.rememberScoped
import com.sebaslogen.resacaapp.sample.ui.main.NavigationButtons
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.compose.examples.DemoScopedViewModelComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeRepo
import com.sebaslogen.resacaapp.sample.ui.main.showSingleScopedViewModel

/**
 * This Screen is only used in automated tests
 */
@Composable
fun ComposeScreenWithSingleViewModelScoped(navController: NavHostController) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = "The objects below will be shown only in light mode and ViewModel will be garbage collected in dark mode"
        )
        // The ViewModel is only shown in light mode, to demo how the ViewModel is properly garbage collected in a different config (dark mode)
        val key = "MyKey"
        val keys = rememberKeysInScope(inputListOfKeys = listOf(key))
        if (showSingleScopedViewModel ?: !isSystemInDarkTheme()) {
            DemoScopedViewModelComposable()
            Text(
                modifier = Modifier.padding(8.dp),
                text = "The FakeRepo (only visible in light mode) will survive being disposed of in dark mode due to KeysInScope"
            )
            val fakeRepo: FakeRepo = rememberScoped(key = key, keyInScopeResolver = keys) { FakeRepo() }
            DemoComposable(inputObject = fakeRepo, objectType = "FakeRepo", scoped = true)
        }
        NavigationButtons(navController)
    }
}