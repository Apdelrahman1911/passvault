@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.passvault.feature.onboarding.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.passvault.feature.onboarding.presentation.OnboardingViewModel
import kotlinx.coroutines.delay

@Composable
fun MasterPasswordConfirmationScreen(
    state: OnboardingViewModel.OnboardingState,
    onEvent: (OnboardingViewModel.OnboardingEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val confirmFocusRequester = remember { FocusRequester() }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        confirmFocusRequester.requestFocus()
    }
    LaunchedEffect(passwordVisible, state.confirmPassword) {
        if (passwordVisible) {
            delay(15_000)
            passwordVisible = false
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onEvent(OnboardingViewModel.OnboardingEvent.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.ui_go_back))
                    }
                },
                colors = passVaultTopAppBarColors(),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = ComponentSpacing.formMaxWidth)
                    .scaffoldVerticalScroll(rememberScrollState(), paddingValues)
                    .padding(
                        horizontal = ComponentSpacing.screenHorizontal,
                        vertical = ComponentSpacing.screenVertical,
                    ),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
            ) {
                EditorialPageHeader(
                    eyebrow = stringResource(Res.string.ui_confirm_master_password),
                    title = stringResource(Res.string.ui_type_it_again_carefully),
                    subtitle = stringResource(
                        Res.string.ui_the_vault_cannot_be_recovered_without_this_password_it,
                    ),
                )

                EditorialPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(Spacing.lg),
                ) {
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = {
                            onEvent(OnboardingViewModel.OnboardingEvent.OnConfirmPasswordChanged(it))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(confirmFocusRequester),
                        label = { Text(stringResource(Res.string.ui_confirm_password)) },
                        visualTransformation = if (passwordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                onEvent(OnboardingViewModel.OnboardingEvent.OnConfirmPasswordClick)
                            },
                        ),
                        singleLine = true,
                        trailingIcon = {
                            TextButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(
                                    if (passwordVisible) {
                                        stringResource(Res.string.action_hide)
                                    } else {
                                        stringResource(Res.string.action_show)
                                    },
                                )
                            }
                        },
                        isError = state.confirmPassword.isNotEmpty() && !state.passwordsMatch,
                        supportingText = {
                            if (state.confirmPassword.isNotEmpty() && !state.passwordsMatch) {
                                Text(stringResource(Res.string.ui_passwords_do_not_match))
                            }
                        },
                    )

                    if (state.confirmPassword.isNotEmpty()) {
                        MatchIndicator(state.passwordsMatch)
                    }

                    EditorialStatusBanner(
                        icon = Icons.Default.Warning,
                        title = stringResource(Res.string.ui_important_store_your_master_password_safely),
                        message = stringResource(
                            Res.string.ui_passvault_cannot_reset_it_or_decrypt_your_vault_withou,
                        ),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )

                    state.errorMessage?.let { error ->
                        EditorialStatusBanner(
                            icon = Icons.Default.Warning,
                            title = stringResource(Res.string.ui_confirm_master_password),
                            message = error.resolve(),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }

                    Button(
                        onClick = {
                            onEvent(OnboardingViewModel.OnboardingEvent.OnConfirmPasswordClick)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = state.canCreateVault,
                        shape = MaterialTheme.shapes.large,
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(
                                stringResource(Res.string.ui_create_encrypted_vault),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchIndicator(passwordsMatch: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = MaterialTheme.shapes.small,
            color = if (passwordsMatch) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (passwordsMatch) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (passwordsMatch) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onError
                    },
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Text(
            if (passwordsMatch) stringResource(Res.string.ui_passwords_match) else stringResource(Res.string.ui_passwords_do_not_match_b6eb82cd),
            style = MaterialTheme.typography.bodyMedium,
            color = if (passwordsMatch) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            },
        )
    }
}
