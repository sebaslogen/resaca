package com.sebaslogen.resacaapp.sample.ui.main.compose.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sebaslogen.resaca.rememberKeysInScope
import com.sebaslogen.resaca.viewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.NavigationButtons
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeScopedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.NumberContainer


private val listItems = (1..1000).toList().map { NumberContainer(it) }

@Composable
fun ComposeScreenWithSingleViewModelScopedWithKeys(navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavigationButtons(navController)
        Text(
            modifier = Modifier.padding(8.dp),
            text = "The list below contains one ViewModel per row that will stay in memory due to KeysInScope as long as the list and screen are displayed"
        )
        val keys = rememberKeysInScope(inputListOfKeys = listItems)
        LazyColumn(modifier = Modifier.fillMaxHeight().testTag("LazyList")) {
            items(items = listItems, key = { it.number }) { item ->
                val fakeScopedVM: FakeScopedViewModel = viewModelScoped(key = item, keyInScopeResolver = keys)
                DemoComposable(inputObject = fakeScopedVM, objectType = "FakeScopedViewModel $item", scoped = true)
            }
        }
    }
}