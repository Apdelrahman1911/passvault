import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "passvault"

include(
    ":app-android",
    ":app-desktop",
    ":shared",

    ":core:domain",
    ":core:database",
    ":core:crypto",
    ":core:security",
    ":core:designsystem",
    ":core:navigation",
    ":core:otp",
    ":core:testing",

    ":feature:onboarding",
    ":feature:unlock",
    ":feature:vault",
    ":feature:credential",
    ":feature:generator",
    ":feature:health",
    ":feature:settings",
    ":feature:backup",
)
