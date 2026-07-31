package com.passvault.feature.backup.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.resolve
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.SecureTextField
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.components.EditorialStatusBanner
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.backup.presentation.BackupViewModel

@Composable
internal fun BackupExportContent(
    state: BackupViewModel.BackupState,
    onEvent: (BackupViewModel.BackupEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackupScrollableColumn(modifier) {
        EditorialPageHeader(
            eyebrow = stringResource(Res.string.ui_backup_and_restore),
            title = stringResource(Res.string.ui_create_an_encrypted_copy_of_this_vault),
            subtitle = stringResource(
                Res.string.ui_the_backup_passphrase_is_separate_from_your_vault_pass,
            ),
        )

        EditorialPanel(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.ui_vault_snapshot), style = MaterialTheme.typography.titleMedium)
                Text(
                    pluralStringResource(
                        Res.plurals.ui_credential_count,
                        state.credentialCount,
                        state.credentialCount,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    stringResource(Res.string.ui_attachment_files_are_not_packaged_encrypted_attachment),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }

        SecureTextField(
            value = state.exportPassword,
            onValueChange = { onEvent(BackupViewModel.BackupEvent.OnPasswordChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.ui_backup_passphrase),
            supportingText = stringResource(Res.string.ui_use_at_least_12_characters_and_avoid_reusing_your_vaul),
            enabled = !state.isExporting,
        )
        if (state.exportPassword.isNotEmpty()) {
            PasswordStrengthIndicator(state.passwordStrength)
        }

        Button(
            onClick = { onEvent(BackupViewModel.BackupEvent.OnExportClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.canExport,
        ) {
            if (state.isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
                Text(stringResource(Res.string.ui_creating_backup), modifier = Modifier.padding(start = 8.dp))
            } else {
                Icon(Icons.Default.Upload, contentDescription = null)
                Text(stringResource(Res.string.ui_save_encrypted_backup), modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (state.isExporting) {
            LinearProgressIndicator(
                progress = { state.exportProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                stringResource(Res.string.ui_percent, state.exportProgress),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End),
            )
        }

        FeedbackMessage(
            message = state.successMessage,
            success = true,
            onDismiss = { onEvent(BackupViewModel.BackupEvent.OnDismissSuccess) },
        )
        FeedbackMessage(
            message = state.errorMessage,
            success = false,
            onDismiss = { onEvent(BackupViewModel.BackupEvent.OnDismissError) },
        )
    }
}

@Composable
internal fun BackupImportContent(
    state: BackupViewModel.BackupState,
    onEvent: (BackupViewModel.BackupEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackupScrollableColumn(modifier) {
        EditorialPageHeader(
            eyebrow = stringResource(Res.string.ui_backup_and_restore),
            title = stringResource(Res.string.ui_restore_encrypted_backup),
        )

        EditorialStatusBanner(
            icon = Icons.Default.Warning,
            title = stringResource(Res.string.ui_restoring_replaces_this_vault),
            message = stringResource(
                Res.string.ui_passvault_validates_the_complete_encrypted_file_before,
            ) + stringResource(Res.string.ui_create_a_current_backup_first_if_you_need_to_keep_this),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )

        Button(
            onClick = { onEvent(BackupViewModel.BackupEvent.OnImportFilePickerClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isImporting && !state.isAnalyzingFile,
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
            Text(stringResource(Res.string.ui_choose_encrypted_backup), modifier = Modifier.padding(start = 8.dp))
        }

        if (state.selectedImportDisplayName.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(stringResource(Res.string.ui_selected_file), style = MaterialTheme.typography.titleSmall)
                    Text(
                        state.selectedImportDisplayName.takeIf(String::isNotBlank)
                            ?: stringResource(Res.string.ui_selected_backup),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        when (state.detectedImportFormat) {
                            BackupViewModel.ImportFormat.ENCRYPTED -> stringResource(Res.string.ui_passvault_encrypted_backup)
                            BackupViewModel.ImportFormat.UNKNOWN -> stringResource(Res.string.ui_unsupported_file_type)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (state.detectedImportFormat == BackupViewModel.ImportFormat.ENCRYPTED) {
            SecureTextField(
                value = state.importPassword,
                onValueChange = { onEvent(BackupViewModel.BackupEvent.OnImportPasswordChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.ui_backup_passphrase),
                supportingText = stringResource(Res.string.ui_the_passphrase_used_when_this_file_was_created),
                enabled = !state.isImporting,
            )
        }

        state.importPreview?.let { preview ->
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

        if (state.isAnalyzingFile) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(
                stringResource(Res.string.ui_validating_backup),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Button(
            onClick = { onEvent(BackupViewModel.BackupEvent.OnImportClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.canImport,
        ) {
            if (state.isImporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
                Text(stringResource(Res.string.ui_restoring), modifier = Modifier.padding(start = 8.dp))
            } else {
                Icon(Icons.Default.Download, contentDescription = null)
                Text(stringResource(Res.string.ui_review_and_restore), modifier = Modifier.padding(start = 8.dp))
            }
        }

        if (state.isImporting) {
            LinearProgressIndicator(
                progress = { state.importProgress / 100f },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        FeedbackMessage(
            message = state.importError,
            success = false,
            onDismiss = { onEvent(BackupViewModel.BackupEvent.OnDismissError) },
        )
        FeedbackMessage(
            message = state.successMessage,
            success = true,
            onDismiss = { onEvent(BackupViewModel.BackupEvent.OnDismissSuccess) },
        )
    }

    if (state.showRestoreConfirmation) {
        AlertDialog(
            onDismissRequest = { onEvent(BackupViewModel.BackupEvent.OnRestoreCancelClick) },
            icon = { Icon(Icons.Default.Lock, contentDescription = null) },
            title = { Text(stringResource(Res.string.ui_replace_the_current_vault)) },
            text = {
                Text(
                    stringResource(Res.string.ui_this_action_replaces_all_current_records_with_the_vali) +
                        stringResource(Res.string.ui_you_will_need_the_restored_vault_password_to_unlock_it),
                )
            },
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
}

@Composable
private fun BackupScrollableColumn(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = ComponentSpacing.formMaxWidth)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = ComponentSpacing.screenHorizontal,
                    vertical = ComponentSpacing.screenVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
            content = content,
        )
    }
}

@Composable
private fun PasswordStrengthIndicator(strength: BackupViewModel.PasswordStrength) {
    val (color, progress, label) = when (strength) {
        BackupViewModel.PasswordStrength.EMPTY -> Triple(MaterialTheme.colorScheme.outline, 0f, "")
        BackupViewModel.PasswordStrength.TOO_SHORT ->
            Triple(MaterialTheme.colorScheme.error, 0.2f, stringResource(Res.string.ui_too_short))
        BackupViewModel.PasswordStrength.WEAK ->
            Triple(MaterialTheme.colorScheme.error, 0.4f, stringResource(Res.string.password_strength_weak))
        BackupViewModel.PasswordStrength.GOOD ->
            Triple(MaterialTheme.colorScheme.tertiary, 0.7f, stringResource(Res.string.password_strength_good))
        BackupViewModel.PasswordStrength.STRONG ->
            Triple(MaterialTheme.colorScheme.primary, 1f, stringResource(Res.string.password_strength_strong))
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = color,
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun FeedbackMessage(
    message: UiText?,
    success: Boolean,
    onDismiss: () -> Unit,
) {
    if (message == null) return
    val container = if (success) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val icon = if (success) Icons.Default.CheckCircle else Icons.Default.Error
    Card(
        colors = CardDefaults.cardColors(containerColor = container),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null)
            Text(
                message.resolve(),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            )
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.ui_dismiss)) }
        }
    }
}
