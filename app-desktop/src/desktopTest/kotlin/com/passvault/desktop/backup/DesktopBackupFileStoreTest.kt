package com.passvault.desktop.backup

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopBackupFileStoreTest {
    @Test
    fun `safe file names preserve normal names`() {
        assertEquals(
            "PassVault-2026-08-10.pvault",
            safeDesktopBackupFileName("PassVault-2026-08-10.pvault", "backup"),
        )
    }

    @Test
    fun `unsafe file names use a neutral fallback`() {
        val unsafeNames = listOf(
            "../spoof.pvault",
            "folder\\spoof.pvault",
            "report\u202Eexe.pvault",
            "line\nbreak.pvault",
            ".",
            " ",
        )

        unsafeNames.forEach { name ->
            assertEquals("backup", safeDesktopBackupFileName(name, "backup"))
        }
    }

    @Test
    fun `overlong file names use a neutral fallback`() {
        assertEquals(
            "backup",
            safeDesktopBackupFileName("a".repeat(161), "backup"),
        )
    }

    @Test
    fun `cancellation during output handoff removes the unclaimed temporary`() = runTest {
        val directory = Files.createTempDirectory("passvault-backup-handoff-")
        try {
            val job = launch {
                createDesktopBackupOutput(
                    selected = directory.resolve("backup.pvault").toString(),
                    ioDispatcher = CancelAfterDispatchDispatcher(requireNotNull(currentCoroutineContext()[Job])),
                )
            }

            job.join()

            assertTrue(job.isCancelled)
            assertEquals(emptyList(), directory.toFile().list().orEmpty().toList())
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `abort removes a temporary even when its caller is cancelled`() = runTest {
        val directory = Files.createTempDirectory("passvault-backup-abort-")
        try {
            val temporary = Files.createTempFile(directory, ".passvault-", ".tmp")
            val sink = DesktopBackupSink(temporary, directory.resolve("backup.pvault"))
            val payload = "encrypted backup".encodeToByteArray()
            sink.write(payload, payload.size)

            val job = launch(start = CoroutineStart.UNDISPATCHED) {
                currentCoroutineContext()[Job]?.cancel()
                sink.abort()
            }
            job.join()

            assertTrue(job.isCancelled)
            assertFalse(Files.exists(temporary))
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `commit moves the temporary and later abort preserves the destination`() = runTest {
        val directory = Files.createTempDirectory("passvault-backup-commit-")
        try {
            val temporary = Files.createTempFile(directory, ".passvault-", ".tmp")
            val target = directory.resolve("backup.pvault")
            val sink = DesktopBackupSink(temporary, target)
            val payload = "encrypted backup".encodeToByteArray()

            sink.write(payload, payload.size)
            sink.commit()
            sink.abort()

            assertFalse(Files.exists(temporary))
            assertTrue(Files.isRegularFile(target))
            assertContentEquals(payload, Files.readAllBytes(target))
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    private class CancelAfterDispatchDispatcher(
        private val callerJob: Job,
    ) : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            block.run()
            callerJob.cancel()
        }
    }
}
