package com.passvault.desktop.security.biometric

import com.passvault.core.security.BiometricType
import com.passvault.desktop.OperatingSystem
import com.sun.jna.Library
import com.sun.jna.Native
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

internal class DesktopBiometricNativeLoader(
    private val operatingSystem: OperatingSystem,
    private val architectureName: String = System.getProperty("os.arch").orEmpty(),
    private val appResourcesDirectory: String? = System.getProperty(COMPOSE_RESOURCES_PROPERTY),
    private val developmentBridgeDirectory: String? = System.getProperty(DEVELOPMENT_BRIDGE_PROPERTY),
    private val dataDirectoryOverride: Path? = null,
    private val javaHomeDirectory: Path = Paths.get(System.getProperty("java.home").orEmpty()),
    private val windowsAuthenticodeVerifier: ((Path, Path) -> Boolean)? = null,
) {
    fun load(): DesktopBiometricBridge {
        val platform = nativePlatform(operatingSystem, architectureName)
            ?: throw DesktopBiometricBridgeException.NotAvailable
        val location = resolveBridgeDirectory(platform)
        val bridgeDirectory = location.directory
        val manifest = BridgeManifest.read(bridgeDirectory.resolve(MANIFEST_FILE))
        require(manifest.abi == JnaDesktopBiometricBridge.EXPECTED_ABI) {
            "Desktop biometric manifest ABI does not match"
        }
        require(manifest.platform == platform.id) { "Desktop biometric manifest platform does not match" }
        require(manifest.library == platform.libraryName) { "Desktop biometric library name is invalid" }
        require(manifest.integrity == platform.integrityPolicy) {
            "Desktop biometric integrity policy does not match its platform"
        }
        val library = bridgeDirectory.resolve(manifest.library).normalize()
        require(library.parent == bridgeDirectory) { "Desktop biometric library escapes its resource directory" }
        requireSecureRegularFile(library)
        require(Files.size(library) in 1..MAX_LIBRARY_BYTES) { "Desktop biometric library size is invalid" }
        val actualHash = sha256(library)
        val checksumMatches = actualHash.equals(manifest.sha256, ignoreCase = true)
        val validDeveloperIdFallback = !checksumMatches &&
            manifest.integrity == INTEGRITY_SHA256_OR_DEVELOPER_ID &&
            verifyMacOsDeveloperIdBundle(library)
        require(checksumMatches || validDeveloperIdFallback) {
            "Desktop biometric library checksum does not match"
        }
        if (location.isPackaged && operatingSystem == OperatingSystem.MACOS) {
            require(verifyMacOsDeveloperIdBundle(library)) {
                "Packaged macOS biometric code lacks the owning app's Developer ID protection"
            }
        }
        if (location.isPackaged && operatingSystem == OperatingSystem.WINDOWS) {
            val launcher = windowsPackagedLauncher()
                ?: throw IllegalArgumentException("Packaged Windows launcher is missing or unsafe")
            val hasAuthenticodeProtection = windowsAuthenticodeVerifier?.invoke(library, launcher)
                ?: verifyWindowsAuthenticodeBundle(library, launcher)
            require(hasAuthenticodeProtection) {
                "Packaged Windows biometric code lacks the owning app's Authenticode protection"
            }
        }
        val api = Native.load(
            library.toString(),
            NativeApi::class.java,
            mapOf(Library.OPTION_STRING_ENCODING to Charsets.UTF_8.name()),
        )
        val dataDirectory = ensureSecureDataDirectory()
        return JnaDesktopBiometricBridge.create(platform.biometricType, api, dataDirectory.toString())
    }

    private fun resolveBridgeDirectory(platform: NativePlatform): BridgeLocation {
        val packagedResources = packagedResourcesDirectory()
        val developmentRoot = developmentBridgeDirectory?.takeIf(String::isNotBlank)
        val root = when {
            packagedResources != null -> packagedResources.resolve("native")
            developmentRoot != null -> Paths.get(developmentRoot)
            else -> {
                val resources = appResourcesDirectory?.takeIf(String::isNotBlank)
                    ?: throw DesktopBiometricBridgeException.NotAvailable
                Paths.get(resources).resolve("native")
            }
        }
        val normalizedRoot = root.toAbsolutePath().normalize()
        require(Files.isDirectory(normalizedRoot, LinkOption.NOFOLLOW_LINKS)) {
            "Desktop biometric resource directory is missing"
        }
        require(!Files.isSymbolicLink(normalizedRoot)) { "Desktop biometric resource directory is a symlink" }
        val platformDirectory = normalizedRoot.resolve(platform.id).normalize()
        require(platformDirectory.parent == normalizedRoot) { "Desktop biometric platform path escapes resources" }
        require(Files.isDirectory(platformDirectory, LinkOption.NOFOLLOW_LINKS)) {
            "Desktop biometric platform resources are missing"
        }
        require(!Files.isSymbolicLink(platformDirectory)) { "Desktop biometric platform directory is a symlink" }
        return BridgeLocation(platformDirectory, packagedResources != null)
    }

    /**
     * Installed jpackage layouts are derived from the bundled runtime rather
     * than mutable JVM properties. This prevents a development bridge
     * override from taking precedence inside an installed application.
     */
    private fun packagedResourcesDirectory(): Path? {
        val javaHome = javaHomeDirectory.toAbsolutePath().normalize()
        return when (operatingSystem) {
            OperatingSystem.MACOS -> macOsPackagedResources(javaHome)
            OperatingSystem.WINDOWS -> windowsPackagedResources(javaHome)
            OperatingSystem.LINUX,
            OperatingSystem.UNKNOWN,
            -> null
        }
    }

    private fun macOsPackagedResources(javaHome: Path): Path? {
        val contents = generateSequence(javaHome) { current -> current.parent }
            .firstOrNull { current ->
                current.fileName?.toString() == "Contents" &&
                    current.parent?.fileName?.toString()?.endsWith(".app") == true
            }
        return contents?.takeIf { candidate ->
            javaHome == candidate.resolve("runtime/Contents/Home").normalize()
        }?.resolve("app/resources")
    }

    private fun windowsPackagedResources(javaHome: Path): Path? = javaHome
        .takeIf { path -> path.fileName.toString().equals("runtime", ignoreCase = true) }
        ?.parent
        ?.resolve("app/resources")

    private fun windowsPackagedLauncher(): Path? = javaHomeDirectory.toAbsolutePath().normalize()
        .takeIf { javaHome -> javaHome.fileName.toString().equals("runtime", ignoreCase = true) }
        ?.parent
        ?.resolve(WINDOWS_LAUNCHER)
        ?.normalize()
        ?.takeIf { path ->
            Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path)
        }

    private fun ensureSecureDataDirectory(): Path {
        dataDirectoryOverride?.let { override ->
            val normalized = override.toAbsolutePath().normalize()
            if (!Files.exists(normalized, LinkOption.NOFOLLOW_LINKS)) Files.createDirectory(normalized)
            require(Files.isDirectory(normalized, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(normalized)) {
                "Desktop biometric test data directory is not safe"
            }
            return normalized.toRealPath(LinkOption.NOFOLLOW_LINKS)
        }
        val userHome = System.getProperty("user.home").takeUnless { it.isNullOrBlank() }
            ?: throw DesktopBiometricBridgeException.NotAvailable
        val home = Paths.get(userHome).toAbsolutePath().normalize()
        require(Files.isDirectory(home, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(home)) {
            "Desktop user home is not a safe directory"
        }
        val root = home.resolve(".passvault")
        if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) Files.createDirectory(root)
        require(Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(root)) {
            "PassVault data directory is not a safe directory"
        }
        runCatching {
            Files.setPosixFilePermissions(
                root,
                setOf(
                    java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                    java.nio.file.attribute.PosixFilePermission.OWNER_WRITE,
                    java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE,
                ),
            )
        }
        return root.toRealPath(LinkOption.NOFOLLOW_LINKS)
    }

    private fun requireSecureRegularFile(path: Path) {
        require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path)) {
            "Desktop biometric library is missing or unsafe"
        }
    }

    private fun sha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS).use { input ->
            val buffer = ByteArray(HASH_BUFFER_BYTES)
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

    private fun verifyMacOsDeveloperIdBundle(library: Path): Boolean {
        val canVerify = operatingSystem == OperatingSystem.MACOS && Files.isRegularFile(Paths.get(CODESIGN))
        val appBundle = if (canVerify) findOwningAppBundle(library) else null
        return appBundle?.let { app ->
            val libraryDetails = codeSigningDetails(library)
            val appDetails = codeSigningDetails(app)
            libraryDetails != null && appDetails != null &&
                matchingDeveloperIdProtection(libraryDetails, appDetails) &&
                verifyCodeSignature(library) && verifyCodeSignature(app)
        } ?: false
    }

    private fun findOwningAppBundle(library: Path): Path? =
        generateSequence(library.parent) { current -> current.parent }
            .firstOrNull { current ->
                current.fileName?.toString()?.endsWith(".app") == true &&
                    Files.isDirectory(current, LinkOption.NOFOLLOW_LINKS) &&
                    !Files.isSymbolicLink(current)
            }

    private fun matchingDeveloperIdProtection(
        library: CodeSigningDetails,
        app: CodeSigningDetails,
    ): Boolean = library.teamIdentifier == app.teamIdentifier &&
        library.isDeveloperId && app.isDeveloperId &&
        library.hasHardenedRuntime && app.hasHardenedRuntime &&
        library.hasTimestamp && app.hasTimestamp

    private fun codeSigningDetails(path: Path): CodeSigningDetails? {
        val result = runCodeSign("--display", "--verbose=4", path.toString())
            ?.takeIf { value -> value.exitCode == 0 && value.output.length <= MAX_CODESIGN_OUTPUT_CHARS }
        return result?.let(::parseCodeSigningDetails)
    }

    private fun parseCodeSigningDetails(result: ProcessResult): CodeSigningDetails? {
        val pairs = result.output.lineSequence().mapNotNull(::parseCodeSignPair).toList()
        val team = pairs.firstOrNull { it.first == "TeamIdentifier" }
            ?.second
            ?.takeIf(TEAM_ID::matches)
        return team?.let { identifier ->
            val authority = pairs.firstOrNull { it.first == "Authority" }?.second.orEmpty()
            val codeDirectory = result.output.lineSequence()
                .firstOrNull { it.startsWith("CodeDirectory ") }
                .orEmpty()
            val timestamp = pairs.firstOrNull { it.first == "Timestamp" }?.second.orEmpty()
            CodeSigningDetails(
                teamIdentifier = identifier,
                isDeveloperId = authority.startsWith("Developer ID Application:") &&
                    authority.endsWith("($identifier)"),
                hasHardenedRuntime = codeDirectory.contains("(runtime)"),
                hasTimestamp = timestamp.isNotBlank() && timestamp != "none",
            )
        }
    }

    private fun parseCodeSignPair(line: String): Pair<String, String>? {
        val separator = line.indexOf('=')
        return if (separator <= 0 || separator == line.lastIndex) null
        else line.substring(0, separator) to line.substring(separator + 1)
    }

    private fun verifyCodeSignature(path: Path): Boolean =
        runCodeSign("--verify", "--strict", "--verbose=0", path.toString())?.exitCode == 0

    /**
     * Authenticode is the Windows trust root; the adjacent checksum remains an
     * accidental-corruption check only. Both files must be valid, timestamped,
     * and signed by the exact same certificate. The per-machine installer then
     * protects the already-authenticated launcher from same-user replacement.
     */
    private fun verifyWindowsAuthenticodeBundle(library: Path, launcher: Path): Boolean =
        operatingSystem == OperatingSystem.WINDOWS &&
            windowsPowerShellExecutable()?.let { powershell ->
                startWindowsSignatureCheck(powershell, library, launcher)?.let { process ->
                    if (!process.waitFor(AUTHENTICODE_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                        process.destroyForcibly()
                        process.waitFor()
                        false
                    } else {
                        val boundedOutput = process.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                            val buffer = CharArray(MAX_SIGNATURE_OUTPUT_CHARS + 1)
                            reader.read(buffer)
                        }
                        process.exitValue() == 0 && boundedOutput <= MAX_SIGNATURE_OUTPUT_CHARS
                    }
                } ?: false
            } == true

    private fun windowsPowerShellExecutable(): Path? = System.getenv(WINDOWS_SYSTEM_ROOT_ENV)
        ?.takeIf(String::isNotBlank)
        ?.let { systemRoot ->
            runCatching {
                Paths.get(systemRoot).resolve(WINDOWS_POWERSHELL_RELATIVE_PATH).toAbsolutePath().normalize()
            }.getOrNull()
        }
        ?.takeIf { path ->
            Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path)
        }

    private fun startWindowsSignatureCheck(powershell: Path, library: Path, launcher: Path): Process? = runCatching {
        ProcessBuilder(
            powershell.toString(),
            "-NoLogo",
            "-NoProfile",
            "-NonInteractive",
            "-Command",
            WINDOWS_AUTHENTICODE_SCRIPT,
        ).apply {
            redirectErrorStream(true)
            environment()[WINDOWS_BRIDGE_ENV] = library.toString()
            environment()[WINDOWS_LAUNCHER_ENV] = launcher.toString()
        }.start()
    }.getOrNull()

    private fun runCodeSign(vararg arguments: String): ProcessResult? {
        val process = runCatching {
            ProcessBuilder(listOf(CODESIGN) + arguments)
                .redirectErrorStream(true)
                .start()
        }.getOrNull()
        return process?.let { running ->
            if (!running.waitFor(CODESIGN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                running.destroyForcibly()
                running.waitFor()
                null
            } else {
                val output = running.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                    val buffer = CharArray(MAX_CODESIGN_OUTPUT_CHARS + 1)
                    val count = reader.read(buffer)
                    if (count < 0) "" else String(buffer, 0, count)
                }
                ProcessResult(running.exitValue(), output)
            }
        }
    }

    private companion object {
        const val COMPOSE_RESOURCES_PROPERTY = "compose.application.resources.dir"
        const val DEVELOPMENT_BRIDGE_PROPERTY = "passvault.biometric.bridge.dir"
        const val MANIFEST_FILE = "bridge.properties"
        const val INTEGRITY_SHA256 = "sha256"
        const val INTEGRITY_SHA256_OR_DEVELOPER_ID = "sha256-or-developer-id"
        const val INTEGRITY_SHA256_AND_AUTHENTICODE = "sha256-and-authenticode"
        const val MAX_LIBRARY_BYTES = 32L * 1024L * 1024L
        const val HASH_BUFFER_BYTES = 8192
        const val CODESIGN = "/usr/bin/codesign"
        const val CODESIGN_TIMEOUT_SECONDS = 10L
        const val MAX_CODESIGN_OUTPUT_CHARS = 16 * 1024
        const val WINDOWS_LAUNCHER = "PassVault.exe"
        const val WINDOWS_SYSTEM_ROOT_ENV = "SystemRoot"
        const val WINDOWS_BRIDGE_ENV = "PASSVAULT_AUTHENTICODE_BRIDGE"
        const val WINDOWS_LAUNCHER_ENV = "PASSVAULT_AUTHENTICODE_LAUNCHER"
        const val WINDOWS_POWERSHELL_RELATIVE_PATH = "System32/WindowsPowerShell/v1.0/powershell.exe"
        const val AUTHENTICODE_TIMEOUT_SECONDS = 15L
        const val MAX_SIGNATURE_OUTPUT_CHARS = 4096
        val WINDOWS_AUTHENTICODE_SCRIPT = """
            ${'$'}ErrorActionPreference = 'Stop'
            ${'$'}bridge = Get-AuthenticodeSignature -LiteralPath ${'$'}env:$WINDOWS_BRIDGE_ENV
            ${'$'}launcher = Get-AuthenticodeSignature -LiteralPath ${'$'}env:$WINDOWS_LAUNCHER_ENV
            if (${ '$' }bridge.Status -ne 'Valid' -or ${ '$' }launcher.Status -ne 'Valid' -or
                ${'$'}null -eq ${'$'}bridge.SignerCertificate -or ${'$'}null -eq ${'$'}launcher.SignerCertificate -or
                ${'$'}null -eq ${'$'}bridge.TimeStamperCertificate -or
                ${'$'}null -eq ${'$'}launcher.TimeStamperCertificate) { exit 2 }
            if (${ '$' }bridge.SignerCertificate.Thumbprint -cne
                ${'$'}launcher.SignerCertificate.Thumbprint) { exit 3 }
            exit 0
        """.trimIndent()
        val TEAM_ID = Regex("[A-Z0-9]{10}")
    }
}

