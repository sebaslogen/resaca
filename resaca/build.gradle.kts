import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven)
}

// Maven publishing configuration
val mavenGroup: String by project
val defaultVersion: String by project
val currentVersion = System.getenv("PACKAGE_VERSION") ?: defaultVersion
val desc: String by project
val license: String by project
val inceptionYear: String by project
val githubRepo: String by project
val release: String by project
val snapshot: String by project

group = mavenGroup
version = currentVersion

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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01)
    signAllPublications()
    coordinates(mavenGroup, project.name, currentVersion)
    pom {
        name.set(project.name)
        description.set(desc)
        inceptionYear.set(inceptionYear)
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