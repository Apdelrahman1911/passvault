package com.passvault.feature.credential.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.domain.model.AttachmentAvailability
import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.feature.credential.presentation.CredentialViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun CredentialAttachmentSection(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.ui_encrypted_attachments),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(Res.string.ui_attachment_security_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            state.attachments.forEach { attachment ->
                AttachmentItem(
                    attachment = attachment,
                    enabled = !state.isBusy,
                    onEvent = onEvent,
                )
            }
            AttachmentAddControl(state, onEvent)
            AttachmentBusyIndicator(state.isAttachmentBusy)
        }
    }

    AttachmentRenameDialog(state, onEvent)
    AttachmentDeleteDialog(state, onEvent)
}

@Composable
private fun AttachmentItem(
    attachment: AttachmentMetadata,
    enabled: Boolean,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    val displayName = attachment.displayName()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null)
            Text(
                text = stringResource(
                    Res.string.ui_attachment_file_size,
                    displayName,
                    attachment.sizeBytes,
                ),
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val warning = when (attachment.availability) {
            AttachmentAvailability.AVAILABLE -> null
            AttachmentAvailability.LEGACY_METADATA_ONLY -> Res.string.ui_attachment_legacy_unavailable
            AttachmentAvailability.FILENAME_REQUIRES_RENAME -> Res.string.ui_attachment_filename_requires_rename
            AttachmentAvailability.CORRUPTED_FILENAME -> Res.string.ui_attachment_filename_corrupted
        }
        if (warning != null) {
            Text(
                text = stringResource(warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        AttachmentActions(attachment, enabled, onEvent)
    }
}

@Composable
private fun AttachmentAddControl(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    if (state.isNewCredential) {
        Text(
            text = stringResource(Res.string.ui_attachment_save_first),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Button(
            onClick = { onEvent(CredentialViewModel.CredentialEvent.OnAttachmentAddClick) },
            enabled = state.isCredentialLoaded && !state.isBusy,
        ) {
            Icon(Icons.Default.AttachFile, contentDescription = null)
            Text(stringResource(Res.string.ui_add_attachment))
        }
    }
}

@Composable
private fun AttachmentBusyIndicator(visible: Boolean) {
    if (!visible) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        Text(
            text = stringResource(Res.string.ui_attachment_working),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttachmentActions(
    attachment: AttachmentMetadata,
    enabled: Boolean,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (attachment.availability == AttachmentAvailability.AVAILABLE) {
            AttachmentAction(
                label = stringResource(Res.string.ui_open_attachment),
                icon = { Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null) },
                enabled = enabled,
                onClick = {
                    onEvent(CredentialViewModel.CredentialEvent.OnAttachmentOpenClick(attachment.id))
                },
            )
            AttachmentAction(
                label = stringResource(Res.string.ui_export_attachment),
                icon = { Icon(Icons.Default.Download, contentDescription = null) },
                enabled = enabled,
                onClick = {
                    onEvent(CredentialViewModel.CredentialEvent.OnAttachmentExportClick(attachment.id))
                },
            )
        }
        AttachmentAction(
            label = stringResource(Res.string.ui_rename_attachment),
            icon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
            enabled = enabled,
            onClick = {
                onEvent(CredentialViewModel.CredentialEvent.OnAttachmentRenameClick(attachment.id))
            },
        )
        AttachmentAction(
            label = stringResource(Res.string.action_delete),
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            enabled = enabled,
            onClick = {
                onEvent(CredentialViewModel.CredentialEvent.OnAttachmentDeleteClick(attachment.id))
            },
        )
    }
}

@Composable
private fun AttachmentAction(
    label: String,
    icon: @Composable () -> Unit,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, enabled = enabled) {
        icon()
        Text(label)
    }
}

@Composable
private fun AttachmentRenameDialog(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    if (state.attachmentRenameTarget == null) return
    AlertDialog(
        onDismissRequest = {
            onEvent(CredentialViewModel.CredentialEvent.OnAttachmentRenameCancel)
        },
        title = { Text(stringResource(Res.string.ui_attachment_rename_title)) },
        text = {
            OutlinedTextField(
                value = state.attachmentRenameInput,
                onValueChange = {
                    onEvent(CredentialViewModel.CredentialEvent.OnAttachmentRenameChanged(it))
                },
                label = { Text(stringResource(Res.string.ui_filename)) },
                singleLine = true,
                enabled = !state.isBusy,
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onEvent(CredentialViewModel.CredentialEvent.OnAttachmentRenameConfirm)
                },
                enabled = state.attachmentRenameInput.isNotBlank() && !state.isBusy,
            ) {
                Text(stringResource(Res.string.ui_rename_attachment))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onEvent(CredentialViewModel.CredentialEvent.OnAttachmentRenameCancel)
                },
                enabled = !state.isBusy,
            ) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}

@Composable
private fun AttachmentDeleteDialog(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    val attachment = state.attachmentDeleteTarget?.let(state::attachmentFor) ?: return
    AlertDialog(
        onDismissRequest = {
            onEvent(CredentialViewModel.CredentialEvent.OnAttachmentDeleteCancel)
        },
        title = {
            Text(stringResource(Res.string.ui_attachment_delete_title, attachment.displayName()))
        },
        text = { Text(stringResource(Res.string.ui_attachment_delete_message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    onEvent(CredentialViewModel.CredentialEvent.OnAttachmentDeleteConfirm)
                },
                enabled = !state.isBusy,
            ) {
                Text(
                    text = stringResource(Res.string.action_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onEvent(CredentialViewModel.CredentialEvent.OnAttachmentDeleteCancel)
                },
                enabled = !state.isBusy,
            ) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}

private fun CredentialViewModel.CredentialState.attachmentFor(id: AttachmentId): AttachmentMetadata? =
    attachments.firstOrNull { it.id == id }

@Composable
private fun AttachmentMetadata.displayName(): String =
    if (availability == AttachmentAvailability.CORRUPTED_FILENAME) {
        stringResource(Res.string.ui_attachment_corrupted_name, id.value)
    } else {
        fileName
    }
