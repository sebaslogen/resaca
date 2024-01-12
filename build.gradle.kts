import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dagger.hilt.android) apply false
    alias(libs.plugins.binary.compatibility.validator)
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.maven) apply false
    alias(libs.plugins.dokka)
}

val sampleModuleName = "sample"

apiValidation {
    if (System.getenv("JITPACK") == null || System.getenv("CI") == null ) {
        println("ApiValidation is being ignored for module $sampleModuleName in local builds")
        // This block is only applicable on local builds
        ignoredProjects.addAll(listOf(sampleModuleName))
    }
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
    if (project.name != sampleModuleName) {
        plugins.withId("com.vanniktech.maven.publish.base") {
            configure<MavenPublishBaseExtension> {
                publishToMavenCentral(SonatypeHost.S01)
                signAllPublications()
                pom {
                    name.set(project.name)
                    description.set(desc)
                    inceptionYear.set(creationYear)
                    url.set("https://github.com/$githubRepo")
                    licenses {
                        license {
                            name.set(license)
                            url.set("https://github.com/sebaslogen/resaca/blob/main/LICENSE")
                        }
                    }
                    developers {
                        developer {
                            id.set("sebaslogen")
                            name.set("Sebastian Lobato Genco")
                            url.set("https://github.com/sebaslogen/")
                        }
                    }
                    scm {
                        url.set(
                            "https://github.com/$githubRepo.git"
                        )
                        connection.set(
                            "scm:git:git://github.com/$githubRepo.git"
                        )
                        developerConnection.set(
                            "scm:git:git://github.com/$githubRepo.git"
                        )
                    }
                    issueManagement {
                        url.set("https://github.com/$githubRepo/issues")
                    }
                }
            }
        }
    }
}