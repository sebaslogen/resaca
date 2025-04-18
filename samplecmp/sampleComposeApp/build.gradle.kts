import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm("desktop")

    js {
        browser {
            testTask {
                enabled = false // Disabled due to https://youtrack.jetbrains.com/issue/CMP-4906
            }
        }
        useEsModules()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "composeApp"
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val desktopMain by getting
        desktopMain.dependencies {
            // Dependency required to run the tests without org.jetbrains.skiko.LibraryLoadException
            implementation(compose.desktop.currentOs)
        }
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(project(":resaca")) // Depend on local resaca multiplatform library

            implementation(libs.kotlinx.datetime) // Add kotlinx-datetime dependency just for testing purposes
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
    }
}

android {
    namespace = "com.sebaslogen.resacaapp.samplecmp"
    compileSdk = libs.versions.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.sebaslogen.resacaapp.cmp"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += setOf(
                // Exclude consumer proguard files
                "META-INF/proguard/*",
                // Exclude the Firebase/Fabric/other random properties files
                "/*.properties",
                "fabric/*.properties",
                "META-INF/*.properties",
                "/META-INF/{AL2.0,LGPL2.1}",
            )
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        androidTestImplementation(libs.compose.ui.test.junit)
        debugImplementation(libs.androidx.tracing)
        debugImplementation(libs.compose.ui.test.manifest)
        debugImplementation(compose.uiTooling)
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ComposeApp"
            packageVersion = "1.0.0"
        }
    }
}

tasks.withType<AbstractTestTask> {
    // Add test output to gradle console
    afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
        if (desc.parent == null) { // will match the outermost suite
            println("Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)")
        }
    }))

    // Disable unit tests for release build type (Robolectric limitations)
    if (name.contains("testReleaseUnitTest")) {
        enabled = false
    }
}

tasks.whenTaskAdded {
    // We need to disable this test task because the CMP tests can't run as unit tests, they run as connectedAndroidTest and iosSimulatorArm64Test
    if (name == "testDebugUnitTest" || name == "testReleaseUnitTest") {
        enabled = false
    }
}
