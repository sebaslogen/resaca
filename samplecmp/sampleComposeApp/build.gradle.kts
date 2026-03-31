import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    android {
        namespace = "com.sebaslogen.resacaapp.samplecmp.shared"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
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

    jvm("desktop") {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

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
                    static(projectDirPath)
                }
            }
            testTask {
                enabled = false // Disabled due to https://youtrack.jetbrains.com/issue/CMP-4906
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
        commonMain.dependencies {
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.material)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.jetbrains.compose.components.resources)
            implementation(libs.jetbrains.compose.components.ui.tooling.preview)
            implementation(project(":resaca")) // Depend on local resaca multiplatform library

            implementation(libs.kotlinx.datetime) // Add kotlinx-datetime dependency just for testing purposes
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.jetbrains.compose.ui.test)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
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
    addTestListener(object : TestListener {
        override fun beforeSuite(suite: TestDescriptor) {}
        override fun afterSuite(suite: TestDescriptor, result: TestResult) {
            if (suite.parent == null) { // will match the outermost suite
                println("Results: ${result.resultType} (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)")
            }
        }
        override fun beforeTest(testDescriptor: TestDescriptor) {}
        override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {}
    })
}
