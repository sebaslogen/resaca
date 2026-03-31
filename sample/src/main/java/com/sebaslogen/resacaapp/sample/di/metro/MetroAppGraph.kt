package com.sebaslogen.resacaapp.sample.di.metro

import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedRepo
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeMetroInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeMetroSecondInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeMetroSimpleInjectedViewModel
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import java.util.concurrent.atomic.AtomicInteger

/**
 * Metro dependency graph that provides all dependencies for the Metro-based ViewModels.
 * This graph is created at app startup and used to build ViewModels via [MetroSampleViewModelFactory].
 */
@DependencyGraph
abstract class MetroAppGraph {

    @Provides
    fun provideRepo(): FakeInjectedRepo = FakeInjectedRepo()

    @Provides
    fun provideCounter(): AtomicInteger = viewModelsClearedGloballySharedCounter

    /**
     * Accessor for creating [FakeMetroSimpleInjectedViewModel] instances.
     * Metro will inject all dependencies via the @Inject constructor.
     */
    abstract val simpleInjectedViewModel: FakeMetroSimpleInjectedViewModel

    /**
     * Accessor for creating [FakeMetroSecondInjectedViewModel] instances.
     * Metro will inject all dependencies via the @Inject constructor.
     */
    abstract val secondInjectedViewModel: FakeMetroSecondInjectedViewModel

    /**
     * Accessor for the assisted factory of [FakeMetroInjectedViewModel].
     * Metro generates an implementation of this factory at compile time.
     */
    abstract val injectedViewModelFactory: FakeMetroInjectedViewModel.FakeMetroInjectedViewModelFactory
}
