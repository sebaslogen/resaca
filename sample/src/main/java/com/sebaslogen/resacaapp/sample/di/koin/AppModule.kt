package com.sebaslogen.resacaapp.sample.di.koin

import androidx.lifecycle.SavedStateHandle
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedRepo
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeScopedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeSecondInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeSimpleInjectedViewModel
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.util.concurrent.atomic.AtomicInteger

/**
 * This module defines the Koin dependencies that can be created by the DI framework and how to build them.
 */
val appModule = module {
    factory { FakeInjectedRepo() }
    factory { viewModelsClearedGloballySharedCounter }
    viewModelOf(::FakeSimpleInjectedViewModel)
    viewModel { (savedStateHandle: SavedStateHandle, id: Int) -> FakeScopedViewModel(stateSaver = get(), viewModelId = id) }
    viewModelOf(::FakeSecondInjectedViewModel)
//    viewModelOf(::FakeInjectedViewModel) // Instead of this,
//    you can use the constructor below to pass arguments at call site (assisted injection)
    viewModel { (counter: AtomicInteger, id: Int) -> // Assisted injection of viewModelsClearedCounter constructor parameter
        FakeInjectedViewModel(stateSaver = get(), repository = get(), viewModelsClearedCounter = counter, viewModelId = id)
    }
}
