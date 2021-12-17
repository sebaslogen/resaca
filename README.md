[![Build Status](https://app.travis-ci.com/sebaslogen/resaca.svg?branch=main)](https://app.travis-ci.com/sebaslogen/resaca) [![Release](https://jitpack.io/v/sebaslogen/resaca.svg)](https://jitpack.io/#sebaslogen/resaca)

# Resaca 🍹
Scoping for objects and View Models in Android [Compose](https://developer.android.com/jetpack/compose)

Resaca provides a simple way to retain a Jetpack ViewModel (or any other object) in memory in the scope of a `@Composable` function during configuration changes and also when the container Fragment or Compose Navigation destination goes into the backstack.

# Why
Compose allows the creation of fine grained UI components that can be easily reused like Lego pieces 🧱. Well architectured Android apps isolate functionality in small business logic components (like use cases, interactors, repositories, etc.) that are also reusable like Lego pieces 🧱.

Screens are built using Compose components together with business logic components, and the standard tool to connect these components is a [Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel). Unfortunately, ViewModels can only be scoped to a whole screen, not to Compose components on the screen.

In practice this means that we are gluing UI Lego blocks with business logic Lego blocks using a big glue class, the ViewModel 🗜.

Until now...

# Usage 
Inside your `@Composable` function create and retrieve an object using `rememberScoped` to remember any type of object (including ViewModels).
That's all 🪄✨

Example
```kotlin
@Composable
fun DemoScopedObjectComposable() {
    val myRepository: MyRepository = rememberScoped { MyRepository() }
    DemoComposable(inputObject = myRepository)
}

@Composable
fun DemoScopedViewModelComposable() {
    val myScopedVM: MyViewModel = rememberScoped { MyViewModel() }
    DemoComposable(inputObject = myScopedVM)
}
```

Once you use the `rememberScoped` function, the same object will be restored as long as the Composable is part of the composition, even if it _temporarily_ leaves composition on configuration change (e.g. screen rotation, change to dark mode, etc.) or while being in the backstack.

For ViewModels, on top of being forgotten when they're really not needed anymore, their coroutineScope will also be automatically cancelled.
⚠️ ViewModels remembered with `rememberScoped` **should not be created** using any of the Compose `viewModel()` or `ViewModelProviders` factories, otherwise they will be retained in the scope of the screen regardless of the `rememberScoped`

# Demo app
<p align="center">
  <img src="https://user-images.githubusercontent.com/1936647/144597718-db7e8901-a726-4871-abf8-7fc53333a90e.gif" alt="Resaca-demo" width="340" height="802" />
</p>

Before                     |  After backstack navigation & configuration change
:-------------------------:|:-------------------------:
<img width="429" alt="Before" src="https://user-images.githubusercontent.com/1936647/146558764-42333455-2dd8-43a9-932b-3249d42b7a7d.png">  |  <img width="430" alt="After" src="https://user-images.githubusercontent.com/1936647/146558775-8c77231c-ed0f-4f52-b9b8-cdf9029e106c.png">


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
       implementation 'com.github.sebaslogen:resaca:X.X.X'
   }
```  

## Alternative manual installation
Only two files are needed and they can be found in the `resaca` module under the package `com.sebaslogen.resaca`, they are [ScopedViewModelContainer](https://github.com/sebaslogen/resaca/blob/main/resaca/src/main/java/com/sebaslogen/resaca/ScopedViewModelContainer.kt) and [ScopedMemoizers](https://github.com/sebaslogen/resaca/blob/main/resaca/src/main/java/com/sebaslogen/resaca/compose/ScopedMemoizers.kt).


# Lifecycle
This project uses a ViewModel as a container to store all scoped ViewModels and scoped objects.

The `rememberScoped` functions will retain objects longer than the `remember` function but shorter than `rememberSaveable` because these objects are stored in memory (no serialization involved).

When a Composable is disposed, we don't know for sure if it will return again later. So at the moment of disposal we mark in our container the associated object to be disposed after a small delay (currently 5 seconds). During the span of time of this delay, a few things can happen:
- The Composable is not part of the composition anymore after the delay and the associated object is disposed. 🚮
- The LifecycleOwner of the disposed Composable (i.e. the navigation destination where the Composable lived) is paused (e.g. screen went to background) before the delay finishes. Then the disposal of the scoped object is cancelled, but the object is still marked for disposal at a later stage.
  - This can happen when the application goes through a configuration change and the container Activity is recreated.
  - Also when the Composable is part of a Fragment that has been pushed to the backstack.
  - And also when the Composable is part of a Compose Navigation destination that has been pushed to the backstack.
- When the LifecycleOwner of the disposed Composable is resumed (e.g. screen comes back to foreground), then the disposal of the associated object is scheduled again to happen after a small delay. At this point two things can happen:
  - The Composable becomes part of the composition again and the `rememberScoped` function restores the associated object while also cancelling any pending delayed disposal. 🎉
  - The Composable is not part of the composition anymore after the delay and the associated object is disposed. 🚮

Notes:
- To know that the same Composable is being added to the composition again after being disposed, we generate a random ID and store it with `rememberSaveable`, which survives recreation (and even process death).
- To detect when the requester Composable is not needed anymore (has left composition and the screen for good), the ScopedViewModelContainer observes the resume/pause Lifecycle events of the owner of this ScopedViewModelContainer (i.e. Activity, Fragment or Compose Navigation destination)


## Lifecycle example

![Compose state scope](https://user-images.githubusercontent.com/1936647/144682707-dd06e2ee-5542-400b-9a8d-cb27fb7c28e8.png)

This diagram shows the lifecycle of three Composables (A, B and C) with their respective objects scoped with the `rememberScoped` function.
All these Composables are part of a Composable destination which is part of a Fragment which is part of an Activity which is part of the App.
The horizontal arrows repesent different lifecycle events, events like: Composable being disposed, Composable screen going into the backstack, fragment going into the backstack, returning from backstack or Activity recreated after a configuration change.

The existing alternatives to replicate the lifecycle of the objects in the diagram without using `rememberScoped` are:
- Object A lifecycle could only be achieved using the Compose `viewModel()` or `ViewModelProviders` factories.
- Object B lifecycle could only be achieved using the Compose `remember()` function.
- Object C lifecycle could not simply be achieved neither by using ViewModel provider functions nor Compose `remember` functions.
