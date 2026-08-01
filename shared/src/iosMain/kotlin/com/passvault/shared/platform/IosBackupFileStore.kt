package com.passvault.shared.platform

import com.passvault.feature.backup.BackupFile
import com.passvault.feature.backup.BackupFileStore

/**
 * Startup-safe boundary for the development host.
 *
 * A UIDocumentPicker adapter is still required before backup import/export is
 * supported by a production iOS application.
 */
class IosBackupFileStore : BackupFileStore {
    override suspend fun save(bytes: ByteArray, suggestedName: String): Result<BackupFile> =
        Result.failure(unsupported())

    override suspend fun open(): Result<BackupFile> = Result.failure(unsupported())

    override suspend fun read(file: BackupFile): Result<ByteArray> = Result.failure(unsupported())

    private fun unsupported(): UnsupportedOperationException = UnsupportedOperationException(
        "Backup file selection is not available in the iOS development host",
    )
}
