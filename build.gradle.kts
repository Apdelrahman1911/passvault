import dev.detekt.gradle.Detekt
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
        check("<verify-signatures>false</verify-signatures>" in metadata) {
            "PassVault's reviewed SHA-256 policy must not silently change modes."
        }

        val artifactStartCount = Regex("<artifact name=").findAll(metadata).count()
        val artifactBlocks = Regex(
            pattern = """<artifact name="[^"]+">(.*?)</artifact>""",
            option = RegexOption.DOT_MATCHES_ALL,
        ).findAll(metadata).toList()
        val sha256Pattern = Regex("""<sha256 value="[0-9a-f]{64}"(?: [^>]*)?/>""")
        val everyArtifactHasOneSha256 = artifactBlocks.all { artifact ->
            sha256Pattern.findAll(artifact.value).count() == 1
        }
        check(
            artifactStartCount > 0 &&
                artifactBlocks.size == artifactStartCount &&
                everyArtifactHasOneSha256 &&
                sha256Pattern.findAll(metadata).count() == artifactStartCount,
        ) {
            "Every verified dependency artifact must have exactly one lowercase SHA-256 checksum."
        }
        check(!Regex("<(?:md5|sha1|sha512|pgp) ").containsMatchIn(metadata)) {
            "Dependency verification must use SHA-256 checksums exclusively."
        }

        val trustedArtifactCount = Regex("<trust ").findAll(metadata).count()
        val allowedTrustRules = listOf(
            """<trust file=".*-javadoc[.]jar" regex="true" reason="""",
            """<trust file=".*-sources[.]jar" regex="true" reason="""",
            """<trust group="org[.]apache[.]groovy" version="4[.]0[.]32" """ +
                """file=".*[.]module" regex="true" reason="""",
        )
        check(trustedArtifactCount == allowedTrustRules.size && allowedTrustRules.all(metadata::contains)) {
            "Dependency verification contains an unreviewed or overly broad trust rule."
        }

        logger.lifecycle(
            "Validated SHA-256 dependency verification metadata " +
                "(${file.length()} bytes).",
        )
    }
}

abstract class VerifyReleaseVersionTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val versionPropertiesFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val buildInfoFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val iosConfigurationFile: RegularFileProperty

    @TaskAction
    fun verifyVersions() {
        fun property(text: String, name: String): String =
            Regex("(?m)^${Regex.escape(name)}=([^\\r\\n]+)$")
                .find(text)
                ?.groupValues
                ?.get(1)
                ?.trim()
                ?.takeIf(String::isNotEmpty)
                ?: error("Missing $name")

        val canonical = versionPropertiesFile.get().asFile.readText()
        val expectedMajor = property(canonical, "VERSION_MAJOR")
        val expectedMinor = property(canonical, "VERSION_MINOR")
        val expectedPatch = property(canonical, "VERSION_PATCH")
        val expectedName = property(canonical, "VERSION_NAME")
        val expectedCode = property(canonical, "VERSION_CODE")
        val versionComponent = Regex("""0|[1-9]\d*""")
        check(
            listOf(expectedMajor, expectedMinor, expectedPatch).all(versionComponent::matches),
        ) {
            "VERSION_MAJOR, VERSION_MINOR, and VERSION_PATCH must be canonical non-negative integers"
        }
        val composedName = "$expectedMajor.$expectedMinor.$expectedPatch"
        check(expectedName == composedName) {
            "VERSION_NAME ($expectedName) must equal VERSION_MAJOR.MINOR.PATCH ($composedName)"
        }
        check(
            expectedCode.matches(Regex("""[1-9]\d*""")) &&
                expectedCode.toLongOrNull()?.let { it <= 2_100_000_000L } == true,
        ) {
            "VERSION_CODE must be a canonical positive Android version code"
        }

        val buildInfoVersion = Regex("const val VERSION: String = \"([^\"]+)\"")
            .find(buildInfoFile.get().asFile.readText())
            ?.groupValues
            ?.get(1)
            ?: error("Missing PassVaultBuildInfo.VERSION")
        check(buildInfoVersion == expectedName) {
            "PassVaultBuildInfo.VERSION ($buildInfoVersion) must equal VERSION_NAME ($expectedName)"
        }

        val iosConfiguration = iosConfigurationFile.get().asFile.readText()
        val iosName = property(iosConfiguration, "MARKETING_VERSION")
        val iosCode = property(iosConfiguration, "CURRENT_PROJECT_VERSION")
        check(iosName == expectedName && iosCode == expectedCode) {
            "iOS version $iosName ($iosCode) must equal $expectedName ($expectedCode)"
        }

        logger.lifecycle("Validated cross-platform release version $expectedName ($expectedCode).")
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    alias(libs.plugins.compose) apply false
    alias(libs.plugins.compose.compiler) apply false

    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false

    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false

    // Apply Detekt at the root so Gradle Kotlin is analyzed too.
    alias(libs.plugins.detekt)
}

