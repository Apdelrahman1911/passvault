package com.passvault.feature.backup.presentation

import com.passvault.feature.backup.BackupFile
import com.passvault.feature.backup.BackupFileStore
import com.passvault.feature.backup.BackupOutput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal suspend fun <T> withOwnedBackupOutput(
    fileStore: BackupFileStore,
    suggestedName: String,
    action: suspend (BackupOutput) -> T,
): T {
    var ownedOutput: BackupOutput? = null
    return try {
        val output = fileStore.create(suggestedName).getOrThrow()
        ownedOutput = output
        action(output)
    } finally {
        ownedOutput?.sink?.let { sink ->
            withContext(NonCancellable) { runCatching { sink.abort() } }
        }
    }
}

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
