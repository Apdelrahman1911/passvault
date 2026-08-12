package com.passvault.feature.generator.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passvault.feature.generator.presentation.GeneratorViewModel
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors
import com.passvault.core.designsystem.platform.passVaultScrollableTopAppBarInsets
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.domain.model.codePointLength
import com.passvault.feature.generator.ui.components.GeneratorOptionsPanel
import com.passvault.feature.generator.ui.components.PasswordDisplay
import com.passvault.feature.generator.ui.components.StrengthMeter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    state: GeneratorViewModel.GeneratorState,
    onEvent: (GeneratorViewModel.GeneratorEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHealth: () -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = state.errorMessage?.resolve()
    val statusMessage = state.statusMessage?.resolve()
    LaunchedEffect(errorMessage, statusMessage) {
        when {
            errorMessage != null -> snackbarHostState.showSnackbar(
                message = errorMessage,
                withDismissAction = true,
                duration = SnackbarDuration.Long,
            )
            statusMessage != null -> snackbarHostState.showSnackbar(statusMessage)
            else -> return@LaunchedEffect
        }
        onEvent(GeneratorViewModel.GeneratorEvent.OnDismissMessage)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            GeneratorContent(
                state = state,
                onEvent = onEvent,
                onNavigateBack = onNavigateBack,
                onNavigateToHealth = onNavigateToHealth,
                showBackButton = showBackButton,
                paddingValues = paddingValues,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeneratorContent(
    state: GeneratorViewModel.GeneratorState,
    onEvent: (GeneratorViewModel.GeneratorEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHealth: () -> Unit,
    showBackButton: Boolean,
    paddingValues: PaddingValues,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 760.dp)
            .fillMaxWidth()
            .fillMaxHeight()
            .scaffoldVerticalScroll(rememberScrollState(), paddingValues)
            .imePadding()
            .padding(
                start = ComponentSpacing.screenHorizontal,
                end = ComponentSpacing.screenHorizontal,
                top = if (showBackButton) ComponentSpacing.screenVertical else 0.dp,
                bottom = if (showBackButton) ComponentSpacing.screenVertical else 112.dp,
            ),
    ) {
        if (showBackButton) GeneratorBackBar(onNavigateBack)
        EditorialPageHeader(
            eyebrow = stringResource(Res.string.ui_encrypted_vault),
            title = stringResource(Res.string.ui_password_generator),
        )
        Spacer(modifier = Modifier.height(Spacing.lg))
        GeneratorModeSelector(state.isPassphraseMode, onEvent)
        Spacer(modifier = Modifier.height(24.dp))
        PasswordDisplay(
            password = state.generatedPassword,
            isGenerating = state.isGenerating,
            onCopy = { onEvent(GeneratorViewModel.GeneratorEvent.OnCopyClick) },
            onRefresh = { onEvent(GeneratorViewModel.GeneratorEvent.OnGenerateClick) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (state.generatedPassword.isNotEmpty()) {
            StrengthMeter(
                strength = state.passwordStrength,
                passwordLength = state.generatedPassword.codePointLength(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        UseGeneratedPasswordButton(
            enabled = state.generatedPassword.isNotEmpty(),
            onClick = { onEvent(GeneratorViewModel.GeneratorEvent.OnUseClick) },
        )
        Spacer(modifier = Modifier.height(32.dp))
        GeneratorOptionsPanel(
            state = state,
            onEvent = onEvent,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        PasswordHealthButton(onNavigateToHealth)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PasswordHealthButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Default.HealthAndSafety,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(stringResource(Res.string.ui_open_password_health))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeneratorBackBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {},
        windowInsets = passVaultScrollableTopAppBarInsets(),
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.ui_go_back),
                )
            }
        },
        colors = passVaultTopAppBarColors(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GeneratorModeSelector(
    isPassphraseMode: Boolean,
    onEvent: (GeneratorViewModel.GeneratorEvent) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        SegmentedButton(
            selected = !isPassphraseMode,
            onClick = {
                if (isPassphraseMode) {
                    onEvent(GeneratorViewModel.GeneratorEvent.OnPassphraseModeChanged)
                }
            },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
        ) {
            Text(stringResource(Res.string.ui_password))
        }
        SegmentedButton(
            selected = isPassphraseMode,
            onClick = {
                if (!isPassphraseMode) {
                    onEvent(GeneratorViewModel.GeneratorEvent.OnPassphraseModeChanged)
                }
            },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
        ) {
            Text(stringResource(Res.string.ui_passphrase))
        }
    }
}

@Composable
private fun UseGeneratedPasswordButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = MaterialTheme.shapes.large,
        enabled = enabled,
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(Res.string.ui_use_this_password),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
