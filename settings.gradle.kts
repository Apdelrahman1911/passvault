pluginManagement {
    includeBuild("build-logic")

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
    repositories {
        google()
        mavenCentral()

        // Keep only if a dependency is unavailable on Maven Central.
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "passvault"

include(
    ":app-android",
    ":app-desktop",
    ":shared",

    ":core:domain",
    ":core:data",
    ":core:database",
    ":core:crypto",
    ":core:security",
    ":core:designsystem",
    ":core:navigation",
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
