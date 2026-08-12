import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJLinkTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files
import java.util.Properties
import java.util.zip.ZipFile

abstract class ValidateDesktopPublisherMetadata : DefaultTask() {
    @get:Input
    abstract val supportEmail: Property<String>

    @TaskAction
    fun validate() {
        val configuredEmail = supportEmail.get()
        val emailPattern = Regex(
            """^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*""" +
                """@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?""" +
                """(?:\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$""",
        )
        val localPart = configuredEmail.substringBeforeLast('@', missingDelimiterValue = "")
        val domain = configuredEmail.substringAfterLast('@', missingDelimiterValue = "").lowercase()
        val hasConventionalLengths = configuredEmail.length <= MAX_EMAIL_LENGTH &&
            localPart.length in 1..MAX_LOCAL_PART_LENGTH &&
            domain.length in 1..MAX_DOMAIN_LENGTH
        val reservedSuffixes = setOf("invalid", "test", "example", "localhost")
        val reservedExampleDomains = setOf("example.com", "example.net", "example.org")
        val usesReservedDomain = reservedSuffixes.any { suffix ->
            domain == suffix || domain.endsWith(".$suffix")
        } || reservedExampleDomains.any { reserved ->
            domain == reserved || domain.endsWith(".$reserved")
        }
        if (!hasConventionalLengths || !emailPattern.matches(configuredEmail) || usesReservedDomain) {
            throw GradleException(
                "SUPPORT_EMAIL must be a configured, syntactically valid, non-reserved production address " +
                    "before packaging PassVault Desktop",
            )
        }
    }

    private companion object {
        const val MAX_EMAIL_LENGTH = 254
        const val MAX_LOCAL_PART_LENGTH = 64
        const val MAX_DOMAIN_LENGTH = 253
    }
}

