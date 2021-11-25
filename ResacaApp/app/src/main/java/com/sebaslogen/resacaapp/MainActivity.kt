package com.sebaslogen.resacaapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.sebaslogen.resaca.compose.installScopedViewModelContainer
import com.sebaslogen.resacaapp.ui.main.MainFragment
import com.sebaslogen.resacaapp.ui.main.compose.DemoScreenInActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
        findViewById<ComposeView>(R.id.activity_composable)?.apply {
            // This is required so the Compose Views follow the lifecycle of the Activity, not the Window
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                installScopedViewModelContainer() // ScopedViewModelContainer will live in the scope of this Activity
                CreateActivityComposeContent(::navigateToComposeActivity)
            }
        }
    }

    private fun navigateToComposeActivity() {
        //TODO
    }
}

@Composable
fun CreateActivityComposeContent(clickListener: () -> Unit) {
    MaterialTheme(colors = MaterialTheme.colors.copy(primary = Color.Gray)) {
        DemoScreenInActivity(clickListener)
    }
}