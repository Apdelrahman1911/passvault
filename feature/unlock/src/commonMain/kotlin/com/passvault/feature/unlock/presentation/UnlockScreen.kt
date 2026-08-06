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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricType
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

@Composable
fun UnlockScreenRoute(
    viewModel: UnlockViewModel,
    onUnlockSuccess: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                UnlockViewModel.UnlockEffect.NavigateToVault -> onUnlockSuccess()
                UnlockViewModel.UnlockEffect.NavigateToOnboarding -> onNavigateToOnboarding()
            }
        }
    }

    UnlockScreen(
        state = state,
        onEvent = viewModel::onEvent,
        modifier = modifier,
    )

    if (state.showRecoveryInfo) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(UnlockViewModel.UnlockEvent.OnDismissRecoveryInfo)
            },
            icon = { Icon(Icons.Default.Lock, contentDescription = null) },
            title = { Text(stringResource(Res.string.ui_master_password_recovery)) },
            text = {
                Text(
                    stringResource(Res.string.ui_passvault_cannot_recover_or_reset_the_master_password) +
                        stringResource(Res.string.ui_an_encrypted_backup_can_only_be_restored_with_its_sepa),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(UnlockViewModel.UnlockEvent.OnDismissRecoveryInfo)
                    },
                ) {
                    Text(stringResource(Res.string.ui_ok))
                }
            },
        )
    }
}

@Composable
fun UnlockScreen(
    state: UnlockViewModel.UnlockState,
    onEvent: (UnlockViewModel.UnlockEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.isBiometricStatusLoaded, state.isBiometricEnabled) {
        if (state.isBiometricStatusLoaded && !state.isBiometricEnabled) {
            passwordFocusRequester.requestFocus()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 980.dp),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                        verticalAlignment = Alignment.Top,
                    ) {
                        LockedVaultVisual(
                            modifier = Modifier.weight(0.85f),
                            biometricAvailable = state.biometricAvailability != BiometricAvailability.UNAVAILABLE,
                        )
                        UnlockForm(
                            state = state,
                            onEvent = onEvent,
                            passwordFocusRequester = passwordFocusRequester,
                            onSubmit = {
                                focusManager.clearFocus()
                                onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)
                            },
                            modifier = Modifier.weight(1.15f),
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp)
                            .padding(vertical = Spacing.md),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        LockedVaultVisual(
                            compact = true,
                            biometricAvailable = state.biometricAvailability != BiometricAvailability.UNAVAILABLE,
                        )
                        UnlockForm(
                            state = state,
                            onEvent = onEvent,
                            passwordFocusRequester = passwordFocusRequester,
                            onSubmit = {
                                focusManager.clearFocus()
                                onEvent(UnlockViewModel.UnlockEvent.OnUnlockClick)
                            },
                        )
                    }
                }
            }
        }
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

        state.errorMessage?.let { message ->
            EditorialStatusBanner(
                icon = Icons.Default.Warning,
                title = stringResource(Res.string.ui_vault_locked),
                message = message.resolve(),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                action = if (!state.isLockedOut) {
                    {
                        IconButton(
                            onClick = {
                                onEvent(UnlockViewModel.UnlockEvent.OnDismissError)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.ui_dismiss),
                            )
                        }
                    }
                } else {
                    null
                },
            )
        }

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
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(passwordFocusRequester),
                label = stringResource(Res.string.ui_master_password),
                enabled = !state.isLockedOut && !state.isBiometricLoading,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            )

            if (state.biometricAvailability != BiometricAvailability.UNAVAILABLE) {
                val biometricLabel = stringResource(
                    when (state.biometricType) {
                        BiometricType.FACE -> Res.string.ui_unlock_with_face_id
                        BiometricType.FINGERPRINT -> Res.string.ui_unlock_with_touch_id
                        BiometricType.GENERIC -> Res.string.ui_unlock_with_biometrics
                    },
                )
                OutlinedButton(
                    onClick = {
                        onEvent(UnlockViewModel.UnlockEvent.OnBiometricUnlockClick)
                    },
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
        }

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
