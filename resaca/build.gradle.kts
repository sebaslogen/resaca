import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
    androidLibrary {
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
            implementation(compose.runtime)
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            api(libs.coroutines.core)
        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.common)
            implementation(libs.coroutines.swing)
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
