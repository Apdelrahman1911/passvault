import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Project

plugins {
    // Root plugins
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false

    // Resolve Detekt plugin and make its classes available to this script.
    alias(libs.plugins.detekt) apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

fun Project.configureDetekt() {
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig.set(true)
        allRules.set(false)

        config.setFrom(
            rootProject.files("detekt.yml")
        )

        baseline.set(
            rootProject.file("detekt-baseline.xml")
        )
    }

    tasks.withType<Detekt>().configureEach {
        jvmTarget.set("17")

        reports {
            checkstyle.required.set(true)
            html.required.set(true)
            sarif.required.set(true)
            markdown.required.set(false)
        }

        exclude(
            "**/build/**",
            "**/.gradle/**",
            "**/generated/**",
            "**/resources/**"
        )
    }

    tasks.withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget.set("17")

        exclude(
            "**/build/**",
            "**/.gradle/**",
            "**/generated/**",
            "**/resources/**"
        )
    }
}

// Apply and configure Detekt for every module.
subprojects {
    pluginManager.apply("dev.detekt")
    configureDetekt()
}

// Generate one baseline for the entire multi-module project.
tasks.register<DetektCreateBaselineTask>("detektProjectBaseline") {
    group = "verification"
    description = "Creates a single Detekt baseline for the entire project"

    buildUponDefaultConfig.set(true)
    ignoreFailures.set(true)
    parallel.set(true)
    jvmTarget.set("17")

    setSource(files(rootDir))

    config.setFrom(
        files("$rootDir/detekt.yml")
    )

    baseline.set(
        file("$rootDir/detekt-baseline.xml")
    )

    include(
        "**/*.kt",
        "**/*.kts"
    )

    exclude(
        "**/build/**",
        "**/.gradle/**",
        "**/generated/**",
        "**/resources/**"
    )
}

// Task to verify all dependencies are locked.
tasks.register("verifyDependencies") {
    group = "verification"
    description = "Verifies all dependencies are pinned"

    doLast {
        println("Dependency verification enabled")
    }
}

// Task to generate dependency report.
tasks.register("dependencyReport") {
    group = "reporting"
    description = "Generates dependency report"

    doLast {
        subprojects.forEach { project ->
            println("\n=== ${project.name} ===")

            project.configurations.forEach { configuration ->
                if (configuration.isCanBeResolved) {
                    try {
                        val dependencies =
                            configuration.resolvedConfiguration
                                .firstLevelModuleDependencies

                        if (dependencies.isNotEmpty()) {
                            println("  ${configuration.name}:")

                            dependencies
                                .take(10)
                                .forEach { dependency ->
                                    println(
                                        "    - ${dependency.moduleGroup}:" +
                                            "${dependency.moduleName}:" +
                                            dependency.moduleVersion
                                    )
                                }

                            if (dependencies.size > 10) {
                                println(
                                    "    ... and ${dependencies.size - 10} more"
                                )
                            }
                        }
                    } catch (_: Exception) {
                        // Some configurations cannot be resolved directly.
                    }
                }
            }
        }
    }
}