abstract class VerifyDesktopInstalledLegalNotices : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val distributableDirectory: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val legalDocuments: ConfigurableFileCollection

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val thirdPartyLicenseDirectory: DirectoryProperty

    @TaskAction
    fun verify() {
        val distributableRoot = distributableDirectory.get().asFile
        val sourceLicenseFiles = loadCanonicalLicenseFiles()
        val sourceLegalDocuments = loadCanonicalLegalDocuments()
        val bundledLegalDirectory = findBundledLegalDirectory(distributableRoot, sourceLegalDocuments)

        verifyBundledLegalDocuments(bundledLegalDirectory, sourceLegalDocuments)
        verifyBundledLicenseFiles(bundledLegalDirectory, sourceLicenseFiles)
        verifyOpenJdkLegalNotices(distributableRoot)
    }

    private fun loadCanonicalLicenseFiles(): List<File> {
        val sourceLicenseDirectory = thirdPartyLicenseDirectory.get().asFile
        check(sourceLicenseDirectory.isDirectory && !Files.isSymbolicLink(sourceLicenseDirectory.toPath())) {
            "Canonical third-party license directory is missing or unsafe."
        }
        val sourceLicenseFiles = sourceLicenseDirectory.listFiles()
            .orEmpty()
            .sortedBy { source -> source.name }
        check(sourceLicenseFiles.isNotEmpty() && sourceLicenseFiles.all { source ->
            source.isFile &&
                !Files.isSymbolicLink(source.toPath()) &&
                Regex("""[A-Za-z0-9._+-]+""").matches(source.name)
        }) {
            "Canonical third-party license directory must contain only safe regular files."
        }
        return sourceLicenseFiles
    }

    private fun loadCanonicalLegalDocuments(): List<File> {
        val sourceLegalDocuments = legalDocuments.files
            .sortedBy { source -> source.name }
        check(sourceLegalDocuments.size == 3 && sourceLegalDocuments.all { source ->
            source.isFile &&
                !Files.isSymbolicLink(source.toPath()) &&
                Regex("""[A-Za-z0-9._+-]+""").matches(source.name)
        }) {
            "Canonical top-level legal documents are missing or unsafe."
        }
        return sourceLegalDocuments
    }

    private fun findBundledLegalDirectory(
        distributableRoot: File,
        sourceLegalDocuments: List<File>,
    ): File {
        val legalDirectories = distributableRoot.walkTopDown()
            .filter { candidate ->
                candidate.isDirectory &&
                    candidate.name == "legal" &&
                    candidate.parentFile?.name == "resources" &&
                    sourceLegalDocuments.all { source ->
                        candidate.resolve(source.name).isFile
                    }
            }
            .toList()
        check(legalDirectories.size == 1) {
            "Expected exactly one application legal resource directory, found ${legalDirectories.size}."
        }
        val bundledLegalDirectory = legalDirectories.single()
        check(!Files.isSymbolicLink(bundledLegalDirectory.toPath())) {
            "Installed application legal directory must not be a symlink."
        }
        return bundledLegalDirectory
    }

    private fun verifyBundledLegalDocuments(
        bundledLegalDirectory: File,
        sourceLegalDocuments: List<File>,
    ) {
        val expectedLegalEntries =
            (sourceLegalDocuments.map { source -> source.name } + "THIRD_PARTY_LICENSES").sorted()
        val bundledLegalEntries = bundledLegalDirectory.listFiles()
            .orEmpty()
            .sortedBy { candidate -> candidate.name }
        check(
            bundledLegalEntries.map { candidate -> candidate.name } == expectedLegalEntries &&
                bundledLegalEntries.all { candidate -> !Files.isSymbolicLink(candidate.toPath()) },
        ) {
            "Installed application legal resource set contains missing, extra, or unsafe entries."
        }

        sourceLegalDocuments.forEach { source ->
            val bundled = bundledLegalDirectory.resolve(source.name)
            check(
                bundled.isFile &&
                    !Files.isSymbolicLink(bundled.toPath()) &&
                    bundled.readBytes().contentEquals(source.readBytes()),
            ) {
                "Installed legal document is missing, unsafe, or stale: ${source.name}"
            }
        }
    }

    private fun verifyBundledLicenseFiles(
        bundledLegalDirectory: File,
        sourceLicenseFiles: List<File>,
    ) {
        val bundledLicenseDirectory = bundledLegalDirectory.resolve("THIRD_PARTY_LICENSES")
        check(bundledLicenseDirectory.isDirectory && !Files.isSymbolicLink(bundledLicenseDirectory.toPath())) {
            "Installed third-party license directory is missing or unsafe."
        }
        val bundledLicenseFiles = bundledLicenseDirectory.listFiles()
            .orEmpty()
            .sortedBy { candidate -> candidate.name }
        check(
            bundledLicenseFiles.all { candidate ->
                candidate.isFile && !Files.isSymbolicLink(candidate.toPath())
            } &&
                bundledLicenseFiles.map { candidate -> candidate.name } ==
                sourceLicenseFiles.map { source -> source.name },
        ) {
            "Installed third-party license file set does not match the canonical source set."
        }
        sourceLicenseFiles.forEach { source ->
            val bundled = bundledLicenseDirectory.resolve(source.name)
            check(
                !Files.isSymbolicLink(bundled.toPath()) &&
                    bundled.readBytes().contentEquals(source.readBytes()),
            ) {
                "Installed third-party license is unsafe or stale: ${source.name}"
            }
        }
    }

    private fun verifyOpenJdkLegalNotices(distributableRoot: File) {
        val openJdkRuntimeLicenses = distributableRoot.walkTopDown()
            .filter { candidate ->
                candidate.isFile &&
                    candidate.name == "LICENSE" &&
                    candidate.relativeTo(distributableRoot).invariantSeparatorsPath
                        .contains("/runtime/") &&
                    candidate.parentFile?.name == "java.base" &&
                    candidate.parentFile?.parentFile?.name == "legal"
            }
            .toList()
        check(openJdkRuntimeLicenses.isNotEmpty() && openJdkRuntimeLicenses.all { candidate ->
            candidate.length() > 0L && !Files.isSymbolicLink(candidate.toPath())
        }) {
            "The packaged OpenJDK runtime's built-in java.base legal notice is missing or unsafe."
        }
    }
}

