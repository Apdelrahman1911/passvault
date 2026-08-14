package com.passvault.desktop.security.biometric

import com.passvault.desktop.OperatingSystem
import com.passvault.desktop.getOperatingSystem
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopBiometricNativeLoaderTest {
    @Test
    fun `reviewed bridge loads with the expected ABI and platform capability`() {
        val stagingDirectory = System.getProperty(STAGING_DIRECTORY_PROPERTY).orEmpty()
        if (stagingDirectory.isBlank() || getOperatingSystem() == OperatingSystem.LINUX) return
        assertTrue(Paths.get(stagingDirectory).isAbsolute)
        val dataDirectory = createTempDirectory("passvault-biometric-loader-test")

        val bridge = DesktopBiometricNativeLoader(
            operatingSystem = getOperatingSystem(),
            developmentBridgeDirectory = stagingDirectory,
            dataDirectoryOverride = dataDirectory,
        ).load()
        try {
            val capability = bridge.getCapability()
            assertEquals(bridge.type, capability.type)
        } finally {
            bridge.close()
            dataDirectory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `tampered bridge bytes fail before native loading`() {
        val stagingDirectory = reviewedStagingDirectory() ?: return
        val temporaryRoot = createTempDirectory("passvault-biometric-tamper-test")
        val dataDirectory = temporaryRoot.resolve("data")
        Files.createDirectory(dataDirectory)
        try {
            copyDirectory(stagingDirectory, temporaryRoot.resolve("staged"))
            val library = Files.walk(temporaryRoot.resolve("staged")).use { paths ->
                paths.filter { path ->
                    Files.isRegularFile(path) &&
                        (path.fileName.toString().endsWith(".dylib") || path.fileName.toString().endsWith(".dll"))
                }.findFirst().orElseThrow()
            }
            val original = Files.readAllBytes(library)
            assertTrue(original.isNotEmpty())
            original[0] = (original[0].toInt() xor 1).toByte()
            Files.write(library, original)
            original.fill(0)

            assertFailsWith<IllegalArgumentException> {
                DesktopBiometricNativeLoader(
                    operatingSystem = getOperatingSystem(),
                    developmentBridgeDirectory = temporaryRoot.resolve("staged").toString(),
                    dataDirectoryOverride = dataDirectory,
                ).load()
            }
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `malformed bridge manifest fails before native loading`() {
        val stagingDirectory = reviewedStagingDirectory() ?: return
        val temporaryRoot = createTempDirectory("passvault-biometric-manifest-test")
        val dataDirectory = temporaryRoot.resolve("data")
        val staged = temporaryRoot.resolve("staged")
        Files.createDirectory(dataDirectory)
        try {
            copyDirectory(stagingDirectory, staged)
            val manifest = Files.walk(staged).use { paths ->
                paths.filter { path -> path.fileName.toString() == "bridge.properties" }
                    .findFirst()
                    .orElseThrow()
            }
            Files.writeString(manifest, Files.readString(manifest) + "abi=1\n")

            assertFailsWith<IllegalArgumentException> {
                DesktopBiometricNativeLoader(
                    operatingSystem = getOperatingSystem(),
                    developmentBridgeDirectory = staged.toString(),
                    dataDirectoryOverride = dataDirectory,
                ).load()
            }
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `unsupported Linux remains explicitly unavailable without loading a bridge`() {
        val runtime = DesktopBiometricRuntime.create(OperatingSystem.LINUX) {
            error("Linux must not attempt to load a biometric bridge")
        }

        assertFalse(kotlinx.coroutines.runBlocking { runtime.keyStore.contains("vault") })
    }

    @Test
    fun `packaged resource layout resolves native bridge without a development override`() {
        val stagingDirectory = reviewedStagingDirectory() ?: return
        val temporaryRoot = createTempDirectory("passvault-biometric-resource-layout")
        val resources = temporaryRoot.resolve("resources")
        val dataDirectory = temporaryRoot.resolve("data")
        Files.createDirectories(resources.resolve("native"))
        Files.createDirectory(dataDirectory)
        try {
            copyDirectory(stagingDirectory, resources.resolve("native"))
            val bridge = DesktopBiometricNativeLoader(
                operatingSystem = getOperatingSystem(),
                appResourcesDirectory = resources.toString(),
                developmentBridgeDirectory = null,
                dataDirectoryOverride = dataDirectory,
            ).load()
            bridge.close()
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    @Test
    fun `installed runtime cannot be redirected to a development bridge`() {
        val stagingDirectory = reviewedStagingDirectory() ?: return
        val temporaryRoot = createTempDirectory("passvault-biometric-packaged-layout")
        val dataDirectory = temporaryRoot.resolve("data")
        val javaHome = when (getOperatingSystem()) {
            OperatingSystem.MACOS -> temporaryRoot.resolve("PassVault.app/Contents/runtime/Contents/Home")
            OperatingSystem.WINDOWS -> temporaryRoot.resolve("PassVault/runtime")
            OperatingSystem.LINUX,
            OperatingSystem.UNKNOWN,
            -> return
        }
        Files.createDirectories(javaHome)
        Files.createDirectory(dataDirectory)
        try {
            assertFailsWith<IllegalArgumentException> {
                DesktopBiometricNativeLoader(
                    operatingSystem = getOperatingSystem(),
                    developmentBridgeDirectory = stagingDirectory.toString(),
                    dataDirectoryOverride = dataDirectory,
                    javaHomeDirectory = javaHome,
                ).load()
            }
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }

    private fun reviewedStagingDirectory(): Path? {
        val stagingDirectory = System.getProperty(STAGING_DIRECTORY_PROPERTY).orEmpty()
        if (stagingDirectory.isBlank() || getOperatingSystem() == OperatingSystem.LINUX) return null
        return Paths.get(stagingDirectory)
    }

    private fun copyDirectory(source: Path, destination: Path) {
        Files.walk(source).use { paths ->
            paths.forEach { path ->
                val target = destination.resolve(source.relativize(path).toString())
                if (Files.isDirectory(path)) Files.createDirectories(target)
                else Files.copy(path, target)
            }
        }
    }

    private companion object {
        const val STAGING_DIRECTORY_PROPERTY = "passvault.biometric.testBridgeDirectory"
    }
}
