package com.passvault.feature.settings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.settings.presentation.SettingsViewModel
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToBackup: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            DataSettingsContent(
                state = state,
                onExport = onNavigateToExport,
                onImport = onNavigateToImport,
                onBackup = onNavigateToBackup,
                onBack = onNavigateBack,
                modifier = Modifier.scaffoldVerticalScroll(rememberScrollState(), paddingValues),
            )
        }
    }
}

@Composable
private fun DataSettingsContent(
    state: SettingsViewModel.SettingsState,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onBackup: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(max = 760.dp)
            .fillMaxWidth()
            .imePadding()
            .padding(
                horizontal = ComponentSpacing.screenHorizontal,
                vertical = ComponentSpacing.screenVertical,
        ),
        verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
    ) {
        SettingsScrollableTopBar(onBack = onBack)
        EditorialPageHeader(
            eyebrow = stringResource(Res.string.action_settings),
            title = stringResource(Res.string.ui_data_management),
        )
        DataActionCard(
            icon = Icons.Default.Upload,
            title = stringResource(Res.string.ui_export_vault),
            description = stringResource(Res.string.ui_backup_your_passwords_to_a_file),
            actionLabel = stringResource(Res.string.action_export),
            actionIcon = Icons.Default.FileDownload,
            onClick = onExport,
        )
        DataActionCard(
            icon = Icons.Default.Download,
            title = stringResource(Res.string.ui_import_vault),
            description = stringResource(Res.string.ui_restore_from_a_backup_file),
            actionLabel = stringResource(Res.string.action_import),
            actionIcon = Icons.Default.FileUpload,
            onClick = onImport,
            outlined = true,
        )
        DataActionCard(
            icon = Icons.Default.Security,
            title = stringResource(Res.string.ui_backup_and_restore),
            description = stringResource(
                Res.string.ui_create_an_authenticated_encrypted_snapshot_or_validate,
            ),
            actionLabel = stringResource(Res.string.ui_open_backup_center),
            onClick = onBackup,
            outlined = true,
        )
        VaultInfoCard(state)
        state.errorMessage?.let { SettingsError(it.resolve()) }
    }
}

@Composable
private fun DataActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String,
    onClick: () -> Unit,
    actionIcon: ImageVector? = null,
    outlined: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(ComponentSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (outlined) {
                OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    actionIcon?.let { Icon(it, contentDescription = null) }
                    if (actionIcon != null) Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(actionLabel)
                }
            } else {
                Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                    actionIcon?.let { Icon(it, contentDescription = null) }
                    if (actionIcon != null) Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
private fun VaultInfoCard(state: SettingsViewModel.SettingsState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(ComponentSpacing.cardPadding)) {
            Text(
                text = stringResource(Res.string.ui_vault_information),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = Spacing.md),
            )
            InfoRow(stringResource(Res.string.ui_total_items), state.vaultEntryCount.toString())
            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))
            InfoRow(
                label = stringResource(Res.string.ui_created),
                value = state.vaultCreatedAt.takeIf(String::isNotEmpty)
                    ?: stringResource(Res.string.password_strength_unknown),
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SettingsError(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(ComponentSpacing.cardPadding),
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}
