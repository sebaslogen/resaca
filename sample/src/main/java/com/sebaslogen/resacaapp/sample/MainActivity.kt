package com.sebaslogen.resacaapp.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.sebaslogen.resacaapp.sample.ui.main.ComposeActivity
import com.sebaslogen.resacaapp.sample.ui.main.MainFragment
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoScreenInActivity
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeRepo
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeScopedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.navigateToFragmentTwo
import java.io.Closeable
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Global counter to track the number of scoped [Closeable] ([FakeRepo]) that have been correctly closed
 */
val closeableClosedGloballySharedCounter: AtomicLong = AtomicLong(0)

/**
 * Global counter to track the number of scoped ViewModels ([FakeScopedViewModel]) that have been correctly cleared
 */
val viewModelsClearedGloballySharedCounter: AtomicInteger = AtomicInteger(0)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
        title = "ComposeViews in Activity & Fragment"
        findViewById<ComposeView>(R.id.activity_composable)?.apply {
            // This is required so the Compose Views follow the lifecycle of the Activity, not the Window
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CreateActivityComposeContent(::navigateToComposeActivity)
            }
        }
    }

    @Composable
    fun CreateActivityComposeContent(clickListener: () -> Unit) {
        Surface(color = MaterialTheme.colors.background.copy(alpha = 0.5f)) {
            Column(Modifier.fillMaxWidth()) {
                IconButton( // Recreate Activity on Refresh button pressed to test scoped objects
                    modifier = Modifier.align(Alignment.End),
                    onClick = { recreate() }
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Recreate Activity")
                }
                DemoScreenInActivity(clickListener)
            }
        }
    }

    private fun navigateToComposeActivity() {
        startActivity(Intent(this, ComposeActivity::class.java))
    }

    fun navigateToFragmentTwo() {
        supportFragmentManager.navigateToFragmentTwo()
    }
}
