pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("https://androidx.dev/snapshots/builds/13617490/artifacts/repository")
        }
    }
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://androidx.dev/storage/compose-compiler/repository/") }
        maven {
            url = uri("https://androidx.dev/snapshots/builds/13617490/artifacts/repository")
        }
    }
}
rootProject.name = "resaca"

val ci = System.getenv("CI_FLOW")
println("Running on CI flow: $ci")
if (ci == null || !ci.contains("Maven")) { // Remove the sample app from Maven publication builds
    include(":sample")
    include(":samplecmp:sampleComposeApp")
}

include(
    ":resaca",
    ":resacahilt",
    ":resacakoin"
)
