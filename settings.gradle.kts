pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.android.application" || requested.id.id == "com.android.library" || requested.id.id == "org.jetbrains.kotlin.android") {
                useModule("com.android.tools.build:gradle:8.5.0")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.google.com") }
    }
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "MyApplication"

include(":app")
include(":core:common")
include(":core:database")
include(":core:network")
include(":core:testing")

// Feature modules (add as needed)
// include(":feature:auth")
// include(":feature:home")
// include(":feature:settings")
// include(":feature:profile")