import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.tasks.AbstractJLinkTask
import org.jetbrains.compose.desktop.application.tasks.AbstractJPackageTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.Properties
import java.util.concurrent.TimeUnit
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

abstract class StageDesktopBiometricBridge : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceLibrary: RegularFileProperty

    @get:Input
    abstract val platform: Property<String>

    @get:Input
    abstract val libraryName: Property<String>

    @get:Input
    abstract val integrityPolicy: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun stage() {
        val source = sourceLibrary.get().asFile
        check(source.isFile && !Files.isSymbolicLink(source.toPath())) {
            "The expected native biometric bridge was not produced."
        }
        val platformDirectory = outputDirectory.get().dir(platform.get()).asFile
        if (platformDirectory.exists()) {
            check(platformDirectory.deleteRecursively()) {
                "Unable to clear the staged native biometric directory."
            }
        }
        check(platformDirectory.mkdirs()) {
            "Unable to create the staged native biometric directory."
        }
        val destination = platformDirectory.resolve(libraryName.get())
        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
        val digest = MessageDigest.getInstance("SHA-256")
        destination.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
            buffer.fill(0)
        }
        val checksum = digest.digest().joinToString(separator = "") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
        platformDirectory.resolve("bridge.properties").writeText(
            "abi=1\n" +
            "platform=${platform.get()}\n" +
                "library=${libraryName.get()}\n" +
                "integrity=${integrityPolicy.get()}\n" +
                "sha256=$checksum\n",
        )
    }
}

