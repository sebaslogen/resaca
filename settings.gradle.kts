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
        maven { setUrl("https://androidx.dev/storage/compose-compiler/repository/") }
    }
}
rootProject.name = "resaca"

val ghWorkflow = System.getenv("GITHUB_WORKFLOW")
println("Running on GitHub workflow(GITHUB_WORKFLOW): $ghWorkflow")
if (ghWorkflow == null || !ghWorkflow.contains("Maven")) // Remove the sample app from Maven publication builds
    include(":sample")

include(
    ":resaca",
    ":resacahilt",
    ":resacakoin"
)
