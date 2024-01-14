package com.sebaslogen.resacaapp.sample.ui.main.data

import dagger.assisted.AssistedFactory

@AssistedFactory
interface FakeAssistedInjectionViewModelFactory {
    fun create(viewModelId: Int): FakeAssistedInjectionViewModel
}
