import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

val sampleModuleName = "sample"

subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
            if (project.name != sampleModuleName) {
                freeCompilerArgs += "-Xexplicit-api=strict"
            }
        }
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configure<KotlinProjectExtension> {
            if (project.name != sampleModuleName) {
                explicitApi()
            }
        }
    }
}