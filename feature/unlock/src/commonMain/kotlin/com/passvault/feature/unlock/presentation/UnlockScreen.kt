package com.passvault.feature.unlock.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.components.EditorialStatusBanner
import com.passvault.core.designsystem.components.SecureTextField
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.Breakpoints
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.security.BiometricType
import org.jetbrains.compose.resources.stringResource

@Composable
fun UnlockScreen(
    state: UnlockViewModel.UnlockState,
    onEvent: (UnlockViewModel.UnlockEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.isBiometricStatusLoaded, state.canUseBiometrics) {
        if (state.isBiometricStatusLoaded && !state.canUseBiometrics) {
            passwordFocusRequester.requestFocus()
        }
    }

    val onSubmit = {
        focusManager.clearFocus()
        onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)
    }
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        UnlockResponsiveLayout(
            state = state,
            onEvent = onEvent,
            passwordFocusRequester = passwordFocusRequester,
            onSubmit = onSubmit,
        )
    }
    if (state.showRecoveryInfo) {
        AlertDialog(
            onDismissRequest = { onEvent(UnlockViewModel.UnlockEvent.OnDismissRecoveryInfo) },
            icon = { Icon(Icons.Default.Lock, contentDescription = null) },
            title = { Text(stringResource(Res.string.ui_master_password_recovery)) },
            text = { Text(stringResource(Res.string.ui_unlock_recovery_warning)) },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(UnlockViewModel.UnlockEvent.OnDismissRecoveryInfo) },
                ) {
                    Text(stringResource(Res.string.ui_ok))
                }
            },
        )
    }
}

@Composable
private fun UnlockResponsiveLayout(
    state: UnlockViewModel.UnlockState,
    onEvent: (UnlockViewModel.UnlockEvent) -> Unit,
    passwordFocusRequester: FocusRequester,
    onSubmit: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val expanded = maxWidth >= Breakpoints.expandedMin
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(ComponentSpacing.screenHorizontal),
            contentAlignment = Alignment.Center,
        ) {
            if (expanded) {
                ExpandedUnlockLayout(state, onEvent, passwordFocusRequester, onSubmit)
            } else {
                CompactUnlockLayout(state, onEvent, passwordFocusRequester, onSubmit)
            }
        }
    }
}

