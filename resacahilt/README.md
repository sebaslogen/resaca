[![Build Status](https://github.com/sebaslogen/resaca/actions/workflows/build.yml/badge.svg)](https://github.com/sebaslogen/resaca/actions/workflows/build.yml)
[![Release](https://jitpack.io/v/sebaslogen/resaca.svg)](https://jitpack.io/#sebaslogen/resaca)
[![API 21+](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
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
    implementation("com.github.sebaslogen.resaca:resacahilt:X.X.X")
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
    implementation 'com.github.sebaslogen.resaca:resacahilt:X.X.X'
}
```
</details>

# Usage

Inside your `@Composable` function create and retrieve a ViewModel using `hiltViewModelScoped` to remember any `@HiltViewModel` annotated ViewModel. This
together with the standard Hilt configuration is all that's needed 🪄✨

Examples:

<details open>
  <summary>Scope a ViewModel injected by Hilt to a Composable</summary>
  
```kotlin
@Composable
fun DemoInjectedViewModelScoped() {
    val myInjectedViewModel: MyViewModel = hiltViewModelScoped()
    DemoComposable(viewModel = myInjectedViewModel)
}
  
@HiltViewModel
class MyViewModel @Inject constructor(private val stateSaver: SavedStateHandle) : ViewModel()
```
</details>

<details>
  <summary>Scope a ViewModel injected by Hilt with a key to a Composable</summary>
  
```kotlin
@Composable
fun DemoInjectedViewModelWithKey(keyOne: String = "myFirstKey", keyTwo: String = "mySecondKey") {
    val scopedVMWithFirstKey: MyViewModel = hiltViewModelScoped(keyOne)
    val scopedVMWithSecondKey: MyViewModel = hiltViewModelScoped(keyTwo)
    // We now have 2 instances on memory of the same ViewModel type, both inside the same Composable scope
    // When one key updates only the ViewModel with that key will be recreated
    DemoComposable(inputObject = scopedVMWithFirstKey)
    DemoComposable(inputObject = scopedVMWithSecondKey)
}
  
@HiltViewModel
class MyViewModel @Inject constructor(private val stateSaver: SavedStateHandle) : ViewModel()
```
</details>

<details>
  <summary>Scope a ViewModel injected by Hilt with an argument or id (pseudo assisted injection) to a Composable</summary>
  
```kotlin
@Composable
fun DemoInjectedViewModelWithId(idOne: String = "myFirstId", idTwo: String = "mySecondId") {
    val scopedVMWithFirstId: MyIdViewModel = hiltViewModelScoped(idOne, defaultArguments = bundleOf(MY_ARGS_KEY to idOne))
    val scopedVMWithSecondId: MyIdViewModel = hiltViewModelScoped(idTwo, defaultArguments = bundleOf(MY_ARGS_KEY to idTwo))
    // We now have 2 instances on memory of the same ViewModel type, both inside the same Composable scope
    // When one Id updates only the ViewModel with that Id will be recreated
    // Each ViewModel instance has its own Id
    DemoComposable(inputObject = scopedVMWithFirstId)
    DemoComposable(inputObject = scopedVMWithSecondId)
}
  
@HiltViewModel
class MyIdViewModel @Inject constructor(
    private val stateSaver: SavedStateHandle
) : ViewModel() {

    companion object {
        const val MY_ARGS_KEY = "MY_ARGS_KEY"
    }

    val viewModelId = stateSaver.get<String>(MY_ARGS_KEY)
}
```
</details>

<details>
  <summary>Use a different ViewModel injected by Hilt for each item in a LazyColumn and scope them to the Composable that contains the LazyColumn</summary>
  
```kotlin
@Composable
fun DemoManyInjectedViewModelsScopedOutsideTheLazyColumn(listItems: List<Int> = (1..1000).toList()) {
    val keys = rememberKeysInScope(inputListOfKeys = listItems)
    LazyColumn() {
        items(items = listItems, key = { it }) { item ->
            val myScopedVM: MyViewModel = hiltViewModelScoped(key = item, keyInScopeResolver = keys)
            DemoComposable(inputObject = myScopedVM)
        }
    }
}

@HiltViewModel
class MyViewModel @Inject constructor(private val stateSaver: SavedStateHandle) : ViewModel()
```
</details>

Once you use the `hiltViewModelScoped` function, the same object will be restored as long as the Composable is part of the composition, even if it _temporarily_
leaves composition on configuration change (e.g. screen rotation, change to dark mode, etc.) or while being in the backstack.

> ⚠️ Note that ViewModels provided with `hiltViewModelScoped` **should not be created** using any of the Hilt `hiltViewModel()` or Compose `viewModel()`
nor `ViewModelProviders` factories, otherwise they will be retained in the scope of the screen regardless of `hiltViewModelScoped`.

# Basic Hilt setup

To use the `hiltViewModelScoped` function you need to follow these 3 Hilt configuration steps:

- Annotate your application class with `@HiltAndroidApp`.
- Annotate with `@AndroidEntryPoint` the Activity class that will contain the Composables with the ViewModel.
- Annotate your ViewModel class with `@HiltViewModel` and the constructor with `@Inject`
  . [See example here](https://github.com/sebaslogen/resaca/blob/main/sample/src/main/java/com/sebaslogen/resacaapp/ui/main/data/FakeInjectedViewModel.kt).

**Optionally: Annotate with `@Inject` any other classes that are part of your ViewModel's constructor.*

Gradle setup for Hilt [can be found here](https://dagger.dev/hilt/gradle-setup.html)

For a complete guide to Hilt check the official documentation. Here are the [quick-start](https://dagger.dev/hilt/quick-start) and
the [Hilt ViewModel](https://dagger.dev/hilt/view-model) docs.

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

# Assisted Injection

Assisted injection is a dependency injection (DI) pattern that is used to construct an object where some parameters may be provided by the DI framework and
others must be passed in at creation time (a.k.a “assisted”) by the user, in our case when the `hiltViewModelScoped` is requested.

If you use Dagger (instead of Hilt), it is possible to use Assisted Injection because it is supported in the
library. [See the official documentation](https://dagger.dev/dev-guide/assisted-injection.html). To use Dagger in combination with scoped ViewModels you need to
use the vanilla `viewModelScoped` from [the Resaca library](https://github.com/sebaslogen/resaca) and use one of Dagger providers as parameter
of `viewModelScoped`.

**Unfortunately, Assisted Injection is not supported by Hilt** at the moment and the feature request is open with no clear
plans: https://github.com/google/dagger/issues/2287

# Pseudo Assisted Injection support

The `hiltViewModelScoped` function accepts a `defaultArguments: Bundle` parameter, this Bundle will be provided to your ViewModel as long as the constructor of your ViewModel contains a `SavedStateHandle` parameter. With this Bundle you can provide default arguments to each ViewModel from the Composable call site. This way, you can have multiple ViewModels in the same Composable with different ids.

Usage example:

```kotlin
val myVM: MyViewModel = hiltViewModelScoped(key = myId, defaultArguments = bundleOf(MyViewModel.MY_ARGS_KEY to myId))
```

```kotlin
class MyViewModel(private val stateSaver: SavedStateHandle, val repoDependency: MyRepository) : ViewModel() {

    companion object {
        const val MY_ARGS_KEY = "MY_ARGS_KEY"
    }

    val viewModelId = stateSaver.get<Int>(MY_ARGS_KEY)
    
    fun getData() = repoDependency.getDataForId(viewModelId)
}
```