abstract class VerifyDesktopInstalledBiometricBridge : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val distributableDirectory: DirectoryProperty

    @get:Input
    abstract val platform: Property<String>

    @get:Input
    abstract val libraryName: Property<String>

    @get:Input
    abstract val integrityPolicy: Property<String>

    @get:Input
    abstract val requireMacOsDeveloperId: Property<Boolean>

    @TaskAction
    fun verify() {
        val root = distributableDirectory.get().asFile
        check(root.isDirectory && !Files.isSymbolicLink(root.toPath())) {
            "The Desktop distributable is missing or unsafe."
        }
        val bridgeFiles = findBridgeFiles(root)
        if (platform.get() == UNSUPPORTED_PLATFORM) {
            check(bridgeFiles.isEmpty()) {
                "Unsupported Desktop targets must not package a biometric bridge."
            }
            return
        }

        val (manifestFile, library) = resolveBridgeResources(bridgeFiles)
        verifyReviewedLocation(library)
        val entries = readBridgeManifest(manifestFile)
        verifyManifestPolicy(entries)
        verifyBridgeIntegrity(root, library, entries.getValue("sha256"))
    }

    private fun findBridgeFiles(root: File): List<File> = root.walkTopDown()
        .filter { candidate ->
            candidate.isFile && (
                candidate.name == "bridge.properties" ||
                    candidate.name == "libpassvault_biometric.dylib" ||
                    candidate.name == "passvault_biometric.dll"
                )
        }
        .toList()

    private fun resolveBridgeResources(bridgeFiles: List<File>): Pair<File, File> {
        check(bridgeFiles.size == 2 && bridgeFiles.none { Files.isSymbolicLink(it.toPath()) }) {
            "The Desktop image must contain exactly one biometric bridge and one manifest."
        }
        val manifestFile = bridgeFiles.singleOrNull { it.name == "bridge.properties" }
            ?: error("The Desktop biometric bridge manifest is missing.")
        val library = bridgeFiles.singleOrNull { it.name == libraryName.get() }
            ?: error("The Desktop biometric bridge library is missing.")
        check(manifestFile.parentFile == library.parentFile) {
            "The Desktop biometric bridge and manifest must be co-located."
        }
        return manifestFile to library
    }

    private fun verifyReviewedLocation(library: File) {
        val normalizedPath = library.parentFile.toPath().toAbsolutePath().normalize()
        val suffix = listOf("resources", "native", platform.get())
        check(normalizedPath.nameCount >= suffix.size && suffix.indices.all { index ->
            normalizedPath.getName(normalizedPath.nameCount - suffix.size + index).toString() == suffix[index]
        }) {
            "The Desktop biometric bridge is outside the reviewed resource path."
        }
    }

    private fun readBridgeManifest(manifestFile: File): Map<String, String> {
        val entries = linkedMapOf<String, String>()
        manifestFile.readLines(Charsets.UTF_8).forEach { line ->
            val match = MANIFEST_LINE.matchEntire(line)
                ?: error("The Desktop biometric bridge manifest is malformed.")
            check(entries.put(match.groupValues[1], match.groupValues[2]) == null) {
                "The Desktop biometric bridge manifest has duplicate keys."
            }
        }
        return entries
    }

    private fun verifyManifestPolicy(entries: Map<String, String>) {
        check(entries.keys == MANIFEST_KEYS) {
            "The Desktop biometric bridge manifest has missing or extra keys."
        }
        check(
            entries.getValue("abi") == "1" &&
                entries.getValue("platform") == platform.get() &&
                entries.getValue("library") == libraryName.get() &&
                entries.getValue("integrity") == integrityPolicy.get() &&
                SHA256.matches(entries.getValue("sha256")),
        ) {
            "The Desktop biometric bridge manifest violates package policy."
        }
    }

    private fun verifyBridgeIntegrity(root: File, library: File, expectedChecksum: String) {
        val checksumMatches = sha256(library).equals(expectedChecksum, ignoreCase = true)
        if (platform.get().startsWith("macos-")) {
            verifyMacOsBridgeIntegrity(root, library, checksumMatches)
        } else {
            check(checksumMatches) {
                "The packaged Desktop biometric bridge is not checksum-bound."
            }
        }
    }

    private fun verifyMacOsBridgeIntegrity(root: File, library: File, checksumMatches: Boolean) {
        check(integrityPolicy.get() == "sha256-or-developer-id") {
            "The packaged macOS biometric bridge has an invalid integrity policy."
        }
        val appBundle = findSafeMacOsAppBundle(root)
        val hasDeveloperIdProtection = hasMatchingDeveloperIdProtection(library, appBundle)
        if (requireMacOsDeveloperId.get()) {
            check(hasDeveloperIdProtection) {
                "Installed macOS biometric support requires the bridge and owning app to share valid " +
                    "timestamped Developer ID Application signatures with Hardened Runtime."
            }
        } else {
            check(checksumMatches || hasValidAdHocPackageSignature(library, appBundle)) {
                "The unsigned macOS candidate bridge is neither checksum-bound nor validly ad-hoc packaged."
            }
        }
    }

    private fun findSafeMacOsAppBundle(root: File): File =
        root.listFiles().orEmpty().filter { candidate ->
            candidate.isDirectory &&
                candidate.extension == "app" &&
                !Files.isSymbolicLink(candidate.toPath())
        }.singleOrNull() ?: error("The Desktop image must contain exactly one safe macOS app bundle.")

    private fun hasMatchingDeveloperIdProtection(library: File, appBundle: File): Boolean {
        val libraryDetails = codeSigningDetails(library)
        val appDetails = codeSigningDetails(appBundle)
        return libraryDetails != null &&
            appDetails != null &&
            libraryDetails.teamIdentifier == appDetails.teamIdentifier &&
            libraryDetails.isDeveloperId &&
            appDetails.isDeveloperId &&
            libraryDetails.hasHardenedRuntime &&
            appDetails.hasHardenedRuntime &&
            libraryDetails.hasTimestamp &&
            appDetails.hasTimestamp &&
            verifyCodeSignature(library) &&
            verifyCodeSignature(appBundle)
    }

    private fun hasValidAdHocPackageSignature(library: File, appBundle: File): Boolean =
        hasAdHocSignature(library) &&
            hasAdHocSignature(appBundle) &&
            verifyCodeSignature(library) &&
            verifyCodeSignature(appBundle)

    private fun hasAdHocSignature(file: File): Boolean {
        val result = runCodeSign("--display", "--verbose=4", file.absolutePath)
            ?.takeIf { value -> value.exitCode == 0 }
            ?: return false
        val pairs = result.output.lineSequence().mapNotNull { line ->
            val separator = line.indexOf('=')
            if (separator <= 0 || separator == line.lastIndex) null
            else line.substring(0, separator) to line.substring(separator + 1)
        }.toMap()
        return pairs["Signature"] == "adhoc" && pairs["TeamIdentifier"] == "not set"
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            try {
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    digest.update(buffer, 0, count)
                }
            } finally {
                buffer.fill(0)
            }
        }
        return digest.digest().joinToString(separator = "") { byte ->
            "%02x".format(byte.toInt() and 0xff)
        }
    }

    private fun verifyCodeSignature(file: File): Boolean {
        val result = runCodeSign("--verify", "--strict", file.absolutePath)
        return result?.exitCode == 0
    }

    private fun codeSigningDetails(file: File): CodeSigningDetails? {
        return runCodeSign("--display", "--verbose=4", file.absolutePath)
            ?.takeIf { value -> value.exitCode == 0 }
            ?.let { result -> codeSigningDetails(result) }
    }

    private fun codeSigningDetails(result: CodeSignResult): CodeSigningDetails? {
        val pairs = result.output.lineSequence().mapNotNull(::parseCodeSignPair).toList()
        return pairs.firstOrNull { it.first == "TeamIdentifier" }
            ?.second
            ?.takeIf(TEAM_ID::matches)
            ?.let { teamIdentifier ->
                val authority = pairs.firstOrNull { it.first == "Authority" }?.second.orEmpty()
                val codeDirectory = result.output.lineSequence()
                    .firstOrNull { it.startsWith("CodeDirectory ") }
                    .orEmpty()
                val timestamp = pairs.firstOrNull { it.first == "Timestamp" }?.second.orEmpty()
                CodeSigningDetails(
                    teamIdentifier = teamIdentifier,
                    isDeveloperId = authority.startsWith("Developer ID Application:") &&
                        authority.endsWith("($teamIdentifier)"),
                    hasHardenedRuntime = codeDirectory.substringAfter('(', "")
                        .substringBefore(')', "")
                        .split(',')
                        .any { flag -> flag.trim() == "runtime" },
                    hasTimestamp = timestamp.isNotBlank() && timestamp != "none",
                )
            }
    }

    private fun parseCodeSignPair(line: String): Pair<String, String>? {
        val separator = line.indexOf('=')
        return if (separator <= 0 || separator == line.lastIndex) null
        else line.substring(0, separator) to line.substring(separator + 1)
    }

    private fun runCodeSign(vararg arguments: String): CodeSignResult? {
        return File("/usr/bin/codesign").takeIf(File::isFile)?.let { codesign ->
            val process = runCatching {
                ProcessBuilder(listOf(codesign.absolutePath) + arguments)
                    .redirectErrorStream(true)
                    .start()
            }.getOrNull() ?: return@let null
            if (process.waitFor(CODESIGN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                val outputBytes = process.inputStream.use { input ->
                    input.readNBytes(MAX_CODESIGN_OUTPUT_BYTES + 1)
                }
                if (outputBytes.size > MAX_CODESIGN_OUTPUT_BYTES) {
                    outputBytes.fill(0)
                    null
                } else {
                    val output = outputBytes.toString(Charsets.UTF_8)
                    outputBytes.fill(0)
                    CodeSignResult(process.exitValue(), output)
                }
            } else {
                process.destroyForcibly()
                process.waitFor()
                null
            }
        }
    }

    private data class CodeSigningDetails(
        val teamIdentifier: String,
        val isDeveloperId: Boolean,
        val hasHardenedRuntime: Boolean,
        val hasTimestamp: Boolean,
    )

    private data class CodeSignResult(val exitCode: Int, val output: String)

    private companion object {
        const val UNSUPPORTED_PLATFORM = "unsupported"
        val MANIFEST_KEYS = setOf("abi", "platform", "library", "integrity", "sha256")
        val MANIFEST_LINE = Regex("([a-z][a-z0-9_]{0,31})=([A-Za-z0-9._-]{1,256})")
        val SHA256 = Regex("[0-9a-f]{64}")
        val TEAM_ID = Regex("[A-Z0-9]{10}")
        const val CODESIGN_TIMEOUT_SECONDS = 10L
        const val MAX_CODESIGN_OUTPUT_BYTES = 16 * 1024
    }
}

