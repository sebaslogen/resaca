plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kover)
    `maven-publish`
}

group = "com.github.sebaslogen"

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
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
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

    // Config publication component to publish artifacts to JitPack
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {

    api(project(":resaca"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.coroutines.android)

    // Compose dependencies
    implementation(libs.compose.compiler)
    // Integration with ViewModels
    implementation(libs.bundles.androidx.lifecycle.viewmodel)

    // Hilt dependencies
    implementation(libs.dagger.hilt)
    implementation(libs.dagger.hilt.navigation.compose)
    kapt(libs.dagger.hilt.android.compiler)
}

// Config for publishing artifacts to JitPack
afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("Maven") {// Creates a Maven publication called "release"
                from(components["release"])
                artifactId = "resacahilt"
            }
        }
    }
}