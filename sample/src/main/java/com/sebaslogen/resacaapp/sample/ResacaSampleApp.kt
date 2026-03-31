package com.sebaslogen.resacaapp.sample

import android.app.Application
import com.sebaslogen.resacaapp.sample.di.koin.appModule
import com.sebaslogen.resacaapp.sample.di.metro.MetroAppGraph
import dagger.hilt.android.HiltAndroidApp
import dev.zacsweers.metro.createGraph
import org.koin.android.ext.koin.androidContext

import org.koin.core.context.startKoin

@HiltAndroidApp
class ResacaSampleApp : Application() {

    companion object {
        lateinit var metroGraph: MetroAppGraph
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Metro DI graph
        metroGraph = createGraph<MetroAppGraph>()

        // Initialize Koin DI framework
        startKoin {
            // Reference Android context
            androidContext(this@ResacaSampleApp)
            // Load modules
            modules(appModule)
        }
    }
}
