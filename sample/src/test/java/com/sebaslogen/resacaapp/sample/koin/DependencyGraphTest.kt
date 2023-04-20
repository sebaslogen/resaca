package com.sebaslogen.resacaapp.sample.koin

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.sebaslogen.resacaapp.sample.di.koin.appModule
import org.junit.Test
import org.koin.android.test.verify.verify

class DependencyGraphTest {

    @Test
    fun checkKoinModule() {
        // Tests that the Koin module is correctly defined and it should not crash at runtime
        appModule.verify(
            extraTypes = listOf(
                Context::class,
                SavedStateHandle::class
            )
        )
    }
}