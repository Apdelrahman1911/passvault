package com.passvault.shared.navigation.adapters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation3.runtime.EntryProviderScope
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.repository.lockWithBoundedRetry
import com.passvault.core.navigation.BackDisposition
import com.passvault.core.navigation.NavigationToken
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.PassVaultRouteKind
import com.passvault.core.navigation.TopLevelDestination
import com.passvault.core.navigation.VaultRoute
import com.passvault.feature.credential.presentation.CredentialViewModel
import com.passvault.feature.credential.ui.CredentialDetailScreen
import com.passvault.feature.credential.ui.CredentialEditScreen
import com.passvault.feature.vault.presentation.VaultViewModel
import com.passvault.feature.vault.ui.VaultScreen
import com.passvault.shared.navigation.RegisterBackDisposition
import com.passvault.shared.navigation.RouteAdapterContext
import com.passvault.shared.navigation.checkExpected
import com.passvault.shared.navigation.entryNavigationToken
import org.koin.compose.viewmodel.koinViewModel

internal val vaultRouteAdapterKinds = setOf(
    PassVaultRouteKind.VAULT_ROOT,
    PassVaultRouteKind.VAULT_CREDENTIAL_DETAIL,
    PassVaultRouteKind.VAULT_CREDENTIAL_CREATE,
    PassVaultRouteKind.VAULT_CREDENTIAL_EDIT,
)

internal fun EntryProviderScope<PassVaultRoute>.vaultRouteAdapters(context: RouteAdapterContext) {
    entry<VaultRoute.Vault> { route -> VaultRootEntry(context, route) }
    entry<VaultRoute.CredentialDetail> { route -> CredentialDetailEntry(context, route) }
    entry<VaultRoute.CredentialCreate> { route -> CredentialCreateEntry(context, route) }
    entry<VaultRoute.CredentialEdit> { route -> CredentialEditEntry(context, route) }
}

@Composable
private fun VaultRootEntry(context: RouteAdapterContext, route: VaultRoute.Vault) {
    val state by context.vaultViewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    val disposition = vaultBackDisposition(state, context.navigator.defaultBackDisposition())
    RegisterBackDisposition(
        coordinator = context.backCoordinator,
        token = token,
        disposition = disposition,
        handleInPlace = {
            when {
                state.showNewFolderDialog ->
                    context.vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnDismissNewFolder)
                state.folderPendingDeletion != null ->
                    context.vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnDismissDeleteFolder)
                state.isSearchActive ->
                    context.vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnSearchDismiss)
            }
        },
        blocksForwardNavigation = disposition == BackDisposition.HandleInPlace,
    )
    ObserveVaultEffects(context, token)
    VaultScreen(
        state = state,
        onEvent = context.vaultViewModel::onEvent,
        showActionDock = false,
    )
}

@Composable
private fun CredentialDetailEntry(context: RouteAdapterContext, route: VaultRoute.CredentialDetail) {
    val viewModel: CredentialViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    RegisterCredentialBack(context, token, viewModel, credentialBackDisposition(state))
    ObserveCredentialEffects(
        context = context,
        viewModel = viewModel,
        token = token,
        onNavigateBack = {
            context.vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnRefresh)
            context.popAfterGuard(token)
        },
    )
    LaunchedEffect(viewModel, route.credentialId) {
        viewModel.loadCredential(CredentialId(route.credentialId))
    }
    CredentialDetailScreen(
        state = state,
        credentialId = CredentialId(route.credentialId),
        onEvent = viewModel::onEvent,
        onNavigateToEdit = { id -> context.push(VaultRoute.CredentialEdit(id.value), token) },
    )
}

