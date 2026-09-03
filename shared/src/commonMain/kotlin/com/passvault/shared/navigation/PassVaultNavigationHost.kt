package com.passvault.shared.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.domain.repository.FolderRepository
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.domain.repository.lockWithBoundedRetry
import com.passvault.core.navigation.AppCommand
import com.passvault.core.navigation.AppCommandDispatcher
import com.passvault.core.navigation.AuthRoute
import com.passvault.core.navigation.BackDisposition
import com.passvault.core.navigation.BackupRoute
import com.passvault.core.navigation.ExternalNavigationDispatcher
import com.passvault.core.navigation.ExternalNavigationParseResult
import com.passvault.core.navigation.ExternalNavigationParser
import com.passvault.core.navigation.GeneratorRoute
import com.passvault.core.navigation.HealthRoute
import com.passvault.core.navigation.NavigationRoot
import com.passvault.core.navigation.NavigationToken
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.SettingsRoute
import com.passvault.core.navigation.TopLevelDestination
import com.passvault.core.navigation.VaultRoute
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.VaultUiSecurityCoordinator
import com.passvault.feature.backup.presentation.BackupViewModel
import com.passvault.feature.onboarding.presentation.OnboardingViewModel
import com.passvault.feature.settings.presentation.SettingsViewModel
import com.passvault.feature.unlock.presentation.UnlockViewModel
import com.passvault.feature.vault.presentation.VaultViewModel
import com.passvault.shared.SessionCleanupObservation
import com.passvault.shared.VaultTabShell
import com.passvault.shared.clearForLockTransition
import com.passvault.shared.platform.platformNavigationTransitionSpecs
import com.passvault.shared.platform.preservesSensitiveClipboardOnBackgroundLock
import com.passvault.shared.security.AutoLockTimer
import com.passvault.shared.security.UserActivitySignal
import com.passvault.shared.security.recordUserActivity
import com.passvault.shared.sessionCleanupPolicy
import com.passvault.shared.shouldAcknowledgeVaultUiSecurity
import com.passvault.shared.shouldGuardUnlockedRoutes
import com.passvault.shared.toSessionCleanupObservation
import com.passvault.shared.navigation.adapters.ObserveNavigationFeatureEffects
import com.passvault.shared.navigation.adapters.ObserveAuthenticationNavigationEffects
import com.passvault.shared.navigation.adapters.authRouteAdapters
import com.passvault.shared.navigation.adapters.settingsRouteAdapters
import com.passvault.shared.navigation.adapters.toolsRouteAdapters
import com.passvault.shared.navigation.adapters.vaultRouteAdapters
import kotlinx.coroutines.CancellationException
import org.koin.compose.koinInject

private data class NavigationHostRuntime(
    val context: RouteAdapterContext,
    val validator: RestoredNavigationValidator,
    val commandDispatcher: AppCommandDispatcher,
    val externalDispatcher: ExternalNavigationDispatcher,
)

@Composable
private fun rememberNavigationHostRuntime(
    composition: NavigationComposition,
    vaultRepository: VaultRepository,
    clipboardService: ClipboardService,
): NavigationHostRuntime {
    val commandDispatcher: AppCommandDispatcher = koinInject()
    val externalDispatcher: ExternalNavigationDispatcher = koinInject()
    val vaultViewModel: VaultViewModel = koinInject()
    val settingsViewModel: SettingsViewModel = koinInject()
    val backupViewModel: BackupViewModel = koinInject()
    val onboardingViewModel: OnboardingViewModel = koinInject()
    val unlockViewModel: UnlockViewModel = koinInject()
    val credentialRepository: CredentialRepository = koinInject()
    val folderRepository: FolderRepository = koinInject()
    val validator = remember(credentialRepository, folderRepository) {
        RestoredNavigationValidator(credentialRepository, folderRepository)
    }
    val backCoordinator = remember(composition.navigator) {
        NavigationBackCoordinator(composition.navigator)
    }
    val context = remember(
        composition.navigator,
        backCoordinator,
        vaultRepository,
        clipboardService,
        vaultViewModel,
        settingsViewModel,
        backupViewModel,
        onboardingViewModel,
        unlockViewModel,
    ) {
        RouteAdapterContext(
            navigator = composition.navigator,
            backCoordinator = backCoordinator,
            vaultRepository = vaultRepository,
            clipboardService = clipboardService,
            vaultViewModel = vaultViewModel,
            settingsViewModel = settingsViewModel,
            backupViewModel = backupViewModel,
            onboardingViewModel = onboardingViewModel,
            unlockViewModel = unlockViewModel,
        )
    }
    return remember(context, validator, commandDispatcher, externalDispatcher) {
        NavigationHostRuntime(context, validator, commandDispatcher, externalDispatcher)
    }
}