/**
 * Normalizes unsigned macOS app images across host architectures.
 *
 * Apple Silicon launchers receive an ad-hoc signature as part of normal Mach-O
 * linking, while Intel launchers can remain completely unsigned. jpackage
 * signs bundled native libraries in both cases, so without this normalization
 * the x64 candidate has a signed bridge inside an unsigned owning app. This
 * task signs only an otherwise unsigned testing app. Production Developer ID
 * builds never register it.
 */
abstract class AdHocSignUnsignedMacOsApp : DefaultTask() {
    @get:Internal
    abstract val distributableDirectory: DirectoryProperty

    @TaskAction
    fun sign() {
        val codesign = File(CODESIGN)
        check(codesign.isFile) { "macOS candidate normalization requires /usr/bin/codesign." }
        val root = distributableDirectory.get().asFile
        check(root.isDirectory && !Files.isSymbolicLink(root.toPath())) {
            "The Desktop distributable is missing or unsafe."
        }
        val appBundle = root.listFiles().orEmpty().filter { candidate ->
            candidate.isDirectory &&
                candidate.extension == "app" &&
                !Files.isSymbolicLink(candidate.toPath())
        }.singleOrNull() ?: error("The Desktop image must contain exactly one safe macOS app bundle.")

        val existing = runCodeSign("--display", "--verbose=4", appBundle.absolutePath)
        if (existing.exitCode == 0) {
            val hasExpectedSignature = isAdHocSignature(existing.output)
            val isValidBundle = verifyAdHocBundle(appBundle)
            check(hasExpectedSignature && isValidBundle) {
                "An unsigned macOS candidate unexpectedly contains a non-ad-hoc or invalid app signature " +
                    "(adHocRuntime=$hasExpectedSignature, deepStrict=$isValidBundle)."
            }
            return
        }
        check(existing.output.contains(UNSIGNED_CODE_OBJECT_MESSAGE)) {
            "Refusing to replace an unrecognized or invalid macOS app signature."
        }

        val signed = runCodeSign(
            "--force",
            "--sign",
            "-",
            "--options",
            "runtime",
            appBundle.absolutePath,
        )
        check(signed.exitCode == 0) { "Unable to apply the macOS candidate ad-hoc app signature." }
        val normalized = runCodeSign("--display", "--verbose=4", appBundle.absolutePath)
        val hasExpectedSignature = normalized.exitCode == 0 && isAdHocSignature(normalized.output)
        val isValidBundle = verifyAdHocBundle(appBundle)
        check(hasExpectedSignature && isValidBundle) {
            "The normalized macOS candidate app does not have a valid deep ad-hoc signature " +
                "(displayExit=${normalized.exitCode}, adHocRuntime=$hasExpectedSignature, deepStrict=$isValidBundle)."
        }
    }

