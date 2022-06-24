[![Build Status](https://github.com/sebaslogen/resaca/actions/workflows/build.yml/badge.svg)](https://github.com/sebaslogen/resaca/actions/workflows/build.yml)
[![Release](https://jitpack.io/v/sebaslogen/resaca.svg)](https://jitpack.io/#sebaslogen/resaca)
[![GitHub license](https://img.shields.io/github/license/sebaslogen/resaca)](https://github.com/sebaslogen/resaca/blob/main/LICENSE)

# Resaca Hilt 🍹🗡️

Short lived View Models provided by [**HILT**](https://dagger.dev/hilt/quick-start) with the right scope in
Android [Compose](https://developer.android.com/jetpack/compose).

# Why

Compose allows the creation of fine-grained UI components that can be easily reused like Lego blocks 🧱. Well architected Android apps isolate functionality in
small business logic components (like use cases, interactors, repositories, etc.) that are also reusable like Lego blocks 🧱.

Screens are built using Compose components together with business logic components, and the standard tool to connect these two types of components is
a [Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel). Unfortunately, ViewModels can only be scoped to a whole screen (or
larger scope), but not to smaller Compose components on the screen.

In practice, this means that we are gluing UI Lego blocks with business logic Lego blocks using a big glue class for the whole screen, the ViewModel 🗜.

Until now...

# Usage

Inside your `@Composable` function create and retrieve a ViewModel using `hiltViewModelScoped` to remember any `@HiltViewModel` annotated ViewModel. This
together with the standard Hilt configuration is all that's needed 🪄✨

Example

```kotlin
@Composable
fun DemoInjectedViewModelScoped() {
    val myInjectedViewModel: MyViewModel = hiltViewModelScoped()
    DemoComposable(viewModel = myInjectedViewModel)
}
```

Once you use the `hiltViewModelScoped` function, the same object will be restored as long as the Composable is part of the composition, even if it _temporarily_
leaves composition on configuration change (e.g. screen rotation, change to dark mode, etc.) or while being in the backstack.

⚠️ ViewModels provided with `hiltViewModelScoped` **should not be created** using any of the Hilt `hiltViewModel()` or Compose `viewModel()`
nor `ViewModelProviders` factories, otherwise they will be retained in the scope of the screen regardless of `hiltViewModelScoped`.

❕ Due to the keyed factory limitations mentioned below, any call to `hiltViewModelScoped` from a new location in the Compose code (_inside the same Activity_)
will return the same instance of the ViewModel instead of a new instance per call location.

# Basic Hilt setup

To use the `hiltViewModelScoped` function you need to follow these 3 Hilt configuration steps:

- Annotate your application class with `@HiltAndroidApp`.
- Annotate with `@AndroidEntryPoint` the Activity class that will contain the Composables with the ViewModel.
- Annotate your ViewModel class with `@HiltViewModel` and the constructor with `@Inject`
  . [See example here](https://github.com/sebaslogen/resaca/blob/main/sample/src/main/java/com/sebaslogen/resacaapp/ui/main/data/FakeInjectedViewModel.kt).

**Optionally: Annotate with `@Inject` any other class that is part of your ViewModel's constructor.*

Gradle setup for Hilt [can be found here](https://dagger.dev/hilt/gradle-setup.html)

For a complete guide to Hilt check the official documentation. Here are the [quick-start](https://dagger.dev/hilt/quick-start) and
the [Hilt ViewModel](https://dagger.dev/hilt/view-model) docs.

# Sample use cases

Here are some sample use cases reported by the users of this library:

- ❤️ Isolated and stateful UI components like a **favorite button** that are widely used across the screens. This `FavoriteViewModel` can be very small, focused
  and only require an id to work without affecting the rest of the screen's UI and state.

# Installation

Add the Jitpack repo and include the library:

```gradle
   allprojects {
       repositories {
           [..]
           maven { url "https://jitpack.io" }
       }
   }
   dependencies {
       // The latest version of the lib is available in the badget at the top, replace X.X.X with that version
       implementation 'com.github.sebaslogen.resaca:resacahilt:X.X.X'
   }
```  

# Hilt keyed factory limitations

Unfortunately, Hilt does not yet support multiple instances of the same ViewModel using **keys**. This feature is currently a work in progress in the
Dagger/Hilt project and its status can be tracked
here: [\[Hilt\] Multiple viewmodel instances with different keys crash · Issue #2328 · google/dagger](https://github.com/google/dagger/issues/2328).

The consequence of this limitation is that we can't create multiple instances of the same ViewModel in the same scope (i.e. Activity, Fragment or Compose
Navigation Destination). This is required for example to implement view-pager use cases
like [this one](https://github.com/sebaslogen/resaca/blob/main/README.md#sample-use-cases) from the vanilla Resaca library.
