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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.SecureTextField
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.components.EditorialStatusBanner
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.backup.presentation.BackupViewModel

@Composable
internal fun BackupExportContent(
    state: BackupViewModel.BackupState,
    onEvent: (BackupViewModel.BackupEvent) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    BackupScrollableColumn(modifier, contentPadding) {
        EditorialPageHeader(
            eyebrow = stringResource(Res.string.ui_backup_and_restore),
            title = stringResource(Res.string.ui_create_an_encrypted_copy_of_this_vault),
            subtitle = stringResource(
                Res.string.ui_the_backup_passphrase_is_separate_from_your_vault_pass,
            ),
        )
        BackupSnapshotPanel(state.credentialCount)
        BackupExportPassphrase(state, onEvent)
        BackupExportAction(state, onEvent)
        BackupExportProgress(state)
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
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    BackupScrollableColumn(modifier, contentPadding) {
        EditorialPageHeader(
            eyebrow = stringResource(Res.string.ui_backup_and_restore),
            title = stringResource(Res.string.ui_restore_encrypted_backup),
        )

        EditorialStatusBanner(
            icon = Icons.Default.Warning,
            title = stringResource(Res.string.ui_restoring_replaces_this_vault),
            message = stringResource(Res.string.ui_restore_backup_replacement_warning),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
        BackupFilePickerAction(state, onEvent)
        SelectedBackupCard(state)
        BackupImportPassphrase(state, onEvent)
        RestorePreviewCard(state.importPreview)
        BackupAnalysisProgress(state.isAnalyzingFile)
        BackupRestoreAction(state, onEvent)
        BackupRestoreProgress(state)
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

    RestoreConfirmationDialog(state.showRestoreConfirmation, onEvent)
}

@Composable
private fun BackupScrollableColumn(
    modifier: Modifier,
    contentPadding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .widthIn(max = ComponentSpacing.formMaxWidth)
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .scaffoldVerticalScroll(rememberScrollState(), contentPadding)
                .imePadding()
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
    val content = if (success) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    val icon = if (success) Icons.Default.CheckCircle else Icons.Default.Error
    Card(
        colors = CardDefaults.cardColors(
            containerColor = container,
            contentColor = content,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
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
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = content),
            ) {
                Text(stringResource(Res.string.ui_dismiss))
            }
        }
    }
}
