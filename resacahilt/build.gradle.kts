plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kover)
    alias(libs.plugins.dokka)
    alias(libs.plugins.maven)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.sebaslogen.resaca.hilt"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures { // Enables Jetpack Compose for this module
        compose = true
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {

    api(project(":resaca"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.android)

    // Integration with ViewModels
    implementation(libs.androidx.lifecycle.viewmodel)

    // Hilt dependencies
    implementation(libs.dagger.hilt)
    implementation(libs.dagger.hilt.navigation.compose)
    ksp(libs.dagger.hilt.android.compiler)

    // Test dependencies

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.kotlin.coroutines.test)
    // Test rules and transitive dependencies:
    testImplementation(libs.compose.ui.test.junit)
    // Needed for createComposeRule, but not createAndroidComposeRule:
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.dagger.hilt.android.testing)
    kspTest(libs.dagger.hilt.android.compiler)
    testImplementation(libs.koin.android.test)
    // Espresso dependencies for Activity recreation tests
    androidTestImplementation(libs.espresso)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit)
    androidTestImplementation(libs.koin.android.test)
}

// Maven publishing configuration
val artifactId = "resacahilt"
val mavenGroup: String by project
val defaultVersion: String by project
val currentVersion = System.getenv("PACKAGE_VERSION") ?: defaultVersion

group = mavenGroup
version = currentVersion

mavenPublishing {
    coordinates(mavenGroup, artifactId, currentVersion)
}