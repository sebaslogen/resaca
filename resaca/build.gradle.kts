import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven)
}

kotlin {
    android {
        namespace = "com.sebaslogen.resaca"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "resaca"
            isStatic = true
        }
    }

    jvm("desktop")

    js {
        browser()
        useEsModules()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.jetbrains.compose.runtime)
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            api(libs.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlin.coroutines.test)
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(libs.jetbrains.compose.desktop.common)
            implementation(libs.coroutines.swing)
            // Skiko native libs are required by runComposeUiTest on desktop. Adding here (rather than desktopTest)
            // because the native artifact must be on the runtime classpath that the test inherits.
            implementation(compose.desktop.currentOs)
        }

        val desktopTest by getting
        desktopTest.dependencies {
            implementation(libs.jetbrains.compose.ui.test)
        }

        jsMain.dependencies {
            implementation(dependencies.platform(npm("http-proxy-middleware", "^3.0.5"))) // Force updated version to fix security issues
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.navigation.compose)
        }
    }
}


// Coverage gate: enforce a minimum line coverage on the desktop variant of :resaca via `./gradlew :resaca:check`
// (which transitively runs `koverVerifyDesktop`). Baseline is 88.5% at the time of writing; the threshold sits a few
// points below so routine refactors don't break the build, but a meaningful regression will. Ratchet upward when
// coverage materially improves.
//
// The Android variant has no unit tests in this module (Android-side coverage lives in `:sample`'s instrumentation
// suite), so the rule is scoped to `variant("desktop")`. The aggregate `koverVerify` task is configured to no-op for
// the same reason — verifying it would otherwise fail with ~75% from the empty Android variant pulling down totals.
kover {
    reports {
        variant("desktop") {
            verify {
                rule("resaca line coverage gate (desktop)") {
                    bound {
                        minValue = 85
                        coverageUnits = kotlinx.kover.gradle.plugin.dsl.CoverageUnit.LINE
                    }
                }
            }
        }
    }
}

// Aggregate `koverVerify` would merge desktop + android coverage; the android side has no JVM tests in this module
// so the merged number is misleading. Disable the aggregate, then wire the per-variant `koverVerifyDesktop` directly
// into `check` so the gate still runs on every CI invocation of `./gradlew check` (see .github/workflows/build.yml).
tasks.matching { it.name == "koverVerify" }.configureEach {
    enabled = false
}
tasks.named("check") {
    dependsOn("koverVerifyDesktop")
}

// Maven publishing configuration
val artifactId = project.name
val mavenGroup: String by project
val defaultVersion: String by project
val currentVersion = System.getenv("PACKAGE_VERSION") ?: defaultVersion

group = mavenGroup
version = currentVersion

mavenPublishing {
    coordinates(mavenGroup, artifactId, currentVersion)
}
