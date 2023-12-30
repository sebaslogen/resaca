import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven)
}

group = "com.github.sebaslogen"
version = System.getenv("PACKAGE_VERSION") ?: "1.0.0"

android {
    namespace = "com.sebaslogen.resaca"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { // Enables Jetpack Compose for this module
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    packaging {
        resources {
            excludes += setOf(
                // Exclude AndroidX version files
                "META-INF/*.version",
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.android)

    // Compose dependencies
    implementation(libs.compose.compiler)
    // Integration with ViewModels
    implementation(libs.bundles.androidx.lifecycle.viewmodel)
}

object Meta {
    const val desc = "Android library to scope ViewModels to a Composable, surviving configuration changes and navigation"
    const val license = "MIT license"
    const val inceptionYear = "2021"
    const val githubRepo = "sebaslogen/resaca"
    const val release = "https://s01.oss.sonatype.org/service/local/"
    const val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01)
    signAllPublications()
    coordinates(group.toString(), project.name, version.toString())
    pom {
        name.set(project.name)
        description.set(Meta.desc)
        inceptionYear.set(Meta.inceptionYear)
        url.set("https://github.com/${Meta.githubRepo}")
        licenses {
            license {
                name.set(Meta.license)
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
                "https://github.com/${Meta.githubRepo}.git"
            )
            connection.set(
                "scm:git:git://github.com/${Meta.githubRepo}.git"
            )
            developerConnection.set(
                "scm:git:git://github.com/${Meta.githubRepo}.git"
            )
        }
        issueManagement {
            url.set("https://github.com/${Meta.githubRepo}/issues")
        }
    }
}