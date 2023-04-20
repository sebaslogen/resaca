package com.sebaslogen.resacaapp.sample

import android.app.Application
import com.sebaslogen.resacaapp.sample.di.koin.appModule
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext

import org.koin.core.context.startKoin

@HiltAndroidApp
class ResacaSampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Koin DI framework
        startKoin {
            // Reference Android context
            androidContext(this@ResacaSampleApp)
            // Load modules
            modules(appModule)
        }
    }
}