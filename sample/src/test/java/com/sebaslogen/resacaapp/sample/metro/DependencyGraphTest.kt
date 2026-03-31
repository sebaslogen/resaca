package com.sebaslogen.resacaapp.sample.metro

import com.sebaslogen.resacaapp.sample.di.metro.MetroAppGraph
import dev.zacsweers.metro.createGraph
import org.junit.Test
import kotlin.test.assertNotNull

class DependencyGraphTest {

    @Test
    fun `verify Metro dependency graph can be created without runtime errors`() {
        // Tests that the Metro graph is correctly defined and it should not crash at runtime
        val graph = createGraph<MetroAppGraph>()
        // Verify that graph accessors work
        val simpleVM = graph.simpleInjectedViewModel
        assertNotNull(simpleVM, "simpleInjectedViewModel should not be null")

        val secondVM = graph.secondInjectedViewModel
        assertNotNull(secondVM, "secondInjectedViewModel should not be null")

        val factory = graph.injectedViewModelFactory
        assertNotNull(factory, "injectedViewModelFactory should not be null")

        val assistedVM = factory.create(viewModelId = 42)
        assert(assistedVM.viewModelId == 42) { "Assisted injection should pass viewModelId correctly" }
    }
}