@Composable
internal fun PassVaultNavigationHost(
    initialRoute: PassVaultRoute,
    vaultRepository: VaultRepository,
    clipboardService: ClipboardService,
    vaultUiSecurityCoordinator: VaultUiSecurityCoordinator,
    requestedSecurityEpoch: Long,
) {
    val composition = rememberNavigationComposition(initialRoute)
    val runtime = rememberNavigationHostRuntime(composition, vaultRepository, clipboardService)
    val context = runtime.context
    val navigator = context.navigator
    val userActivitySignal = remember { UserActivitySignal() }
    val sessionState by vaultRepository.getSessionState()
        .collectAsState(initial = VaultSessionState.Uninitialized)
    val currentSessionState by rememberUpdatedState(sessionState)
    val settingsState by context.settingsViewModel.state.collectAsState()
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val hostResumed = lifecycleState == Lifecycle.State.RESUMED
    var previousSessionObservation by remember { mutableStateOf(SessionCleanupObservation()) }
    var pendingSecurityAcknowledgement by remember { mutableLongStateOf(0L) }

    DisposableEffect(navigator, hostResumed) {
        navigator.setHostResumed(hostResumed)
        onDispose { navigator.setHostResumed(false) }
    }
    DisposableEffect(userActivitySignal) { onDispose(userActivitySignal::close) }

    ObserveAuthenticationNavigationEffects(context)
    ObserveNavigationFeatureEffects(context)
    ObserveSessionSecurity(
        context = context,
        validator = runtime.validator,
        sessionState = sessionState,
        previousSessionObservation = previousSessionObservation,
        updatePreviousSessionObservation = { previousSessionObservation = it },
        requestedSecurityEpoch = requestedSecurityEpoch,
        vaultUiSecurityCoordinator = vaultUiSecurityCoordinator,
        onSecurityAcknowledgementPending = { pendingSecurityAcknowledgement = it },
    )
    ObserveSecurityAcknowledgement(
        epoch = pendingSecurityAcknowledgement,
        sessionState = sessionState,
        route = navigator.state.currentRoute(),
        coordinator = vaultUiSecurityCoordinator,
        onAcknowledged = { pendingSecurityAcknowledgement = 0L },
    )
    ObserveCommands(
        context = context,
        dispatcher = runtime.commandDispatcher,
        sessionState = currentSessionState,
        userActivitySignal = userActivitySignal,
    )
    ObserveExternalNavigation(
        context = context,
        dispatcher = runtime.externalDispatcher,
        validator = runtime.validator,
        sessionState = sessionState,
        hostResumed = hostResumed,
    )
    ObserveAutoLock(
        context = context,
        sessionState = sessionState,
        timeoutMinutes = settingsState.autoLockTimeoutMinutes,
        userActivitySignal = userActivitySignal,
    )

    NavigationDisplay(context, userActivitySignal, hostResumed)
}

