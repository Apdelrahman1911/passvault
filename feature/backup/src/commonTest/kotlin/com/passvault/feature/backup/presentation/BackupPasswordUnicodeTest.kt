package com.passvault.feature.backup.presentation

import com.passvault.core.database.backup.BackupInsufficientStorageException
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.error_backup_insufficient_storage
import com.passvault.core.designsystem.generated.resources.error_backup_invalid
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.domain.model.BackupPasswordPolicy
import com.passvault.feature.backup.BackupFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackupPasswordUnicodeTest {
    @Test
    fun `supplementary characters count as one new backup password character`() {
        val state = BackupViewModel.BackupState(
            exportPassword = "🔐🔑🛡️🔒🔓🔏",
            passwordStrength = BackupViewModel.PasswordStrength.GOOD,
        )

        assertFalse(state.canExport)
    }

    @Test
    fun `legacy import accepts a password within the code point maximum`() {
        val state = BackupViewModel.BackupState(
            selectedImportFile = "backup.pvault",
            detectedImportFormat = BackupViewModel.ImportFormat.ENCRYPTED,
            importPassword = "🔐".repeat(600),
            importPreview = BackupViewModel.ImportPreview(
                credentialCount = 0,
                folderCount = 0,
                tagCount = 0,
                attachmentCount = 0,
                warnings = emptyList(),
            ),
        )

        assertTrue(state.canImport)
    }

    @Test
    fun `overlong backup passwords cannot export or import`() {
        val password = "🔐".repeat(BackupPasswordPolicy.MAX_LENGTH + 1)
        val preview = BackupViewModel.ImportPreview(0, 0, 0, 0, emptyList())

        assertFalse(
            BackupViewModel.BackupState(
                exportPassword = password,
                passwordStrength = BackupViewModel.PasswordStrength.STRONG,
            ).canExport,
        )
        assertFalse(
            BackupViewModel.BackupState(
                selectedImportFile = "backup.pvault",
                detectedImportFormat = BackupViewModel.ImportFormat.ENCRYPTED,
                importPassword = password,
                importPreview = preview,
            ).canImport,
        )
    }

    @Test
    fun `every active operation blocks export and restore actions`() {
        val preview = BackupViewModel.ImportPreview(0, 0, 0, 0, emptyList())
        val ready = BackupViewModel.BackupState(
            exportPassword = "StrongBackupPassword123!",
            passwordStrength = BackupViewModel.PasswordStrength.STRONG,
            selectedImportFile = "backup.pvault",
            detectedImportFormat = BackupViewModel.ImportFormat.ENCRYPTED,
            importPassword = "StrongBackupPassword123!",
            importPreview = preview,
        )
        val activeStates = listOf(
            ready.copy(isExporting = true),
            ready.copy(isImporting = true),
            ready.copy(isAnalyzingFile = true),
            ready.copy(isSelectingImportFile = true),
        )

        assertTrue(ready.canExport)
        assertTrue(ready.canImport)
        activeStates.forEach { state ->
            assertFalse(state.canExport)
            assertFalse(state.canImport)
        }
    }

    @Test
    fun `temporary import ownership follows whether a failed restore still has a retry path`() {
        val file = BackupFile("/temporary/backup.pvault", "backup.pvault")
        val retryableState = BackupViewModel.BackupState(selectedImportFile = file.path)
        val postLockScrubbedState = BackupViewModel.BackupState(selectedImportFile = null)

        assertFalse(
            shouldDiscardImportFileAfterRestore(
                file = file,
                state = retryableState,
                restoreCompleted = false,
            ),
        )
        assertTrue(
            shouldDiscardImportFileAfterRestore(
                file = file,
                state = postLockScrubbedState,
                restoreCompleted = false,
            ),
        )
        assertTrue(
            shouldDiscardImportFileAfterRestore(
                file = file,
                state = retryableState,
                restoreCompleted = true,
            ),
        )
    }

    @Test
    fun `restore capacity failures have an actionable localized message`() {
        val capacityError = backupRestoreError(BackupInsufficientStorageException(20, 10))
        val otherError = backupRestoreError(IllegalStateException("restore failed"))

        assertEquals(
            Res.string.error_backup_insufficient_storage,
            (capacityError as UiText.Resource).resource,
        )
        assertEquals(Res.string.error_backup_invalid, (otherError as UiText.Resource).resource)
    }
}
