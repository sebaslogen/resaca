@file:OptIn(ResacaPackagePrivate::class)

package com.sebaslogen.resaca

import androidx.compose.runtime.Composable
import com.sebaslogen.resaca.utils.ResacaPackagePrivate

@Composable
@PublishedApi
internal expect fun ObserveComposableContainerLifecycle(scopedViewModelContainer: ScopedViewModelContainer)