package com.passvault.shared.navigation.adapters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.EntryProviderScope
import com.passvault.core.navigation.BackDisposition
import com.passvault.core.navigation.BackupRoute
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.PassVaultRouteKind
import com.passvault.core.navigation.SettingsRoute
import com.passvault.feature.backup.presentation.BackupViewModel
import com.passvault.feature.backup.ui.BackupScreen
import com.passvault.feature.backup.ui.ExportScreen
import com.passvault.feature.backup.ui.ImportScreen
import com.passvault.feature.settings.presentation.SettingsViewModel
import com.passvault.feature.settings.ui.AppearanceSettingsScreen
import com.passvault.feature.settings.ui.DataSettingsScreen
import com.passvault.feature.settings.ui.SecuritySettingsScreen
import com.passvault.feature.settings.ui.SettingsScreen
import com.passvault.shared.navigation.RegisterBackDisposition
import com.passvault.shared.navigation.RouteAdapterContext
import com.passvault.shared.navigation.checkExpected
import com.passvault.shared.navigation.entryNavigationToken

internal val settingsRouteAdapterKinds = setOf(
    PassVaultRouteKind.SETTINGS_ROOT,
    PassVaultRouteKind.SETTINGS_SECURITY,
    PassVaultRouteKind.SETTINGS_APPEARANCE,
    PassVaultRouteKind.SETTINGS_DATA,
    PassVaultRouteKind.BACKUP_ROOT,
    PassVaultRouteKind.BACKUP_EXPORT,
    PassVaultRouteKind.BACKUP_IMPORT,
)

internal fun EntryProviderScope<PassVaultRoute>.settingsRouteAdapters(context: RouteAdapterContext) {
    entry<SettingsRoute.Settings> { route -> SettingsRootEntry(context, route) }
    entry<SettingsRoute.Security> { route -> SecurityEntry(context, route) }
    entry<SettingsRoute.Appearance> { route -> AppearanceEntry(context, route) }
    entry<SettingsRoute.Data> { route -> DataEntry(context, route) }
    entry<BackupRoute.Backup> { route -> BackupEntry(context, route) }
    entry<BackupRoute.Export> { route -> ExportEntry(context, route) }
    entry<BackupRoute.Import> { route -> ImportEntry(context, route) }
}

@Composable
private fun SettingsRootEntry(context: RouteAdapterContext, route: SettingsRoute.Settings) {
    val state by context.settingsViewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    RegisterSettingsBack(context, token, state)
    SettingsScreen(
        state = state,
        onEvent = context.settingsViewModel::onEvent,
        showBackButton = false,
    )
}

@Composable
private fun SecurityEntry(context: RouteAdapterContext, route: SettingsRoute.Security) {
    val state by context.settingsViewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    RegisterSettingsBack(context, token, state)
    SecuritySettingsScreen(state = state, onEvent = context.settingsViewModel::onEvent)
}

@Composable
private fun AppearanceEntry(context: RouteAdapterContext, route: SettingsRoute.Appearance) {
    val state by context.settingsViewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    RegisterSettingsBack(context, token, state)
    AppearanceSettingsScreen(state = state, onEvent = context.settingsViewModel::onEvent)
}

@Composable
private fun DataEntry(context: RouteAdapterContext, route: SettingsRoute.Data) {
    val state by context.settingsViewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    RegisterSettingsBack(context, token, state)
    DataSettingsScreen(
        viewModel = context.settingsViewModel,
        onNavigateBack = { context.backCoordinator.requestBack() },
        onNavigateToExport = { context.push(BackupRoute.Export, token) },
        onNavigateToImport = { context.push(BackupRoute.Import, token) },
        onNavigateToBackup = { context.push(BackupRoute.Backup, token) },
    )
}

@Composable
private fun RegisterSettingsBack(
    context: RouteAdapterContext,
    token: com.passvault.core.navigation.NavigationToken,
    state: SettingsViewModel.SettingsState,
) {
    val localStateVisible = state.showChangePasswordDialog || state.infoDialogTitle != null ||
        state.infoDialogMessage != null || state.errorMessage != null
    val disposition = settingsBackDisposition(state, context.navigator.defaultBackDisposition())
    RegisterBackDisposition(
        coordinator = context.backCoordinator,
        token = token,
        disposition = disposition,
        handleInPlace = {
            if (localStateVisible) {
                context.settingsViewModel.onEvent(SettingsViewModel.SettingsEvent.OnBackClick)
            } else {
                context.navigator.handleDefaultInPlaceBack(token).checkExpected()
            }
        },
        blocksForwardNavigation = localStateVisible || disposition == BackDisposition.Blocked,
    )
}

