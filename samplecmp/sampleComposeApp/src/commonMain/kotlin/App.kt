import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import resaca.samplecmp.samplecomposeapp.generated.resources.Res
import resaca.samplecmp.samplecomposeapp.generated.resources.compose_multiplatform


@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by rememberSaveable { mutableStateOf(false) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
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
                    Image(painterResource(Res.drawable.compose_multiplatform), null)
                    Text("Compose: $greeting")
                }
            }
        }
    }
}