    private fun verifyAdHocBundle(appBundle: File): Boolean =
        runCodeSign("--verify", "--deep", "--strict", appBundle.absolutePath).exitCode == 0

    private fun isAdHocSignature(output: String): Boolean {
        val pairs = output.lineSequence().mapNotNull { line ->
            val separator = line.indexOf('=')
            if (separator <= 0 || separator == line.lastIndex) null
            else line.substring(0, separator) to line.substring(separator + 1)
        }.toMap()
        val hasHardenedRuntime = output.lineSequence()
            .filter { line -> line.startsWith("CodeDirectory ") }
            .flatMap { line ->
                line.substringAfter('(', "")
                    .substringBefore(')', "")
                    .split(',')
                    .asSequence()
            }
            .any { flag -> flag.trim() == "runtime" }
        return pairs["Signature"] == "adhoc" &&
            pairs["TeamIdentifier"] == "not set" &&
            hasHardenedRuntime
    }

    private fun runCodeSign(vararg arguments: String): CodeSignResult {
        val process = ProcessBuilder(listOf(CODESIGN) + arguments)
            .redirectErrorStream(true)
            .start()
        check(process.waitFor(CODESIGN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            process.waitFor()
            "codesign timed out while normalizing the macOS candidate."
        }
        val outputBytes = process.inputStream.use { input ->
            input.readNBytes(MAX_CODESIGN_OUTPUT_BYTES + 1)
        }
        if (outputBytes.size > MAX_CODESIGN_OUTPUT_BYTES) {
            outputBytes.fill(0)
            error("codesign returned unexpectedly large output while normalizing the macOS candidate.")
        }
        val output = outputBytes.toString(Charsets.UTF_8)
        outputBytes.fill(0)
        return CodeSignResult(process.exitValue(), output)
    }

    private data class CodeSignResult(val exitCode: Int, val output: String)

    private companion object {
        const val CODESIGN = "/usr/bin/codesign"
        const val CODESIGN_TIMEOUT_SECONDS = 10L
        const val MAX_CODESIGN_OUTPUT_BYTES = 16 * 1024
        const val UNSIGNED_CODE_OBJECT_MESSAGE = "code object is not signed at all"
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
val configuredMacJPackageJavaHome =
    System.getenv("MACOS_JPACKAGE_JAVA_HOME")
        ?.takeUnless(String::isBlank)
        ?.let(::file)
val nativeBiometricSourceDirectory = project.layout.projectDirectory.dir("native/biometric-bridge")
val nativeBiometricBuildDirectory = project.layout.buildDirectory.dir("native-biometric/current")
val nativeBiometricStagingDirectory = project.layout.buildDirectory.dir("native-biometric/staged")
val hostOperatingSystem = System.getProperty("os.name").lowercase()
val hostArchitecture = System.getProperty("os.arch").lowercase()
val nativeBiometricPlatform = when {
    hostOperatingSystem.contains("mac") && hostArchitecture in setOf("aarch64", "arm64") -> "macos-arm64"
    hostOperatingSystem.contains("mac") && hostArchitecture in setOf("amd64", "x86_64") -> "macos-x64"
    hostOperatingSystem.contains("win") && hostArchitecture in setOf("amd64", "x86_64") -> "windows-x64"
    else -> null
}
val nativeBiometricLibraryName = when {
    nativeBiometricPlatform?.startsWith("macos-") == true -> "libpassvault_biometric.dylib"
    nativeBiometricPlatform == "windows-x64" -> "passvault_biometric.dll"
    else -> null
}
val configureDesktopBiometricBridge = nativeBiometricPlatform?.let { platform ->
    tasks.register<Exec>("configureDesktopBiometricBridge") {
        group = "build setup"
        description = "Configures the reviewed native biometric bridge for $platform."
        inputs.dir(nativeBiometricSourceDirectory)
        outputs.file(nativeBiometricBuildDirectory.map { directory ->
            directory.file("CMakeCache.txt")
        })
        val command = mutableListOf(
            "cmake",
            "-S",
            nativeBiometricSourceDirectory.asFile.absolutePath,
            "-B",
            nativeBiometricBuildDirectory.get().asFile.absolutePath,
            "-DCMAKE_BUILD_TYPE=Release",
        )
        if (platform.startsWith("macos-")) {
            command += "-DCMAKE_OSX_ARCHITECTURES=${if (platform.endsWith("arm64")) "arm64" else "x86_64"}"
        }
        commandLine(command)
    }
}
val buildDesktopBiometricBridge = configureDesktopBiometricBridge?.let { configureTask ->
    tasks.register<Exec>("buildDesktopBiometricBridge") {
        group = "build"
        description = "Builds the reviewed native biometric bridge."
        dependsOn(configureTask)
        inputs.dir(nativeBiometricSourceDirectory)
        outputs.files(
            nativeBiometricBuildDirectory.map { directory ->
                if (nativeBiometricPlatform == "windows-x64") {
                    directory.file("Release/$nativeBiometricLibraryName")
                } else {
                    directory.file(requireNotNull(nativeBiometricLibraryName))
                }
            },
        )
        commandLine(
            "cmake",
            "--build",
            nativeBiometricBuildDirectory.get().asFile.absolutePath,
            "--config",
            "Release",
            "--parallel",
        )
    }
}
val testDesktopBiometricBridge = buildDesktopBiometricBridge?.let { buildTask ->
    tasks.register<Exec>("testDesktopBiometricBridge") {
        group = "verification"
        description = "Runs native biometric ABI and platform security tests."
        dependsOn(buildTask)
        commandLine(
            "ctest",
            "--test-dir",
            nativeBiometricBuildDirectory.get().asFile.absolutePath,
            "--build-config",
            "Release",
            "--output-on-failure",
        )
    }
}
val stageDesktopBiometricBridge = buildDesktopBiometricBridge?.let { buildTask ->
    tasks.register<StageDesktopBiometricBridge>("stageDesktopBiometricBridge") {
        group = "build setup"
        description = "Stages the native biometric bridge with a strict checksum manifest."
        dependsOn(buildTask)
        sourceLibrary.set(nativeBiometricBuildDirectory.map { directory ->
            if (nativeBiometricPlatform == "windows-x64") {
                directory.file("Release/$nativeBiometricLibraryName")
            } else {
                directory.file(requireNotNull(nativeBiometricLibraryName))
            }
        })
        platform.set(requireNotNull(nativeBiometricPlatform))
        libraryName.set(requireNotNull(nativeBiometricLibraryName))
        integrityPolicy.set(
            if (nativeBiometricPlatform.startsWith("macos-")) {
                "sha256-or-developer-id"
            } else if (nativeBiometricPlatform == "windows-x64") {
                "sha256-and-authenticode"
            } else {
                "sha256"
            },
        )
        outputDirectory.set(nativeBiometricStagingDirectory)
    }
}
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
        if (stageDesktopBiometricBridge != null) {
            dependsOn(stageDesktopBiometricBridge)
            from(nativeBiometricStagingDirectory) {
                into("common/native")
            }
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

if (stageDesktopBiometricBridge != null) {
    tasks.withType<JavaExec>().configureEach {
        if (name == "run" || name == "desktopRun") {
            dependsOn(stageDesktopBiometricBridge)
            systemProperty(
                "passvault.biometric.bridge.dir",
                nativeBiometricStagingDirectory.get().asFile.absolutePath,
            )
        }
    }
    tasks.withType<Test>().configureEach {
        dependsOn(stageDesktopBiometricBridge)
        if (testDesktopBiometricBridge != null) dependsOn(testDesktopBiometricBridge)
        systemProperty(
            "passvault.biometric.testBridgeDirectory",
            nativeBiometricStagingDirectory.get().asFile.absolutePath,
        )
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
                implementation(libs.jna)
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

                val macProvisioningProfile =
                    System.getenv("MACOS_PROVISIONING_PROFILE_PATH")
                        ?.takeUnless(String::isBlank)
                        ?.let(::file)

                val macJPackageJavaHome = configuredMacJPackageJavaHome

                val macEntitlementsFile =
                    resourcesDirectory.asFile.resolve("macos/PassVault.entitlements")

                if (macSign) {
                    require(macIdentity.isNotBlank()) {
                        "MACOS_IDENTITY is required when MACOS_SIGN=true"
                    }
                    require(macKeychain.isNotBlank()) {
                        "MACOS_KEYCHAIN_PATH is required when MACOS_SIGN=true"
                    }
                    require(
                        macProvisioningProfile?.isFile == true &&
                            !Files.isSymbolicLink(macProvisioningProfile.toPath()) &&
                            macProvisioningProfile.name == "embedded.provisionprofile"
                    ) {
                        "MACOS_PROVISIONING_PROFILE_PATH must name a real, non-symlink " +
                            "embedded.provisionprofile when MACOS_SIGN=true"
                    }
                    require(macEntitlementsFile.isFile) {
                        "The PassVault macOS production entitlements file is missing"
                    }
                    require(
                        macJPackageJavaHome?.resolve("bin/jpackage")?.isFile == true &&
                            macJPackageJavaHome.resolve("release").isFile
                    ) {
                        "MACOS_JPACKAGE_JAVA_HOME must point to a JDK 21+ home with jpackage " +
                            "when MACOS_SIGN=true"
                    }
                    val jPackageJavaVersion =
                        macJPackageJavaHome.resolve("release")
                            .useLines { lines ->
                                lines.firstOrNull { line -> line.startsWith("JAVA_VERSION=") }
                            }
                            ?.substringAfter('=')
                            ?.trim('"')
                            ?.substringBefore('.')
                            ?.toIntOrNull()
                    require(jPackageJavaVersion != null && jPackageJavaVersion >= 21) {
                        "MACOS_JPACKAGE_JAVA_HOME must provide JDK 21+ because older jpackage " +
                            "versions cannot embed the Developer ID provisioning profile"
                    }

                    provisioningProfile.set(requireNotNull(macProvisioningProfile))
                    entitlementsFile.set(macEntitlementsFile)

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
                // Program Files ACLs are part of the Windows native-code trust boundary.
                perUserInstall = false
                dirChooser = false

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

if (System.getenv("MACOS_SIGN")?.toBooleanStrictOrNull() == true) {
    val macJPackageJavaHome = requireNotNull(configuredMacJPackageJavaHome)
    afterEvaluate {
        tasks.withType<AbstractJPackageTask>().configureEach {
            javaHome.set(macJPackageJavaHome.absolutePath)
            if (targetFormat == TargetFormat.Dmg) {
                // Compose 1.11 appends "<packageName>.app" for jpackage 18+.
                // Point at the containing image directory so JDK 21 receives
                // the real PassVault.app instead of a duplicated nested path.
                appImage.set(
                    project.layout.buildDirectory.dir(
                        "compose/binaries/main-release/app",
                    ),
                )
            }
        }
    }
}

val adHocSignUnsignedMacOsApp =
    if (
        nativeBiometricPlatform?.startsWith("macos-") == true &&
        System.getenv("MACOS_SIGN")?.toBooleanStrictOrNull() != true
    ) {
        tasks.register<AdHocSignUnsignedMacOsApp>("adHocSignUnsignedMacOsApp") {
            group = "build setup"
            description = "Applies and verifies a hardened-runtime ad-hoc signature on an unsigned macOS app image."
            dependsOn("createReleaseDistributable")
            distributableDirectory.set(
                project.layout.buildDirectory.dir("compose/binaries/main-release/app"),
            )
            outputs.upToDateWhen { false }
        }
    } else {
        null
    }

val desktopDistributableVerificationPrerequisite: Any =
    adHocSignUnsignedMacOsApp ?: "createReleaseDistributable"

val verifyDesktopInstalledLegalNotices =
    tasks.register<VerifyDesktopInstalledLegalNotices>("verifyDesktopInstalledLegalNotices") {
        group = "verification"
        description = "Verifies app-added notices and the separate OpenJDK legal set in the installed image."
        dependsOn(desktopDistributableVerificationPrerequisite)
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
        dependsOn(desktopDistributableVerificationPrerequisite)
        distributableDirectory.set(
            project.layout.buildDirectory.dir("compose/binaries/main-release/app"),
        )
        sqliteVersion.set(libs.versions.sqlite.bundled)
    }

val verifyDesktopInstalledBiometricBridge =
    tasks.register<VerifyDesktopInstalledBiometricBridge>("verifyDesktopInstalledBiometricBridge") {
        group = "verification"
        description = "Verifies the native biometric bridge set in the installed Desktop image."
        dependsOn(desktopDistributableVerificationPrerequisite)
        distributableDirectory.set(
            project.layout.buildDirectory.dir("compose/binaries/main-release/app"),
        )
        platform.set(nativeBiometricPlatform ?: "unsupported")
        libraryName.set(nativeBiometricLibraryName.orEmpty())
        integrityPolicy.set(
            when {
                nativeBiometricPlatform?.startsWith("macos-") == true -> "sha256-or-developer-id"
                nativeBiometricPlatform == "windows-x64" -> "sha256-and-authenticode"
                else -> "unsupported"
            },
        )
        requireMacOsDeveloperId.set(
            providers.gradleProperty("passvault.requireInstalledMacOsBiometric")
                .map { value -> value.toBooleanStrict() }
                .orElse(
                    providers.environmentVariable("MACOS_SIGN")
                        .map { value -> value.toBooleanStrict() }
                        .orElse(false),
                ),
        )
    }

tasks.configureEach {
    if (
        name.matches(Regex("""packageRelease(Dmg|Msi|Exe|Deb|Rpm)""")) ||
        name == "packageReleaseDistributionForCurrentOS" ||
        name == "packagePortable"
    ) {
        dependsOn(verifyDesktopInstalledLegalNotices)
        dependsOn(verifyDesktopInstalledRuntime)
        dependsOn(verifyDesktopInstalledBiometricBridge)
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
