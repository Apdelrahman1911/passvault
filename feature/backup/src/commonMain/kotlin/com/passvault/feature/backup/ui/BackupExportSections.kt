package com.passvault.feature.backup.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.components.SecureTextField
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.feature.backup.presentation.BackupViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BackupSnapshotPanel(credentialCount: Int) {
    EditorialPanel(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(Res.string.ui_vault_snapshot),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            pluralStringResource(
                Res.plurals.ui_credential_count,
                credentialCount,
                credentialCount,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            stringResource(
                Res.string.ui_attachment_files_are_packaged,
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun BackupExportPassphrase(
    state: BackupViewModel.BackupState,
    onEvent: (BackupViewModel.BackupEvent) -> Unit,
) {
    SecureTextField(
        value = state.exportPassword,
        onValueChange = { onEvent(BackupViewModel.BackupEvent.OnPasswordChanged(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(Res.string.ui_backup_passphrase),
        supportingText = stringResource(
            Res.string.ui_use_at_least_12_characters_and_avoid_reusing_your_vaul,
        ),
        enabled = !state.isExporting,
    )
    if (state.exportPassword.isNotEmpty()) {
        PasswordStrengthIndicator(state.passwordStrength)
    }
}

@Composable
internal fun BackupExportAction(
    state: BackupViewModel.BackupState,
    onEvent: (BackupViewModel.BackupEvent) -> Unit,
) {
    Button(
        onClick = { onEvent(BackupViewModel.BackupEvent.OnExportClick) },
        modifier = Modifier.fillMaxWidth(),
        enabled = state.canExport,
    ) {
        if (state.isExporting) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
            )
            Text(
                stringResource(Res.string.ui_creating_backup),
                modifier = Modifier.padding(start = 8.dp),
            )
        } else {
            Icon(Icons.Default.Upload, contentDescription = null)
            Text(
                stringResource(Res.string.ui_save_encrypted_backup),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Composable
internal fun BackupExportProgress(state: BackupViewModel.BackupState) {
    if (state.isExporting) {
        LinearProgressIndicator(
            progress = { state.exportProgress / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            stringResource(Res.string.ui_percent, state.exportProgress),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
        )
    }
}

@Composable
private fun PasswordStrengthIndicator(strength: BackupViewModel.PasswordStrength) {
    val presentation = backupStrengthPresentation(strength)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearProgressIndicator(
            progress = { presentation.progress },
            modifier = Modifier.fillMaxWidth(),
            color = presentation.color,
        )
        Text(
            text = presentation.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun backupStrengthPresentation(
    strength: BackupViewModel.PasswordStrength,
): BackupStrengthPresentation = when (strength) {
    BackupViewModel.PasswordStrength.EMPTY -> BackupStrengthPresentation(
        MaterialTheme.colorScheme.outline,
        0f,
        "",
    )
    BackupViewModel.PasswordStrength.TOO_SHORT -> BackupStrengthPresentation(
        MaterialTheme.colorScheme.error,
        0.2f,
        stringResource(Res.string.ui_too_short),
    )
    BackupViewModel.PasswordStrength.WEAK -> BackupStrengthPresentation(
        MaterialTheme.colorScheme.error,
        0.4f,
        stringResource(Res.string.password_strength_weak),
    )
    BackupViewModel.PasswordStrength.GOOD -> BackupStrengthPresentation(
        MaterialTheme.colorScheme.tertiary,
        0.7f,
        stringResource(Res.string.password_strength_good),
    )
    BackupViewModel.PasswordStrength.STRONG -> BackupStrengthPresentation(
        MaterialTheme.colorScheme.primary,
        1f,
        stringResource(Res.string.password_strength_strong),
    )
}

private data class BackupStrengthPresentation(
    val color: androidx.compose.ui.graphics.Color,
    val progress: Float,
    val label: String,
)
