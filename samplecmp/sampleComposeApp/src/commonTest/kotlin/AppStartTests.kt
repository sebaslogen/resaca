import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlin.test.Test

class AppStartTests {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun myTest() = runComposeUiTest {
        // Declares a mock UI to demonstrate API calls
        //
        // Replace with your own declarations to test the code of your project
        val storeOwner = object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore
                get() = ViewModelStore()
        }
        val lifecycleOwner = object : androidx.lifecycle.LifecycleOwner {
            override val lifecycle: Lifecycle = LifecycleRegistry(this)
        }

        setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides storeOwner,
                LocalLifecycleOwner provides lifecycleOwner,
    //            LocalSavedStateRegistryOwner provides this
            ) {
                Content()
    //            var text by remember { mutableStateOf("Hello") }
    //            Text(
    //                text = text,
    //                modifier = Modifier.testTag("text")
    //            )
    //            Button(
    //                onClick = { text = "Compose" },
    //                modifier = Modifier.testTag("button")
    //            ) {
    //                Text("Click me")
    //            }
            }
        }

//        // Tests the declared UI with assertions and actions of the Compose Multiplatform testing API
//        onNodeWithTag("text").assertTextEquals("Hello")
//        onNodeWithTag("button").performClick()
//        onNodeWithTag("text").assertTextEquals("Compose")
        // TODO: Build some tests for the Compose UI on iOS
    }
}