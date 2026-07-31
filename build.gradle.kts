import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class VerifyDependencyMetadataTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val metadataFile: RegularFileProperty

    @TaskAction
    fun verifyMetadata() {
        val file = metadataFile.get().asFile
        check(file.isFile) {
            "Missing gradle/verification-metadata.xml. " +
                "Regenerate it with --write-verification-metadata sha256."
        }

        val metadata = file.readText()
        check("<verify-metadata>true</verify-metadata>" in metadata) {
            "Gradle module metadata verification must remain enabled."
        }
        check("<sha256 value=" in metadata) {
            "Dependency verification metadata does not contain SHA-256 checksums."
        }

        logger.lifecycle(
            "Validated SHA-256 dependency verification metadata " +
                "(${file.length()} bytes).",
        )
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.kmp.library) apply false

    /*
     * Adds Detekt plugin classes to this build script's classpath,
     * without applying Detekt to the root project.
     */
    alias(libs.plugins.detekt) apply false
}

fun Project.configureDetekt() {
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig.set(true)
        allRules.set(false)
        parallel.set(true)

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
            "**/.kotlin/**",
            "**/generated/**",
            "**/resources/**"
        )
    }

    /*
     * Disable the automatically generated per-module baseline tasks.
     * The project uses one combined root baseline instead.
     */
    tasks.withType<DetektCreateBaselineTask>().configureEach {
        enabled = false
    }
}

subprojects {
    pluginManager.apply("dev.detekt")
    configureDetekt()
}

/*
 * Creates one Detekt baseline for the complete multi-module project.
 */
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
        "**/.kotlin/**",
        "**/.idea/**",
        "**/generated/**",
        "**/resources/**"
    )
}

tasks.register<VerifyDependencyMetadataTask>("verifyDependencies") {
    group = "verification"
    description = "Validates the Gradle SHA-256 dependency verification policy"

    metadataFile.set(
        layout.projectDirectory.file("gradle/verification-metadata.xml")
    )
}

/*
 * Kotlin Multiplatform's JVM test task is named `desktopTest` in the
 * production modules. Gradle's unqualified `test` selector otherwise only
 * reaches Android/JVM tasks and can report success without executing the
 * shared test suites. Keep a real root verification task so CI and local
 * developers get the same coverage from `gradlew test`.
 */
tasks.register("test") {
    group = "verification"
    description = "Runs all shared Desktop/JVM and Android host test suites"
}

val sharedTestPaths = listOf(
    ":app-desktop:desktopTest",
    ":core:crypto:desktopTest",
    ":core:data:desktopTest",
    ":core:database:desktopTest",
    ":core:designsystem:desktopTest",
    ":core:domain:desktopTest",
    ":core:navigation:jvmTest",
    ":core:security:desktopTest",
    ":core:testing:desktopTest",
    ":feature:backup:desktopTest",
    ":feature:credential:desktopTest",
    ":feature:generator:desktopTest",
    ":feature:health:desktopTest",
    ":feature:onboarding:desktopTest",
    ":feature:settings:desktopTest",
    ":feature:unlock:desktopTest",
    ":feature:vault:desktopTest",
    ":shared:desktopTest",
)

val androidHostTestPaths = listOf(
    ":core:crypto:testAndroidHostTest",
    ":core:data:testAndroidHostTest",
    ":core:database:testAndroidHostTest",
    ":core:designsystem:testAndroidHostTest",
    ":core:domain:testAndroidHostTest",
    ":core:navigation:testAndroidHostTest",
    ":core:security:testAndroidHostTest",
    ":core:testing:testAndroidHostTest",
    ":feature:backup:testAndroidHostTest",
    ":feature:credential:testAndroidHostTest",
    ":feature:generator:testAndroidHostTest",
    ":feature:health:testAndroidHostTest",
    ":feature:onboarding:testAndroidHostTest",
    ":feature:settings:testAndroidHostTest",
    ":feature:unlock:testAndroidHostTest",
    ":feature:vault:testAndroidHostTest",
    ":shared:testAndroidHostTest",
)

tasks.named("test").configure {
    /*
     * Use task paths rather than eagerly looking up task instances. The
     * Kotlin Multiplatform plugin registers `desktopTest` lazily and Gradle's
     * configuration-on-demand mode may otherwise skip the dependency.
     */
    dependsOn(sharedTestPaths)
    dependsOn(androidHostTestPaths)
}

tasks.register("dependencyReport") {
    group = "reporting"
    description = "Generates a report of resolved project dependencies"

    doLast {
        subprojects.forEach { subproject ->
            println("\n=== ${subproject.path} ===")

            subproject.configurations.forEach { configuration ->
                if (!configuration.isCanBeResolved) {
                    return@forEach
                }

                runCatching {
                    configuration.resolvedConfiguration
                        .firstLevelModuleDependencies
                }.onSuccess { dependencies ->
                    if (dependencies.isEmpty()) {
                        return@onSuccess
                    }

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
                }.onFailure { error ->
                    logger.info(
                        "Could not resolve configuration " +
                            "${subproject.path}:${configuration.name}",
                        error
                    )
                }
            }
        }
    }
}
