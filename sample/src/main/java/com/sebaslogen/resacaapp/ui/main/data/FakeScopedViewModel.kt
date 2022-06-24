package com.sebaslogen.resacaapp.ui.main.data

import androidx.lifecycle.ViewModel
import com.sebaslogen.resacaapp.viewModelsClearedGloballySharedCounter
import java.util.concurrent.atomic.AtomicInteger

/**
 * ViewModel used to test the lifecycle of the app and the library.
 * @param viewModelsClearedCounter Is a counter to inform the providers of this parameter that this ViewModel has been correctly cleared
 *                                 A default value is required to use the vanilla [viewModelScoped] which cannot pass constructor parameters to [ViewModels]
 */
class FakeScopedViewModel(private val viewModelsClearedCounter: AtomicInteger = viewModelsClearedGloballySharedCounter) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        viewModelsClearedCounter.incrementAndGet()
    }
}