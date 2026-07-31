import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJLinkTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

// Load version properties.
val versionProperties = Properties().apply {
    val versionFile = rootProject.file("version.properties")

    if (versionFile.exists()) {
        versionFile.inputStream().use(::load)
    }
}

val publisherProperties = Properties().apply {
    val publisherFile = rootProject.file("release/publisher.properties")

    if (publisherFile.exists()) {
        publisherFile.inputStream().use(::load)
    }
}

fun publisherValue(name: String, fallback: String): String =
    System.getenv(name)?.takeUnless(String::isBlank)
        ?: publisherProperties.getProperty(name)?.takeUnless(String::isBlank)
        ?: fallback

val publisherName = publisherValue("PUBLISHER_NAME", "PassVault")
val copyrightHolder = publisherValue("COPYRIGHT_HOLDER", publisherName)

val versionMajor =
    versionProperties.getProperty("VERSION_MAJOR", "1")

val versionMinor =
    versionProperties.getProperty("VERSION_MINOR", "0")

val versionPatch =
    versionProperties.getProperty("VERSION_PATCH", "0")

val versionName =
    (
        providers.gradleProperty("passvault.versionName").orNull
            ?: "$versionMajor.$versionMinor.$versionPatch"
        ).also {
        require(Regex("""\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?""").matches(it)) {
            "passvault.versionName must be a semantic version"
        }
    }