@Composable
private fun NavigationDisplay(
    context: RouteAdapterContext,
    userActivitySignal: UserActivitySignal,
    hostResumed: Boolean,
) {
    val state = context.navigator.state
    val root by state.root.collectAsState()
    val selected by state.selectedDestination.collectAsState()
    val layoutDirection = LocalLayoutDirection.current
    val entryCollections = rememberNavigationEntryCollections(context)
    val activeEntries = entryCollections.active(root, selected)
    val disposition = context.backCoordinator.effectiveDisposition(hostResumed)
    val visibleEntries = entriesAllowedByBackPolicy(activeEntries, disposition)
    val transitions = remember(layoutDirection) {
        platformNavigationTransitionSpecs(layoutDirection)
    }
    val modifier = Modifier
        .fillMaxSize()
        .recordUserActivity(userActivitySignal)
        .onPreviewKeyEvent { event ->
            userActivitySignal.recordActivity()
            isApplicationBackKey(event.key, event.type) &&
                context.backCoordinator.requestBack()
        }

    BoxWithConstraints(modifier) {
        val showTabs = root == NavigationRoot.MAIN
        VaultTabShell(
            selectedTab = selected,
            onSelectedTab = { destination ->
                if (context.backCoordinator.canLeaveForForwardNavigation()) {
                    selectShellTab(context, destination)
                } else if (disposition == BackDisposition.HandleInPlace) {
                    context.backCoordinator.requestBack()
                }
            },
            onAdd = {
                if (context.backCoordinator.canLeaveForForwardNavigation()) {
                    openCredentialCreate(context)
                } else if (disposition == BackDisposition.HandleInPlace) {
                    context.backCoordinator.requestBack()
                }
            },
            showActionDock = showTabs,
            content = { contentModifier ->
                NavDisplay(
                    entries = visibleEntries,
                    modifier = contentModifier,
                    onBack = { context.backCoordinator.completeInteractivePop().checkExpected() },
                    transitionSpec = transitions.forward,
                    popTransitionSpec = transitions.pop,
                    predictivePopTransitionSpec = transitions.predictivePop,
                )
            },
        )
        ConsumeGuardedPlatformBack(disposition, context.backCoordinator)
    }
}

private fun selectShellTab(context: RouteAdapterContext, destination: TopLevelDestination) {
    context.navigator.selectTab(destination, context.navigator.currentToken()).checkExpected()
}

private fun openCredentialCreate(context: RouteAdapterContext) {
    context.navigator.openInTab(
        destination = TopLevelDestination.HOME,
        route = VaultRoute.CredentialCreate(),
        resetStack = false,
        token = context.navigator.currentToken(),
    ).checkExpected()
}

internal fun isApplicationBackKey(key: Key, type: KeyEventType): Boolean =
    type == KeyEventType.KeyDown && (key == Key.Escape || key == Key.Back)

private data class NavigationEntryCollections(
    val authentication: List<NavEntry<PassVaultRoute>>,
    val home: List<NavEntry<PassVaultRoute>>,
    val generator: List<NavEntry<PassVaultRoute>>,
    val twoFactor: List<NavEntry<PassVaultRoute>>,
    val settings: List<NavEntry<PassVaultRoute>>,
) {
    fun active(
        root: NavigationRoot,
        selected: TopLevelDestination,
    ): List<NavEntry<PassVaultRoute>> = when (root) {
        NavigationRoot.AUTHENTICATION -> authentication
        NavigationRoot.MAIN -> when (selected) {
            TopLevelDestination.HOME -> home
            TopLevelDestination.GENERATOR -> generator
            TopLevelDestination.TWO_FACTOR_CODES -> twoFactor
            TopLevelDestination.SETTINGS -> settings
        }
    }
}

@Composable
private fun rememberNavigationEntryCollections(context: RouteAdapterContext): NavigationEntryCollections {
    val state = context.navigator.state
    verifyRouteAdapterRegistry()
    val provider = entryProvider {
        authRouteAdapters(context)
        vaultRouteAdapters(context)
        toolsRouteAdapters(context)
        settingsRouteAdapters(context)
    }
    return NavigationEntryCollections(
        authentication = decoratedEntries(state.authenticationStack, provider),
        home = decoratedEntries(state.stack(TopLevelDestination.HOME), provider),
        generator = decoratedEntries(state.stack(TopLevelDestination.GENERATOR), provider),
        twoFactor = decoratedEntries(state.stack(TopLevelDestination.TWO_FACTOR_CODES), provider),
        settings = decoratedEntries(state.stack(TopLevelDestination.SETTINGS), provider),
    )
}

