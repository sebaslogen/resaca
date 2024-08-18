package com.sebaslogen.resaca.viewmodel

import androidx.lifecycle.ViewModelProvider

/**
 * This object provides a default implementation of [ViewModelProvider.Factory] that creates a new instance of a ViewModel using the default constructor.
 */
internal expect object DefaultViewModelProviderFactory : ViewModelProvider.Factory