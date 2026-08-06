package com.passvault.feature.settings.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.resolve
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.SecureTextField
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors
import com.passvault.core.designsystem.platform.passVaultScrollableTopAppBarInsets
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.theme.PassVaultTextStyles
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.settings.presentation.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 880.dp)
                .align(Alignment.TopCenter)
                .scaffoldVerticalScroll(rememberScrollState(), paddingValues)
                .padding(
                    start = ComponentSpacing.screenHorizontal,
                    end = ComponentSpacing.screenHorizontal,
                    top = if (showBackButton) ComponentSpacing.screenVertical else 0.dp,
                    bottom = if (showBackButton) ComponentSpacing.screenVertical else 112.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (showBackButton) {
                TopAppBar(
                    title = {},
                    windowInsets = passVaultScrollableTopAppBarInsets(),
                    navigationIcon = {
                        IconButton(onClick = {
                            onEvent(SettingsViewModel.SettingsEvent.OnBackClick)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.ui_go_back)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            onEvent(SettingsViewModel.SettingsEvent.OnLockVaultClick)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(Res.string.ui_lock_vault)
                            )
                        }
                    },
                    colors = passVaultTopAppBarColors(),
                )
            }

            EditorialPageHeader(
                eyebrow = stringResource(Res.string.ui_passvault),
                title = stringResource(Res.string.action_settings),
                actions = {
                    if (!showBackButton) {
                        IconButton(onClick = {
                            onEvent(SettingsViewModel.SettingsEvent.OnLockVaultClick)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(Res.string.ui_lock_vault)
                            )
                        }
                    }
                },
            )

            // Security section
            SettingsSection(title = stringResource(Res.string.ui_security)) {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = stringResource(Res.string.ui_security_settings),
                    subtitle = stringResource(Res.string.ui_master_password_auto_lock_clipboard),
                    onClick = { onEvent(SettingsViewModel.SettingsEvent.OnSecurityClick) }
                )

                SettingsItem(
                    icon = Icons.Default.Timer,
                    title = stringResource(Res.string.ui_auto_lock),
                    subtitle = pluralStringResource(
                        Res.plurals.ui_lock_after_minutes,
                        state.autoLockTimeoutMinutes,
                        state.autoLockTimeoutMinutes,
                    ),
                    onClick = { onEvent(SettingsViewModel.SettingsEvent.OnSecurityClick) }
                )
            }

            // Appearance section
            SettingsSection(title = stringResource(Res.string.ui_appearance)) {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = stringResource(Res.string.ui_appearance),
                    subtitle = stringResource(Res.string.ui_theme_colors_display),
                    onClick = { onEvent(SettingsViewModel.SettingsEvent.OnAppearanceClick) }
                )

                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = stringResource(Res.string.ui_theme),
                    subtitle = when (state.theme) {
                        SettingsViewModel.AppTheme.LIGHT -> stringResource(Res.string.ui_theme_light)
                        SettingsViewModel.AppTheme.DARK -> stringResource(Res.string.ui_theme_dark)
                        SettingsViewModel.AppTheme.SYSTEM -> stringResource(Res.string.ui_theme_system)
                    },
                    onClick = { onEvent(SettingsViewModel.SettingsEvent.OnAppearanceClick) }
                )
            }

            // Data section
            SettingsSection(title = stringResource(Res.string.ui_data)) {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = stringResource(Res.string.ui_data_management),
                    subtitle = stringResource(Res.string.ui_export_import_backup),
                    onClick = { onEvent(SettingsViewModel.SettingsEvent.OnDataClick) }
                )

                SettingsItem(
                    icon = Icons.Default.Upload,
                    title = stringResource(Res.string.ui_export_vault),
                    subtitle = stringResource(Res.string.ui_save_your_passwords_to_a_file),
                    onClick = { onEvent(SettingsViewModel.SettingsEvent.OnExportClick) }
                )

                SettingsItem(
                    icon = Icons.Default.Download,
                    title = stringResource(Res.string.ui_import_vault),
                    subtitle = stringResource(Res.string.ui_import_passwords_from_a_file),
                    onClick = { onEvent(SettingsViewModel.SettingsEvent.OnImportClick) }
                )

                SettingsItem(
                    icon = Icons.Default.Cloud,
                    title = stringResource(Res.string.ui_backup),
                    subtitle = stringResource(Res.string.ui_encrypted_local_backup_options),
                    onClick = { onEvent(SettingsViewModel.SettingsEvent.OnBackupClick) }
                )
            }

            // About section
            SettingsSection(title = stringResource(Res.string.ui_about)) {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(Res.string.ui_vault_info),
                    subtitle = state.vaultCreatedAt
                        .takeIf(String::isNotEmpty)
                        ?.let { createdAt ->
                            if (state.vaultEntryCount > 0) {
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
                        ?: stringResource(Res.string.password_strength_unknown),
                    onClick = { onEvent(SettingsViewModel.SettingsEvent.OnVaultInfoClick) }
                )

                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = stringResource(Res.string.ui_help_and_security_guidance),
                    subtitle = stringResource(Res.string.ui_local_guidance_for_protecting_your_vault),
                    onClick = { onEvent(SettingsViewModel.SettingsEvent.OnHelpClick) }
                )

                SettingsItem(
                    icon = Icons.Default.Policy,
                    title = stringResource(Res.string.ui_privacy_policy),
                    subtitle = stringResource(Res.string.ui_how_we_handle_your_data),
                    onClick = { onEvent(SettingsViewModel.SettingsEvent.OnPrivacyClick) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Version info
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.ui_passvault_v1_0_0),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
        }

        // Error snackbar
        state.errorMessage?.let { error ->
            ErrorSnackbar(
                error = error,
                onDismiss = { onEvent(SettingsViewModel.SettingsEvent.OnDismissError) }
            )
        }

        // Change password dialog
        if (state.showChangePasswordDialog) {
            ChangePasswordDialog(
                currentPassword = state.currentPassword,
                newPassword = state.newPassword,
                confirmPassword = state.confirmPassword,
                passwordStrength = state.passwordStrength,
                error = state.passwordError,
                onCurrentPasswordChange = {
                    onEvent(SettingsViewModel.SettingsEvent.OnCurrentPasswordChanged(it))
                },
                onNewPasswordChange = {
                    onEvent(SettingsViewModel.SettingsEvent.OnNewPasswordChanged(it))
                },
                onConfirmPasswordChange = {
                    onEvent(SettingsViewModel.SettingsEvent.OnConfirmPasswordChanged(it))
                },
                onConfirm = {
                    onEvent(SettingsViewModel.SettingsEvent.OnChangePasswordConfirm)
                },
                onDismiss = {
                    onEvent(SettingsViewModel.SettingsEvent.OnChangePasswordCancel)
                },
                isLoading = state.isChangingPassword,
                canChange = state.canChangePassword
            )
        }

        state.infoDialogTitle?.let { infoTitle ->
            state.infoDialogMessage?.let { infoMessage ->
            AlertDialog(
                onDismissRequest = {
                    onEvent(SettingsViewModel.SettingsEvent.OnDismissInfo)
                },
                title = { Text(infoTitle.resolve()) },
                text = { Text(infoMessage.resolve()) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onEvent(SettingsViewModel.SettingsEvent.OnDismissInfo)
                        }
                    ) {
                        Text(stringResource(Res.string.ui_ok))
                    }
                }
            )
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = title,
            style = PassVaultTextStyles.Eyebrow,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm),
        )

        EditorialPanel(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(Spacing.xs),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = androidx.compose.ui.graphics.Color.Transparent,
    ) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        trailingContent = trailing,
        colors = ListItemDefaults.colors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
        ),
    )
    }
}

