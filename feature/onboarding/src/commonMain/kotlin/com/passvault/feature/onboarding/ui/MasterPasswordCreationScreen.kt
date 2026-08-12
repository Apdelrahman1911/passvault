@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.passvault.feature.onboarding.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import org.jetbrains.compose.resources.stringResource

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.components.EditorialStatusBanner
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.domain.model.MasterPasswordPolicy
import com.passvault.core.domain.model.codePointLength
import com.passvault.feature.onboarding.presentation.OnboardingViewModel
import com.passvault.feature.onboarding.presentation.OnboardingViewModel.PasswordStrength
import kotlinx.coroutines.delay

@Composable
fun MasterPasswordCreationScreen(
    state: OnboardingViewModel.OnboardingState,
    onEvent: (OnboardingViewModel.OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        passwordFocusRequester.requestFocus()
    }
    LaunchedEffect(passwordVisible, state.masterPassword) {
        if (passwordVisible) {
            delay(15_000)
            passwordVisible = false
        }
    }

    MasterPasswordCreationScaffold(
        state = state,
        onEvent = onEvent,
        passwordFocusRequester = passwordFocusRequester,
        passwordVisible = passwordVisible,
        onToggleVisibility = { passwordVisible = !passwordVisible },
        onSubmit = {
            focusManager.clearFocus()
            onEvent(OnboardingViewModel.OnboardingEvent.OnCreateVaultClick)
        },
        modifier = modifier,
    )
}

@Composable
private fun MasterPasswordCreationScaffold(
    state: OnboardingViewModel.OnboardingState,
    onEvent: (OnboardingViewModel.OnboardingEvent) -> Unit,
    passwordFocusRequester: FocusRequester,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(OnboardingViewModel.OnboardingEvent.OnBackClick) },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.ui_go_back),
                        )
                    }
                },
                colors = passVaultTopAppBarColors(),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        CreationContent(
            state = state,
            onEvent = onEvent,
            passwordFocusRequester = passwordFocusRequester,
            passwordVisible = passwordVisible,
            onToggleVisibility = onToggleVisibility,
            onSubmit = onSubmit,
            modifier = Modifier.scaffoldVerticalScroll(rememberScrollState(), paddingValues),
        )
    }
}

@Composable
private fun CreationContent(
    state: OnboardingViewModel.OnboardingState,
    onEvent: (OnboardingViewModel.OnboardingEvent) -> Unit,
    passwordFocusRequester: FocusRequester,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = modifier
                .widthIn(max = ComponentSpacing.formMaxWidth)
                .fillMaxWidth()
                .padding(
                    horizontal = ComponentSpacing.screenHorizontal,
                    vertical = ComponentSpacing.screenVertical,
                ),
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
        ) {
            EditorialPageHeader(
                eyebrow = stringResource(Res.string.ui_passvault),
                title = stringResource(Res.string.ui_create_a_strong_master_password),
                subtitle = stringResource(
                    Res.string.ui_your_master_password_protects_the_vault_encryption_key,
                ),
            )
            CreationFormPanel(
                state = state,
                onEvent = onEvent,
                passwordFocusRequester = passwordFocusRequester,
                passwordVisible = passwordVisible,
                onToggleVisibility = onToggleVisibility,
                onSubmit = onSubmit,
            )
        }
    }
}

