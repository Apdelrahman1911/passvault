package com.passvault.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.SecureTextField
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.settings.presentation.SettingsViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChangePasswordDialog(
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
                modifier = Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                Text(
                    stringResource(
                        Res.string.ui_enter_your_current_password_and_a_new_strong_password,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
                CurrentPasswordField(currentPassword, onCurrentPasswordChange, !isLoading) {
                    newPasswordFocus.requestFocus()
                }
                NewPasswordField(
                    newPassword,
                    onNewPasswordChange,
                    passwordStrength,
                    !isLoading,
                    newPasswordFocus,
                ) { confirmPasswordFocus.requestFocus() }
                ConfirmPasswordField(
                    confirmPassword,
                    onConfirmPasswordChange,
                    !isLoading,
                    confirmPasswordFocus,
                ) {
                    focusManager.clearFocus()
                    if (canChange && !isLoading) onConfirm()
                }
                error?.let {
                    Text(
                        text = it.resolve(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            }
        },
        confirmButton = { ChangePasswordConfirmButton(onConfirm, canChange, isLoading) },
        dismissButton = { ChangePasswordDismissButton(onDismiss, isLoading) },
    )
}

@Composable
private fun ChangePasswordConfirmButton(
    onConfirm: () -> Unit,
    canChange: Boolean,
    isLoading: Boolean,
) {
    Button(onClick = onConfirm, enabled = canChange && !isLoading) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = LocalContentColor.current,
            )
        } else {
            Text(stringResource(Res.string.ui_change))
        }
    }
}

@Composable
private fun ChangePasswordDismissButton(onDismiss: () -> Unit, isLoading: Boolean) {
    TextButton(onClick = onDismiss, enabled = !isLoading) {
        Text(stringResource(Res.string.action_cancel))
    }
}

@Composable
private fun CurrentPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    onNext: () -> Unit,
) {
    SecureTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(Res.string.ui_current_password),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        imeAction = ImeAction.Next,
        keyboardActions = KeyboardActions(onNext = { onNext() }),
    )
}

@Composable
private fun NewPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    strength: SettingsViewModel.PasswordStrength,
    enabled: Boolean,
    focusRequester: FocusRequester,
    onNext: () -> Unit,
) {
    SecureTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(Res.string.ui_new_password),
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        enabled = enabled,
        imeAction = ImeAction.Next,
        keyboardActions = KeyboardActions(onNext = { onNext() }),
    )
    if (value.isNotEmpty()) PasswordStrengthFeedback(strength)
}

@Composable
private fun PasswordStrengthFeedback(strength: SettingsViewModel.PasswordStrength) {
    val label = when (strength) {
        SettingsViewModel.PasswordStrength.EMPTY -> ""
        SettingsViewModel.PasswordStrength.TOO_SHORT -> stringResource(Res.string.ui_too_short)
        SettingsViewModel.PasswordStrength.WEAK -> stringResource(Res.string.password_strength_weak)
        SettingsViewModel.PasswordStrength.GOOD -> stringResource(Res.string.password_strength_good)
        SettingsViewModel.PasswordStrength.STRONG ->
            stringResource(Res.string.password_strength_strong)
    }
    if (label.isNotEmpty()) {
        Text(
            stringResource(Res.string.ui_strength_value, label),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ConfirmPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    focusRequester: FocusRequester,
    onDone: () -> Unit,
) {
    SecureTextField(
        value = value,
        onValueChange = onValueChange,
        label = stringResource(Res.string.ui_confirm_new_password),
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        enabled = enabled,
        imeAction = ImeAction.Done,
        keyboardActions = KeyboardActions(onDone = { onDone() }),
    )
}
