package com.passvault.feature.settings.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.settings.presentation.SettingsViewModel
import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
                colors = passVaultTopAppBarColors(),
            )
        },
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
                .widthIn(max = 760.dp)
                .align(Alignment.TopCenter)
                .scaffoldVerticalScroll(rememberScrollState(), paddingValues)
                .imePadding()
                .padding(
                    horizontal = ComponentSpacing.screenHorizontal,
                    vertical = ComponentSpacing.screenVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
        ) {
            EditorialPageHeader(
                eyebrow = stringResource(Res.string.action_settings),
                title = stringResource(Res.string.ui_security_settings),
            )

            // Master Password Card
            SecurityCard(
                icon = Icons.Default.Lock,
                title = stringResource(Res.string.ui_master_password),
                description = stringResource(Res.string.ui_change_vault_master_password),
            ) {
                Button(
                    onClick = { onEvent(SettingsViewModel.SettingsEvent.OnChangePasswordClick) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.ui_change_master_password))
                }
            }

            if (state.biometricAvailability != BiometricAvailability.UNAVAILABLE || state.isBiometricEnabled) {
                val biometricName = stringResource(
                    when (state.biometricType) {
                        BiometricType.FACE -> Res.string.ui_face_id
                        BiometricType.FINGERPRINT -> Res.string.ui_touch_id
                        BiometricType.GENERIC -> Res.string.ui_biometrics
                    },
                )
                SecurityCard(
                    icon = if (state.biometricType == BiometricType.FACE) {
                        Icons.Default.Face
                    } else {
                        Icons.Default.Fingerprint
                    },
                    title = stringResource(Res.string.ui_biometric_unlock),
                    description = stringResource(
                        Res.string.ui_biometric_unlock_description,
                        biometricName,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (state.isBiometricEnabled) {
                                    stringResource(Res.string.ui_biometric_unlock_on, biometricName)
                                } else {
                                    stringResource(Res.string.ui_biometric_unlock_off, biometricName)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (state.biometricAvailability == BiometricAvailability.NOT_ENROLLED) {
                                Text(
                                    text = stringResource(Res.string.error_biometric_not_enrolled),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                        if (state.isBiometricLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Switch(
                                checked = state.isBiometricEnabled,
                                onCheckedChange = {
                                    onEvent(SettingsViewModel.SettingsEvent.OnBiometricUnlockChanged(it))
                                },
                                enabled = state.biometricAvailability == BiometricAvailability.AVAILABLE,
                            )
                        }
                    }
                }
            }

            // Auto-Lock Card
            SecurityCard(
                icon = Icons.Default.Timer,
                title = stringResource(Res.string.ui_auto_lock),
                description = stringResource(Res.string.ui_auto_lock_description),
            ) {
                Text(
                    text = pluralStringResource(
                        Res.plurals.ui_lock_after_minutes,
                        state.autoLockTimeoutMinutes,
                        state.autoLockTimeoutMinutes,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Slider(
                    value = state.autoLockTimeoutMinutes.toFloat(),
                    onValueChange = {
                        onEvent(SettingsViewModel.SettingsEvent.OnAutoLockTimeoutChanged(it.toInt()))
                    },
                    valueRange = AppSettings.MIN_AUTO_LOCK_TIMEOUT_MINUTES.toFloat()..
                        AppSettings.MAX_AUTO_LOCK_TIMEOUT_MINUTES.toFloat(),
                    steps = AppSettings.MAX_AUTO_LOCK_TIMEOUT_MINUTES -
                        AppSettings.MIN_AUTO_LOCK_TIMEOUT_MINUTES - 1,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        pluralStringResource(
                            Res.plurals.ui_minutes_compact,
                            AppSettings.MIN_AUTO_LOCK_TIMEOUT_MINUTES,
                            AppSettings.MIN_AUTO_LOCK_TIMEOUT_MINUTES,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(stringResource(Res.string.ui_30_min), style = MaterialTheme.typography.labelSmall)
                    Text(
                        pluralStringResource(
                            Res.plurals.ui_minutes_compact,
                            AppSettings.MAX_AUTO_LOCK_TIMEOUT_MINUTES,
                            AppSettings.MAX_AUTO_LOCK_TIMEOUT_MINUTES,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            // Clipboard Card
            SecurityCard(
                icon = Icons.Default.ContentPaste,
                title = stringResource(Res.string.ui_clipboard_security),
                description = stringResource(Res.string.ui_clipboard_security_description),
            ) {
                Text(
                    text = pluralStringResource(
                        Res.plurals.ui_clear_clipboard_seconds,
                        state.clipboardClearSeconds,
                        state.clipboardClearSeconds,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Slider(
                    value = state.clipboardClearSeconds.toFloat(),
                    onValueChange = {
                        onEvent(SettingsViewModel.SettingsEvent.OnClipboardClearChanged(it.toInt()))
                    },
                    valueRange = AppSettings.MIN_CLIPBOARD_CLEAR_SECONDS.toFloat()..
                        AppSettings.MAX_CLIPBOARD_CLEAR_SECONDS.toFloat(),
                    steps = AppSettings.MAX_CLIPBOARD_CLEAR_SECONDS -
                        AppSettings.MIN_CLIPBOARD_CLEAR_SECONDS - 1,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(
                            Res.string.ui_seconds_compact,
                            AppSettings.MIN_CLIPBOARD_CLEAR_SECONDS,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(stringResource(Res.string.ui_2_min), style = MaterialTheme.typography.labelSmall)
                    Text(
                        stringResource(
                            Res.string.ui_seconds_compact,
                            AppSettings.MAX_CLIPBOARD_CLEAR_SECONDS,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }

            // Security Info Card
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(Res.string.ui_security_tips),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(Res.string.ui_use_a_strong_master_password_16_plus_characters),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = stringResource(Res.string.ui_set_auto_lock_to_protect_your_vault_when_idle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = stringResource(Res.string.ui_clear_clipboard_quickly_after_copying_passwords),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
        }
    }

    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = {
                onEvent(SettingsViewModel.SettingsEvent.OnDismissError)
            },
            title = { Text(stringResource(Res.string.ui_security_setting)) },
            text = { Text(message.resolve()) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(SettingsViewModel.SettingsEvent.OnDismissError)
                    }
                ) {
                    Text(stringResource(Res.string.ui_ok))
                }
            }
        )
    }
}

@Composable
private fun SecurityCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    EditorialPanel(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Spacing.lg),
    ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            content()
    }
}
