package com.passvault.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.domain.PassVaultBuildInfo
import com.passvault.feature.settings.presentation.SettingsViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = state.errorMessage?.resolve()
    val statusMessage = state.statusMessage?.resolve()
    LaunchedEffect(errorMessage, statusMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
            onEvent(SettingsViewModel.SettingsEvent.OnDismissError)
        } else if (statusMessage != null) {
            snackbarHostState.showSnackbar(statusMessage)
            onEvent(SettingsViewModel.SettingsEvent.OnDismissStatusMessage)
        }
    }
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            SettingsContent(
                state = state,
                onEvent = onEvent,
                showBackButton = showBackButton,
                modifier = Modifier.scaffoldVerticalScroll(rememberScrollState(), paddingValues),
            )
        }
    }
    SettingsInfoDialog(state = state, onEvent = onEvent)
}

@Composable
private fun SettingsContent(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
    showBackButton: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(max = 880.dp)
            .fillMaxWidth()
            .padding(
                start = ComponentSpacing.screenHorizontal,
                end = ComponentSpacing.screenHorizontal,
                top = if (showBackButton) ComponentSpacing.screenVertical else 0.dp,
                bottom = if (showBackButton) ComponentSpacing.screenVertical else 112.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        if (showBackButton) SettingsTopBar(onEvent)
        EditorialPageHeader(
            eyebrow = stringResource(Res.string.ui_passvault),
            title = stringResource(Res.string.action_settings),
            actions = { if (!showBackButton) LockButton(onEvent) },
        )
        SecuritySection(state, onEvent)
        AppearanceSection(state, onEvent)
        DataSection(onEvent)
        AboutSection(state, onEvent)
        Spacer(modifier = Modifier.height(Spacing.xl))
        Text(
            text = stringResource(Res.string.ui_passvault_version, PassVaultBuildInfo.VERSION),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(modifier = Modifier.height(Spacing.xl))
    }
}

@Composable
private fun SettingsTopBar(onEvent: (SettingsViewModel.SettingsEvent) -> Unit) {
    SettingsScrollableTopBar(
        onBack = { onEvent(SettingsViewModel.SettingsEvent.OnBackClick) },
        actions = { LockButton(onEvent) },
    )
}

@Composable
private fun LockButton(onEvent: (SettingsViewModel.SettingsEvent) -> Unit) {
    IconButton(onClick = { onEvent(SettingsViewModel.SettingsEvent.OnLockVaultClick) }) {
        Icon(Icons.Default.Lock, contentDescription = stringResource(Res.string.ui_lock_vault))
    }
}

@Composable
private fun SecuritySection(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
) {
    SettingsSection(title = stringResource(Res.string.ui_security)) {
        SettingsItem(
            icon = Icons.Default.Security,
            title = stringResource(Res.string.ui_security_settings),
            subtitle = stringResource(Res.string.ui_master_password_auto_lock_clipboard),
            onClick = { onEvent(SettingsViewModel.SettingsEvent.OnSecurityClick) },
        )
        SettingsItem(
            icon = Icons.Default.Timer,
            title = stringResource(Res.string.ui_auto_lock),
            subtitle = pluralStringResource(
                Res.plurals.ui_lock_after_minutes,
                state.autoLockTimeoutMinutes,
                state.autoLockTimeoutMinutes,
            ),
            onClick = { onEvent(SettingsViewModel.SettingsEvent.OnSecurityClick) },
        )
    }
}

@Composable
private fun AppearanceSection(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
) {
    val theme = when (state.theme) {
        SettingsViewModel.AppTheme.LIGHT -> stringResource(Res.string.ui_theme_light)
        SettingsViewModel.AppTheme.DARK -> stringResource(Res.string.ui_theme_dark)
        SettingsViewModel.AppTheme.SYSTEM -> stringResource(Res.string.ui_theme_system)
    }
    val language = when (state.language) {
        SettingsViewModel.AppLanguage.SYSTEM -> stringResource(Res.string.ui_language_system)
        SettingsViewModel.AppLanguage.ENGLISH -> stringResource(Res.string.ui_language_english)
        SettingsViewModel.AppLanguage.ARABIC -> stringResource(Res.string.ui_language_arabic)
    }
    SettingsSection(title = stringResource(Res.string.ui_appearance)) {
        SettingsItem(
            Icons.Default.Palette,
            stringResource(Res.string.ui_appearance),
            stringResource(Res.string.ui_theme_colors_display),
            { onEvent(SettingsViewModel.SettingsEvent.OnAppearanceClick) },
        )
        SettingsItem(
            Icons.Default.DarkMode,
            stringResource(Res.string.ui_theme),
            theme,
            { onEvent(SettingsViewModel.SettingsEvent.OnAppearanceClick) },
        )
        SettingsItem(
            Icons.Default.Language,
            stringResource(Res.string.ui_app_language),
            language,
            { onEvent(SettingsViewModel.SettingsEvent.OnAppearanceClick) },
        )
    }
}

@Composable
private fun DataSection(onEvent: (SettingsViewModel.SettingsEvent) -> Unit) {
    SettingsSection(title = stringResource(Res.string.ui_data)) {
        SettingsItem(
            Icons.Default.Storage,
            stringResource(Res.string.ui_data_management),
            stringResource(Res.string.ui_export_import_backup),
            { onEvent(SettingsViewModel.SettingsEvent.OnDataClick) },
        )
        SettingsItem(
            Icons.Default.Upload,
            stringResource(Res.string.ui_export_vault),
            stringResource(Res.string.ui_save_your_passwords_to_a_file),
            { onEvent(SettingsViewModel.SettingsEvent.OnExportClick) },
        )
        SettingsItem(
            Icons.Default.Download,
            stringResource(Res.string.ui_import_vault),
            stringResource(Res.string.ui_import_passwords_from_a_file),
            { onEvent(SettingsViewModel.SettingsEvent.OnImportClick) },
        )
        SettingsItem(
            Icons.Default.Cloud,
            stringResource(Res.string.ui_backup),
            stringResource(Res.string.ui_encrypted_local_backup_options),
            { onEvent(SettingsViewModel.SettingsEvent.OnBackupClick) },
        )
    }
}

@Composable
private fun AboutSection(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
) {
    SettingsSection(title = stringResource(Res.string.ui_about)) {
        SettingsItem(
            Icons.Default.Info,
            stringResource(Res.string.ui_vault_info),
            vaultInfoSubtitle(state),
            { onEvent(SettingsViewModel.SettingsEvent.OnVaultInfoClick) },
        )
        SettingsItem(
            Icons.AutoMirrored.Filled.Help,
            stringResource(Res.string.ui_help_and_security_guidance),
            stringResource(Res.string.ui_local_guidance_for_protecting_your_vault),
            { onEvent(SettingsViewModel.SettingsEvent.OnHelpClick) },
        )
        SettingsItem(
            Icons.Default.Policy,
            stringResource(Res.string.ui_privacy_policy),
            stringResource(Res.string.ui_how_we_handle_your_data),
            { onEvent(SettingsViewModel.SettingsEvent.OnPrivacyClick) },
        )
    }
}

@Composable
private fun vaultInfoSubtitle(state: SettingsViewModel.SettingsState): String {
    val createdAt = state.vaultCreatedAt.takeIf(String::isNotEmpty)
        ?: return stringResource(Res.string.password_strength_unknown)
    return if (state.vaultEntryCount > 0) {
        pluralStringResource(
            Res.plurals.ui_created_value_with_items,
            state.vaultEntryCount,
            createdAt,
            state.vaultEntryCount,
        )
    } else {
        stringResource(Res.string.ui_created_value, createdAt)
    }
}

@Composable
private fun SettingsInfoDialog(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
) {
    val title = state.infoDialogTitle ?: return
    val message = state.infoDialogMessage ?: return
    AlertDialog(
        onDismissRequest = { onEvent(SettingsViewModel.SettingsEvent.OnDismissInfo) },
        title = { Text(title.resolve()) },
        text = { Text(message.resolve()) },
        confirmButton = {
            TextButton(onClick = { onEvent(SettingsViewModel.SettingsEvent.OnDismissInfo) }) {
                Text(stringResource(Res.string.ui_ok))
            }
        },
    )
}