private data class ProcessResult(val exitCode: Int, val output: String)

private data class BridgeLocation(
    val directory: Path,
    val isPackaged: Boolean,
)

private data class CodeSigningDetails(
    val teamIdentifier: String,
    val isDeveloperId: Boolean,
    val hasHardenedRuntime: Boolean,
    val hasTimestamp: Boolean,
)

private data class NativePlatform(
    val id: String,
    val libraryName: String,
    val biometricType: BiometricType,
    val integrityPolicy: String,
)

private fun nativePlatform(operatingSystem: OperatingSystem, architectureName: String): NativePlatform? {
    val architecture = architectureName.lowercase()
    return when (operatingSystem) {
        OperatingSystem.MACOS -> when (architecture) {
            "aarch64", "arm64" -> NativePlatform(
                id = "macos-arm64",
                libraryName = "libpassvault_biometric.dylib",
                biometricType = BiometricType.TOUCH_ID,
                integrityPolicy = "sha256-or-developer-id",
            )
            "amd64", "x86_64" -> NativePlatform(
                id = "macos-x64",
                libraryName = "libpassvault_biometric.dylib",
                biometricType = BiometricType.TOUCH_ID,
                integrityPolicy = "sha256-or-developer-id",
            )
            else -> null
        }
        OperatingSystem.WINDOWS -> when (architecture) {
            "amd64", "x86_64" -> NativePlatform(
                id = "windows-x64",
                libraryName = "passvault_biometric.dll",
                biometricType = BiometricType.WINDOWS_HELLO,
                integrityPolicy = "sha256-and-authenticode",
            )
            else -> null
        }
        OperatingSystem.LINUX,
        OperatingSystem.UNKNOWN,
        -> null
    }
}

