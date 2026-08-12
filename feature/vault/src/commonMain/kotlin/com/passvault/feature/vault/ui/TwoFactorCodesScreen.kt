package com.passvault.feature.vault.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.components.EditorialStatusBanner
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.platform.passVaultScrollableTopAppBarInsets
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.vault.presentation.TwoFactorCodesViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorCodesScreen(
    state: TwoFactorCodesViewModel.TwoFactorCodesState,
    onEvent: (TwoFactorCodesViewModel.TwoFactorCodesEvent) -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = state.errorMessage?.resolve()
    val statusMessage = state.statusMessage?.resolve()

    DisposableEffect(Unit) {
        onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnScreenVisible)
        onDispose { onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnScreenHidden) }
    }
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
        onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnDismissMessage)
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showBackButton) {
                TopAppBar(
                    title = {},
                    windowInsets = passVaultScrollableTopAppBarInsets(),
                    navigationIcon = {
                        IconButton(
                            onClick = { onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnBackClick) },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.ui_go_back),
                            )
                        }
                    },
                    colors = passVaultTopAppBarColors(),
                )
            }
        },
    ) { paddingValues ->
        TwoFactorCodesContent(
            state = state,
            onEvent = onEvent,
            showBackButton = showBackButton,
            paddingValues = paddingValues,
        )
    }
}

@Composable
private fun TwoFactorCodesContent(
    state: TwoFactorCodesViewModel.TwoFactorCodesState,
    onEvent: (TwoFactorCodesViewModel.TwoFactorCodesEvent) -> Unit,
    showBackButton: Boolean,
    paddingValues: PaddingValues,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = ComponentSpacing.contentMaxWidth)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = ComponentSpacing.screenHorizontal,
                top = paddingValues.calculateTopPadding(),
                end = ComponentSpacing.screenHorizontal,
                bottom = twoFactorCodesBottomPadding(showBackButton),
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                TwoFactorCodesHeader(state.isLoading, onEvent)
            }
            item {
                Text(
                    text = stringResource(Res.string.ui_two_factor_codes_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item { TwoFactorCodesStatus(state, onEvent) }
            items(
                items = state.items,
                key = { it.credentialId.value },
            ) { item ->
                TwoFactorCodeCard(item = item, onEvent = onEvent)
            }
        }
    }
}

private fun twoFactorCodesBottomPadding(showBackButton: Boolean) =
    if (showBackButton) ComponentSpacing.screenVertical else 112.dp

@Composable
private fun TwoFactorCodesHeader(
    isLoading: Boolean,
    onEvent: (TwoFactorCodesViewModel.TwoFactorCodesEvent) -> Unit,
) {
    EditorialPageHeader(
        eyebrow = stringResource(Res.string.ui_encrypted_vault),
        title = stringResource(Res.string.ui_two_factor_codes),
        actions = {
            IconButton(
                onClick = { onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnRefresh) },
                enabled = !isLoading,
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(Res.string.ui_refresh_two_factor_codes),
                )
            }
        },
    )
}

@Composable
private fun TwoFactorCodesStatus(
    state: TwoFactorCodesViewModel.TwoFactorCodesState,
    onEvent: (TwoFactorCodesViewModel.TwoFactorCodesEvent) -> Unit,
) {
    when {
        state.isLoading -> LoadingTwoFactorCodesPanel()
        state.loadFailed -> FailedTwoFactorCodesPanel(onEvent)
        state.items.isEmpty() -> EmptyTwoFactorCodesPanel()
    }
}

@Composable
private fun LoadingTwoFactorCodesPanel() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FailedTwoFactorCodesPanel(
    onEvent: (TwoFactorCodesViewModel.TwoFactorCodesEvent) -> Unit,
) {
    EditorialStatusBanner(
        icon = Icons.Default.Warning,
        title = stringResource(Res.string.error_state_title),
        message = stringResource(Res.string.error_two_factor_codes_load),
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        action = {
            TextButton(
                onClick = { onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnRefresh) },
            ) {
                Text(stringResource(Res.string.action_retry))
            }
        },
    )
}

@Composable
private fun EmptyTwoFactorCodesPanel() {
    EditorialPanel(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Spacing.xl),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(Res.string.ui_no_two_factor_codes),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(Res.string.ui_add_authenticator_to_credential_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TwoFactorCodeCard(
    item: TwoFactorCodesViewModel.TwoFactorCodeItem,
    onEvent: (TwoFactorCodesViewModel.TwoFactorCodesEvent) -> Unit,
) {
    Card(
        onClick = {
            onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnCredentialClick(item.credentialId))
        },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            TwoFactorCodeHeader(item)
            if (item.generationFailed) {
                Text(
                    text = stringResource(Res.string.ui_totp_code_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                TwoFactorCodeValue(item, onEvent)
            }
        }
    }
}

@Composable
private fun TwoFactorCodeHeader(item: TwoFactorCodesViewModel.TwoFactorCodeItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, style = MaterialTheme.typography.titleMedium)
            item.accountSubtitle()?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(Res.string.ui_open_credential),
        )
    }
}

@Composable
private fun TwoFactorCodeValue(
    item: TwoFactorCodesViewModel.TwoFactorCodeItem,
    onEvent: (TwoFactorCodesViewModel.TwoFactorCodesEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = formatTotpCode(item.code),
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.width(Spacing.sm))
        IconButton(
            onClick = {
                onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnCopyCodeClick(item.credentialId))
            },
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(Res.string.ui_copy_verification_code),
            )
        }
    }
    LinearProgressIndicator(
        progress = { item.progress },
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = pluralStringResource(
            Res.plurals.ui_totp_expires_in_seconds,
            item.secondsRemaining,
            item.secondsRemaining,
        ),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun TwoFactorCodesViewModel.TwoFactorCodeItem.accountSubtitle(): String? =
    listOfNotNull(issuer, accountName)
        .filter(String::isNotBlank)
        .distinct()
        .joinToString(" · ")
        .ifBlank { displayUsername.orEmpty() }
        .ifBlank { null }

internal fun formatTotpCode(code: String): String = when {
    code.length == 6 -> "${code.take(3)} ${code.drop(3)}"
    code.length == 8 -> "${code.take(4)} ${code.drop(4)}"
    else -> code
}
