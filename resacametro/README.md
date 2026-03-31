[![Maven Central Release](https://img.shields.io/maven-central/v/io.github.sebaslogen/resaca)](https://maven-badges.herokuapp.com/maven-central/io.github.sebaslogen/resacametro)
[![Build Status](https://github.com/sebaslogen/resaca/actions/workflows/build.yml/badge.svg)](https://github.com/sebaslogen/resaca/actions/workflows/build.yml)
[![API 21+](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![GitHub license](https://img.shields.io/github/license/sebaslogen/resaca)](https://github.com/sebaslogen/resaca/blob/main/LICENSE)
[![Supported Compose Platforms](https://img.shields.io/badge/Platforms-Android_|_iOS_|_Desktop_JVM_|_Web-blue)](https://central.sonatype.com/namespace/io.github.sebaslogen)
[![Kotlin Weekly](https://user-images.githubusercontent.com/1936647/277181222-6aba882e-eafe-4a38-b8ef-631bb66b442f.svg)](https://mailchi.mp/kotlinweekly/kotlin-weekly-285)
[![Android Weekly](https://github-production-user-asset-6210df.s3.amazonaws.com/1936647/277184200-dbb226b8-9730-49b0-8b7e-23873debea1e.svg)](https://androidweekly.net/issues/issue-593)

# Resaca Metro 🍹🚇

Short lived View Models provided by [**Metro**](https://zacsweers.dev/metro/) with the right scope in
Android [Compose](https://developer.android.com/jetpack/compose) and [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/).

[Metro](https://zacsweers.dev/metro/) is a Kotlin-first dependency injection framework by Zac Sweers that uses a compiler plugin for compile-time dependency graph validation and code generation.

# Why

Compose allows the creation of fine-grained UI components that can be easily reused like Lego blocks 🧱. Well architected Android apps isolate functionality in
small business logic components (like use cases, interactors, repositories, etc.) that are also reusable like Lego blocks 🧱.

Screens are built using Compose components together with business logic components, and the standard tool to connect these two types of components is
a [Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel). Unfortunately, ViewModels can only be scoped to a whole screen (or
larger scope), but not to smaller Compose components on the screen.

In practice, this means that we are gluing UI Lego blocks with business logic Lego blocks using a big glue class for the whole screen, the ViewModel 🗜.

Until now...

# Installation

Add the library and the Metro Gradle plugin:

<details open>
  <summary>Kotlin (KTS)</summary>
  
```kotlin
// In your project's build.gradle.kts
plugins {
    id("dev.zacsweers.metro") version "0.12.1" // Metro compiler plugin
}

// In module's build.gradle.kts
dependencies {
    // The latest version of the lib is available in the badge at the top from Maven Central, replace X.X.X with that version
    implementation("io.github.sebaslogen:resacametro:X.X.X")
}
```
</details>

<details>
  <summary>Groovy</summary>
  
```gradle
// In your project's build.gradle
plugins {
    id 'dev.zacsweers.metro' version '0.12.1' // Metro compiler plugin
}

dependencies {
    // The latest version of the lib is available in the badge at the top from Maven Central, replace X.X.X with that version
    implementation 'io.github.sebaslogen:resacametro:X.X.X'
}
```
</details>

> **Note**: The Metro Gradle plugin automatically includes the Metro runtime dependency. You only need to apply the plugin in the module where you define your `@DependencyGraph`.

# Usage

Inside your `@Composable` function create and retrieve a ViewModel using `metroViewModelScoped` with a `ViewModelProvider.Factory` sourced from your Metro `@DependencyGraph`. This is all that's needed 🪄✨

Examples:

<details open>
  <summary>Scope a ViewModel injected by Metro to a Composable</summary>
  
```kotlin
@Composable
fun DemoInjectedViewModelScoped() {
    val myInjectedViewModel: MyViewModel = metroViewModelScoped(
        factory = myMetroViewModelFactory // Obtained from your Metro @DependencyGraph
    )
    DemoComposable(viewModel = myInjectedViewModel)
}
  
class MyViewModel @Inject constructor(private val repository: MyRepository) : ViewModel()
```
</details>

<details>
  <summary>Scope a ViewModel injected by Metro with a key to a Composable</summary>
  
```kotlin
@Composable
fun DemoInjectedViewModelWithKey(keyOne: String = "myFirstKey", keyTwo: String = "mySecondKey") {
    val scopedVMWithFirstKey: MyViewModel = metroViewModelScoped(key = keyOne, factory = myMetroViewModelFactory)
    val scopedVMWithSecondKey: MyViewModel = metroViewModelScoped(key = keyTwo, factory = myMetroViewModelFactory)
    // We now have 2 instances on memory of the same ViewModel type, both inside the same Composable scope
    // When one key updates only the ViewModel with that key will be recreated
    DemoComposable(inputObject = scopedVMWithFirstKey)
    DemoComposable(inputObject = scopedVMWithSecondKey)
}

class MyViewModel @Inject constructor(private val repository: MyRepository) : ViewModel()
```
</details>

<details>
  <summary>Scope a ViewModel injected by Metro with assisted injection to a Composable</summary>
  
```kotlin
@Composable
fun DemoAssistedInjectedViewModelScoped(myViewModelId: Int) {
    val myScopedVM: MyViewModel = metroViewModelScoped(
        key = myViewModelId,
        factory = createAssistedFactory(metroGraph, myViewModelId)
    )
    DemoComposable(inputObject = myScopedVM)
}

class MyViewModel @AssistedInject constructor(
    private val repository: MyRepository,
    @Assisted val viewModelId: Int
) : ViewModel() {
    @AssistedFactory
    fun interface Factory {
        fun create(viewModelId: Int): MyViewModel
    }
}

// Helper function to create a ViewModelProvider.Factory from a Metro assisted factory
fun createAssistedFactory(graph: MyAppGraph, viewModelId: Int): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            graph.myViewModelFactory.create(viewModelId) as T
    }
```
</details>

<details>
  <summary>Use a different ViewModel injected by Metro for each item in a LazyColumn and scope them to the Composable that contains the LazyColumn</summary>
  
```kotlin
@Composable
fun DemoManyInjectedViewModelsScopedOutsideTheLazyColumn(listItems: List<Int> = (1..1000).toList()) {
    val keys = rememberKeysInScope(inputListOfKeys = listItems)
    LazyColumn() {
        items(items = listItems, key = { it }) { item ->
            val myScopedVM: MyViewModel = metroViewModelScoped(
                key = item,
                keyInScopeResolver = keys,
                factory = createAssistedFactory(metroGraph, item)
            )
            DemoComposable(inputObject = myScopedVM)
        }
    }
}
```
</details>

<details>
  <summary>Scope a ViewModel injected by Metro with a clear delay to a Composable</summary>
  
```kotlin
@Composable
fun DemoInjectedViewModelWithClearDelay() {
    // The ViewModel will be kept in memory for 5 seconds after the Composable is disposed,
    // giving it a chance to be reused if the Composable returns to composition (e.g. quick navigation back and forth)
    val myInjectedViewModel: MyViewModel = metroViewModelScoped(
        clearDelay = 5.seconds,
        factory = myMetroViewModelFactory
    )
    DemoComposable(viewModel = myInjectedViewModel)
}
```
</details>

Once you use the `metroViewModelScoped` function, the same object will be restored as long as the Composable is part of the composition, even if it _temporarily_
leaves composition on configuration change (e.g. screen rotation, change to dark mode, etc.) or while being in the backstack.

> ⚠️ Note that ViewModels provided with `metroViewModelScoped` **should not be created** using any of the Compose `viewModel()`
nor `ViewModelProviders` factories, otherwise they will be retained in the scope of the screen regardless of `metroViewModelScoped`.

# Basic Metro setup

To use the `metroViewModelScoped` function you need to follow these steps:

1. Apply the Metro Gradle plugin (`dev.zacsweers.metro`) to your module.
2. Create a `@DependencyGraph` that provides your dependencies and exposes your ViewModels (or their factories for assisted injection). [See example here](https://github.com/sebaslogen/resaca/blob/main/sample/src/main/java/com/sebaslogen/resacaapp/sample/di/metro/MetroAppGraph.kt).
3. Create a `ViewModelProvider.Factory` that uses your Metro graph to instantiate ViewModels. [See example here](https://github.com/sebaslogen/resaca/blob/main/sample/src/main/java/com/sebaslogen/resacaapp/sample/di/metro/MetroSampleViewModelFactory.kt).
4. Initialize the Metro graph (e.g. in your Application's `onCreate` using `createGraph<MyGraph>()`). [See example here](https://github.com/sebaslogen/resaca/blob/main/sample/src/main/java/com/sebaslogen/resacaapp/sample/ResacaSampleApp.kt).

For a complete guide to Metro check the [official documentation](https://zacsweers.dev/metro/).

# Sample use cases

Here are some sample use cases reported by the users of this library:

- ❤️ Isolated and stateful UI components like a **favorite button** that are widely used across the screens. This `FavoriteViewModel` can be very small, focused
  and only require an id to work without affecting the rest of the screen's UI and state.
- 🗪 **Dialog pop-ups** can have their own business-logic with state that is better to isolate in a separate ViewModel but the lifespan of these dialogs might be short, 
so it's important to clean-up the ViewModel associated to a Dialog after it has been closed.
- 📃 A LazyColumn with a **ViewModel per list item**. Each item can have its own complex logic in an isolated ViewModel that will be lazily loaded when the item is
visible for the first time. The ViewModel will cleared and destroyed when the item is not part of the list in the source data or the whole LazyColumn is removed.
- 📄📄 Multiple instances of the same type of ViewModel in a screen with a **view-pager**. This screen will have multiple sub-pages that use the same ViewModel
  class with different ids. For example, a screen of holiday destinations with multiple pages and each page with its own `HolidayDestinationViewModel`.

# Scoping in a LazyColumn, LazyRow, etc
This is handy for the typical case where you have a lazy list of items and you want to have a separate ViewModel for each item in the list, using the `metroViewModelScoped` function.
<details>
  <summary>How to use `rememberKeysInScope` to control the lifecycle of a scoped object in a Lazy* list</summary>

When using the Lazy* family of Composables it is recommended that -just above the call to the Lazy* Composable- you use `rememberKeysInScope` with a list of 
keys corresponding to the items used in the Lazy* Composable to obtain a `KeyInScopeResolver` (it's already highly recommended in Compose that items in a Lazy* list have unique keys).

Then in the Lazy* Composable, once you are creating an item and you need a ViewModel for that item, 
all you have to do is include in the call to `metroViewModelScoped` the key for the current item and the `KeyInScopeResolver` you previously got from `rememberKeysInScope`.

With this setup, when an item of the Lazy* list becomes visible for the first time, its associated `metroViewModelScoped` ViewModel will be created and even if the item is scrolled away, the scoped ViewModel will still be alive. Only once the associated key is not present anymore in the list provided to `rememberKeysInScope` and the item is either not part of the Lazy* list or scrolled away, then the associated ViewModel will be cleared and destroyed.

🏷️ Example of a different ViewModel for each item in a LazyColumn and scope them to the Composable that contains the LazyColumn
  
```kotlin
@Composable
fun DemoManyInjectedViewModelsScopedOutsideTheLazyColumn(listItems: List<Int> = (1..1000).toList()) {
    val keys = rememberKeysInScope(inputListOfKeys = listItems)
    LazyColumn() {
        items(items = listItems, key = { it }) { item ->
            val myScopedVM: MyViewModel = metroViewModelScoped(
                key = item,
                keyInScopeResolver = keys,
                factory = createAssistedFactory(metroGraph, item)
            )
            DemoComposable(inputObject = myScopedVM)
        }
    }
}
```
</details>

<details>
  <summary>Combine `clearDelay` with `keyInScopeResolver` in a Lazy* list</summary>

You can use `clearDelay` alongside `keyInScopeResolver` in the same Lazy* list. This is useful when some items need a grace period before being cleared (e.g. the first item uses `clearDelay` for a smoother experience) while others should stay in memory as long as they are in the list (via `keyInScopeResolver`).

> 💡 Note: `clearDelay` and `keyInScopeResolver` are independent mechanisms. An item using `clearDelay` without `keyInScopeResolver` will be cleared after the delay, regardless of whether it is still in the list. An item using `keyInScopeResolver` without `clearDelay` will be kept alive as long as its key is present in the list.

🏷️ Example of a LazyColumn where the first item uses `clearDelay` and the rest use `keyInScopeResolver`

```kotlin
@Composable
fun DemoMixedScopingInLazyColumn(listItems: List<Int> = (1..1000).toList()) {
    val keys = rememberKeysInScope(inputListOfKeys = listItems)
    LazyColumn() {
        items(items = listItems, key = { it }) { item ->
            val myScopedVM: MyViewModel = if (item == 1) {
                // This item's ViewModel will be cleared 5 seconds after scrolling away
                metroViewModelScoped(key = item, clearDelay = 5.seconds, factory = myMetroViewModelFactory)
            } else {
                // These items' ViewModels stay alive as long as the item is in the list
                metroViewModelScoped(key = item, keyInScopeResolver = keys, factory = myMetroViewModelFactory)
            }
            DemoComposable(inputObject = myScopedVM)
        }
    }
}
```
</details>

# Assisted Injection

Assisted injection is a dependency injection (DI) pattern that is used to construct an object where some parameters may be provided by the DI framework and
others must be passed in at creation time (a.k.a "assisted") by the user, in our case when the `metroViewModelScoped` is requested.

Metro supports assisted injection natively with the `@AssistedInject`, `@Assisted`, and `@AssistedFactory` annotations.

To use assisted injection with `metroViewModelScoped`:

1. Annotate the ViewModel constructor with `@AssistedInject` and mark assisted parameters with `@Assisted`.
2. Create a nested `@AssistedFactory` interface in your ViewModel.
3. Expose the factory from your Metro `@DependencyGraph`.
4. Create a `ViewModelProvider.Factory` wrapper that calls the Metro-generated assisted factory with the assisted parameters.

<details>
  <summary>Example of ViewModel with Metro Assisted Injection</summary>

```kotlin
@Composable
fun DemoAssistedInjectedViewModelScoped(myViewModelId: Int) {
    val myScopedVM: MyViewModel = metroViewModelScoped(
        key = myViewModelId,
        factory = createAssistedFactory(metroGraph, myViewModelId)
    )
    DemoComposable(inputObject = myScopedVM)
}

class MyViewModel @AssistedInject constructor(
    private val repository: MyRepository,
    @Assisted val viewModelId: Int
) : ViewModel() {

    @AssistedFactory
    fun interface Factory {
        fun create(viewModelId: Int): MyViewModel
    }
}

@DependencyGraph
abstract class MyAppGraph {
    @Provides fun provideRepo(): MyRepository = MyRepository()
    abstract val myViewModelFactory: MyViewModel.Factory
}

// Helper to wrap Metro's assisted factory into a ViewModelProvider.Factory
fun createAssistedFactory(graph: MyAppGraph, viewModelId: Int): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            graph.myViewModelFactory.create(viewModelId) as T
    }
```
</details>

# Multiplatform support

The `resacametro` module supports **Compose Multiplatform** for Android, iOS, Desktop-JVM, JS and WasmJS targets, matching the same platform support as the core `resaca` library.

Metro itself is a Kotlin Multiplatform library, so your `@DependencyGraph` definitions and `@Inject`-annotated classes work across all platforms.
