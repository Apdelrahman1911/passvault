package com.passvault.feature.vault.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialStatusBanner
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.Breakpoints
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.domain.model.CredentialId
import com.passvault.feature.vault.presentation.VaultViewModel
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun VaultScreenRoute(
    viewModel: VaultViewModel,
    onNavigateToCredential: (CredentialId) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToGenerator: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLock: () -> Unit,
    showActionDock: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is VaultViewModel.VaultEffect.NavigateToCredentialDetail ->
                    onNavigateToCredential(effect.credentialId)
                is VaultViewModel.VaultEffect.NavigateToCredentialEdit -> {
                    if (effect.credentialId == null) onNavigateToCreate()
                    else onNavigateToCredential(effect.credentialId)
                }
                VaultViewModel.VaultEffect.NavigateToSettings -> onNavigateToSettings()
                VaultViewModel.VaultEffect.NavigateToGenerator -> onNavigateToGenerator()
                VaultViewModel.VaultEffect.NavigateToHealth -> onNavigateToHealth()
                VaultViewModel.VaultEffect.LockVault -> onLock()
                is VaultViewModel.VaultEffect.NavigateToSearch,
                is VaultViewModel.VaultEffect.NavigateToFolder,
                is VaultViewModel.VaultEffect.NavigateToTag,
                -> Unit
            }
        }
    }

    VaultScreen(
        state = state,
        onEvent = viewModel::onEvent,
        showActionDock = showActionDock,
        modifier = modifier,
    )
}

@Composable
fun VaultScreen(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
    showActionDock: Boolean = true,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compact = maxWidth < Breakpoints.mediumMin
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (compact && showActionDock) {
                    VaultActionDock(
                        onAddClick = { onEvent(VaultViewModel.VaultEvent.OnAddCredentialClick) },
                        onGeneratorClick = { onEvent(VaultViewModel.VaultEvent.OnGeneratorClick) },
                        onHealthClick = { onEvent(VaultViewModel.VaultEvent.OnHealthClick) },
                        onSettingsClick = { onEvent(VaultViewModel.VaultEvent.OnSettingsClick) },
                    )
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = ComponentSpacing.screenHorizontal,
                            end = ComponentSpacing.screenHorizontal,
                            top = ComponentSpacing.screenVertical,
                            bottom = Spacing.sm,
                        ),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    VaultHeader(
                        state = state,
                        compact = compact,
                        onEvent = onEvent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = ComponentSpacing.contentMaxWidth),
                    )
                }

                state.errorMessage?.let { error ->
                    EditorialStatusBanner(
                        icon = Icons.Default.Warning,
                        title = stringResource(Res.string.ui_passvault),
                        message = error.resolve(),
                        modifier = Modifier
                            .padding(
                                horizontal = ComponentSpacing.screenHorizontal,
                                vertical = Spacing.sm,
                            )
                            .widthIn(max = 760.dp)
                            .align(Alignment.CenterHorizontally),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        action = {
                            TextButton(
                                onClick = {
                                    onEvent(VaultViewModel.VaultEvent.OnDismissError)
                                },
                            ) {
                                Text(stringResource(Res.string.ui_dismiss))
                            }
                        },
                    )
                }

                CredentialListScreen(
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxSize(),
                    isCompact = compact,
                )
            }
        }
    }
}

@Composable
private fun VaultHeader(
    state: VaultViewModel.VaultState,
    compact: Boolean,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isSearchActive) {
        TextField(
            value = state.searchQuery,
            onValueChange = {
                onEvent(VaultViewModel.VaultEvent.OnSearchQueryChanged(it))
            },
            placeholder = { Text(stringResource(Res.string.ui_search_credentials)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        onEvent(VaultViewModel.VaultEvent.OnSearchDismiss)
                    },
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(Res.string.ui_close_search),
                    )
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = modifier,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
        )
        return
    }

    EditorialPageHeader(
        eyebrow = stringResource(Res.string.ui_encrypted_vault),
        title = stringResource(Res.string.ui_passvault),
        subtitle = pluralStringResource(
            Res.plurals.ui_credential_count,
            state.credentialCount,
            state.credentialCount,
        ),
        modifier = modifier,
        actions = {
            HeaderAction(
                icon = Icons.Default.Search,
                contentDescription = stringResource(Res.string.ui_search_credentials),
                onClick = { onEvent(VaultViewModel.VaultEvent.OnSearchClick) },
            )
            if (!compact) {
                HeaderAction(
                    icon = Icons.Default.Password,
                    contentDescription = stringResource(Res.string.ui_password_generator),
                    onClick = { onEvent(VaultViewModel.VaultEvent.OnGeneratorClick) },
                )
                HeaderAction(
                    icon = Icons.Default.HealthAndSafety,
                    contentDescription = stringResource(Res.string.ui_password_health),
                    onClick = { onEvent(VaultViewModel.VaultEvent.OnHealthClick) },
                )
                HeaderAction(
                    icon = Icons.Default.Settings,
                    contentDescription = stringResource(Res.string.action_settings),
                    onClick = { onEvent(VaultViewModel.VaultEvent.OnSettingsClick) },
                )
                Button(
                    onClick = { onEvent(VaultViewModel.VaultEvent.OnAddCredentialClick) },
                    modifier = Modifier.heightIn(min = 48.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(
                        stringResource(Res.string.ui_add_credential),
                        modifier = Modifier.padding(start = Spacing.sm),
                    )
                }
            }
            HeaderAction(
                icon = Icons.Default.Lock,
                contentDescription = stringResource(Res.string.ui_lock_vault),
                onClick = { onEvent(VaultViewModel.VaultEvent.OnLockClick) },
            )
        },
    )
}

@Composable
private fun HeaderAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}

@Composable
fun VaultActionDock(
    onAddClick: () -> Unit,
    onGeneratorClick: () -> Unit,
    onHealthClick: () -> Unit,
    onSettingsClick: () -> Unit,
    generatorSelected: Boolean = false,
    healthSelected: Boolean = false,
    settingsSelected: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface,
            shape = CircleShape,
            shadowElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier.padding(Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onAddClick,
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    contentColor = MaterialTheme.colorScheme.inverseSurface,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(Res.string.ui_add_credential),
                        )
                    }
                }
                DockAction(
                    icon = Icons.Default.Password,
                    contentDescription = stringResource(Res.string.ui_password_generator),
                    selected = generatorSelected,
                    onClick = onGeneratorClick,
                )
                DockAction(
                    icon = Icons.Default.HealthAndSafety,
                    contentDescription = stringResource(Res.string.ui_password_health),
                    selected = healthSelected,
                    onClick = onHealthClick,
                )
                DockAction(
                    icon = Icons.Default.Settings,
                    contentDescription = stringResource(Res.string.action_settings),
                    selected = settingsSelected,
                    onClick = onSettingsClick,
                )
            }
        }
    }
}

@Composable
private fun DockAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(52.dp),
        shape = CircleShape,
        color = if (selected) {
            MaterialTheme.colorScheme.inverseOnSurface
        } else {
            androidx.compose.ui.graphics.Color.Transparent
        },
        contentColor = if (selected) {
            MaterialTheme.colorScheme.inverseSurface
        } else {
            MaterialTheme.colorScheme.inverseOnSurface
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}