@Composable
private fun CredentialCreateEntry(context: RouteAdapterContext, route: VaultRoute.CredentialCreate) {
    val viewModel: CredentialViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    RegisterCredentialBack(context, token, viewModel, credentialBackDisposition(state))
    ObserveCredentialEffects(
        context = context,
        viewModel = viewModel,
        token = token,
        onSaveCompleted = { id ->
            context.vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnRefresh)
            if (id == null) {
                context.popAfterGuard(token)
            } else {
                context.replaceCurrent(VaultRoute.CredentialDetail(id.value), token)
            }
        },
    )
    LaunchedEffect(viewModel, route.folderId) {
        viewModel.createNewCredential(CredentialType.Login)
        route.folderId?.let { folderId ->
            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnFolderChanged(folderId))
        }
    }
    CredentialEditScreen(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun CredentialEditEntry(context: RouteAdapterContext, route: VaultRoute.CredentialEdit) {
    val viewModel: CredentialViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    RegisterCredentialBack(context, token, viewModel, credentialBackDisposition(state))
    ObserveCredentialEffects(
        context = context,
        viewModel = viewModel,
        token = token,
        onSaveCompleted = {
            context.vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnRefresh)
            context.popThenEnsure(VaultRoute.CredentialDetail(route.credentialId), token)
        },
    )
    LaunchedEffect(viewModel, route.credentialId) {
        viewModel.loadCredential(CredentialId(route.credentialId))
    }
    CredentialEditScreen(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun ObserveVaultEffects(context: RouteAdapterContext, token: NavigationToken) {
    LaunchedEffect(context.vaultViewModel, token) {
        context.vaultViewModel.effect.collect { effect ->
            if (!context.navigator.isCurrent(token)) return@collect
            when (effect) {
                is VaultViewModel.VaultEffect.NavigateToCredentialDetail ->
                    context.openCredential(effect.credentialId, token)
                is VaultViewModel.VaultEffect.NavigateToCredentialEdit -> {
                    val route = effect.credentialId?.let { id -> VaultRoute.CredentialEdit(id.value) }
                        ?: VaultRoute.CredentialCreate()
                    context.push(route, token)
                }
                VaultViewModel.VaultEffect.NavigateToSettings ->
                    selectTab(context, TopLevelDestination.SETTINGS, token)
                VaultViewModel.VaultEffect.NavigateToGenerator ->
                    selectTab(context, TopLevelDestination.GENERATOR, token)
                VaultViewModel.VaultEffect.NavigateToTwoFactorCodes ->
                    selectTab(context, TopLevelDestination.TWO_FACTOR_CODES, token)
                VaultViewModel.VaultEffect.LockVault -> if (!context.vaultRepository.lockWithBoundedRetry()) {
                    context.vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnLockFailed)
                }
            }
        }
    }
}

@Composable
private fun ObserveCredentialEffects(
    context: RouteAdapterContext,
    viewModel: CredentialViewModel,
    token: NavigationToken,
    onNavigateBack: () -> Unit = { context.popAfterGuard(token) },
    onSaveCompleted: (CredentialId?) -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(viewModel, token, uriHandler) {
        viewModel.effect.collect { effect ->
            if (!context.navigator.isCurrent(token)) return@collect
            when (effect) {
                CredentialViewModel.CredentialEffect.NavigateBack -> onNavigateBack()
                is CredentialViewModel.CredentialEffect.SaveCompleted -> onSaveCompleted(effect.credentialId)
                is CredentialViewModel.CredentialEffect.CopyToClipboard -> {
                    val copied = context.copySensitive(effect.text)
                    viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyResult(copied))
                }
                is CredentialViewModel.CredentialEffect.LaunchUrl -> {
                    // Intentional OS trust-boundary crossing. The credential router emits only an HTTP(S)
                    // URL accepted by normalizeCredentialUrl; the receiving handler may retain or resolve it.
                    val opened = try {
                        uriHandler.openUri(effect.url)
                        true
                    } catch (_: Exception) {
                        false
                    }
                    viewModel.onEvent(CredentialViewModel.CredentialEvent.OnUrlLaunchResult(opened))
                }
            }
        }
    }
}

@Composable
private fun RegisterCredentialBack(
    context: RouteAdapterContext,
    token: NavigationToken,
    viewModel: CredentialViewModel,
    disposition: BackDisposition,
) {
    RegisterBackDisposition(
        coordinator = context.backCoordinator,
        token = token,
        disposition = disposition,
        handleInPlace = { viewModel.onEvent(CredentialViewModel.CredentialEvent.OnBackClick) },
        blocksForwardNavigation = disposition != BackDisposition.PopNow,
    )
}

internal fun credentialBackDisposition(state: CredentialViewModel.CredentialState): BackDisposition = when {
    state.isBusy -> BackDisposition.Blocked
    state.showTotpScanner || state.showTotpReplaceConfirmation || state.showTotpRemoveConfirmation ||
        state.showDeleteConfirmation || state.attachmentRenameTarget != null ||
        state.attachmentDeleteTarget != null || state.showDiscardConfirmation || state.isDirty ->
        BackDisposition.HandleInPlace
    else -> BackDisposition.PopNow
}

internal fun vaultBackDisposition(
    state: VaultViewModel.VaultState,
    defaultDisposition: BackDisposition,
): BackDisposition = if (
    state.showNewFolderDialog || state.folderPendingDeletion != null || state.isSearchActive
) {
    BackDisposition.HandleInPlace
} else {
    defaultDisposition
}

private fun selectTab(
    context: RouteAdapterContext,
    destination: TopLevelDestination,
    token: NavigationToken,
) {
    if (!context.backCoordinator.canLeaveForForwardNavigation()) return
    context.navigator.selectTab(destination, token).checkExpected()
}
