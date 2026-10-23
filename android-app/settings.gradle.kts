pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // UVCCamera (libausbc) is published via JitPack, not Maven Central.
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "magic-mirror-vton"
include(":app")
