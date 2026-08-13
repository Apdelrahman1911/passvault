package com.passvault.feature.vault.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialStatusBanner
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.Breakpoints
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.vault.presentation.VaultViewModel
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun VaultScreen(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
    showActionDock: Boolean = true,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compact = maxWidth < Breakpoints.expandedMin
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (compact && showActionDock) {
                    VaultActionDock(
                        onAddClick = {
                            onEvent(VaultViewModel.VaultEvent.OnAddCredentialClick)
                        },
                        onGeneratorClick = {
                            onEvent(VaultViewModel.VaultEvent.OnGeneratorClick)
                        },
                        onTwoFactorCodesClick = {
                            onEvent(VaultViewModel.VaultEvent.OnTwoFactorCodesClick)
                        },
                        onSettingsClick = {
                            onEvent(VaultViewModel.VaultEvent.OnSettingsClick)
                        },
                    )
                }
            },
        ) { padding ->
            VaultScreenContent(
                state = state,
                compact = compact,
                onEvent = onEvent,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun VaultScreenContent(
    state: VaultViewModel.VaultState,
    compact: Boolean,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        VaultHeaderContainer(state, compact, onEvent)
        VaultErrorBanner(state, onEvent)
        CredentialListContainer(state, compact, onEvent)
    }
}

@Composable
private fun VaultHeaderContainer(
    state: VaultViewModel.VaultState,
    compact: Boolean,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = ComponentSpacing.screenHorizontal,
                end = ComponentSpacing.screenHorizontal,
                top = if (compact) 0.dp else ComponentSpacing.screenVertical,
                bottom = Spacing.sm,
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        VaultHeader(
            state = state,
            compact = compact,
            onEvent = onEvent,
            modifier = Modifier
                .widthIn(max = ComponentSpacing.contentMaxWidth)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun ColumnScope.VaultErrorBanner(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    val error = state.errorMessage ?: return
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
            VaultErrorAction(
                canRetry = state.canRetryLoad,
                onEvent = onEvent,
            )
        },
    )
}

@Composable
private fun VaultErrorAction(
    canRetry: Boolean,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    TextButton(
        onClick = {
            onEvent(
                if (canRetry) {
                    VaultViewModel.VaultEvent.OnRefresh
                } else {
                    VaultViewModel.VaultEvent.OnDismissError
                },
            )
        },
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
    ) {
        Text(
            stringResource(
                if (canRetry) Res.string.action_retry else Res.string.ui_dismiss,
            ),
        )
    }
}

@Composable
private fun ColumnScope.CredentialListContainer(
    state: VaultViewModel.VaultState,
    compact: Boolean,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.TopCenter,
    ) {
        CredentialListScreen(
            state = state,
            onEvent = onEvent,
            modifier = Modifier
                .widthIn(max = ComponentSpacing.contentMaxWidth)
                .fillMaxWidth()
                .fillMaxHeight(),
            isCompact = compact,
        )
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
        VaultSearchHeader(
            query = state.searchQuery,
            onQueryChanged = {
                onEvent(VaultViewModel.VaultEvent.OnSearchQueryChanged(it))
            },
            onDismiss = { onEvent(VaultViewModel.VaultEvent.OnSearchDismiss) },
            modifier = modifier,
        )
    } else {
        VaultEditorialHeader(state, compact, onEvent, modifier)
    }
}

@Composable
private fun VaultSearchHeader(
    query: String,
    onQueryChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text(stringResource(Res.string.ui_search_credentials)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onDismiss) {
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
}

@Composable
private fun VaultEditorialHeader(
    state: VaultViewModel.VaultState,
    compact: Boolean,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
    modifier: Modifier,
) {
    EditorialPageHeader(
        eyebrow = stringResource(Res.string.ui_encrypted_vault),
        title = stringResource(Res.string.ui_passvault),
        subtitle = pluralStringResource(
            Res.plurals.ui_credential_count,
            state.credentialCount,
            state.credentialCount,
        ),
        modifier = modifier,
        actions = { VaultHeaderActions(compact, onEvent) },
    )
}

@Composable
private fun VaultHeaderActions(
    compact: Boolean,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
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
            icon = Icons.Default.Key,
            contentDescription = stringResource(Res.string.ui_two_factor_codes),
            onClick = { onEvent(VaultViewModel.VaultEvent.OnTwoFactorCodesClick) },
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
    onTwoFactorCodesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHomeClick: () -> Unit = {},
    homeSelected: Boolean = true,
    generatorSelected: Boolean = false,
    twoFactorCodesSelected: Boolean = false,
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
                DockAction(
                    icon = Icons.Default.Home,
                    contentDescription = stringResource(Res.string.ui_home),
                    selected = homeSelected,
                    onClick = onHomeClick,
                )
                DockAction(
                    icon = Icons.Default.Password,
                    contentDescription = stringResource(Res.string.ui_password_generator),
                    selected = generatorSelected,
                    onClick = onGeneratorClick,
                )
                DockAddAction(onClick = onAddClick)
                DockAction(
                    icon = Icons.Default.Key,
                    contentDescription = stringResource(Res.string.ui_two_factor_codes),
                    selected = twoFactorCodesSelected,
                    onClick = onTwoFactorCodesClick,
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
private fun DockAddAction(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(ComponentSpacing.touchTargetMin),
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
}

@Composable
private fun DockAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(ComponentSpacing.touchTargetMin)
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick,
            ),
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