@Composable
private fun ExpandedUnlockLayout(
    state: UnlockViewModel.UnlockState,
    onEvent: (UnlockViewModel.UnlockEvent) -> Unit,
    passwordFocusRequester: FocusRequester,
    onSubmit: () -> Unit,
) {
    Row(
        modifier = Modifier.widthIn(max = 980.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
        verticalAlignment = Alignment.Top,
    ) {
        LockedVaultVisual(
            modifier = Modifier.weight(0.85f),
            biometricAvailable = state.canUseBiometrics,
        )
        UnlockForm(
            state = state,
            onEvent = onEvent,
            passwordFocusRequester = passwordFocusRequester,
            onSubmit = onSubmit,
            modifier = Modifier.weight(1.15f),
        )
    }
}

@Composable
private fun CompactUnlockLayout(
    state: UnlockViewModel.UnlockState,
    onEvent: (UnlockViewModel.UnlockEvent) -> Unit,
    passwordFocusRequester: FocusRequester,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth().padding(vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        LockedVaultVisual(
            compact = true,
            biometricAvailable = state.canUseBiometrics,
        )
        UnlockForm(
            state = state,
            onEvent = onEvent,
            passwordFocusRequester = passwordFocusRequester,
            onSubmit = onSubmit,
        )
    }
}

@Composable
private fun LockedVaultVisual(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    biometricAvailable: Boolean = false,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 210.dp else 480.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
    ) {
        Column(
            modifier = Modifier.padding(if (compact) Spacing.lg else Spacing.xl),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Surface(
                modifier = Modifier.size(if (compact) 52.dp else 64.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                contentColor = MaterialTheme.colorScheme.inverseSurface,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(if (compact) 26.dp else 30.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = stringResource(Res.string.ui_vault_locked),
                    style = if (compact) {
                        MaterialTheme.typography.headlineMedium
                    } else {
                        MaterialTheme.typography.displaySmall
                    },
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(
                        if (biometricAvailable) {
                            Res.string.ui_use_master_password_or_biometrics_to_unlock
                        } else {
                            Res.string.ui_enter_your_master_password_to_unlock
                        },
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.72f),
                )
            }
        }
    }
}

@Composable
private fun UnlockForm(
    state: UnlockViewModel.UnlockState,
    onEvent: (UnlockViewModel.UnlockEvent) -> Unit,
    passwordFocusRequester: FocusRequester,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EditorialPanel(
        modifier = modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.lg),
    ) {
        if (state.failedAttempts > 0 && state.errorMessage == null) {
            FailedAttemptsWarning(attempts = state.failedAttempts)
        }
        UnlockErrorBanner(state, onEvent)
        UnlockInputRow(state, onEvent, passwordFocusRequester, onSubmit)

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            enabled = state.canUnlock && !state.isLockedOut,
        ) {
            if (state.isLoading && !state.isBiometricLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = stringResource(Res.string.ui_unlock_vault),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        TextButton(
            onClick = { onEvent(UnlockViewModel.UnlockEvent.OnForgotPasswordClick) },
            enabled = !state.isLoading,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(stringResource(Res.string.ui_forgot_password))
        }
    }
}

@Composable
private fun UnlockErrorBanner(
    state: UnlockViewModel.UnlockState,
    onEvent: (UnlockViewModel.UnlockEvent) -> Unit,
) {
    state.errorMessage?.let { message ->
        EditorialStatusBanner(
            icon = Icons.Default.Warning,
            title = stringResource(Res.string.ui_vault_locked),
            message = message.resolve(),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            action = if (state.isLockedOut) null else {
                {
                    IconButton(
                        onClick = { onEvent(UnlockViewModel.UnlockEvent.OnDismissError) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.ui_dismiss),
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun UnlockInputRow(
    state: UnlockViewModel.UnlockState,
    onEvent: (UnlockViewModel.UnlockEvent) -> Unit,
    passwordFocusRequester: FocusRequester,
    onSubmit: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SecureTextField(
            value = state.password,
            onValueChange = {
                onEvent(UnlockViewModel.UnlockEvent.OnPasswordChanged(it))
            },
            modifier = Modifier.weight(1f).focusRequester(passwordFocusRequester),
            label = stringResource(Res.string.ui_master_password),
            enabled = !state.isLockedOut && !state.isLoading,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        )
        if (state.canUseBiometrics) {
            BiometricUnlockButton(state, onEvent)
        }
    }
}

@Composable
private fun BiometricUnlockButton(
    state: UnlockViewModel.UnlockState,
    onEvent: (UnlockViewModel.UnlockEvent) -> Unit,
) {
    val biometricLabel = stringResource(
        when (state.biometricType) {
            BiometricType.FACE -> Res.string.ui_unlock_with_face_id
            BiometricType.FINGERPRINT -> Res.string.ui_unlock_with_touch_id
            BiometricType.GENERIC -> Res.string.ui_unlock_with_biometrics
        },
    )
    OutlinedButton(
        onClick = { onEvent(UnlockViewModel.UnlockEvent.OnBiometricUnlockClick) },
        modifier = Modifier.size(56.dp),
        enabled = !state.isLoading,
        contentPadding = PaddingValues(0.dp),
    ) {
        if (state.isBiometricLoading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = if (state.biometricType == BiometricType.FACE) {
                    Icons.Default.Face
                } else {
                    Icons.Default.Fingerprint
                },
                contentDescription = biometricLabel,
            )
        }
    }
}

@Composable
private fun FailedAttemptsWarning(
    attempts: Int,
    modifier: Modifier = Modifier,
) {
    val remaining = (5 - attempts).coerceAtLeast(0)
    EditorialStatusBanner(
        icon = Icons.Default.Warning,
        title = stringResource(Res.string.ui_vault_locked),
        message = stringResource(
            Res.string.ui_failed_attempts_remaining,
            attempts,
            remaining,
        ),
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    )
}
