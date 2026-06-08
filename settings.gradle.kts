pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "kukso-hytale"

include(":lib", ":econ", ":warps")
project(":lib").projectDir = file("modules/lib")
project(":econ").projectDir = file("modules/econ")
project(":warps").projectDir = file("modules/warps")