fun Project.configureDetekt() {
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig.set(true)
        allRules.set(false)
        // Detekt 2.0.0-alpha.5 can crash its Kotlin analysis service when
        // multiple KMP/native files are analyzed concurrently. Correctness of
        // the gate is more important than a few seconds of parallelism.
        parallel.set(false)

        config.setFrom(
            rootProject.files("detekt.yml")
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

}

configureDetekt()

subprojects {
    pluginManager.apply("dev.detekt")
    configureDetekt()

    // Detekt's generic task does not discover Kotlin Multiplatform source
    // sets automatically. Point the one CI-facing task at source roots only;
    // source-set-specific tasks otherwise duplicate common code and can ingest
    // Room/KSP output under build/generated.
    tasks.named<Detekt>("detekt") {
        setSource(layout.projectDirectory.dir("src"))
        include("**/*.kt", "**/*.kts")
    }
}

tasks.named<Detekt>("detekt") {
    setSource(fileTree(rootDir))
    setIncludes(
        listOf(
            "*.gradle.kts",
            "**/*.gradle.kts",
        ),
    )
    exclude(
        "**/build/**",
        "**/.gradle/**",
        "**/.kotlin/**",
        "**/.idea/**",
        "**/generated/**",
    )
    dependsOn(subprojects.map { it.tasks.named("detekt") })
}

val verifyReleaseVersion = tasks.register<VerifyReleaseVersionTask>("verifyReleaseVersion") {
    group = "verification"
    description = "Validates shared, Android, desktop, and iOS release-version alignment"
    versionPropertiesFile.set(layout.projectDirectory.file("version.properties"))
    buildInfoFile.set(
        layout.projectDirectory.file(
            "core/domain/src/commonMain/kotlin/com/passvault/core/domain/PassVaultBuildInfo.kt",
        ),
    )
    iosConfigurationFile.set(layout.projectDirectory.file("iosApp/Configuration/Config.xcconfig"))
}

tasks.register<VerifyDependencyMetadataTask>("verifyDependencies") {
    group = "verification"
    description = "Validates the Gradle SHA-256 dependency verification policy"
    dependsOn(verifyReleaseVersion)

    metadataFile.set(
        layout.projectDirectory.file("gradle/verification-metadata.xml")
    )
}

val verifyLocalization = tasks.register<Exec>("verifyLocalization") {
    group = "verification"
    description = "Validates complete Arabic resources, plurals, and formatting placeholders"
    inputs.files(
        layout.projectDirectory.file(
            "core/designsystem/src/commonMain/composeResources/values/strings.xml",
        ),
        layout.projectDirectory.file(
            "core/designsystem/src/commonMain/composeResources/values-ar/strings.xml",
        ),
        layout.projectDirectory.file("scripts/validate-localizations.rb"),
    )
    commandLine("ruby", "scripts/validate-localizations.rb")
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
    ":core:database:desktopTest",
    ":core:designsystem:desktopTest",
    ":core:domain:desktopTest",
    ":core:navigation:jvmTest",
    ":core:otp:desktopTest",
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
    ":app-android:testFdroidDebugUnitTest",
    ":app-android:testStandardDebugUnitTest",
    ":core:crypto:testAndroidHostTest",
    ":core:database:testAndroidHostTest",
    ":core:designsystem:testAndroidHostTest",
    ":core:domain:testAndroidHostTest",
    ":core:navigation:testAndroidHostTest",
    ":core:otp:testAndroidHostTest",
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
    dependsOn(verifyReleaseVersion)
    dependsOn(verifyLocalization)
}

val rootCheck = tasks.register("check") {
    group = "verification"
    description = "Runs all project tests, static analysis, and dependency-policy checks"
    dependsOn(tasks.named("test"))
    dependsOn(tasks.named("detekt"))
    dependsOn(tasks.named("verifyDependencies"))
    dependsOn(verifyLocalization)
}

subprojects {
    tasks.matching { it.name == "check" }.configureEach {
        rootCheck.configure {
            dependsOn(this@configureEach)
        }
        dependsOn(rootProject.tasks.named("verifyReleaseVersion"))
    }
}

tasks.register("dependencyReport") {
    group = "reporting"
    description = "Generates a report of resolved project dependencies"

    doLast {
        subprojects.forEach { subproject ->
            println("\n=== ${subproject.path} ===")

            // Resolving a configuration can cause Android/KMP plugins to
            // register additional configurations. Iterate over a stable
            // snapshot so the report cannot fail midway with a concurrent
            // modification and silently leave dependency coverage partial.
            subproject.configurations.toList().forEach { configuration ->
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
