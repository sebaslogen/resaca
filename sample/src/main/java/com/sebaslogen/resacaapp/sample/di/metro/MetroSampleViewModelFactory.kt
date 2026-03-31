package com.sebaslogen.resacaapp.sample.di.metro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeMetroInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeMetroSecondInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeMetroSimpleInjectedViewModel

/**
 * Custom [ViewModelProvider.Factory] that uses the Metro [MetroAppGraph] to create ViewModels.
 * Each call to [create] creates a new ViewModel instance using the graph's dependency injection.
 *
 * For simple ViewModels (without assisted injection), this factory accesses the graph's
 * properties to get new instances with all dependencies injected.
 */
class MetroSampleViewModelFactory(private val graph: MetroAppGraph) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(FakeMetroSimpleInjectedViewModel::class.java) ->
            graph.simpleInjectedViewModel as T

        modelClass.isAssignableFrom(FakeMetroSecondInjectedViewModel::class.java) ->
            graph.secondInjectedViewModel as T

        else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Creates a [ViewModelProvider.Factory] for [FakeMetroInjectedViewModel] using Metro's assisted injection.
 * The [viewModelId] parameter is passed to the Metro-generated assisted factory at ViewModel creation time.
 *
 * @param graph The Metro dependency graph providing the assisted factory.
 * @param viewModelId The assisted parameter to inject into the ViewModel.
 */
fun createMetroAssistedFactory(
    graph: MetroAppGraph,
    viewModelId: Int
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return graph.injectedViewModelFactory.create(viewModelId) as T
    }
}