@Composable
private fun BackupEntry(context: RouteAdapterContext, route: BackupRoute.Backup) {
    val state by context.backupViewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    RegisterBackupBack(context, token, state)
    LaunchedEffect(context.backupViewModel) { context.backupViewModel.refresh() }
    BackupScreen(state = state, onEvent = context.backupViewModel::onEvent)
}

@Composable
private fun ExportEntry(context: RouteAdapterContext, route: BackupRoute.Export) {
    val state by context.backupViewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    RegisterBackupBack(context, token, state)
    LaunchedEffect(context.backupViewModel) { context.backupViewModel.refresh() }
    ExportScreen(
        viewModel = context.backupViewModel,
        onNavigateBack = { context.backupViewModel.onEvent(BackupViewModel.BackupEvent.OnBackClick) },
    )
}

@Composable
private fun ImportEntry(context: RouteAdapterContext, route: BackupRoute.Import) {
    val state by context.backupViewModel.state.collectAsState()
    val token = entryNavigationToken(context.navigator, route)
    RegisterBackupBack(context, token, state)
    LaunchedEffect(context.backupViewModel) { context.backupViewModel.refresh() }
    ImportScreen(
        viewModel = context.backupViewModel,
        onNavigateBack = { context.backupViewModel.onEvent(BackupViewModel.BackupEvent.OnBackClick) },
    )
}

@Composable
private fun RegisterBackupBack(
    context: RouteAdapterContext,
    token: com.passvault.core.navigation.NavigationToken,
    state: BackupViewModel.BackupState,
) {
    RegisterBackDisposition(
        coordinator = context.backCoordinator,
        token = token,
        disposition = backupBackDisposition(state),
        handleInPlace = {
            context.backupViewModel.onEvent(BackupViewModel.BackupEvent.OnRestoreCancelClick)
        },
        beforePop = context.backupViewModel::clearForLock,
        blocksForwardNavigation = state.hasActiveOperation || state.showRestoreConfirmation,
    )
}

internal fun settingsBackDisposition(
    state: SettingsViewModel.SettingsState,
    defaultDisposition: BackDisposition,
): BackDisposition = when {
    state.isChangingPassword || state.isLockingVault || state.isBiometricLoading -> BackDisposition.Blocked
    state.showChangePasswordDialog || state.infoDialogTitle != null || state.infoDialogMessage != null ||
        state.errorMessage != null -> BackDisposition.HandleInPlace
    else -> defaultDisposition
}

internal fun backupBackDisposition(state: BackupViewModel.BackupState): BackDisposition = when {
    state.hasActiveOperation -> BackDisposition.Blocked
    state.showRestoreConfirmation -> BackDisposition.HandleInPlace
    else -> BackDisposition.PopNow
}

@Composable
internal fun ObserveNavigationFeatureEffects(context: RouteAdapterContext) {
    LaunchedEffect(context.settingsViewModel, context.navigator) {
        context.settingsViewModel.effect.collect { effect ->
            if (context.navigator.state.currentRoute() !is SettingsRoute) return@collect
            val token = context.navigator.currentToken()
            when (effect) {
                SettingsViewModel.SettingsEffect.NavigateBack -> context.backCoordinator.requestBack()
                SettingsViewModel.SettingsEffect.NavigateToSecurity -> context.push(SettingsRoute.Security, token)
                SettingsViewModel.SettingsEffect.NavigateToAppearance -> context.push(SettingsRoute.Appearance, token)
                SettingsViewModel.SettingsEffect.NavigateToData -> context.push(SettingsRoute.Data, token)
                SettingsViewModel.SettingsEffect.LockVault -> Unit
                SettingsViewModel.SettingsEffect.ShowExportDialog -> context.push(BackupRoute.Export, token)
                SettingsViewModel.SettingsEffect.ShowImportDialog -> context.push(BackupRoute.Import, token)
                SettingsViewModel.SettingsEffect.ShowBackupDialog -> context.push(BackupRoute.Backup, token)
            }
        }
    }
    LaunchedEffect(context.backupViewModel, context.navigator) {
        context.backupViewModel.effect.collect { effect ->
            if (context.navigator.state.currentRoute() !is BackupRoute) return@collect
            when (effect) {
                BackupViewModel.BackupEffect.NavigateBack -> context.popAfterGuard(context.navigator.currentToken())
                BackupViewModel.BackupEffect.ShowImportSuccess -> context.navigator.requireAuthentication()
            }
        }
    }
}
