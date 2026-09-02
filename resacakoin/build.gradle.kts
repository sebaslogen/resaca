
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
        namespace = "com.sebaslogen.resaca.koin"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "resacakoin"
            isStatic = true
        }
    }

    jvm("desktop")

    js {
        browser()
        useEsModules()
        // Required so that webpack bundles the Skiko runtime that Compose UI needs, see https://youtrack.jetbrains.com/issue/CMP-4906
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":resaca"))

            api(libs.koin.core)
            api(libs.koin.core.viewmodel)
            api(libs.koin.compose)

            // Koin brings these Compose libraries in at an older version than the Compose plugin used here.
            // Declaring them keeps every target on the plugin's version instead of only the JVM ones, where
            // compose.desktop.currentOs happens to raise them.
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.ui)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        val desktopMain by getting
        desktopMain.dependencies {
            // Skiko native libs are required by runComposeUiTest on desktop. Adding here (rather than desktopTest)
            // because the native artifact must be on the runtime classpath that the test inherits.
            implementation(compose.desktop.currentOs)
        }

        val desktopTest by getting
        desktopTest.dependencies {
            implementation(libs.jetbrains.compose.ui.test)
            implementation(libs.androidx.lifecycle.runtime.compose) // for LocalLifecycleOwner
        }
    }
}


// Maven publishing configuration
val artifactId = "resacakoin"
val mavenGroup: String by project
val defaultVersion: String by project
val currentVersion = System.getenv("PACKAGE_VERSION") ?: defaultVersion

group = mavenGroup
version = currentVersion

mavenPublishing {
    coordinates(mavenGroup, artifactId, currentVersion)
}
