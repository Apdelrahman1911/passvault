package com.passvault.core.database

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class VaultDatabaseRecoveryStorageTest {
    @Test
    fun `failed preservation rolls every moved source back into place`() {
        withTempDirectory { directory ->
            val database = directory.resolve("vault.db")
            val wal = directory.resolve("vault.db-wal")
            val attachments = directory.resolve("attachments")
            Files.write(database, byteArrayOf(1, 2, 3))
            Files.write(wal, byteArrayOf(4, 5, 6))
            Files.createDirectories(attachments)
            Files.write(attachments.resolve("object.bin"), byteArrayOf(7, 8, 9))
            val protectedPaths = mutableListOf<String>()
            val storage = storage(directory) { path ->
                protectedPaths += path
                if (path.endsWith("vault.db-wal")) error("injected protection failure")
            }

            assertFailsWith<IllegalStateException> { storage.preserveForRecovery() }

            assertContentEquals(byteArrayOf(1, 2, 3), Files.readAllBytes(database))
            assertContentEquals(byteArrayOf(4, 5, 6), Files.readAllBytes(wal))
            assertContentEquals(byteArrayOf(7, 8, 9), Files.readAllBytes(attachments.resolve("object.bin")))
            assertTrue(protectedPaths.any { it.endsWith("attachments") })
            assertTrue(protectedPaths.any { it.endsWith("vault.db-wal") })
            assertFalse(recoveryFiles(directory).any())
        }
    }

    @Test
    fun `startup restores an interrupted preservation before inspecting SQLite`() {
        withTempDirectory { directory ->
            val database = directory.resolve("vault.db")
            val wal = directory.resolve("vault.db-wal")
            val attachments = directory.resolve("attachments")
            Files.write(database, byteArrayOf(1, 2, 3))
            Files.write(wal, byteArrayOf(4, 5, 6))
            Files.createDirectories(attachments)
            Files.write(attachments.resolve("object.bin"), byteArrayOf(7, 8, 9))
            val interrupted = storage(directory) { path ->
                if (path.endsWith("vault.db-wal")) throw SimulatedProcessDeath()
            }

            assertFailsWith<SimulatedProcessDeath> { interrupted.preserveForRecovery() }
            assertTrue(Files.isRegularFile(database), "the main file is the recovery commit marker")
            assertFalse(Files.exists(wal))
            assertFalse(Files.exists(attachments))

            storage(directory).prepareForOpen()

            assertContentEquals(byteArrayOf(1, 2, 3), Files.readAllBytes(database))
            assertContentEquals(byteArrayOf(4, 5, 6), Files.readAllBytes(wal))
            assertContentEquals(byteArrayOf(7, 8, 9), Files.readAllBytes(attachments.resolve("object.bin")))
            assertFalse(recoveryFiles(directory).any())
        }
    }

    @Test
    fun `missing main file with an orphaned sidecar cannot become a fresh database`() {
        withTempDirectory { directory ->
            Files.write(directory.resolve("vault.db-wal"), byteArrayOf(1, 2, 3))

            assertFailsWith<IllegalStateException> { storage(directory).prepareForOpen() }

            assertFalse(Files.exists(directory.resolve("vault.db")))
            assertContentEquals(byteArrayOf(1, 2, 3), Files.readAllBytes(directory.resolve("vault.db-wal")))
        }
    }

    @Test
    fun `diagnostics are bounded fixed codes with no retained arbitrary text`() {
        withTempDirectory { directory ->
            val diagnostic = directory.resolve("database-health.events")
            Files.writeString(
                diagnostic,
                "private/path/vault.db user@example.test raw exception\n" +
                    "1700000000000 QUICK_CHECK_FAILED trailing-secret\n",
            )
            val storage = storage(directory, clock = FixedClock(Instant.fromEpochMilliseconds(1_700_000_000_000)))

            repeat(40) { index ->
                val code = VaultDatabaseDiagnosticCode.entries[index % VaultDatabaseDiagnosticCode.entries.size]
                storage.record(code)
            }

            val text = Files.readString(diagnostic)
            val lines = text.lineSequence().filter(String::isNotBlank).toList()
            assertEquals(16, lines.size)
            assertTrue(lines.all(DIAGNOSTIC_PATTERN::matches))
            assertFalse("private/path" in text)
            assertFalse("user@example.test" in text)
            assertFalse("exception" in text)
            assertFalse("secret" in text)
        }
    }

    @Test
    fun `oversized diagnostic input is discarded before a bounded rewrite`() {
        withTempDirectory { directory ->
            val diagnostic = directory.resolve("database-health.events")
            Files.write(diagnostic, ByteArray(1_000_000) { 'x'.code.toByte() })
            val storage = storage(directory, clock = FixedClock(Instant.fromEpochMilliseconds(42)))

            storage.record(VaultDatabaseDiagnosticCode.DATABASE_OPEN_FAILED)

            assertEquals("42 DATABASE_OPEN_FAILED\n", Files.readString(diagnostic))
        }
    }

    private fun storage(
        directory: Path,
        clock: Clock = Clock.System,
        protectPath: (String) -> Unit = {},
    ): LocalVaultDatabaseStorage = LocalVaultDatabaseStorage(
        databasePath = directory.resolve("vault.db").toString(),
        attachmentRootPath = directory.resolve("attachments").toString(),
        databaseRecoveryRootPath = directory.resolve("recovery").toString(),
        attachmentRecoveryRootPath = directory.resolve("recovery").toString(),
        diagnosticPath = directory.resolve("database-health.events").toString(),
        clock = clock,
        protectPath = protectPath,
    )

    private fun recoveryFiles(directory: Path): Sequence<Path> {
        val recovery = directory.resolve("recovery")
        if (!Files.exists(recovery)) return emptySequence()
        return Files.walk(recovery).use { paths ->
            paths.filter(Files::isRegularFile).toList().asSequence()
        }
    }

    private inline fun withTempDirectory(block: (Path) -> Unit) {
        val directory = Files.createTempDirectory("passvault-database-recovery-")
        try {
            block(directory)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    private class FixedClock(private val instant: Instant) : Clock {
        override fun now(): Instant = instant
    }

    private class SimulatedProcessDeath : Error()

    private companion object {
        val DIAGNOSTIC_PATTERN = Regex(
            "[0-9]{1,20} (QUICK_CHECK_FAILED|INVALID_DATABASE_FILE|DATABASE_OPEN_FAILED|" +
                "RECOVERY_COPY_PRESERVED|RECOVERY_PRESERVATION_FAILED)",
        )
    }
}
