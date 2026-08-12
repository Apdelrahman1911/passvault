package com.passvault.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricType
import com.passvault.feature.settings.presentation.SettingsViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecuritySettingsScreen(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val statusMessage = state.statusMessage?.resolve()
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(SettingsViewModel.SettingsEvent.OnDismissStatusMessage)
        }
    }
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            SecuritySettingsContent(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.scaffoldVerticalScroll(rememberScrollState(), paddingValues),
            )
        }
    }
    SecurityDialogs(state = state, onEvent = onEvent)
}

@Composable
private fun SecuritySettingsContent(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
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
        SettingsScrollableTopBar(
            onBack = { onEvent(SettingsViewModel.SettingsEvent.OnBackClick) },
        )
        EditorialPageHeader(
            eyebrow = stringResource(Res.string.action_settings),
            title = stringResource(Res.string.ui_security_settings),
        )
        MasterPasswordCard(onEvent)
        if (
            state.isBiometricLoading ||
            state.biometricAvailability != BiometricAvailability.UNAVAILABLE ||
            state.isBiometricEnabled
        ) {
            BiometricCard(state, onEvent)
        }
        AutoLockCard(state.autoLockTimeoutMinutes, onEvent)
        ClipboardCard(state.clipboardClearSeconds, onEvent)
        SecurityTips()
    }
}

