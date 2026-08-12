@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.passvault.feature.health.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.platform.drawsBehindSystemBars
import com.passvault.core.designsystem.platform.passVaultScrollableTopAppBarInsets
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors
import com.passvault.core.designsystem.platform.scaffoldLazyViewport
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.feature.health.presentation.HealthViewModel
import com.passvault.feature.health.presentation.HealthViewModel.HealthTab
import org.jetbrains.compose.resources.stringResource

private val HealthContentMaxWidth = 920.dp

@Composable
fun HealthScreen(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val transientMessage = state.transientMessage?.resolve()
    LaunchedEffect(transientMessage) {
        transientMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(HealthViewModel.HealthEvent.OnDismissMessage)
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        HealthList(
            state = state,
            onEvent = onEvent,
            showBackButton = showBackButton,
            innerPadding = innerPadding,
        )
    }
    state.showingDuplicateGroup?.let { group ->
        DuplicateGroupDialog(
            group = group,
            onDismiss = { onEvent(HealthViewModel.HealthEvent.OnDismissDuplicateGroup) },
            onReviewFirst = {
                group.credentials.firstOrNull()?.let { first ->
                    onEvent(HealthViewModel.HealthEvent.OnFixWeakPasswordClick(first.credentialId))
                }
                onEvent(HealthViewModel.HealthEvent.OnDismissDuplicateGroup)
            },
        )
    }
}

@Composable
private fun HealthList(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
    showBackButton: Boolean,
    innerPadding: PaddingValues,
) {
    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    Box(
        modifier = Modifier.fillMaxSize().scaffoldLazyViewport(innerPadding),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier.widthIn(max = HealthContentMaxWidth).fillMaxSize(),
            contentPadding = healthListPadding(innerPadding, layoutDirection, showBackButton),
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
        ) {
            healthHeaderItems(state, onEvent, showBackButton)
            healthSelectedTabItems(state, onEvent)
        }
    }
}

private fun healthListPadding(
    scaffoldPadding: PaddingValues,
    layoutDirection: LayoutDirection,
    showBackButton: Boolean,
): PaddingValues {
    val top = if (showBackButton) ComponentSpacing.screenVertical else 0.dp
    val bottom = if (showBackButton) ComponentSpacing.screenVertical else 112.dp
    return if (drawsBehindSystemBars) {
        PaddingValues(
            start = scaffoldPadding.calculateStartPadding(layoutDirection) +
                ComponentSpacing.screenHorizontal,
            end = scaffoldPadding.calculateEndPadding(layoutDirection) +
                ComponentSpacing.screenHorizontal,
            top = scaffoldPadding.calculateTopPadding() + top,
            bottom = scaffoldPadding.calculateBottomPadding() + bottom,
        )
    } else {
        PaddingValues(
            start = ComponentSpacing.screenHorizontal,
            end = ComponentSpacing.screenHorizontal,
            top = top,
            bottom = bottom,
        )
    }
}

private fun LazyListScope.healthHeaderItems(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
    showBackButton: Boolean,
) {
    if (showBackButton) {
        item(key = "health-top-app-bar") {
            TopAppBar(
                title = {},
                windowInsets = passVaultScrollableTopAppBarInsets(),
                navigationIcon = {
                    IconButton(onClick = { onEvent(HealthViewModel.HealthEvent.OnBackClick) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.action_back),
                        )
                    }
                },
                actions = { HealthActions(state, onEvent) },
                colors = passVaultTopAppBarColors(),
            )
        }
    }
    item(key = "health-header") {
        EditorialPageHeader(
            eyebrow = stringResource(Res.string.ui_encrypted_vault),
            title = stringResource(Res.string.ui_password_health),
            modifier = Modifier.fillMaxWidth(),
            actions = { if (!showBackButton) HealthActions(state, onEvent) },
        )
    }
    item(key = "health-tabs") { HealthTabSelector(state = state, onEvent = onEvent) }
    state.errorMessage?.let { message ->
        item(key = "health-error") {
            ErrorBanner(
                message = message.resolve(),
                onRetry = { onEvent(HealthViewModel.HealthEvent.OnRefreshScan) },
            )
        }
    }
}

private fun LazyListScope.healthSelectedTabItems(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
) {
    if (state.isLoading && state.selectedTab != HealthTab.OVERVIEW) {
        item(key = "health-loading") { HealthLoadingState() }
        return
    }
    when (state.selectedTab) {
        HealthTab.OVERVIEW -> item(key = "health-overview") { OverviewContent(state, onEvent) }
        HealthTab.WEAK_PASSWORDS -> weakPasswordItems(state, onEvent)
        HealthTab.DUPLICATES -> duplicatePasswordItems(state, onEvent)
        HealthTab.OLD_PASSWORDS -> oldPasswordItems(state, onEvent)
    }
}

private fun LazyListScope.weakPasswordItems(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
) {
    if (state.weakPasswords.isEmpty()) {
        item(key = "health-weak-empty") {
            HealthEmptyState(
                title = stringResource(Res.string.ui_no_weak_passwords_found),
                message = stringResource(
                    Res.string.ui_the_local_strength_check_found_no_weak_passwords_among,
                ),
            )
        }
    } else {
        items(state.weakPasswords, key = { it.credentialId.value }) { item ->
            WeakPasswordCard(
                item = item,
                onFixClick = {
                    onEvent(
                        HealthViewModel.HealthEvent.OnFixWeakPasswordClick(item.credentialId),
                    )
                },
                onClick = {
                    onEvent(HealthViewModel.HealthEvent.OnCredentialClick(item.credentialId))
                },
            )
        }
    }
}

private fun LazyListScope.duplicatePasswordItems(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
) {
    if (state.duplicatePasswords.isEmpty()) {
        item(key = "health-duplicates-empty") {
            HealthEmptyState(
                title = stringResource(Res.string.ui_no_duplicate_groups_found),
                message = stringResource(
                    Res.string.ui_the_local_scan_found_no_identical_passwords_shared_by,
                ),
            )
        }
    } else {
        items(
            items = state.duplicatePasswords,
            key = { group -> group.credentials.first().credentialId.value },
        ) { group ->
            DuplicateGroupCard(
                group = group,
                onViewClick = { onEvent(HealthViewModel.HealthEvent.OnFixDuplicateClick(group)) },
            )
        }
    }
}

private fun LazyListScope.oldPasswordItems(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
) {
    if (state.oldPasswords.isEmpty()) {
        item(key = "health-old-empty") {
            HealthEmptyState(
                title = stringResource(Res.string.ui_no_old_passwords_found),
                message = stringResource(
                    Res.string.ui_no_scanned_password_is_known_to_be_at_least_365_days_o,
                ),
            )
        }
    } else {
        items(state.oldPasswords, key = { it.credentialId.value }) { item ->
            OldPasswordCard(
                item = item,
                onUpdateClick = {
                    onEvent(HealthViewModel.HealthEvent.OnFixOldPasswordClick(item.credentialId))
                },
            )
        }
    }
}

@Composable
private fun HealthActions(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
) {
    IconButton(
        onClick = { onEvent(HealthViewModel.HealthEvent.OnCopySummary) },
        enabled = !state.isLoading && state.lastScanAt != null,
    ) {
        Icon(
            Icons.Default.ContentCopy,
            contentDescription = stringResource(Res.string.ui_copy_health_summary),
        )
    }
    IconButton(
        onClick = { onEvent(HealthViewModel.HealthEvent.OnRefreshScan) },
        enabled = !state.isLoading,
    ) {
        Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.ui_scan_again))
    }
}
