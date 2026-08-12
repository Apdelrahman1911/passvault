package com.passvault.feature.backup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.SecureTextField
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import com.passvault.feature.backup.presentation.BackupViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BackupFilePickerAction(
    state: BackupViewModel.BackupState,
    onEvent: (BackupViewModel.BackupEvent) -> Unit,
) {
    Button(
        onClick = { onEvent(BackupViewModel.BackupEvent.OnImportFilePickerClick) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.hasActiveOperation,
    ) {
        Icon(Icons.Default.FolderOpen, contentDescription = null)
        Text(
            stringResource(Res.string.ui_choose_encrypted_backup),
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
internal fun SelectedBackupCard(state: BackupViewModel.BackupState) {
    if (state.selectedImportDisplayName.isBlank()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                stringResource(Res.string.ui_selected_file),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                state.selectedImportDisplayName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                when (state.detectedImportFormat) {
                    BackupViewModel.ImportFormat.ENCRYPTED ->
                        stringResource(Res.string.ui_passvault_encrypted_backup)
                    BackupViewModel.ImportFormat.UNKNOWN ->
                        stringResource(Res.string.ui_unsupported_file_type)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun BackupImportPassphrase(
    state: BackupViewModel.BackupState,
    onEvent: (BackupViewModel.BackupEvent) -> Unit,
) {
    if (state.detectedImportFormat == BackupViewModel.ImportFormat.ENCRYPTED) {
        SecureTextField(
            value = state.importPassword,
            onValueChange = {
                onEvent(BackupViewModel.BackupEvent.OnImportPasswordChanged(it))
            },
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.ui_backup_passphrase),
            supportingText = stringResource(
                Res.string.ui_the_passphrase_used_when_this_file_was_created,
            ),
            enabled = !state.isImporting,
        )
    }
}

@Composable
internal fun RestorePreviewCard(preview: BackupViewModel.ImportPreview?) {
    if (preview == null) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null)
                Text(
                    stringResource(Res.string.ui_validated_restore_preview),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            RestorePreviewCounts(preview)
            if (preview.attachmentCount > 0) {
                Text(
                    pluralStringResource(
                        Res.plurals.ui_attachment_record_count,
                        preview.attachmentCount,
                        preview.attachmentCount,
                    ),
                )
            }
            preview.warnings.forEach { warning ->
                Text(
                    stringResource(Res.string.ui_bullet_value, warning.resolve()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RestorePreviewCounts(preview: BackupViewModel.ImportPreview) {
    Text(
        stringResource(
            Res.string.ui_backup_preview_counts,
            pluralStringResource(
                Res.plurals.ui_credential_count,
                preview.credentialCount,
                preview.credentialCount,
            ),
            pluralStringResource(
                Res.plurals.ui_folder_count,
                preview.folderCount,
                preview.folderCount,
            ),
            pluralStringResource(
                Res.plurals.ui_tag_count,
                preview.tagCount,
                preview.tagCount,
            ),
        ),
    )
}

@Composable
internal fun BackupAnalysisProgress(isAnalyzing: Boolean) {
    if (isAnalyzing) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text(
            stringResource(Res.string.ui_validating_backup),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun BackupRestoreAction(
    state: BackupViewModel.BackupState,
    onEvent: (BackupViewModel.BackupEvent) -> Unit,
) {
    Button(
        onClick = { onEvent(BackupViewModel.BackupEvent.OnImportClick) },
        modifier = Modifier.fillMaxWidth(),
        enabled = state.canImport,
    ) {
        if (state.isImporting) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
            )
            Text(
                stringResource(Res.string.ui_restoring),
                modifier = Modifier.padding(start = 8.dp),
            )
        } else {
            Icon(Icons.Default.Download, contentDescription = null)
            Text(
                stringResource(Res.string.ui_review_and_restore),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
internal fun BackupRestoreProgress(state: BackupViewModel.BackupState) {
    if (state.isImporting) {
        LinearProgressIndicator(
            progress = { state.importProgress / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
internal fun RestoreConfirmationDialog(
    visible: Boolean,
    onEvent: (BackupViewModel.BackupEvent) -> Unit,
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = { onEvent(BackupViewModel.BackupEvent.OnRestoreCancelClick) },
        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
        title = { Text(stringResource(Res.string.ui_replace_the_current_vault)) },
        text = { Text(stringResource(Res.string.ui_restore_backup_confirmation_message)) },
        confirmButton = {
            Button(onClick = { onEvent(BackupViewModel.BackupEvent.OnRestoreConfirmClick) }) {
                Text(stringResource(Res.string.ui_restore_and_lock))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(BackupViewModel.BackupEvent.OnRestoreCancelClick) }) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}
