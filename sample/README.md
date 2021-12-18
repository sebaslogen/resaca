# Resaca Demo App 🍹
The purpose of this app is to show the usage of the `rememberScoped` function in the context of different lifecycle events.

The demo will instantiate different fake business logic objects ([FakeRepo](https://github.com/sebaslogen/resaca/blob/main/sample/src/main/java/com/sebaslogen/resacaapp/ui/main/data/FakeRepo.kt) or [FakeScopedViewModel](https://github.com/sebaslogen/resaca/blob/main/sample/src/main/java/com/sebaslogen/resacaapp/ui/main/data/FakeScopedViewModel.kt)) and either scope them with `rememberScoped` or with the vanilla `remember` from Compose, to illustrate the differences in memory retention across different lifecycle events.

The remembered objects will be represented on the screen with their unique memory location by rendering:
- the object's toString representation in a `Text` Composable
- a unique color for the object's instance using `objectToColorInt` as background
- a semi-unique emoji for the object's instance (limited to list of emojis available in [emojis](https://github.com/sebaslogen/resaca/blob/main/sample/src/main/java/com/sebaslogen/resacaapp/ui/main/ui/theme/Emojis.kt))

# Screens structure of the app
The app contains the following screens:
- **Main Activity**. Purpose: show Fragment navigation/lifecycle events and Activity configuration changes
  + Composable content. Purpose: show Activity configuration changes and entry point for ComposeActivity
    * 2 Scoped objects
    * Button to Navigate to ComposeActivity
  + **MainFragment**. Purpose: show Fragment navigation/lifecycle events when MainFragment goes into the backstack, comes back and its View is recreated
    * 1 Not scoped object
    * 2 Scoped objects
    * Button to Navigate to **FragmentTwo**
  + **FragmentTwo**. Purpose: push **MainFragment** into the backstack and destroy its View
- **ComposeActivity**. Purpose: show Compose navigation/lifecycle events with Compose Navigation destinations
  + **first** Compose destination: show Compose navigation/lifecycle events when destination goes into the backstack, comes back and its Composables are recreated
    * 1 Not scoped object
    * 2 Scoped objects
    * Button to Navigate to first
    * Button to Navigate to second
    * Button to Navigate back
  + **second** Compose destination: exactly the same as **first**

# Lifecycle events
Here is a list of lifecycle events where `rememberScoped` will retain objects:
- Android Configuration change. Examples: App size changes (like in split screen), light/dark mode switches, rotation, language, etc.
- Fragment goes into the backstack
- Composable destination goes into the backstack

# Demo app
<p align="center">
  <img src="https://user-images.githubusercontent.com/1936647/144597718-db7e8901-a726-4871-abf8-7fc53333a90e.gif" alt="Resaca-demo" width="340" height="802" />
</p>

Before                     |  After backstack navigation & configuration change
:-------------------------:|:-------------------------:
<img width="429" alt="Before" src="https://user-images.githubusercontent.com/1936647/146558764-42333455-2dd8-43a9-932b-3249d42b7a7d.png">  |  <img width="430" alt="After" src="https://user-images.githubusercontent.com/1936647/146558775-8c77231c-ed0f-4f52-b9b8-cdf9029e106c.png">