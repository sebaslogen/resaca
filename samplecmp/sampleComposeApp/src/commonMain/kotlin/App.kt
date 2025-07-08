@file:OptIn(ExperimentalTime::class)

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sebaslogen.resaca.rememberScoped
import kotlin.time.Clock
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

@Composable
@Preview
fun App() {
    MaterialTheme {
        Scaffold { paddingValues ->
            Content(paddingValues)
        }
    }
}

@Composable
fun Content(paddingValues: PaddingValues) {
    var showContent by rememberSaveable { mutableStateOf(false) }
    Column(
        Modifier
            .safeContentPadding()
            .padding(paddingValues)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { showContent = !showContent }) {
            Text("Click me!")
        }
        val g: String = rememberScoped {
            "Hello! ${Clock.System.now().epochSeconds}"
        }
        Text("Compose: $g")
        AnimatedVisibility(showContent) {
            val greeting: String = rememberScoped {
                "Hello, Compose Multiplatform! ${Clock.System.now().epochSeconds}"
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Compose: $greeting")
            }
        }
    }
}
