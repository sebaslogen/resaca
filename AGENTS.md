# AI Agent Instructions for Resaca

Welcome, AI Agent! This document provides context, architectural guidelines, and coding standards for interacting with the **Resaca** repository. Please read and adhere to these instructions when generating code, writing tests, or proposing architectural changes.

## 🎯 Project Overview
**Resaca** is an Android library for Jetpack Compose. It provides a simple way to retain Kotlin objects (like ViewModels, state holders, or any other class) across configuration changes and navigation, scoping them to the lifecycle of a Composable. 

Unlike standard `remember`, Resaca's `rememberScoped` survives configuration changes. Unlike standard `ViewModel`, Resaca allows scoping objects to a specific Composable hierarchy rather than an entire Activity or Navigation Route.

## 🛠 Tech Stack
- **Language:** Kotlin
- **UI Framework:** Android Jetpack Compose
- **Lifecycle Management:** AndroidX Lifecycle, ViewModel, Navigation Compose
- **Concurrency:** Kotlin Coroutines
- **Build System:** Gradle (Kotlin DSL)

## 🏗 Architecture & Core Concepts
When working on the codebase, keep the following core mechanisms in mind:
1. **`rememberScoped`:** The primary public API. It creates and retains an object.
2. **`ResacaViewModel` / `ScopedViewModelContainer`:** The internal mechanism that actually stores the scoped objects. It hooks into the Android `ViewModel` infrastructure to survive configuration changes.
3. **Lifecycle Awareness:** Objects are kept alive during configuration changes but must be properly cleared/disposed of when the Composable permanently leaves the composition or the host Activity/Fragment is destroyed.
4. **Memory Leaks:** Preventing memory leaks is critical. Ensure that references to objects are properly cleared when their scope is destroyed.

## 📜 Coding Guidelines

### 1. Kotlin & Compose Best Practices
- Write idiomatic Kotlin code. Use extension functions, higher-order functions, and standard library functions where appropriate.
- Follow Jetpack Compose best practices:
  - Keep Composables pure and side-effect free where possible.
  - Use `DisposableEffect` or `LaunchedEffect` for side effects.
  - Ensure state hoisting principles are respected.
- Avoid using `var` when `val` is sufficient.

### 2. Public API & Backward Compatibility
- **Resaca is a library.** Do not introduce breaking changes to the public API (`rememberScoped`, `viewModelScoped`, etc.) without explicit instruction.
- If a public API must be changed, use the `@Deprecated` annotation with a `ReplaceWith` clause instead of deleting the old function.
- Ensure public functions and classes are well-documented using KDoc. Explain *what* the function does, its parameters, and its lifecycle implications.

### 3. Threading & Concurrency
- UI operations must happen on the Main thread.
- Use Coroutines for asynchronous work. Do not use `Thread` or `AsyncTask`.

## 🧪 Testing Standards
Testing is crucial for this library because lifecycle events and configuration changes are notoriously tricky.
- **Frameworks:** JUnit 4/5, Compose UI Test framework, Robolectric (if applicable), and Espresso.
- **Configuration Changes:** When writing tests for new features, you **must** include tests that simulate Android configuration changes (e.g., screen rotation) to ensure objects are retained.
- **Disposal Tests:** Ensure tests verify that objects are properly garbage collected/cleared when they leave the composition permanently.
- **Navigation Tests:** Test object retention across Compose Navigation backstack events.
- **Code coverage:** Every change should be properly tested and enough code coverage should be maintained using the skill kover-gate-report.

## 📝 Commit & Pull Request Guidelines
- **Commit Messages:** Use Conventional Commits format (e.g., `feat: add support for X`, `fix: resolve memory leak in Y`, `docs: update README`).
- **Scope:** Keep changes small and focused. Do not mix refactoring with new feature development in the same PR.

## 🤖 Agent Directives
- **When asked to fix a bug:** First, identify if the bug is related to a configuration change, a navigation event, or a standard Compose recomposition. Write a failing test before implementing the fix.
- **When asked to add a feature:** Consider how it impacts the internal `ScopedViewModelContainer`. Ensure it does not break existing scoping rules.
- **When refactoring:** Prioritize readability and performance. Compose recompositions happen frequently, so avoid heavy allocations inside Composable functions.
