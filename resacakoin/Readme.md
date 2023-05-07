[![Build Status](https://github.com/sebaslogen/resaca/actions/workflows/build.yml/badge.svg)](https://github.com/sebaslogen/resaca/actions/workflows/build.yml)
[![Release](https://jitpack.io/v/sebaslogen/resaca.svg)](https://jitpack.io/#sebaslogen/resaca)
[![API 21+](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![GitHub license](https://img.shields.io/github/license/sebaslogen/resaca)](https://github.com/sebaslogen/resaca/blob/main/LICENSE)

# Resaca Koin 🍹🪙

Short lived View Models provided by [**Koin**](https://insert-koin.io/docs/reference/koin-android/start/) with the right scope in
Android [Compose](https://developer.android.com/jetpack/compose).

> Note: This library (`com.github.sebaslogen.resaca:resacakoin`) is **only required if you want to use ViewModels with a [SavedStateHandle](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate)**
construtor parameter. If this is not your case, you can simply use the base resaca library (`com.github.sebaslogen.resaca:resaca`) with `viewModelScoped` function in combination with Koin getters, [see example](https://github.com/sebaslogen/resaca/blob/main/README.md#koin-).

# Why

Compose allows the creation of fine-grained UI components that can be easily reused like Lego blocks 🧱. Well architected Android apps isolate functionality in
small business logic components (like use cases, interactors, repositories, etc.) that are also reusable like Lego blocks 🧱.

Screens are built using Compose components together with business logic components, and the standard tool to connect these two types of components is
a [Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel). Unfortunately, ViewModels can only be scoped to a whole screen (or
larger scope), but not to smaller Compose components on the screen.

In practice, this means that we are gluing UI Lego blocks with business logic Lego blocks using a big glue class for the whole screen, the ViewModel 🗜.

Until now...

# Installation

Add the Jitpack repo and include the library:

<details open>
  <summary>Kotlin (KTS)</summary>
  
```kotlin
// In settings.gradle.kts
dependencyResolutionManagement {
    repositories {
         [..]
         maven { setUrl("https://jitpack.io") }
    }
}
// In module's build.gradle.kts
dependencies {
    // The latest version of the lib is available in the badget at the top, replace X.X.X with that version
    implementation("com.github.sebaslogen.resaca:resacakoin:X.X.X")
}
```
</details>

<details>
  <summary>Groovy</summary>
  
```gradle
allprojects {
    repositories {
        [..]
        maven { url "https://jitpack.io" }
    }
}
dependencies {
    // The latest version of the lib is available in the badget at the top, replace X.X.X with that version
    implementation 'com.github.sebaslogen.resaca:resacakoin:X.X.X'
}
```
</details>

# Usage

Inside your `@Composable` function create and retrieve a ViewModel using `koinViewModelScoped` to remember any ViewModel that you have defined in your Koin `module`. This is all that's needed 🪄✨

Example

```kotlin
@Composable
fun DemoInjectedViewModelScoped() {
    val myInjectedViewModel: MyViewModel = koinViewModelScoped()
    DemoComposable(viewModel = myInjectedViewModel)
}

@Composable
fun DemoInjectedViewModelWithKey(keyOne: String = "myFirstKey", keyTwo: String = "mySecondKey") {
    val scopedVMWithFirstKey: MyViewModel = koinViewModelScoped(keyOne)
    val scopedVMWithSecondKey: MyViewModel = koinViewModelScoped(keyTwo)
    // We now have 2 instances on memory of the same ViewModel type, both inside the same Composable scope
    // When one key updates only the ViewModel with that key will be recreated
    DemoComposable(inputObject = scopedVMWithFirstKey)
    DemoComposable(inputObject = scopedVMWithSecondKey)
}

class MyViewModel(private val stateSaver: SavedStateHandle) : ViewModel()

@Composable
fun DemoInjectedViewModelWithId(idOne: String = "myFirstId", idTwo: String = "mySecondId") {
    val scopedVMWithFirstId: MyIdViewModel = koinViewModelScoped(key = idOne, parameters = { parametersOf(idOne) })
    val scopedVMWithSecondId: MyIdViewModel = koinViewModelScoped(key = idTwo, parameters = { parametersOf(idTwo) })
    // We now have 2 instances on memory of the same ViewModel type, both inside the same Composable scope
    // When one Id updates only the ViewModel with that Id will be recreated
    // Each ViewModel instance has its own Id
    DemoComposable(inputObject = scopedVMWithFirstId)
    DemoComposable(inputObject = scopedVMWithSecondId)
}

class MyIdViewModel(private val stateSaver: SavedStateHandle, private val id: String) : ViewModel()
```

Once you use the `koinViewModelScoped` function, the same object will be restored as long as the Composable is part of the composition, even if it _temporarily_
leaves composition on configuration change (e.g. screen rotation, change to dark mode, etc.) or while being in the backstack.

> ⚠️ Note that ViewModels provided with `koinViewModelScoped` **should not be created** using any of the Koin `koinViewModel()` or Compose `getViewModel()`
nor `ViewModelProviders` factories, otherwise they will be retained in the scope of the screen regardless of `koinViewModelScoped`.

# Basic Koin setup

To use the `koinViewModelScoped` function you need to follow these 2 Koin configuration steps:

- Add a configuration module variable with the Koin factories for your ViewModels. [See example here](https://github.com/sebaslogen/resaca/blob/main/sample/src/main/java/com/sebaslogen/resacaapp/sample/di/koin/AppModule.kt#L17)
- Initialize Koin with the module you just created inside the `onCreate` of your application. [See example here](https://github.com/sebaslogen/resaca/blob/main/sample/src/main/java/com/sebaslogen/resacaapp/sample/ResacaSampleApp.kt#L16)

For a complete guide to Koin check the official documentation. Here are the [setup](https://insert-koin.io/docs/setup/koin/) and
the [Koin ViewModel](https://insert-koin.io/docs/reference/koin-android/viewmodel) docs.

# Sample use cases

Here are some sample use cases reported by the users of this library:

- 📃📄 Multiple instances of the same type of ViewModel in a screen with a **view-pager**. This screen will have multiple sub-pages that use the same ViewModel
  class with different ids. For example, a screen of holiday destinations with multiple pages and each page with its own `HolidayDestinationViewModel`.
- ❤️ Isolated and stateful UI components like a **favorite button** that are widely used across the screens. This `FavoriteViewModel` can be very small, focused
  and only require an id to work without affecting the rest of the screen's UI and state.
- 🗪 **Dialog pop-ups** can have their own business-logic with state that is better to isolate in a separate ViewModel but the lifespan of these dialogs might be short, 
so it's important to clean-up the ViewModel associated to a Dialog after it has been closed.

# Assisted Injection

Assisted injection is a dependency injection (DI) pattern that is used to construct an object where some parameters may be provided by the DI framework and
others must be passed in at creation time (a.k.a “assisted”) by the user, in our case when the `koinViewModelScoped` is requested.

Assisted injection is supported by Koin out of the box with the `parametersOf()` syntax. 

When you declare the ViewModel factory in your Koin Module using the `viewModel {}` syntax, then 
you can [declare arguments in the factory](https://github.com/sebaslogen/resaca/blob/main/sample/src/main/java/com/sebaslogen/resacaapp/sample/di/koin/AppModule.kt#L25).
Finally, those arguments can be [passed as parameters at call time](https://github.com/sebaslogen/resaca/blob/aedb3de32b052668d21d1d6662d631b54da7636f/sample/src/main/java/com/sebaslogen/resacaapp/sample/ui/main/compose/examples/KoinInjectedViewModel.kt#L78) from your Composable when calling `koinViewModelScoped`.
