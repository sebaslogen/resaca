@file:OptIn(ExperimentalTime::class)

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.sebaslogen.resaca.rememberScoped
import kotlin.time.Clock
import kotlin.test.Test
import kotlin.time.ExperimentalTime

class AppStartTests {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun myTest() = runComposeUiTest {

        // Setup test infrastructure a ViewModelStoreOwner and a LifecycleOwner
        val storeOwner = object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore
                get() = ViewModelStore()
        }
        val lifecycleOwner = object : androidx.lifecycle.LifecycleOwner {
            override val lifecycle: Lifecycle = LifecycleRegistry(this)
        }

        // Given the starting screen with a scoped object
        val textTitle = "Test text"
        setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides storeOwner,
                LocalLifecycleOwner provides lifecycleOwner,
                //            LocalSavedStateRegistryOwner provides this
            ) {
                Column {
                    Text(textTitle)
                    val g: String = rememberScoped {
                        "Hello! ${Clock.System.now().epochSeconds}"
                    }
                    Text(
                        text = "Compose: $g",
                        modifier = Modifier.testTag("text")
                    )
                }
            }
        }

        // Then check that the text is displayed
        onNodeWithTag("text").assertTextContains("1", substring = true)
        val readText = (onNodeWithTag("text").fetchSemanticsNode().config.first { it.key.name == "Text" }
            .value as List<*>).first().toString()
        println("readText: $readText")
    }
}