abstract class VerifyDesktopInstalledRuntime : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val distributableDirectory: DirectoryProperty

    @get:Input
    abstract val sqliteVersion: Property<String>

    @TaskAction
    fun verify() {
        val distributableRoot = distributableDirectory.get().asFile
        check(distributableRoot.isDirectory && !Files.isSymbolicLink(distributableRoot.toPath())) {
            "Desktop release distributable is missing or unsafe."
        }
        val sqliteJars = distributableRoot.walkTopDown()
            .filter { candidate ->
                candidate.isFile &&
                    !Files.isSymbolicLink(candidate.toPath()) &&
                    candidate.name.startsWith("sqlite-bundled-jvm-") &&
                    candidate.extension == "jar"
            }
            .toList()
        check(sqliteJars.size == 1) {
            "Expected exactly one bundled SQLite JVM archive, found ${sqliteJars.size}."
        }
        val sqliteJar = sqliteJars.single()
        val expectedPrefix = "sqlite-bundled-jvm-${sqliteVersion.get()}"
        check(sqliteJar.nameWithoutExtension == expectedPrefix || sqliteJar.name.startsWith("$expectedPrefix-")) {
            "Packaged SQLite archive does not match the configured version: ${sqliteJar.name}"
        }
        verifySqliteArchive(sqliteJar)
    }

    private fun verifySqliteArchive(sqliteJar: File) {
        ZipFile(sqliteJar).use { archive ->
            val entries = archive.entries().asSequence().toList()
            val entryNames = entries.map { entry -> entry.name }
            check(entryNames.size == entryNames.toSet().size) {
                "Packaged SQLite archive contains duplicate ZIP entries."
            }
            val nativeEntries = entries
                .filter { entry -> !entry.isDirectory && entry.name.startsWith("natives/") }
                .associateBy { entry -> entry.name }
            check(nativeEntries.keys == EXPECTED_SQLITE_NATIVES.keys) {
                "Packaged SQLite native target set is incomplete or unexpected: ${nativeEntries.keys.sorted()}"
            }
            nativeEntries.forEach { (name, entry) ->
                check(entry.size in 1..MAX_NATIVE_SIZE_BYTES) {
                    "Packaged SQLite native has an invalid size: $name"
                }
                val header = archive.getInputStream(entry).use { input ->
                    input.readNBytes(NATIVE_HEADER_BYTES)
                }
                verifyNativeHeader(name, EXPECTED_SQLITE_NATIVES.getValue(name), header)
            }
            val license = archive.getEntry(SQLITE_LICENSE_ENTRY)
            check(license != null && !license.isDirectory && license.size > 0L) {
                "Packaged SQLite archive is missing its AndroidX license."
            }
        }
    }

    private fun verifyNativeHeader(name: String, expected: NativeBinary, header: ByteArray) {
        val matches = when (expected) {
            NativeBinary.ELF_ARM64 -> isElf(header, ELF_MACHINE_ARM64)
            NativeBinary.ELF_X64 -> isElf(header, ELF_MACHINE_X64)
            NativeBinary.MACH_O_ARM64 -> isMachO(header, MACH_CPU_ARM64)
            NativeBinary.MACH_O_X64 -> isMachO(header, MACH_CPU_X64)
            NativeBinary.PE_X64 -> isPortableExecutable(header, PE_MACHINE_X64)
        }
        check(matches) { "Packaged SQLite native has the wrong binary architecture: $name" }
    }

    private fun isElf(header: ByteArray, expectedMachine: Int): Boolean =
        header.hasBytes(20) &&
            header.unsignedByte(0) == 0x7f &&
            header.copyOfRange(1, 4).contentEquals("ELF".encodeToByteArray()) &&
            header.unsignedByte(4) == ELF_CLASS_64 &&
            header.unsignedByte(5) == LITTLE_ENDIAN &&
            header.littleEndian16(18) == expectedMachine

    private fun isMachO(header: ByteArray, expectedCpu: Int): Boolean =
        header.hasBytes(8) &&
            header.littleEndian32(0) == MACH_O_64_MAGIC &&
            header.littleEndian32(4) == expectedCpu

    private fun isPortableExecutable(header: ByteArray, expectedMachine: Int): Boolean {
        val hasDosHeader =
            header.hasBytes(PE_POINTER_OFFSET + Int.SIZE_BYTES) &&
                header.unsignedByte(0) == 'M'.code &&
                header.unsignedByte(1) == 'Z'.code
        val peOffset = if (hasDosHeader) {
            header.littleEndian32(PE_POINTER_OFFSET)
        } else {
            INVALID_PE_OFFSET
        }
        val peHeaderEnd = peOffset.toLong() + PE_MACHINE_OFFSET + Short.SIZE_BYTES
        val hasCompletePeHeader = peOffset >= 0 && peHeaderEnd <= header.size.toLong()
        return hasCompletePeHeader &&
            header.copyOfRange(peOffset, peOffset + PE_SIGNATURE.size).contentEquals(PE_SIGNATURE) &&
            header.littleEndian16(peOffset + PE_MACHINE_OFFSET) == expectedMachine
    }

    private fun ByteArray.hasBytes(requiredSize: Int): Boolean = size >= requiredSize

    private fun ByteArray.unsignedByte(offset: Int): Int = this[offset].toInt() and 0xff

    private fun ByteArray.littleEndian16(offset: Int): Int =
        unsignedByte(offset) or (unsignedByte(offset + 1) shl Byte.SIZE_BITS)

    private fun ByteArray.littleEndian32(offset: Int): Int =
        littleEndian16(offset) or (littleEndian16(offset + Short.SIZE_BYTES) shl Short.SIZE_BITS)

    private enum class NativeBinary {
        ELF_ARM64,
        ELF_X64,
        MACH_O_ARM64,
        MACH_O_X64,
        PE_X64,
    }

    private companion object {
        const val MAX_NATIVE_SIZE_BYTES = 32L * 1024L * 1024L
        const val NATIVE_HEADER_BYTES = 4096
        const val SQLITE_LICENSE_ENTRY = "META-INF/androidx/sqlite/sqlite-bundled/LICENSE.txt"
        const val ELF_CLASS_64 = 2
        const val LITTLE_ENDIAN = 1
        const val ELF_MACHINE_X64 = 62
        const val ELF_MACHINE_ARM64 = 183
        const val MACH_O_64_MAGIC = -17958193 // 0xfeedfacf
        const val MACH_CPU_X64 = 0x01000007
        const val MACH_CPU_ARM64 = 0x0100000c
        const val PE_POINTER_OFFSET = 0x3c
        const val PE_MACHINE_OFFSET = 4
        const val PE_MACHINE_X64 = 0x8664
        const val INVALID_PE_OFFSET = -1
        val PE_SIGNATURE = byteArrayOf('P'.code.toByte(), 'E'.code.toByte(), 0, 0)
        val EXPECTED_SQLITE_NATIVES = mapOf(
            "natives/linux_arm64/libsqliteJni.so" to NativeBinary.ELF_ARM64,
            "natives/linux_x64/libsqliteJni.so" to NativeBinary.ELF_X64,
            "natives/osx_arm64/libsqliteJni.dylib" to NativeBinary.MACH_O_ARM64,
            "natives/osx_x64/libsqliteJni.dylib" to NativeBinary.MACH_O_X64,
            "natives/windows_x64/sqliteJni.dll" to NativeBinary.PE_X64,
        )
    }
}

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