@Composable
private fun decoratedEntries(
    stack: List<PassVaultRoute>,
    provider: (PassVaultRoute) -> NavEntry<PassVaultRoute>,
): List<NavEntry<PassVaultRoute>> = rememberDecoratedNavEntries(
    backStack = stack,
    entryDecorators = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
    ),
    entryProvider = provider,
)

@Composable
private fun ConsumeGuardedPlatformBack(
    disposition: BackDisposition,
    coordinator: NavigationBackCoordinator,
) {
    val gestureState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = gestureState,
        isBackEnabled = disposition == BackDisposition.HandleInPlace || disposition == BackDisposition.Blocked,
        onBackCompleted = {
            if (disposition == BackDisposition.HandleInPlace) coordinator.requestBack()
        },
    )
}

@Composable
private fun ObserveSessionSecurity(
    context: RouteAdapterContext,
    validator: RestoredNavigationValidator,
    sessionState: VaultSessionState,
    previousSessionObservation: SessionCleanupObservation,
    updatePreviousSessionObservation: (SessionCleanupObservation) -> Unit,
    requestedSecurityEpoch: Long,
    vaultUiSecurityCoordinator: VaultUiSecurityCoordinator,
    onSecurityAcknowledgementPending: (Long) -> Unit,
) {
    LaunchedEffect(sessionState, requestedSecurityEpoch) {
        val hasPendingRequest = requestedSecurityEpoch > vaultUiSecurityCoordinator.acknowledgedEpoch.value &&
            sessionState is VaultSessionState.Locked
        val cleanup = sessionCleanupPolicy(
            sessionState = sessionState,
            previousSessionObservation = previousSessionObservation,
            restoreInProgress = context.backupViewModel.state.value.isImporting,
            preserveClipboardOnBackgroundLock = preservesSensitiveClipboardOnBackgroundLock(),
        )
        updatePreviousSessionObservation(sessionState.toSessionCleanupObservation())
        if (cleanup.clearSensitiveUiState || hasPendingRequest) {
            val preserveRestore = cleanup.preserveBackupRestore ||
                ((sessionState as? VaultSessionState.Locked)?.reason == LockReason.Restore &&
                    context.backupViewModel.state.value.isImporting)
            clearApplicationSensitiveState(
                context = context,
                vaultUiSecurityCoordinator = vaultUiSecurityCoordinator,
                clearUnlock = cleanup.clearUnlockUiState || hasPendingRequest,
                preserveRestore = preserveRestore,
            )
        }
        if (cleanup.clearClipboard) context.clipboardService.clearForLockTransition()

        when {
            shouldGuardUnlockedRoutes(sessionState) -> context.navigator.requireAuthentication()
            sessionState is VaultSessionState.Unlocked -> {
                context.navigator.markSessionUnlocked()
                val validated = validator.validate(context.navigator.state.restorableSnapshot())
                context.navigator.activateUnlocked(validated).checkExpected()
                context.vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnRefresh)
            }
        }

        if (
            shouldAcknowledgeVaultUiSecurity(
                requestedEpoch = requestedSecurityEpoch,
                acknowledgedEpoch = vaultUiSecurityCoordinator.acknowledgedEpoch.value,
                sessionState = sessionState,
                route = context.navigator.state.currentRoute(),
            )
        ) {
            onSecurityAcknowledgementPending(requestedSecurityEpoch)
        }
    }
}

