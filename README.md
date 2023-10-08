[![Release](https://jitpack.io/v/sebaslogen/resaca.svg)](https://jitpack.io/#sebaslogen/resaca)
[![Build Status](https://github.com/sebaslogen/resaca/actions/workflows/build.yml/badge.svg)](https://github.com/sebaslogen/resaca/actions/workflows/build.yml)
[![API 21+](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![GitHub license](https://img.shields.io/github/license/sebaslogen/resaca)](https://github.com/sebaslogen/resaca/blob/main/LICENSE)

Article about this library: [Every Composable deserves a ViewModel](https://engineering.q42.nl/compose/)

# Resaca 🍹

The right scope for objects and View Models in Android [Compose](https://developer.android.com/jetpack/compose).

Resaca provides a simple way to keep a Jetpack ViewModel (or any other object) in memory during the lifecycle of a `@Composable` function and automatically
clean it up when not needed anymore. This means, it retains your object or ViewModel across recompositions, during configuration changes, and also when the
container Fragment or Compose Navigation destination goes into the backstack.

With Resaca you can create fine grained ViewModels for fine grained Composables and finally have reusable components across screens.

# Why

Compose allows the creation of fine-grained UI components that can be easily reused like Lego blocks 🧱. Well architected Android apps isolate functionality in
small business logic components (like use cases, interactors, repositories, etc.) that are also reusable like Lego blocks 🧱.

Screens are built using Compose components together with business logic components, and the standard tool to connect these two types of components is
a [Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel). Unfortunately, ViewModels can only be scoped to a whole screen (or
larger scope), but not to smaller Compose components on the screen.

In practice, this means that we are gluing UI Lego blocks with business logic Lego blocks using a big glue class for the whole screen, the ViewModel 🗜.

Until now...

# Installation

Add the Jitpack repo and include the library (less than 5Kb):


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
    implementation("com.github.sebaslogen.resaca:resaca:X.X.X")
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
    implementation 'com.github.sebaslogen.resaca:resaca:X.X.X'
}
```
</details>

# Usage

Inside your `@Composable` function create and retrieve an object using `rememberScoped` to remember any type of object (except ViewModels). For ViewModels use
`viewModelScoped`. That's all 🪄✨

Examples:

<details open>
  <summary>Scope to a Composable an object</summary>
  
```kotlin
@Composable
fun DemoScopedObject() {
    val myRepository: MyRepository = rememberScoped { MyRepository() }
    DemoComposable(inputObject = myRepository)
}
```
</details>

<details open>
  <summary>Scope to a Composable a ViewModel</summary>
  
```kotlin
@Composable
fun DemoScopedViewModel() {
    val myScopedVM: MyViewModel = viewModelScoped()
    DemoComposable(inputObject = myScopedVM)
}
```
</details>

<details>
  <summary>Scope to a Composable a ViewModel with a dependency</summary>
  
```kotlin
@Composable
fun DemoScopedViewModelWithDependency() {
    val myScopedVM: MyViewModelWithDependencies = viewModelScoped { MyViewModelWithDependencies(myDependency) }
    DemoComposable(inputObject = myScopedVM)
}
```
</details>

<details>
  <summary>Scope to a Composable a ViewModel with a key</summary>
  
```kotlin
@Composable
fun DemoViewModelWithKey() {
    val scopedVMWithFirstKey: MyViewModel = viewModelScoped("myFirstKey") { MyViewModel("myFirstKey") }
    val scopedVMWithSecondKey: MyViewModel = viewModelScoped("mySecondKey") { MyViewModel("mySecondKey") }
    // We now have 2 ViewModels of the same type with different data inside the same Composable scope
    DemoComposable(inputObject = scopedVMWithFirstKey)
    DemoComposable(inputObject = scopedVMWithSecondKey)
}
```
</details>

<details>
  <summary>Scope to a Composable a ViewModel with a dependency injected with Koin</summary>
  
```kotlin
@Composable
fun DemoKoinInjectedViewModelWithDependency() {
    val myInjectedScopedVM: MyViewModelWithDependencies = viewModelScoped() { getKoin().get { parametersOf(myConstructorDependency) } }
    DemoComposable(inputObject = myInjectedScopedVM)
}
```
</details>

Once you use the `rememberScoped` or `viewModelScoped` functions, the same object will be restored as long as the Composable is part of the composition, even if
it _temporarily_ leaves composition on configuration change (e.g. screen rotation, change to dark mode, etc.) or while being in the backstack.

For ViewModels, in addition to being forgotten when they're really not needed anymore, their _coroutineScope_ will also be automatically canceled because
ViewModel's `onCleared` method will be automatically called by this library.

> 💡 _Optional key_: a key can be provided to the call, `rememberScoped(key) { ... }` or `viewModelScoped(key) { ... }`. This makes possible to forget an old
object when there is new input data during a recomposition (e.g. a new input id for your ViewModel).

> ⚠️ Note that ViewModels remembered with `viewModelScoped` **should not be created** using any of the Compose `viewModel()` or `ViewModelProviders` factories,
otherwise they will be retained in the scope of the screen regardless of `viewModelScoped`. Also, if a ViewModel is remembered with `rememberScoped` its
clean-up method won't be called, that's the reason to use `viewModelScoped` instead.

# Sample use cases

Here are some sample use cases reported by the users of this library:

- 📃📄 Multiple instances of the same type of ViewModel in a screen with a **view-pager**. This screen will have multiple sub-pages that use the same ViewModel
  class with different ids. For example, a screen of holiday destinations with multiple pages and each page with its own `HolidayDestinationViewModel`.
- ❤️ Isolated and stateful UI components like a **favorite button** that are widely used across the screens. This `FavoriteViewModel` can be very small, focused
  and only require an id to work without affecting the rest of the screen's UI and state.
- 🗪 **Dialog pop-ups** can have their own business-logic with state that is better to isolate in a separate ViewModel but the lifespan of these dialogs might be short, 
so it's important to clean-up the ViewModel associated to a Dialog after it has been closed.

# Demo app

Demo app [documentation can be found here](https://github.com/sebaslogen/resaca/blob/main/sample/README.md).

<p align="center">
  <img src="https://user-images.githubusercontent.com/1936647/144597718-db7e8901-a726-4871-abf8-7fc53333a90e.gif" alt="Resaca-demo" width="340" height="802" />
</p>

Before                     |  After backstack navigation & configuration change
:-------------------------:|:-------------------------:
<img width="429" alt="Before" src="https://user-images.githubusercontent.com/1936647/146558764-42333455-2dd8-43a9-932b-3249d42b7a7d.png">  |  <img width="430" alt="After" src="https://user-images.githubusercontent.com/1936647/146558775-8c77231c-ed0f-4f52-b9b8-cdf9029e106c.png">

# Dependency injection support

This library does not influence how your app provides or creates objects so it's dependency injection strategy and framework agnostic.

Nevertheless, this library supports two of the main **dependency injection frameworks**:

## Hilt 🗡️
[HILT](https://dagger.dev/hilt/quick-start) (Dagger) support is available in a small extension of this library: [**resaca-hilt**](https://github.com/sebaslogen/resaca/tree/main/resacahilt/).

[Documentation and installation instructions are available here](https://github.com/sebaslogen/resaca/tree/main/resacahilt/README.md).

## Koin 🪙
[Koin](https://insert-koin.io/) is out of the box supported by simply changing the way you request a dependency.

Instead of using the `getViewModel` or `koinViewModel` functions from Koin, you have to use the standard way of getting a dependency from Koin `getKoin().get()`.

Usage example: `val viewModel: MyViewModel = viewModelScoped(myId) { getKoin().get { parametersOf(myId) } }`

> **Note**: if you plan to use a ViewModel with a [SavedStateHandle](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-savedstate), then you need to use the `koinViewModelScoped` function from the small extension library [**resaca-koin**](https://github.com/sebaslogen/resaca/blob/main/resacakoin/Readme.md).

-----

### General considerations for State Hoisting
Here are a few suggestions of how to provide objects in combination with this library in a Compose screen:

- When using the Lazy* family of Composables it is recommended that you use `rememberScoped`/`viewModelScoped` outside the scope of Composables created by Lazy
  constructors (e.g. LazyColumn) because there is a chance that a lazy initialized Composable will be disposed of when it is not visible anymore (e.g. scrolled
  away) and that will also dispose of the `rememberScoped`/`viewModelScoped` object immediately, this might not be the intended behavior. For more
  info see Compose's [State Hoisting](https://developer.android.com/jetpack/compose/state#state-hoisting).
- When a Composable is used more than once in the same screen with the same input, then the ViewModel (or business logic object) should be provided only once
  with `viewModelScoped` at a higher level in the tree using Compose's [State Hoisting](https://developer.android.com/jetpack/compose/state#state-hoisting).

# Why not use remember?

**[Remember](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#remember(kotlin.Function0))** will keep our object alive as
long as the Composable is not disposed of. Unfortunately, there are a few cases where our Composable will be disposed of and then added again, breaking the
lifecycle parity with the remember function. 😢
<details>
  <summary>Pros and Cons</summary>
  
**_Pros_**

- Simple API

**_Cons_**

- remember value will **NOT** survive a configuration change
- remember value will **NOT** survive when going into the backstack
- remember value will **NOT** survive a process death
</details>

**[RememberSaveable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/saveable/package-summary#rememberSaveable(kotlin.Array,androidx.compose.runtime.saveable.Saver,kotlin.String,kotlin.Function0))**
will follow the lifecycle of the Composable, even in the few cases where the Composable is temporarily disposed of. But the object we want to remember needs to
implement Parcelable or the [Saver](https://developer.android.com/reference/kotlin/androidx/compose/runtime/saveable/Saver) interface in an additional class. 😢
Implementing these interfaces might not trivial.
<details>
  <summary>Pros and Cons</summary>
  
**_Pros_**

- rememberSaveable value will survive a configuration change
- rememberSaveable value will survive when going into the backstack
- rememberSaveable value will survive a process death

**_Cons_**

- **Complex** integration work is required to correctly implement Parcelable
  or [Saver](https://developer.android.com/reference/kotlin/androidx/compose/runtime/saveable/Saver)
</details>
  
### The new RememberScoped 🪄✨

**[RememberScoped](https://github.com/sebaslogen/resaca/blob/main/resaca/src/main/java/com/sebaslogen/resaca/ScopedMemoizers.kt#L33)** function keeps
objects in memory during the lifecycle of the Composable, even in a few cases where the Composable is disposed of, and then added again. Therefore, it will
retain objects longer than the `remember` function but shorter than `rememberSaveable` because there is no serialization involved.
<details>
  <summary>Pros and Cons</summary>
  
**_Pros_**

- Simple API
- rememberScoped/viewModelScoped value will survive a configuration change
- rememberScoped/viewModelScoped value will survive when going into the backstack

**_Cons_**

- rememberScoped/viewModelScoped value will **NOT** survive a process death
</details>

# Lifecycle

**[RememberScoped](https://github.com/sebaslogen/resaca/blob/main/resaca/src/main/java/com/sebaslogen/resaca/ScopedMemoizers.kt#L33)** function keeps
objects in memory during the lifecycle of the Composable, even in a few cases where the Composable is disposed of, and then added again.

## RememberScoped lifecycle internal implementation details

This project uses internally a ViewModel as a container to store all scoped ViewModels and scoped objects.

<details>
  <summary>What happens when a Composable is disposed?</summary>
  
When a Composable is disposed of, we don't know for sure if it will return again later. So at the moment of disposal, we mark in our container the associated
object to be disposed of after the next frame when the Activity is resumed. During the span of time of this next frame, a few things can happen:

- The Composable is not part of the composition anymore after the next frame and the associated object is disposed of. 🚮
- The LifecycleOwner of the disposed Composable (i.e. the navigation destination where the Composable lived) is paused (e.g. screen went to background) before
  the next frame happened. Then the disposal of the scoped object is canceled, but the object is still marked for disposal at a later stage.
    - This can happen when the application goes through a configuration change and the container Activity is recreated.
    - Also when the Composable is part of a Fragment that has been pushed to the backstack.
    - And also when the Composable is part of a Compose Navigation destination that has been pushed to the backstack.
- When the LifecycleOwner of the disposed Composable is resumed (e.g. Fragment comes back to foreground), then the disposal of the associated object is
  scheduled again to happen after the next frame when the Activity is resumed. At this point two things can happen:
    - The Composable becomes part of the composition again and the `rememberScoped`/`viewModelScoped` function restores the associated object while also
      cancelling any pending disposal in the next frame when the Activity is resumed. 🎉
    - The Composable is not part of the composition anymore after the next frame and then the associated object is disposed of. 🚮

> **Note**:

- To know that the same Composable is being added to the composition again after being disposed of, we generate a random ID and store it with `rememberSaveable`
  , which survives recomposition, recreation and even process death.
- To detect when the requester Composable is not needed anymore (has left composition and the screen for good), the ScopedViewModelContainer also observes the
  resume/pause Lifecycle events of the owner of this ScopedViewModelContainer (i.e. Activity, Fragment, or Compose Navigation destination)
</details>

## Lifecycle example

![Compose state scope](https://user-images.githubusercontent.com/1936647/144682707-dd06e2ee-5542-400b-9a8d-cb27fb7c28e8.png)

This diagram shows the lifecycle of three Composables (A, B, and C) with their respective objects scoped with the `rememberScoped` function. All these
Composables are part of a Composable destination which is part of a Fragment which is part of an Activity which is part of the App. The horizontal arrows
represent different lifecycle events, events like Composable being disposed of, Composable screen going into the backstack, Fragment going into the backstack
and returning from backstack, or Activity recreated after a configuration change.

The existing alternatives to replicate the lifecycle of the objects in the diagram without using `rememberScoped` are:

- Object A lifecycle could only be achieved using the Compose `viewModel()` or `ViewModelProviders` factories.
- Object B lifecycle could only be achieved using the Compose `remember()` function.
- Object C lifecycle could not be achieved neither by using ViewModel provider functions nor Compose `remember` functions.