@Composable
private fun CreationFormPanel(
    state: OnboardingViewModel.OnboardingState,
    onEvent: (OnboardingViewModel.OnboardingEvent) -> Unit,
    passwordFocusRequester: FocusRequester,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onSubmit: () -> Unit,
) {
    EditorialPanel(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.lg),
    ) {
        MasterPasswordInput(
            state = state,
            onPasswordChanged = {
                onEvent(OnboardingViewModel.OnboardingEvent.OnPasswordChanged(it))
            },
            focusRequester = passwordFocusRequester,
            passwordVisible = passwordVisible,
            onToggleVisibility = onToggleVisibility,
            onSubmit = onSubmit,
        )
        AnimatedVisibility(
            visible = state.masterPassword.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
        ) {
            PasswordStrengthMeter(
                strength = state.passwordStrength,
                feedback = state.strengthFeedback.resolve(),
                password = state.masterPassword,
            )
        }
        state.errorMessage?.let { error ->
            EditorialStatusBanner(
                icon = Icons.Default.Warning,
                title = stringResource(Res.string.ui_create_master_password),
                message = error.resolve(),
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
        CreationContinueButton(state, onSubmit)
    }
}

@Composable
private fun MasterPasswordInput(
    state: OnboardingViewModel.OnboardingState,
    onPasswordChanged: (String) -> Unit,
    focusRequester: FocusRequester,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    onSubmit: () -> Unit,
) {
    OutlinedTextField(
        value = state.masterPassword,
        onValueChange = onPasswordChanged,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        label = { Text(stringResource(Res.string.ui_master_password)) },
        placeholder = { Text(stringResource(Res.string.ui_use_a_unique_passphrase)) },
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        singleLine = true,
        trailingIcon = {
            TextButton(onClick = onToggleVisibility) {
                Text(
                    if (passwordVisible) {
                        stringResource(Res.string.action_hide)
                    } else {
                        stringResource(Res.string.action_show)
                    },
                )
            }
        },
        isError = state.masterPassword.isNotEmpty() &&
            !MasterPasswordPolicy.accepts(state.masterPassword),
        supportingText = {
            if (
                state.masterPassword.isNotEmpty() &&
                state.masterPassword.codePointLength() < MasterPasswordPolicy.MIN_LENGTH
            ) {
                Text(stringResource(Res.string.ui_use_at_least_12_characters))
            }
        },
    )
}

@Composable
private fun CreationContinueButton(
    state: OnboardingViewModel.OnboardingState,
    onSubmit: () -> Unit,
) {
    Button(
        onClick = onSubmit,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        enabled = state.canContinueToConfirmation,
        shape = MaterialTheme.shapes.large,
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(
                stringResource(Res.string.ui_continue),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun PasswordStrengthMeter(
    strength: PasswordStrength,
    feedback: String,
    password: String,
    modifier: Modifier = Modifier,
) {
    val (color, progress) = when (strength) {
        PasswordStrength.TOO_SHORT -> MaterialTheme.colorScheme.outline to 0.1f
        PasswordStrength.VERY_WEAK -> MaterialTheme.colorScheme.error to 0.2f
        PasswordStrength.WEAK -> MaterialTheme.colorScheme.error to 0.4f
        PasswordStrength.FAIR -> MaterialTheme.colorScheme.tertiary to 0.6f
        PasswordStrength.GOOD -> MaterialTheme.colorScheme.secondary to 0.75f
        PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary to 0.9f
        PasswordStrength.VERY_STRONG -> MaterialTheme.colorScheme.primary to 1f
    }
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(Res.string.ui_password_strength),
            style = MaterialTheme.typography.labelMedium,
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Text(feedback, style = MaterialTheme.typography.bodySmall, color = color)
        PasswordRequirements(password)
    }
}

@Composable
private fun PasswordRequirements(password: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        RequirementItem(
            stringResource(Res.string.passwordCriteria_minLength),
            password.codePointLength() >= MasterPasswordPolicy.MIN_LENGTH,
        )
        RequirementItem(
            stringResource(Res.string.ui_uppercase_letters),
            password.any(Char::isUpperCase),
        )
        RequirementItem(
            stringResource(Res.string.ui_lowercase_letters),
            password.any(Char::isLowerCase),
        )
        RequirementItem(stringResource(Res.string.ui_numbers), password.any(Char::isDigit))
        RequirementItem(
            stringResource(Res.string.ui_special_characters),
            password.any { !it.isLetterOrDigit() },
        )
    }
}

@Composable
private fun RequirementItem(text: String, met: Boolean) {
    val requirementState = if (met) {
        stringResource(Res.string.ui_requirement_met)
    } else {
        stringResource(Res.string.ui_requirement_not_met)
    }
    Row(
        modifier = Modifier
            .padding(vertical = 2.dp)
            .semantics { stateDescription = requirementState },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(20.dp),
            shape = MaterialTheme.shapes.small,
            color = if (met) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (met) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = if (met) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}