private data class BridgeManifest(
    val abi: Int,
    val platform: String,
    val library: String,
    val integrity: String,
    val sha256: String,
) {
    companion object {
        fun read(path: Path): BridgeManifest {
            require(Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(path)) {
                "Desktop biometric manifest is missing or unsafe"
            }
            require(Files.size(path) in 1..MAX_MANIFEST_BYTES) { "Desktop biometric manifest is invalid" }
            val entries = linkedMapOf<String, String>()
            Files.readAllLines(path, Charsets.UTF_8).forEach { line ->
                require(line.isNotBlank() && !line.startsWith('#')) { "Desktop biometric manifest is malformed" }
                val separator = line.indexOf('=')
                require(separator in 1 until line.lastIndex) { "Desktop biometric manifest is malformed" }
                val key = line.substring(0, separator)
                val value = line.substring(separator + 1)
                require(key.matches(KEY_PATTERN) && value.matches(VALUE_PATTERN)) {
                    "Desktop biometric manifest contains invalid data"
                }
                require(entries.put(key, value) == null) { "Desktop biometric manifest has duplicate keys" }
            }
            require(entries.keys == REQUIRED_KEYS) { "Desktop biometric manifest has missing or extra keys" }
            val checksum = entries.getValue("sha256")
            require(checksum.matches(SHA256_PATTERN)) { "Desktop biometric checksum is malformed" }
            return BridgeManifest(
                abi = entries.getValue("abi").toIntOrNull()
                    ?: throw IllegalArgumentException("Desktop biometric ABI is malformed"),
                platform = entries.getValue("platform"),
                library = entries.getValue("library"),
                integrity = entries.getValue("integrity"),
                sha256 = checksum,
            )
        }

        private const val MAX_MANIFEST_BYTES = 4096L
        private val REQUIRED_KEYS = setOf("abi", "platform", "library", "integrity", "sha256")
        private val KEY_PATTERN = Regex("[a-z][a-z0-9_]{0,31}")
        private val VALUE_PATTERN = Regex("[A-Za-z0-9._-]{1,256}")
        private val SHA256_PATTERN = Regex("[0-9a-fA-F]{64}")
    }
}
