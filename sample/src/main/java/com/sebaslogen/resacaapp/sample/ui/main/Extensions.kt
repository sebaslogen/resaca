package com.sebaslogen.resacaapp.sample.ui.main

import androidx.fragment.app.FragmentManager
import com.sebaslogen.resacaapp.sample.R

fun Int.toHexString(): String = Integer.toHexString(this)

fun FragmentManager.navigateToFragmentTwo() = this
    .beginTransaction()
    .replace(R.id.container, FragmentTwo())
    .addToBackStack(null)
    .commit()