package com.sebaslogen.resacaapp.sample.ui.main.dependencyinjection

import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import java.util.concurrent.atomic.AtomicInteger

@Module
@InstallIn(ViewModelComponent::class)
object ClearedCounterModule {

    // This binding is "scoped" (even though the returned object is a global fixed object)
    @Provides
    @ViewModelScoped
    fun provideViewModelClearedCounterScopedBinding(): AtomicInteger = viewModelsClearedGloballySharedCounter
}
