package com.passvault.feature.backup.presentation

import com.passvault.feature.backup.BackupFile
import com.passvault.feature.backup.BackupFileStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal fun discardSelectedImportFile(
    scope: CoroutineScope,
    fileStore: BackupFileStore,
    state: BackupViewModel.BackupState,
) {
    val file = state.selectedImportFile?.let { path ->
        BackupFile(path, state.selectedImportDisplayName)
    } ?: return
    scope.launch {
        try {
            fileStore.discard(file)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // This is best-effort cleanup of a platform-owned temporary file.
        }
    }
}

internal suspend fun discardIgnoringFailure(
    fileStore: BackupFileStore,
    file: BackupFile,
) {
    try {
        fileStore.discard(file)
    } catch (_: Exception) {
        // Restore has committed or no longer has a retry path; cleanup failure
        // must not replace the operation's real outcome.
    }
}

internal fun shouldDiscardImportFileAfterRestore(
    file: BackupFile,
    state: BackupViewModel.BackupState,
    restoreCompleted: Boolean,
): Boolean = restoreCompleted || state.selectedImportFile != file.path
