package com.sebaslogen.resacaapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.sebaslogen.resaca.compose.installScopedViewModelContainer
import com.sebaslogen.resacaapp.R
import com.sebaslogen.resacaapp.ui.main.compose.DemoScreenInFragment

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false).apply {
            this.findViewById<ComposeView>(R.id.composeContainer)?.apply {
                // This is required so the Compose Views follow the lifecycle of the Fragment, not the Window
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    installScopedViewModelContainer() // ScopedViewModelContainer will live in the scope of this Fragment
                    CreateFragmentComposeContent(::navigateToFragmentTwo)
                }
            }
        }
    }

    private fun navigateToFragmentTwo() {
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container, FragmentTwo())
            .addToBackStack(null)
            .commit()
    }
}

@Composable
fun CreateFragmentComposeContent(clickListener: () -> Unit) {
    MaterialTheme(colors = MaterialTheme.colors.copy(primary = Color.DarkGray)) {
        DemoScreenInFragment(clickListener)
    }
}