kotlin {
    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }

        // Do not use withJava().
        // Java sources are compiled automatically.
    }

    sourceSets {
        val desktopMain = getByName("desktopMain") {
            dependencies {
                implementation(project(":shared"))
                implementation(project(":core:designsystem"))
                implementation(compose.desktop.currentOs)
                implementation(libs.compose.material3)
                implementation(libs.compose.components.resources)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.swing)

                // Desktop-specific dependencies.
                implementation(libs.java.keyring)
            }
        }

        val desktopTest = getByName("desktopTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.passvault.desktop.MainKt"

        /*
         * Compose Desktop's ProGuard step is intentionally disabled. The
         * optimized image strips or rewrites runtime contracts used by Room,
         * SQLite/JNA, and Navigation 3, causing the packaged launcher to fail
         * even though the development run succeeds. Android release shrinking
         * remains independently enabled through R8.
         */
        buildTypes.release.proguard.isEnabled.set(false)

        jvmArgs += listOf(
            "--add-opens",
            "java.base/java.lang=ALL-UNNAMED",

            "--add-opens",
            "java.base/java.util=ALL-UNNAMED",

            "--add-opens",
            "java.base/sun.nio.ch=ALL-UNNAMED",

            "--add-opens",
            "java.base/java.nio=ALL-UNNAMED",

            "--add-opens",
            "java.base/java.io=ALL-UNNAMED",

            "--add-opens",
            "java.base/java.security=ALL-UNNAMED",

            "--add-opens",
            "java.base/sun.security.provider=ALL-UNNAMED",

            "--add-opens",
            "java.desktop/java.awt=ALL-UNNAMED",

            "--add-opens",
            "java.desktop/sun.awt=ALL-UNNAMED",
        )

        nativeDistributions {
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Exe,
                TargetFormat.Deb,
                TargetFormat.Rpm,
            )

            packageName = "PassVault"
            packageVersion = versionName

            description =
                "A secure password manager with end-to-end encryption"

            copyright =
                "© 2026 $copyrightHolder. All rights reserved."

            vendor = publisherName

            val projectLicenseFile =
                rootProject.file("LICENSE.txt")

            if (projectLicenseFile.exists()) {
                licenseFile.set(projectLicenseFile)
            }

            val resourcesDirectory =
                project.layout.projectDirectory.dir("resources")

            if (resourcesDirectory.asFile.exists()) {
                appResourcesRootDir.set(resourcesDirectory)
            }

            modules(
                "java.instrument",
                "java.management",
                "java.naming",
                "java.sql",
                "jdk.unsupported",
            )

            macOS {
                bundleID = "com.passvault.desktop"
                packageName = "PassVault"
                dockName = "PassVault"

                setDockNameSameAsPackageName = false

                val macSign =
                    System.getenv("MACOS_SIGN")
                        ?.toBooleanStrictOrNull()
                        ?: false

                val macIdentity =
                    System.getenv("MACOS_IDENTITY").orEmpty()

                val notarizationAppleId =
                    System.getenv(
                        "MACOS_NOTARIZATION_APPLE_ID"
                    ).orEmpty()

                val notarizationPassword =
                    System.getenv(
                        "MACOS_NOTARIZATION_PASSWORD"
                    ).orEmpty()

                val notarizationTeamId =
                    System.getenv(
                        "MACOS_NOTARIZATION_TEAM_ID"
                    ).orEmpty()

                if (macSign && macIdentity.isNotBlank()) {
                    signing {
                        sign.set(true)
                        identity.set(macIdentity)
                    }

                    if (
                        notarizationAppleId.isNotBlank() &&
                        notarizationPassword.isNotBlank() &&
                        notarizationTeamId.isNotBlank()
                    ) {
                        notarization {
                            appleID.set(notarizationAppleId)
                            password.set(notarizationPassword)
//                            teamId.set(notarizationTeamId)
                        }
                    }
                }

                val macIconFile =
                    resourcesDirectory.asFile.resolve(
                        "macos/icon.icns"
                    )

                if (macIconFile.exists()) {
                    iconFile.set(macIconFile)
                }
            }

            windows {
                packageName = "PassVault"
                menuGroup = "PassVault"

                shortcut = true
                console = false
                perUserInstall = true
                dirChooser = true

                /*
                 * Keep this UUID constant between releases.
                 */
                upgradeUuid =
                    "B3B60257-BA42-4233-AF33-5CECFA171EB0"

                val windowsIconFile =
                    resourcesDirectory.asFile.resolve(
                        "windows/icon.ico"
                    )

                if (windowsIconFile.exists()) {
                    iconFile.set(windowsIconFile)
                }
            }

            linux {
                packageName = "passvault"
                // A verified publisher contact must be supplied before a
                // public Debian release. Do not embed a fictional address.
                debMaintainer = "PassVault publisher"
                appCategory = "Office"
                rpmLicenseType = "LicenseRef-Unspecified"

                val linuxIconFile =
                    resourcesDirectory.asFile.resolve(
                        "linux/icon.png"
                    )

                if (linuxIconFile.exists()) {
                    iconFile.set(linuxIconFile)
                }
            }
        }
    }
}

// Copy additional files before generating distributions.
val copyDistributionFiles =
    tasks.register<Copy>("copyDistributionFiles") {
        group = "distribution"
        description =
            "Copies additional distribution files"

        val readmeFile = rootProject.file("README.md")
        val changeLogFile = rootProject.file("CHANGELOG.md")

        if (readmeFile.exists()) {
            from(readmeFile)
        }

        if (changeLogFile.exists()) {
            from(changeLogFile)
        }

        into(
            project.layout.buildDirectory.dir(
                "compose/binaries"
            )
        )
    }

// Create a portable ZIP from the generated runtime image.
tasks.register<Zip>("packagePortable") {
    group = "distribution"
    description =
        "Creates a portable ZIP distribution"

    val runtimeImageTask =
        tasks.named("createRuntimeImage")

    dependsOn(runtimeImageTask)

    from(
        runtimeImageTask.map { task ->
            task.outputs.files
        }
    )

    destinationDirectory.set(
        project.layout.buildDirectory.dir(
            "distributions"
        )
    )

    archiveFileName.set(
        "passvault-$versionName-portable.zip"
    )
}

tasks.withType<AbstractJLinkTask>().configureEach {
    dependsOn(copyDistributionFiles)
}
