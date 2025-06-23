[![Maven Central Release](https://img.shields.io/maven-central/v/io.github.sebaslogen/resaca)](https://maven-badges.herokuapp.com/maven-central/io.github.sebaslogen/resacahilt)
[![Build Status](https://github.com/sebaslogen/resaca/actions/workflows/build.yml/badge.svg)](https://github.com/sebaslogen/resaca/actions/workflows/build.yml)
[![API 21+](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![GitHub license](https://img.shields.io/github/license/sebaslogen/resaca)](https://github.com/sebaslogen/resaca/blob/main/LICENSE)
[![Kotlin Weekly](https://user-images.githubusercontent.com/1936647/277181222-6aba882e-eafe-4a38-b8ef-631bb66b442f.svg)](https://mailchi.mp/kotlinweekly/kotlin-weekly-285)
[![Android Weekly](https://github-production-user-asset-6210df.s3.amazonaws.com/1936647/277184200-dbb226b8-9730-49b0-8b7e-23873debea1e.svg)](https://androidweekly.net/issues/issue-593)

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

Just include the library:

<details open>
  <summary>Kotlin (KTS)</summary>
  
```kotlin
// In module's build.gradle.kts
dependencies {
    // The latest version of the lib is available in the badget at the top from Maven Central, replace X.X.X with that version
    implementation("io.github.sebaslogen:resacahilt:X.X.X")
}
```
</details>

<details>
  <summary>Groovy</summary>
  
```gradle
dependencies {
    // The latest version of the lib is available in the badget at the top from Maven Central, replace X.X.X with that version
    implementation 'io.github.sebaslogen:resacahilt:X.X.X'
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
  <summary>Scope a ViewModel injected by Hilt with an argument or id to a Composable</summary>

  Use Hilt's Assisted Injection, see https://dagger.dev/hilt/view-model#assisted-injection
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
  . [See example here](https://github.com/sebaslogen/resaca/blob/main/sample/src/main/java/com/sebaslogen/resacaapp/sample/ui/main/data/FakeInjectedViewModel.kt).

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

# Scoping in a LazyColumn, LazyRow, etc
This is handy for the typical case where you have a lazy list of items and you want to have a separate ViewModel for each item in the list, using the `viewModelScoped` function.
<details>
  <summary>How to use `rememberKeysInScope` to control the lifecycle of a scoped object in a Lazy* list</summary>

When using the Lazy* family of Composables it is recommended that -just above the call to the Lazy* Composable- you use `rememberKeysInScope` with a list of 
keys corresponding to the items used in the Lazy* Composable to obtain a `KeyInScopeResolver` (it's already highly recommended in Compose that items in a Lazy* list have unique keys).

Then in the Lazy* Composable, once you are creating an item and you need a ViewModel for that item, 
all you have to do is include in the call to `hiltViewModelScoped` the key for the current item and the `KeyInScopeResolver` you previously got from `rememberKeysInScope`.

With this setup, when an item of the Lazy* list becomes visible for the first time, its associated `hiltViewModelScoped` ViewModel will be created and even if the item is scrolled away, the scoped ViewModel will still be alive. Only once the associated key is not present anymore in the list provided to `rememberKeysInScope` and the item is either not part of the Lazy* list or scrolled away, then the associated ViewModel will be cleared and destroyed.

🏷️ Example of a different ViewModel for each item in a LazyColumn and scope them to the Composable that contains the LazyColumn
  
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

# Assisted Injection

Assisted injection is a dependency injection (DI) pattern that is used to construct an object where some parameters may be provided by the DI framework and
others must be passed in at creation time (a.k.a “assisted”) by the user, in our case when the `hiltViewModelScoped` is requested.

### Dagger
If you use Dagger (instead of Hilt), it is possible to use Assisted Injection because it is supported in the
library. [See the official documentation](https://dagger.dev/dev-guide/assisted-injection.html). To use Dagger in combination with scoped ViewModels you need to
use the vanilla `viewModelScoped` from [the Resaca library](https://github.com/sebaslogen/resaca) and use one of Dagger providers as parameter
of `viewModelScoped`.

### Hilt
Assisted Injection is supported by Hilt, see the [official documentation](https://dagger.dev/hilt/view-model#assisted-injection). To use Hilt in combination with
scoped ViewModels you need to use the `hiltViewModelScoped` with `creationCallback` parameter from this library and do four things:

- Create a factory interface that has a function to return your ViewModel and annotate it with `@AssistedFactory`
- Add the `assistedFactory` parameter to the `@HiltViewModel` annotation of your ViewModel and set it to the factory interface you created in the previous step. 
- Add the `@AssistedInject` annotation the constructor of your ViewModel.
- Add the `@Assisted` annotation to the parameters of the constructor of your ViewModel that you want to be provided by the `hiltViewModelScoped` function.

<details>
  <summary>Example of ViewModel with Assisted injection</summary>

```kotlin
@Composable
fun DemoAssitedInjectedViewModelScoped(myViewModelId: String) {
    val myScopedVM: MyViewModel = hiltViewModelScoped(key = myViewModelId) { myViewModelFactory: MyViewModel.Factory ->
        myViewModelFactory.create(myViewModelId)
    }
    DemoComposable(inputObject = myScopedVM)
}

@HiltViewModel(assistedFactory = MyViewModel.Factory::class)
class MyViewModel @AssistedInject constructor(
    @Assisted private val viewModelId: String,
    private val stateSaver: SavedStateHandle
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(viewModelId: String): FakeAssistedInjectionViewModel
    }
}
```
</details>