private fun clearApplicationSensitiveState(
    context: RouteAdapterContext,
    vaultUiSecurityCoordinator: VaultUiSecurityCoordinator,
    clearUnlock: Boolean,
    preserveRestore: Boolean,
) {
    vaultUiSecurityCoordinator.clearEntrySensitiveStateForLock()
    context.vaultViewModel.clearForLock()
    context.settingsViewModel.clearForLock()
    if (clearUnlock) context.unlockViewModel.clearForLock()
    if (preserveRestore) context.backupViewModel.clearForRestoreLock() else context.backupViewModel.clearForLock()
    context.onboardingViewModel.clearForLock()
}

@Composable
private fun ObserveSecurityAcknowledgement(
    epoch: Long,
    sessionState: VaultSessionState,
    route: PassVaultRoute,
    coordinator: VaultUiSecurityCoordinator,
    onAcknowledged: () -> Unit,
) {
    LaunchedEffect(epoch, sessionState, route) {
        if (
            !shouldAcknowledgeVaultUiSecurity(
                requestedEpoch = epoch,
                acknowledgedEpoch = coordinator.acknowledgedEpoch.value,
                sessionState = sessionState,
                route = route,
            )
        ) return@LaunchedEffect
        withFrameNanos { }
        if (
            shouldAcknowledgeVaultUiSecurity(
                requestedEpoch = epoch,
                acknowledgedEpoch = coordinator.acknowledgedEpoch.value,
                sessionState = sessionState,
                route = route,
            )
        ) {
            coordinator.acknowledge(epoch)
            onAcknowledged()
        }
    }
}

@Composable
private fun ObserveCommands(
    context: RouteAdapterContext,
    dispatcher: AppCommandDispatcher,
    sessionState: VaultSessionState,
    userActivitySignal: UserActivitySignal,
) {
    val currentSession by rememberUpdatedState(sessionState)
    LaunchedEffect(dispatcher, context.navigator) {
        dispatcher.commands.collect { command ->
            if (currentSession is VaultSessionState.Unlocked) userActivitySignal.recordActivity()
            when (command) {
                AppCommand.CLEAR_CLIPBOARD -> context.clipboardService.clear()
                AppCommand.TOGGLE_THEME -> {
                    val next = if (context.settingsViewModel.state.value.theme == SettingsViewModel.AppTheme.DARK) {
                        SettingsViewModel.AppTheme.LIGHT
                    } else {
                        SettingsViewModel.AppTheme.DARK
                    }
                    context.settingsViewModel.onEvent(SettingsViewModel.SettingsEvent.OnThemeChanged(next))
                }
                else -> if (
                    currentSession is VaultSessionState.Unlocked &&
                    context.backCoordinator.canLeaveForForwardNavigation()
                ) {
                    applyUnlockedCommand(context, command)
                }
            }
        }
    }
}

private fun applyUnlockedCommand(context: RouteAdapterContext, command: AppCommand) {
    val token = context.navigator.currentToken()
    when (command) {
        AppCommand.NEW_CREDENTIAL -> openCommandTarget(
            context,
            TopLevelDestination.HOME,
            VaultRoute.CredentialCreate(),
            token = token,
        )
        AppCommand.SEARCH -> {
            context.navigator.openTabRoot(
                TopLevelDestination.HOME,
                resetStack = true,
                token = token,
            ).checkExpected()
            context.vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnSearchClick)
        }
        AppCommand.GENERATOR -> openCommandTarget(context, TopLevelDestination.GENERATOR, token = token)
        AppCommand.HEALTH -> openCommandTarget(
            context,
            TopLevelDestination.GENERATOR,
            HealthRoute.Health,
            token = token,
        )
        AppCommand.SETTINGS -> openCommandTarget(context, TopLevelDestination.SETTINGS, token = token)
        AppCommand.IMPORT -> openCommandTarget(
            context,
            TopLevelDestination.SETTINGS,
            BackupRoute.Import,
            token = token,
        )
        AppCommand.EXPORT -> openCommandTarget(
            context,
            TopLevelDestination.SETTINGS,
            BackupRoute.Export,
            token = token,
        )
        AppCommand.HELP -> {
            context.navigator.openTabRoot(
                TopLevelDestination.SETTINGS,
                resetStack = false,
                token = token,
            ).checkExpected()
            context.settingsViewModel.onEvent(SettingsViewModel.SettingsEvent.OnHelpClick)
        }
        AppCommand.ABOUT -> {
            context.navigator.openTabRoot(
                TopLevelDestination.SETTINGS,
                resetStack = false,
                token = token,
            ).checkExpected()
            context.settingsViewModel.onEvent(SettingsViewModel.SettingsEvent.OnVaultInfoClick)
        }
        AppCommand.CLEAR_CLIPBOARD,
        AppCommand.TOGGLE_THEME,
        -> Unit
    }
}

