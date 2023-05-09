import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.binary.compatibility.validator)
}

val sampleModuleName = "sample"

apiValidation {
    ignoredProjects.addAll(listOf(sampleModuleName))
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

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