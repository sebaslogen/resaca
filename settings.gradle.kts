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

val ghWorkflow = System.getenv("GITHUB_WORKFLOW")
if (ghWorkflow == null || !ghWorkflow.contains("Maven")) // Remove the sample app from Maven publication builds
    include(":sample")

include(
    ":resaca",
    ":resacahilt",
    ":resacakoin"
)
