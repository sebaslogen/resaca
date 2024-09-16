import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.maven) apply false
    alias(libs.plugins.dokka)
    alias(libs.plugins.compose.compiler) apply false
}

val sampleModuleName = "sample"

apiValidation {
    val ci = System.getenv("CI_FLOW")
    if (ci == null || !ci.contains("Maven")) { // This block is only applicable on build that include the sample app
        println("ApiValidation is being ignored for module $sampleModuleName in local builds")
        ignoredProjects.addAll(listOf(sampleModuleName, "sampleComposeApp", "sampleDesktopApp"))
    }
    @OptIn(kotlinx.validation.ExperimentalBCVApi::class)
    klib {
        enabled = true
    }
    nonPublicMarkers.add("kotlin.PublishedApi")
}

subprojects {
    /**
     * Enable Strict API to force the library modules to explicitly declare visibility of function and classes in the API
     */
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
            freeCompilerArgs.add("-Xexpect-actual-classes")
            if (!project.name.contains(sampleModuleName)) {
                freeCompilerArgs.add("-Xexplicit-api=strict")
            }
        }
    }
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
        configure<KotlinProjectExtension> {
            if (!project.name.contains(sampleModuleName)) {
                explicitApi()
            }
        }
    }
}

tasks.withType<DokkaMultiModuleTask>().configureEach {
    outputDirectory = layout.projectDirectory.dir("docs/api")
}

// Must be afterEvaluate or else com.vanniktech.maven.publish will overwrite our
// dokka and version configuration.
afterEvaluate {
    tasks.withType<DokkaTaskPartial>().configureEach {
        dokkaSourceSets.configureEach {
            jdkVersion = 17
            failOnWarning = true
            skipDeprecated = true
            suppressInheritedMembers = true
        }
    }
}

// Maven publishing configuration
val mavenGroup: String by project
val defaultVersion: String by project
val currentVersion = System.getenv("PACKAGE_VERSION") ?: defaultVersion
val desc: String by project
val license: String by project
val creationYear: String by project
val githubRepo: String by project

group = mavenGroup
version = currentVersion

subprojects {
    if (!project.name.contains(sampleModuleName)) {
        plugins.withId("com.vanniktech.maven.publish.base") {
            configure<MavenPublishBaseExtension> {
                publishToMavenCentral(SonatypeHost.S01)
                signAllPublications()
                pom {
                    name = project.name
                    description = desc
                    inceptionYear = creationYear
                    url = "https://github.com/$githubRepo"
                    licenses {
                        license {
                            name = license
                            url = "https://github.com/sebaslogen/resaca/blob/main/LICENSE"
                        }
                    }
                    developers {
                        developer {
                            id = "sebaslogen"
                            name = "Sebastian Lobato Genco"
                            url = "https://github.com/sebaslogen/"
                        }
                    }
                    scm {
                        url = "https://github.com/$githubRepo.git"
                        connection = "scm:git:git://github.com/$githubRepo.git"
                        developerConnection = "scm:git:git://github.com/$githubRepo.git"
                    }
                    issueManagement {
                        url = "https://github.com/$githubRepo/issues"
                    }
                }
            }
        }
    }
}