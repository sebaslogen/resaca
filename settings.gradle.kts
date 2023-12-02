pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
        maven { setUrl("https://androidx.dev/storage/compose-compiler/repository/") }
    }
}
rootProject.name = "resaca"

if (System.getenv("JITPACK") == null) // Remove the sample app to reduce Jitpack builds
    include(":sample")

include(
    ":resaca",
    ":resacahilt",
    ":resacakoin"
)