@Composable
private fun MasterPasswordCard(onEvent: (SettingsViewModel.SettingsEvent) -> Unit) {
    SecurityCard(
        icon = Icons.Default.Lock,
        title = stringResource(Res.string.ui_master_password),
        description = stringResource(Res.string.ui_change_vault_master_password),
    ) {
        Button(
            onClick = { onEvent(SettingsViewModel.SettingsEvent.OnChangePasswordClick) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(stringResource(Res.string.ui_change_master_password))
        }
    }
}

@Composable
private fun BiometricCard(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
) {
    val biometricName = biometricName(state.biometricType)
    val status = if (state.isBiometricEnabled) {
        stringResource(Res.string.ui_biometric_unlock_on, biometricName)
    } else {
        stringResource(Res.string.ui_biometric_unlock_off, biometricName)
    }
    SecurityCard(
        icon = if (state.biometricType == BiometricType.FACE) {
            Icons.Default.Face
        } else {
            Icons.Default.Fingerprint
        },
        title = stringResource(Res.string.ui_biometric_unlock),
        description = stringResource(Res.string.ui_biometric_unlock_description, biometricName),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(status, style = MaterialTheme.typography.bodyMedium)
                if (state.biometricAvailability == BiometricAvailability.NOT_ENROLLED) {
                    Text(
                        stringResource(Res.string.error_biometric_not_enrolled),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            if (state.isBiometricLoading) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
            } else {
                Switch(
                    checked = state.isBiometricEnabled,
                    onCheckedChange = {
                        onEvent(SettingsViewModel.SettingsEvent.OnBiometricUnlockChanged(it))
                    },
                    enabled = state.biometricAvailability == BiometricAvailability.AVAILABLE,
                    modifier = Modifier.semantics { stateDescription = status },
                )
            }
        }
    }
}

@Composable
private fun biometricName(type: BiometricType): String = when (type) {
    BiometricType.FACE -> stringResource(Res.string.ui_face_id)
    BiometricType.FINGERPRINT -> stringResource(Res.string.ui_touch_id)
    BiometricType.GENERIC -> stringResource(Res.string.ui_biometrics)
}

@Composable
private fun AutoLockCard(
    timeoutMinutes: Int,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
) {
    val description = pluralStringResource(
        Res.plurals.ui_lock_after_minutes,
        timeoutMinutes,
        timeoutMinutes,
    )
    SecurityCard(
        icon = Icons.Default.Timer,
        title = stringResource(Res.string.ui_auto_lock),
        description = stringResource(Res.string.ui_auto_lock_description),
    ) {
        Text(description, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = timeoutMinutes.toFloat(),
            onValueChange = {
                onEvent(SettingsViewModel.SettingsEvent.OnAutoLockTimeoutChanged(it.toInt()))
            },
            valueRange = AppSettings.MIN_AUTO_LOCK_TIMEOUT_MINUTES.toFloat()..
                AppSettings.MAX_AUTO_LOCK_TIMEOUT_MINUTES.toFloat(),
            steps = AppSettings.MAX_AUTO_LOCK_TIMEOUT_MINUTES -
                AppSettings.MIN_AUTO_LOCK_TIMEOUT_MINUTES - 1,
            modifier = Modifier.semantics { stateDescription = description },
        )
        RangeLabels(
            minimum = pluralStringResource(
                Res.plurals.ui_minutes_compact,
                AppSettings.MIN_AUTO_LOCK_TIMEOUT_MINUTES,
                AppSettings.MIN_AUTO_LOCK_TIMEOUT_MINUTES,
            ),
            middle = stringResource(Res.string.ui_30_min),
            maximum = pluralStringResource(
                Res.plurals.ui_minutes_compact,
                AppSettings.MAX_AUTO_LOCK_TIMEOUT_MINUTES,
                AppSettings.MAX_AUTO_LOCK_TIMEOUT_MINUTES,
            ),
        )
    }
}

@Composable
private fun ClipboardCard(
    clearSeconds: Int,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
) {
    val description = pluralStringResource(
        Res.plurals.ui_clear_clipboard_seconds,
        clearSeconds,
        clearSeconds,
    )
    SecurityCard(
        icon = Icons.Default.ContentPaste,
        title = stringResource(Res.string.ui_clipboard_security),
        description = stringResource(Res.string.ui_clipboard_security_description),
    ) {
        Text(description, style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = clearSeconds.toFloat(),
            onValueChange = {
                onEvent(SettingsViewModel.SettingsEvent.OnClipboardClearChanged(it.toInt()))
            },
            valueRange = AppSettings.MIN_CLIPBOARD_CLEAR_SECONDS.toFloat()..
                AppSettings.MAX_CLIPBOARD_CLEAR_SECONDS.toFloat(),
            steps = AppSettings.MAX_CLIPBOARD_CLEAR_SECONDS -
                AppSettings.MIN_CLIPBOARD_CLEAR_SECONDS -
                1,
            modifier = Modifier.semantics { stateDescription = description },
        )
        RangeLabels(
            minimum = stringResource(
                Res.string.ui_seconds_compact,
                AppSettings.MIN_CLIPBOARD_CLEAR_SECONDS,
            ),
            middle = stringResource(Res.string.ui_2_min),
            maximum = stringResource(
                Res.string.ui_seconds_compact,
                AppSettings.MAX_CLIPBOARD_CLEAR_SECONDS,
            ),
        )
    }
}

@Composable
private fun RangeLabels(minimum: String, middle: String, maximum: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(minimum, style = MaterialTheme.typography.labelSmall)
        Text(middle, style = MaterialTheme.typography.labelSmall)
        Text(maximum, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SecurityTips() {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(ComponentSpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                Text(
                    stringResource(Res.string.ui_security_tips),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            listOf(
                Res.string.ui_use_a_strong_master_password_16_plus_characters,
                Res.string.ui_set_auto_lock_to_protect_your_vault_when_idle,
                Res.string.ui_clear_clipboard_quickly_after_copying_passwords,
            ).forEach { tip ->
                Text(
                    stringResource(tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }
    }
}

@Composable
private fun SecurityCard(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    EditorialPanel(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Spacing.lg),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
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
        Spacer(modifier = Modifier.height(Spacing.md))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(Spacing.md))
        content()
    }
}

@Composable
private fun SecurityDialogs(
    state: SettingsViewModel.SettingsState,
    onEvent: (SettingsViewModel.SettingsEvent) -> Unit,
) {
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
            onConfirm = { onEvent(SettingsViewModel.SettingsEvent.OnChangePasswordConfirm) },
            onDismiss = { onEvent(SettingsViewModel.SettingsEvent.OnChangePasswordCancel) },
            isLoading = state.isChangingPassword,
            canChange = state.canChangePassword,
        )
    }
    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { onEvent(SettingsViewModel.SettingsEvent.OnDismissError) },
            title = { Text(stringResource(Res.string.ui_security_setting)) },
            text = { Text(message.resolve()) },
            confirmButton = {
                TextButton(onClick = { onEvent(SettingsViewModel.SettingsEvent.OnDismissError) }) {
                    Text(stringResource(Res.string.ui_ok))
                }
            },
        )
    }
}
