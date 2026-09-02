package com.passvault.feature.backup.presentation

import com.passvault.core.database.backup.BackupContentSink
import com.passvault.core.database.backup.BackupContentSource
import com.passvault.feature.backup.BackupFile
import com.passvault.feature.backup.BackupFileStore
import com.passvault.feature.backup.BackupOutput
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackupOutputOwnershipTest {
    @Test
    fun `cancellation before service ownership aborts the created sink`() = runTest {
        val sink = RecordingSink()
        val fileStore = RecordingFileStore(sink)
        val serviceBoundary = CompletableDeferred<Unit>()
        val reachedBoundary = CompletableDeferred<Unit>()
        val export = launch {
            withOwnedBackupOutput(fileStore, "backup.pvault") {
                reachedBoundary.complete(Unit)
                serviceBoundary.await()
            }
        }
        reachedBoundary.await()

        export.cancel()
        export.join()

        assertTrue(export.isCancelled)
        assertEquals(1, sink.abortCalls)
        assertTrue(sink.abortContextWasActive)
    }

    @Test
    fun `successful commit still releases caller ownership with abort`() = runTest {
        val sink = RecordingSink()
        val fileStore = RecordingFileStore(sink)

        val file = withOwnedBackupOutput(fileStore, "backup.pvault") { output ->
            output.sink.commit()
            output.file
        }

        assertEquals("backup.pvault", file.path)
        assertEquals("backup.pvault", fileStore.suggestedName)
        assertEquals(1, sink.commitCalls)
        assertEquals(1, sink.abortCalls)
        assertTrue(sink.committed)
    }

    private class RecordingFileStore(
        private val sink: BackupContentSink,
    ) : BackupFileStore {
        var suggestedName: String? = null

        override suspend fun create(suggestedName: String): Result<BackupOutput> {
            this.suggestedName = suggestedName
            return Result.success(
                BackupOutput(
                    file = BackupFile("backup.pvault", "backup.pvault"),
                    sink = sink,
                ),
            )
        }

        override suspend fun open(): Result<BackupFile> = error("Not used")

        override suspend fun source(file: BackupFile): Result<BackupContentSource> = error("Not used")
    }

    private class RecordingSink : BackupContentSink {
        var commitCalls = 0
        var abortCalls = 0
        var committed = false
        var abortContextWasActive = false

        override suspend fun write(buffer: ByteArray, byteCount: Int) = Unit

        override suspend fun commit() {
            commitCalls++
            committed = true
        }

        override suspend fun abort() {
            abortCalls++
            abortContextWasActive = currentCoroutineContext().isActive
        }
    }
}