fun configuredPublisherValue(name: String): String? =
    System.getenv(name)?.takeUnless(String::isBlank)
        ?: publisherProperties.getProperty(name)?.takeUnless(String::isBlank)

val publisherName = publisherValue("PUBLISHER_NAME", "PassVault")
val copyrightHolder = publisherValue("COPYRIGHT_HOLDER", publisherName)
val publisherSupportEmail = configuredPublisherValue("SUPPORT_EMAIL").orEmpty()
val resourcesDirectory = project.layout.projectDirectory.dir("resources")
val generatedDesktopAppResources =
    project.layout.buildDirectory.dir("generated/desktopAppResources")
val prepareDesktopAppResources =
    tasks.register<Sync>("prepareDesktopAppResources") {
        group = "build setup"
        description = "Prepares icons and canonical legal notices for installed Desktop packages."

        from(resourcesDirectory)
        from(
            rootProject.files(
                "LICENSE.txt",
                "NOTICE.txt",
                "THIRD_PARTY_NOTICES.md",
            ),
        ) {
            into("common/legal")
        }
        from(rootProject.file("THIRD_PARTY_LICENSES")) {
            into("common/legal/THIRD_PARTY_LICENSES")
        }
        into(generatedDesktopAppResources)
    }

val validateDesktopPublisherMetadata = tasks.register<ValidateDesktopPublisherMetadata>(
    "validateDesktopPublisherMetadata",
) {
    group = "verification"
    description = "Validates publisher metadata required by native Desktop packages"
    supportEmail.set(publisherSupportEmail)
}

