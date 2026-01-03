package com.sebaslogen.resacaapp.sample.ui.main.compose.examples

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.sebaslogen.resaca.hilt.hiltViewModelScoped
import com.sebaslogen.resacaapp.sample.ui.main.compose.DemoComposable
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeAssistedInjectionViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedRepo
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeInjectedViewModel
import com.sebaslogen.resacaapp.sample.ui.main.data.FakeSecondInjectedViewModel
import com.sebaslogen.resacaapp.sample.viewModelsClearedGloballySharedCounter
import kotlin.random.Random

/**
 * Create a [ViewModel] with resaca's [hiltViewModelScoped] function to let
 * Hilt provide the [ViewModel] with all the required dependencies and
 * Resaca handle the lifecycle of the provided [ViewModel].
 *
 * Assisted Injection (i.e. provide some dependencies manually to the [ViewModel] constructor and let Hilt provide the rest)
 * is supported using the [hiltViewModelScoped] and creationCallback with the [@AssistedFactory],
 * for more info and instructions see https://github.com/sebaslogen/resaca/blob/main/resacahilt/README.md#hilt
 */
@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedHiltInjectedViewModelComposable(key: String? = null, fakeInjectedViewModelId: Int = 666) {
    val fakeInjectedVM: FakeInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use hiltViewModelScoped
            FakeInjectedViewModel(
                stateSaver = SavedStateHandle(),
                repository = FakeInjectedRepo(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter,
                viewModelId = fakeInjectedViewModelId
            )
        } else {
            hiltViewModelScoped(key = key) { factory: FakeInjectedViewModel.FakeInjectedViewModelFactory ->
                factory.create(
                    viewModelId = fakeInjectedViewModelId
                )
            }
        }
    DemoComposable(inputObject = fakeInjectedVM, objectType = "Hilt FakeInjectedViewModel", scoped = true)
}

@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedSecondHiltInjectedViewModelComposable() {
    val fakeSecondInjectedVM: FakeSecondInjectedViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use hiltViewModelScoped
            FakeSecondInjectedViewModel(
                stateSaver = SavedStateHandle(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter
            )
        } else {
            hiltViewModelScoped()
        }
    DemoComposable(inputObject = fakeSecondInjectedVM, objectType = "Hilt FakeSecondInjectedViewModel", scoped = true)
}

@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoScopedHiltAssistedInjectionViewModelComposable(key: String? = null, fakeInjectedViewModelId: Int = 666) {
    val fakeInjectedVM: FakeAssistedInjectionViewModel =
        if (LocalInspectionMode.current) { // In Preview we can't use hiltViewModelScoped
            FakeAssistedInjectionViewModel(
                stateSaver = SavedStateHandle(),
                repository = FakeInjectedRepo(),
                viewModelsClearedCounter = viewModelsClearedGloballySharedCounter,
                viewModelId = fakeInjectedViewModelId
            )
        } else {
            hiltViewModelScoped(key = key) { factory: FakeAssistedInjectionViewModel.FakeAssistedInjectionViewModelFactory ->
                factory.create(
                    viewModelId = fakeInjectedViewModelId
                )
            }
        }
    DemoComposable(inputObject = fakeInjectedVM, objectType = "Hilt FakeAssistedInjectionViewModel", scoped = true)
}

/**
 * Button that opens a Dialog with a randomly generated ID.
 * The Dialog uses [hiltViewModelScoped] with the random ID as the id for each unique
 * ViewModel instance on each dialog opening.
 */
@SuppressLint("ViewModelConstructorInComposable") // This is only used for previews
@Composable
fun DemoDialogWithRandomIdHiltViewModel() {
    var showDialog by rememberSaveable { mutableStateOf(false) }

    Column {
        var dialogId by rememberSaveable { mutableIntStateOf(0) }
        Button(onClick = {
            dialogId = Random.nextInt() // Generate a new random ID each time
            showDialog = true
        }) {
            Text("Open Dialog with Random ID ViewModel")
        }

        if (showDialog) {
            val fakeInjectedVM: FakeInjectedViewModel =
                if (LocalInspectionMode.current) { // In Preview we can't use hiltViewModelScoped
                    FakeInjectedViewModel(
                        stateSaver = SavedStateHandle(),
                        repository = FakeInjectedRepo(),
                        viewModelsClearedCounter = viewModelsClearedGloballySharedCounter,
                        viewModelId = dialogId
                    )
                } else {
                    hiltViewModelScoped { factory: FakeInjectedViewModel.FakeInjectedViewModelFactory ->
                        factory.create(
                            viewModelId = dialogId
                        )
                    }
                }

            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Dialog with saved Random ID: ${fakeInjectedVM.savedId}") },
                text = {
                    DemoComposable(
                        inputObject = fakeInjectedVM,
                        objectType = "Hilt FakeInjectedViewModel in Dialog",
                        scoped = true
                    )
                },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
