package com.passvault.feature.backup

/**
 * Platform file access for encrypted backups.
 *
 * The common feature never opens arbitrary paths itself. Android uses the
 * Storage Access Framework and Desktop uses a native file dialog.
 */
interface BackupFileStore {
    suspend fun save(bytes: ByteArray, suggestedName: String): Result<BackupFile>

    suspend fun open(): Result<BackupFile>

    suspend fun read(file: BackupFile): Result<ByteArray>
}

/**
 * Normal user cancellation is not an error state. Platform adapters return
 * this typed failure so the ViewModel does not have to inspect localized
 * exception messages.
 */
class BackupFileSelectionCancelled : Exception()

data class BackupFile(
    val path: String,
    val displayName: String,
)
