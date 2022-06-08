package com.sebaslogen.resacaapp.ui.main.data

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * This is a fake [ViewModel] with dependencies that will be injected by Hilt.
 * @param stateSaver A dependency provided by the Android and Hilt frameworks to save and restore state in a [Bundle]
 * @param repository Sample of a common dependency on a project's object created by Hilt
 */
@HiltViewModel
class FakeInjectedViewModel @Inject constructor(private val stateSaver: SavedStateHandle, private val repository: FakeInjectedRepo) : ViewModel()