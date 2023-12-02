import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.kover) apply false
}

val sampleModuleName = "sample"

apiValidation {
    if (System.getenv("JITPACK") == null) // This block is only applicable outside of Jitpack (local builds)
        ignoredProjects.addAll(listOf(sampleModuleName))
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    /**
     * Enable Strict API to force the library modules to explicitly declare visibility of function and classes in the API
     */
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