private fun openCommandTarget(
    context: RouteAdapterContext,
    destination: TopLevelDestination,
    route: PassVaultRoute? = null,
    resetStack: Boolean = false,
    token: NavigationToken,
) {
    val result = if (route == null) {
        context.navigator.openTabRoot(destination, resetStack, token)
    } else {
        context.navigator.openInTab(destination, route, resetStack, token)
    }
    result.checkExpected()
}

@Composable
private fun ObserveExternalNavigation(
    context: RouteAdapterContext,
    dispatcher: ExternalNavigationDispatcher,
    validator: RestoredNavigationValidator,
    sessionState: VaultSessionState,
    hostResumed: Boolean,
) {
    val pending by context.navigator.pendingExternalState.collectAsState()
    val navigationRoot by context.navigator.state.root.collectAsState()
    val canLeaveForExternalNavigation = context.backCoordinator.canLeaveForForwardNavigation()
    LaunchedEffect(dispatcher, context.navigator) {
        dispatcher.pending.collect { input ->
            input ?: return@collect
            when (val parsed = ExternalNavigationParser.parse(input)) {
                is ExternalNavigationParseResult.Accepted -> context.navigator.submitExternal(parsed.envelope)
                is ExternalNavigationParseResult.Rejected -> Unit
            }
            dispatcher.consume(input.deliveryId)
        }
    }
    LaunchedEffect(
        pending,
        sessionState,
        hostResumed,
        navigationRoot,
        canLeaveForExternalNavigation,
        validator,
    ) {
        val envelope = pending ?: return@LaunchedEffect
        if (
            shouldApplyPendingExternalNavigation(
                sessionState = sessionState,
                hostResumed = hostResumed,
                navigationRoot = navigationRoot,
                canLeaveForForwardNavigation = canLeaveForExternalNavigation,
            )
        ) {
            applyPendingExternal(context, validator, envelope)
        }
    }
}

internal fun shouldApplyPendingExternalNavigation(
    sessionState: VaultSessionState,
    hostResumed: Boolean,
    navigationRoot: NavigationRoot,
    canLeaveForForwardNavigation: Boolean,
): Boolean = sessionState is VaultSessionState.Unlocked &&
    hostResumed &&
    navigationRoot == NavigationRoot.MAIN &&
    canLeaveForForwardNavigation

private suspend fun applyPendingExternal(
    context: RouteAdapterContext,
    validator: RestoredNavigationValidator,
    envelope: com.passvault.core.navigation.ExternalNavigationEnvelope,
) {
    if (validator.validateExternal(envelope.intent)) {
        context.navigator.applyValidatedExternal(envelope).checkExpected()
    } else {
        context.navigator.rejectPendingExternal(envelope.deliveryId)
    }
}

@Composable
private fun ObserveAutoLock(
    context: RouteAdapterContext,
    sessionState: VaultSessionState,
    timeoutMinutes: Int,
    userActivitySignal: UserActivitySignal,
) {
    LaunchedEffect(sessionState, timeoutMinutes, userActivitySignal) {
        if (sessionState is VaultSessionState.Unlocked) {
            AutoLockTimer(
                activitySignal = userActivitySignal,
                timeoutMillis = timeoutMinutes * 60_000L,
                lock = { context.vaultRepository.lockWithBoundedRetry(LockReason.AutoLock) },
                onLockFailed = { context.vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnLockFailed) },
            ).run()
        }
    }
}
