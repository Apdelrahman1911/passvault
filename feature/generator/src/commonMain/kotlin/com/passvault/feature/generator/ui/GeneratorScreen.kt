package com.passvault.feature.generator.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.resolve
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passvault.feature.generator.presentation.GeneratorViewModel
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.generator.ui.components.GeneratorOptionsPanel
import com.passvault.feature.generator.ui.components.PasswordDisplay
import com.passvault.feature.generator.ui.components.StrengthMeter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    state: GeneratorViewModel.GeneratorState,
    onEvent: (GeneratorViewModel.GeneratorEvent) -> Unit,
    onNavigateBack: () -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(
                    start = ComponentSpacing.screenHorizontal,
                    end = ComponentSpacing.screenHorizontal,
                    top = if (showBackButton) ComponentSpacing.screenVertical else 0.dp,
                    bottom = if (showBackButton) ComponentSpacing.screenVertical else 112.dp,
                )
                .widthIn(max = 760.dp)
                .fillMaxWidth()
        ) {
            if (showBackButton) {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.ui_go_back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            }

            EditorialPageHeader(
                eyebrow = stringResource(Res.string.ui_encrypted_vault),
                title = stringResource(Res.string.ui_password_generator),
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Mode toggle
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = !state.isPassphraseMode,
                    onClick = {
                        if (state.isPassphraseMode) {
                            onEvent(GeneratorViewModel.GeneratorEvent.OnPassphraseModeChanged)
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = 0,
                        count = 2
                    )
                ) {
                    Text(stringResource(Res.string.ui_password))
                }
                SegmentedButton(
                    selected = state.isPassphraseMode,
                    onClick = {
                        if (!state.isPassphraseMode) {
                            onEvent(GeneratorViewModel.GeneratorEvent.OnPassphraseModeChanged)
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = 1,
                        count = 2
                    )
                ) {
                    Text(stringResource(Res.string.ui_passphrase))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Password display
            PasswordDisplay(
                password = state.generatedPassword,
                onCopy = { onEvent(GeneratorViewModel.GeneratorEvent.OnCopyClick) },
                onRefresh = { onEvent(GeneratorViewModel.GeneratorEvent.OnGenerateClick) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Strength meter
            StrengthMeter(
                strength = state.passwordStrength,
                passwordLength = state.generatedPassword.length,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Use button
            Button(
                onClick = { onEvent(GeneratorViewModel.GeneratorEvent.OnUseClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                enabled = state.generatedPassword.isNotEmpty(),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.ui_use_this_password),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Options
            GeneratorOptionsPanel(
                state = state,
                onEvent = onEvent,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Error snackbar
        state.errorMessage?.let { error ->
            ErrorSnackbar(
                error = error,
                onDismiss = { onEvent(GeneratorViewModel.GeneratorEvent.OnDismissError) }
            )
        }
    }
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