@Composable
private fun ChangePasswordDialog(
    currentPassword: String,
    newPassword: String,
    confirmPassword: String,
    passwordStrength: SettingsViewModel.PasswordStrength,
    error: UiText?,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    canChange: Boolean,
) {
    val focusManager = LocalFocusManager.current
    val newPasswordFocus = remember { FocusRequester() }
    val confirmPasswordFocus = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(stringResource(Res.string.ui_change_master_password)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = stringResource(Res.string.ui_enter_your_current_password_and_a_new_strong_password),
                    style = MaterialTheme.typography.bodyMedium,
                )

                SecureTextField(
                    value = currentPassword,
                    onValueChange = onCurrentPasswordChange,
                    label = stringResource(Res.string.ui_current_password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { newPasswordFocus.requestFocus() },
                    ),
                )

                SecureTextField(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    label = stringResource(Res.string.ui_new_password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(newPasswordFocus),
                    enabled = !isLoading,
                    imeAction = ImeAction.Next,
                    keyboardActions = KeyboardActions(
                        onNext = { confirmPasswordFocus.requestFocus() },
                    ),
                )

                // Strength indicator
                if (newPassword.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    val (label, color) = when (passwordStrength) {
                        SettingsViewModel.PasswordStrength.EMPTY -> "" to MaterialTheme.colorScheme.outline
                        SettingsViewModel.PasswordStrength.TOO_SHORT -> stringResource(Res.string.ui_too_short) to MaterialTheme.colorScheme.error
                        SettingsViewModel.PasswordStrength.WEAK -> stringResource(Res.string.password_strength_weak) to MaterialTheme.colorScheme.error
                        SettingsViewModel.PasswordStrength.GOOD -> stringResource(Res.string.password_strength_good) to MaterialTheme.colorScheme.secondary
                        SettingsViewModel.PasswordStrength.STRONG -> stringResource(Res.string.password_strength_strong) to MaterialTheme.colorScheme.primary
                    }
                    if (label.isNotEmpty()) {
                        Text(
                            text = stringResource(Res.string.ui_strength_value, label),
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                    }
                }

                SecureTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = stringResource(Res.string.ui_confirm_new_password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(confirmPasswordFocus),
                    enabled = !isLoading,
                    isError = error != null,
                    errorMessage = error?.resolve(),
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (canChange && !isLoading) onConfirm()
                        },
                    ),
                )

            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = canChange && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = androidx.compose.ui.Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(Res.string.ui_change))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}

@Composable
private fun ErrorSnackbar(
    error: UiText,
    onDismiss: () -> Unit,
) {
    Snackbar(
        action = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.ui_dismiss))
            }
        }
    ) {
        Text(error.resolve())
    }
}