// Keep ordinary compilation and tests configuration-only. Every task that
// creates a native/portable package receives the publisher preflight at task
// execution time, including Compose tasks registered later by the plugin.
tasks.configureEach {
    if (name == "prepareAppResources") {
        dependsOn(prepareDesktopAppResources)
    }
    if (name.startsWith("package") || name == "createDistributable" || name == "createReleaseDistributable") {
        dependsOn(validateDesktopPublisherMetadata)
        dependsOn(prepareDesktopAppResources)
    }
}

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
                implementation(libs.compose.components.resources)
                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        val desktopTest = getByName("desktopTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
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

            appResourcesRootDir.set(generatedDesktopAppResources)

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
                appCategory = "public.app-category.productivity"
                // The bundled launcher, JDK runtime, and Skiko native library
                // all declare macOS 11 as their minimum deployment target.
                minimumSystemVersion = "11.0"

                setDockNameSameAsPackageName = false

                val macSign =
                    System.getenv("MACOS_SIGN")
                        ?.toBooleanStrictOrNull()
                        ?: false

                val macIdentity =
                    System.getenv("MACOS_IDENTITY").orEmpty()

                val macKeychain =
                    System.getenv("MACOS_KEYCHAIN_PATH").orEmpty()

                if (macSign) {
                    require(macIdentity.isNotBlank()) {
                        "MACOS_IDENTITY is required when MACOS_SIGN=true"
                    }
                    require(macKeychain.isNotBlank()) {
                        "MACOS_KEYCHAIN_PATH is required when MACOS_SIGN=true"
                    }

                    signing {
                        sign.set(true)
                        identity.set(macIdentity)
                        keychain.set(macKeychain)
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
                debMaintainer = publisherSupportEmail
                appCategory = "Office"
                rpmLicenseType = "Apache-2.0"

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

val verifyDesktopInstalledLegalNotices =
    tasks.register<VerifyDesktopInstalledLegalNotices>("verifyDesktopInstalledLegalNotices") {
        group = "verification"
        description = "Verifies app-added notices and the separate OpenJDK legal set in the installed image."
        dependsOn("createReleaseDistributable")
        distributableDirectory.set(
            project.layout.buildDirectory.dir("compose/binaries/main-release/app"),
        )
        legalDocuments.from(
            rootProject.files("LICENSE.txt", "NOTICE.txt", "THIRD_PARTY_NOTICES.md"),
        )
        thirdPartyLicenseDirectory.set(rootProject.layout.projectDirectory.dir("THIRD_PARTY_LICENSES"))
    }

val verifyDesktopInstalledRuntime =
    tasks.register<VerifyDesktopInstalledRuntime>("verifyDesktopInstalledRuntime") {
        group = "verification"
        description = "Verifies the bundled SQLite target matrix in the installed Desktop image."
        dependsOn("createReleaseDistributable")
        distributableDirectory.set(
            project.layout.buildDirectory.dir("compose/binaries/main-release/app"),
        )
        sqliteVersion.set(libs.versions.sqlite.bundled)
    }

tasks.configureEach {
    if (
        name.matches(Regex("""packageRelease(Dmg|Msi|Exe|Deb|Rpm)""")) ||
        name == "packageReleaseDistributionForCurrentOS" ||
        name == "packagePortable"
    ) {
        dependsOn(verifyDesktopInstalledLegalNotices)
        dependsOn(verifyDesktopInstalledRuntime)
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

// Create a portable ZIP from the complete application distribution.
tasks.register<Zip>("packagePortable") {
    group = "distribution"
    description =
        "Creates a portable ZIP distribution"

    val releaseDistributableTask =
        tasks.named("createReleaseDistributable")

    dependsOn(releaseDistributableTask)

    from(
        releaseDistributableTask.map { task ->
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
