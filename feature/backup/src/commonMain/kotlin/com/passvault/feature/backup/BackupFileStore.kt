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

    /**
     * Releases a platform-owned temporary import copy.
     *
     * Android and Desktop return user-owned paths, so their default behavior
     * is intentionally a no-op. iOS imports an app-sandbox copy and removes it
     * after the restore is complete or the selection is replaced.
     */
    suspend fun discard(file: BackupFile) = Unit
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
