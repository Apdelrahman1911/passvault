package com.passvault.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import com.passvault.core.designsystem.theme.PassVaultTheme
import com.passvault.core.designsystem.components.ErrorState
import com.passvault.core.designsystem.components.LoadingState
import com.passvault.core.designsystem.platform.KeyboardDismissButton
import com.passvault.core.designsystem.tokens.Breakpoints
import com.passvault.core.designsystem.text.resolveSuspending
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.domain.repository.lockWithBoundedRetry
import com.passvault.core.navigation.AuthRoute
import com.passvault.core.navigation.AppCommand
import com.passvault.core.navigation.AppCommandDispatcher
import com.passvault.core.navigation.BackupRoute
import com.passvault.core.navigation.GeneratorRoute
import com.passvault.core.navigation.HealthRoute
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.SettingsRoute
import com.passvault.core.navigation.TwoFactorRoute
import com.passvault.core.navigation.VaultRoute
import com.passvault.core.navigation.requiresUnlockedVault
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.VaultUiSecurityCoordinator
import com.passvault.feature.backup.presentation.BackupViewModel
import com.passvault.feature.credential.presentation.CredentialViewModel
import com.passvault.feature.generator.presentation.GeneratorViewModel
import com.passvault.feature.health.presentation.HealthViewModel
import com.passvault.feature.onboarding.presentation.OnboardingViewModel
import com.passvault.feature.onboarding.ui.ConfirmPasswordScreen
import com.passvault.feature.onboarding.ui.CreatePasswordScreen
import com.passvault.feature.onboarding.ui.OnboardingScreen
import com.passvault.feature.onboarding.ui.SecurityExplanationRoute
import com.passvault.feature.settings.presentation.SettingsViewModel
import com.passvault.feature.unlock.presentation.UnlockScreenRoute
import com.passvault.feature.unlock.presentation.UnlockViewModel
import com.passvault.feature.vault.presentation.VaultViewModel
import com.passvault.feature.vault.presentation.TwoFactorCodesViewModel
import com.passvault.feature.vault.ui.VaultScreenRoute
import com.passvault.shared.security.AutoLockTimer
import com.passvault.shared.security.UserActivitySignal
import com.passvault.shared.security.recordUserActivity
import com.passvault.shared.platform.AppLanguageProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
fun PassVaultApp() {
    val settingsViewModel: SettingsViewModel = koinInject()
    val settingsState by settingsViewModel.state.collectAsState()
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (settingsState.theme) {
        SettingsViewModel.AppTheme.LIGHT -> false
        SettingsViewModel.AppTheme.DARK -> true
        SettingsViewModel.AppTheme.SYSTEM -> systemDark
    }

    AppLanguageProvider(settingsState.language) {
        PassVaultTheme(
            darkTheme = useDarkTheme,
            accent = settingsState.accentColor,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppContent()
                }
                KeyboardDismissButton(
                    modifier = Modifier.align(Alignment.BottomEnd),
                )
            }
        }
    }
}

@Composable
private fun AppContent() {
    val vaultRepository: VaultRepository = koinInject()
    val vaultUiSecurityCoordinator: VaultUiSecurityCoordinator = koinInject()
    val requestedSecurityEpoch by vaultUiSecurityCoordinator.requestedEpoch.collectAsState()
    var bootstrapAttempt by remember { mutableIntStateOf(0) }
    val initialState by produceState<InitialRouteState>(
        InitialRouteState.Loading,
        vaultRepository,
        bootstrapAttempt,
    ) {
        value = resolveInitialRoute(vaultRepository.exists())
    }

    when (val state = initialState) {
        InitialRouteState.Loading -> LoadingState()
        InitialRouteState.Error -> ErrorState(onAction = { bootstrapAttempt++ })
        is InitialRouteState.Ready -> AppNavigation(
            initialRoute = state.route,
            vaultRepository = vaultRepository,
            clipboardService = koinInject(),
            vaultUiSecurityCoordinator = vaultUiSecurityCoordinator,
            requestedSecurityEpoch = requestedSecurityEpoch,
        )
    }
}

/*
 * Navigation3 requires the complete typed entry registry to be built in one
 * entryProvider. Keeping the registry here makes route guards and sensitive
 * state teardown auditable as one boundary; individual screens own their UI.
 */
@Suppress("CyclomaticComplexMethod", "LongMethod", "ThrowsCount")
@Composable
private fun AppNavigation(
    initialRoute: PassVaultRoute,
    vaultRepository: VaultRepository,
    clipboardService: ClipboardService,
    vaultUiSecurityCoordinator: VaultUiSecurityCoordinator,
    requestedSecurityEpoch: Long,
) {
    val commandDispatcher: AppCommandDispatcher = koinInject()
    val unlockViewModel: UnlockViewModel = koinInject()
    val vaultViewModel: VaultViewModel = koinInject()
    val twoFactorCodesViewModel: TwoFactorCodesViewModel = koinInject()
    val settingsViewModel: SettingsViewModel = koinInject()
    val credentialViewModel: CredentialViewModel = koinInject()
    val generatorViewModel: GeneratorViewModel = koinInject()
    val backupViewModel: BackupViewModel = koinInject()
    val healthViewModel: HealthViewModel = koinInject()
    val onboardingViewModel: OnboardingViewModel = koinInject()
    val settingsState by settingsViewModel.state.collectAsState()
    val userActivitySignal = remember { UserActivitySignal() }
    var selectedVaultTab by remember { mutableStateOf(VaultTab.HOME) }
    val backStack = remember { mutableStateListOf<PassVaultRoute>(initialRoute) }
    val sessionState by vaultRepository.getSessionState()
        .collectAsState(initial = VaultSessionState.Uninitialized)
    val currentSessionState by rememberUpdatedState(sessionState)
    // Track only the non-sensitive phase. Retaining the full Unlocked state
    // here would unnecessarily keep its session identifier in Compose state.
    var previousSessionPhase by remember { mutableStateOf(SessionPhase.UNINITIALIZED) }
    var pendingSecurityAcknowledgement by remember { mutableLongStateOf(0L) }

    DisposableEffect(userActivitySignal) {
        onDispose(userActivitySignal::close)
    }

    fun popBack() {
        // removeLast() can compile to a Java 21 List method that is absent on
        // older Android runtimes. removeAt() is supported on every minSdk.
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun replaceRootUnchecked(route: PassVaultRoute) {
        if (backStack.size == 1 && backStack.firstOrNull() == route) return
        backStack.clear()
        backStack.add(route)
    }

    fun replaceRoot(route: PassVaultRoute) {
        // These helpers are also invoked by long-lived effect collectors whose
        // coroutine is intentionally not restarted for every session emission.
        // Read through rememberUpdatedState so they never retain the session
        // value from the composition that originally launched the collector.
        if (shouldRedirectToUnlock(route, currentSessionState)) {
            replaceRootUnchecked(AuthRoute.Unlock)
        } else {
            replaceRootUnchecked(route)
        }
    }

    fun navigate(route: PassVaultRoute) {
        if (shouldRedirectToUnlock(route, currentSessionState)) {
            replaceRootUnchecked(AuthRoute.Unlock)
        } else {
            backStack.add(route)
        }
    }

    fun selectVaultTab(tab: VaultTab) {
        if (selectedVaultTab != tab) {
            selectedVaultTab = tab
        }
    }

    suspend fun copySensitive(text: String) {
        copySensitiveWhileUnlocked(
            sessionState = vaultRepository.getSessionState(),
            clipboardService = clipboardService,
            text = text,
            timeoutMs = settingsViewModel.state.value.clipboardClearSeconds * 1_000L,
        )
    }

    suspend fun copyGeneratedValue(viewModel: GeneratorViewModel, text: String) {
        val succeeded = try {
            copySensitive(text)
            true
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            false
        }
        viewModel.onEvent(GeneratorViewModel.GeneratorEvent.OnCopyResult(succeeded))
    }

    fun clearSensitiveUiState(
        clearUnlockState: Boolean = true,
        preserveBackupRestore: Boolean = false,
    ) {
        selectVaultTab(VaultTab.HOME)
        vaultViewModel.clearForLock()
        if (clearUnlockState) unlockViewModel.clearForLock()
        credentialViewModel.clearForLock()
        generatorViewModel.clearForLock()
        settingsViewModel.clearForLock()
        if (preserveBackupRestore) {
            backupViewModel.clearForRestoreLock()
        } else {
            backupViewModel.clearForLock()
        }
        healthViewModel.clearForLock()
        twoFactorCodesViewModel.clearForLock()
        onboardingViewModel.clearForLock()
    }

    fun leaveBackupIfIdle() {
        val backupState = backupViewModel.state.value
        if (backupState.showRestoreConfirmation) {
            backupViewModel.onEvent(BackupViewModel.BackupEvent.OnRestoreCancelClick)
            return
        }
        if (!backupState.hasActiveOperation) {
            backupViewModel.clearForLock()
            popBack()
        }
    }

    fun handleBack() {
        when (backStack.lastOrNull()) {
            is VaultRoute.Vault -> {
                val vaultState = vaultViewModel.state.value
                when {
                    vaultState.showNewFolderDialog ->
                        vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnDismissNewFolder)
                    vaultState.folderPendingDeletion != null ->
                        vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnDismissDeleteFolder)
                    vaultState.isSearchActive ->
                        vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnSearchDismiss)
                    selectedVaultTab != VaultTab.HOME -> selectVaultTab(VaultTab.HOME)
                    else -> popBack()
                }
            }
            is VaultRoute.CredentialCreate,
            is VaultRoute.CredentialEdit,
            -> credentialViewModel.onEvent(CredentialViewModel.CredentialEvent.OnBackClick)
            is VaultRoute.CredentialDetail ->
                credentialViewModel.onEvent(CredentialViewModel.CredentialEvent.OnBackClick)
            GeneratorRoute.Generator -> {
                generatorViewModel.clearForLock()
                popBack()
            }
            HealthRoute.Health ->
                healthViewModel.onEvent(HealthViewModel.HealthEvent.OnBackClick)
            TwoFactorRoute.Codes ->
                twoFactorCodesViewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnBackClick)
            SettingsRoute.Settings,
            SettingsRoute.Security,
            SettingsRoute.Appearance,
            -> settingsViewModel.onEvent(SettingsViewModel.SettingsEvent.OnBackClick)
            SettingsRoute.Data -> popBack()
            BackupRoute.Backup,
            BackupRoute.Export,
            BackupRoute.Import,
            -> leaveBackupIfIdle()
            AuthRoute.CreatePassword,
            AuthRoute.ConfirmPassword,
            AuthRoute.SecurityExplanation,
            -> onboardingViewModel.onEvent(OnboardingViewModel.OnboardingEvent.OnBackClick)
            AuthRoute.Unlock -> {
                if (unlockViewModel.state.value.showRecoveryInfo) {
                    unlockViewModel.onEvent(UnlockViewModel.UnlockEvent.OnDismissRecoveryInfo)
                } else {
                    popBack()
                }
            }
            else -> popBack()
        }
    }

    LaunchedEffect(sessionState, requestedSecurityEpoch) {
        val hasPendingSecurityRequest =
            requestedSecurityEpoch > vaultUiSecurityCoordinator.acknowledgedEpoch.value &&
                sessionState is VaultSessionState.Locked
        val cleanupPolicy = sessionCleanupPolicy(
            sessionState = sessionState,
            previousSessionPhase = previousSessionPhase,
            restoreInProgress = backupViewModel.state.value.isImporting,
        )
        // Advance before the first suspension so a rapid Locking -> Locked
        // transition cannot leave the tracker stale if this effect is
        // cancelled while clearing a platform clipboard.
        previousSessionPhase = sessionState.toSessionPhase()
        if (cleanupPolicy.clearSensitiveUiState || hasPendingSecurityRequest) {
            val preserveRequestedRestore =
                (sessionState as? VaultSessionState.Locked)?.reason == LockReason.Restore &&
                    backupViewModel.state.value.isImporting
            clearSensitiveUiState(
                clearUnlockState = cleanupPolicy.clearUnlockUiState || hasPendingSecurityRequest,
                preserveBackupRestore = cleanupPolicy.preserveBackupRestore || preserveRequestedRestore,
            )
        }
        if (cleanupPolicy.clearSensitiveUiState) {
            clipboardService.clearForLockTransition()
        }

        if (
            shouldGuardUnlockedRoutes(sessionState) &&
            backStack.lastOrNull()?.requiresUnlockedVault() == true
        ) {
            replaceRootUnchecked(AuthRoute.Unlock)
        } else if (sessionState is VaultSessionState.Unlocked) {
            vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnRefresh)
            if (backStack.lastOrNull() is AuthRoute) {
                replaceRootUnchecked(VaultRoute.Vault)
            }
        }

        if (
            shouldAcknowledgeVaultUiSecurity(
                requestedEpoch = requestedSecurityEpoch,
                acknowledgedEpoch = vaultUiSecurityCoordinator.acknowledgedEpoch.value,
                sessionState = sessionState,
                route = backStack.lastOrNull(),
            )
        ) {
            // Changing this state schedules a new composition after all
            // singleton scrubbing and route mutations above have completed.
            pendingSecurityAcknowledgement = requestedSecurityEpoch
        }
    }

    LaunchedEffect(pendingSecurityAcknowledgement, sessionState) {
        val epoch = pendingSecurityAcknowledgement
        if (
            !shouldAcknowledgeVaultUiSecurity(
                requestedEpoch = epoch,
                acknowledgedEpoch = vaultUiSecurityCoordinator.acknowledgedEpoch.value,
                sessionState = sessionState,
                route = backStack.lastOrNull(),
            )
        ) {
            return@LaunchedEffect
        }

        // This effect exists only after the pending epoch and guarded route
        // were applied by a successful recomposition. Wait one more frame so
        // native privacy surfaces cannot reveal the previous rendered scene.
        withFrameNanos { }
        if (
            shouldAcknowledgeVaultUiSecurity(
                requestedEpoch = epoch,
                acknowledgedEpoch = vaultUiSecurityCoordinator.acknowledgedEpoch.value,
                sessionState = sessionState,
                route = backStack.lastOrNull(),
            )
        ) {
            vaultUiSecurityCoordinator.acknowledge(epoch)
            if (pendingSecurityAcknowledgement == epoch) {
                pendingSecurityAcknowledgement = 0L
            }
        }
    }

    LaunchedEffect(commandDispatcher) {
        commandDispatcher.commands.collect { command ->
            try {
                // Native menu bars and platform-level shortcuts can consume
                // input before the NavDisplay pointer/key modifiers see it.
                // Count the resulting command as activity while a session is
                // open so an actively used vault cannot auto-lock immediately
                // after one of those actions.
                if (currentSessionState is VaultSessionState.Unlocked) {
                    userActivitySignal.recordActivity()
                }
                // Clipboard cleanup is deliberately available even while the
                // vault is locked; every other native command needs an active
                // session and is ignored during onboarding and authentication.
                if (command == AppCommand.CLEAR_CLIPBOARD) {
                    clipboardService.clear()
                    return@collect
                }
                if (command == AppCommand.TOGGLE_THEME) {
                    val nextTheme =
                        if (settingsViewModel.state.value.theme == SettingsViewModel.AppTheme.DARK) {
                            SettingsViewModel.AppTheme.LIGHT
                        } else {
                            SettingsViewModel.AppTheme.DARK
                        }
                    settingsViewModel.onEvent(SettingsViewModel.SettingsEvent.OnThemeChanged(nextTheme))
                    return@collect
                }
                if (currentSessionState !is VaultSessionState.Unlocked) return@collect

                when (command) {
                    AppCommand.NEW_CREDENTIAL -> navigate(VaultRoute.CredentialCreate())
                    AppCommand.SEARCH -> {
                        replaceRoot(VaultRoute.Vault)
                        vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnSearchClick)
                    }
                    AppCommand.GENERATOR -> navigate(GeneratorRoute.Generator)
                    AppCommand.HEALTH -> navigate(HealthRoute.Health)
                    AppCommand.SETTINGS -> navigate(SettingsRoute.Settings)
                    AppCommand.TOGGLE_THEME -> Unit
                    AppCommand.IMPORT -> navigate(BackupRoute.Import)
                    AppCommand.EXPORT -> navigate(BackupRoute.Export)
                    AppCommand.HELP -> {
                        settingsViewModel.onEvent(SettingsViewModel.SettingsEvent.OnHelpClick)
                        navigate(SettingsRoute.Settings)
                    }
                    AppCommand.ABOUT -> {
                        settingsViewModel.onEvent(SettingsViewModel.SettingsEvent.OnVaultInfoClick)
                        navigate(SettingsRoute.Settings)
                    }
                    AppCommand.CLEAR_CLIPBOARD -> clipboardService.clear()
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                // A platform command must not terminate the long-lived command
                // collector. Feature-level commands surface their own errors.
            }
        }
    }

    LaunchedEffect(
        sessionState,
        settingsState.autoLockTimeoutMinutes,
        userActivitySignal,
    ) {
        if (sessionState is VaultSessionState.Unlocked) {
            AutoLockTimer(
                activitySignal = userActivitySignal,
                timeoutMillis = settingsState.autoLockTimeoutMinutes * 60_000L,
                lock = { vaultRepository.lockWithBoundedRetry(LockReason.AutoLock) },
                onLockFailed = {
                    vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnLockFailed)
                },
            ).run()
        }
    }

    NavDisplay(
        modifier = Modifier
            .fillMaxSize()
            .recordUserActivity(userActivitySignal)
            .onPreviewKeyEvent {
                userActivitySignal.recordActivity()
                if (it.type == KeyEventType.KeyDown && it.key == Key.Escape) {
                    handleBack()
                    true
                } else {
                    false
                }
            },
        backStack = backStack,
        entryProvider = entryProvider {
            entry<AuthRoute.Onboarding> {
                OnboardingScreen(
                    viewModel = koinInject(),
                    onNavigateToCreatePassword = {
                        navigate(AuthRoute.CreatePassword)
                    },
                )
            }

            entry<AuthRoute.CreatePassword> {
                CreatePasswordScreen(
                    viewModel = koinInject(),
                    onNavigateToConfirm = {
                        navigate(AuthRoute.ConfirmPassword)
                    },
                    onNavigateBack = {
                        popBack()
                    },
                )
            }

            entry<AuthRoute.ConfirmPassword> {
                ConfirmPasswordScreen(
                    viewModel = koinInject(),
                    onNavigateToSecurity = {
                        navigate(AuthRoute.SecurityExplanation)
                    },
                    onNavigateBack = ::popBack,
                )
            }

            entry<AuthRoute.SecurityExplanation> {
                SecurityExplanationRoute(
                    viewModel = koinInject(),
                    onComplete = { replaceRoot(VaultRoute.Vault) },
                    onNavigateBack = ::popBack,
                )
            }

            entry<AuthRoute.Unlock> {
                UnlockScreenRoute(
                    viewModel = unlockViewModel,
                    onUnlockSuccess = {
                        replaceRoot(VaultRoute.Vault)
                    },
                    onNavigateToOnboarding = {
                        replaceRoot(AuthRoute.Onboarding)
                    },
                )
            }

            entry<VaultRoute.Vault> {
                BoxWithConstraints {
                    val compact = maxWidth < Breakpoints.expandedMin
                    LaunchedEffect(compact) {
                        if (!compact) selectVaultTab(VaultTab.HOME)
                    }
                    val vaultContent: @Composable (Modifier) -> Unit = { contentModifier ->
                        VaultScreenRoute(
                            viewModel = vaultViewModel,
                            onNavigateToCredential = { credentialId ->
                                navigate(VaultRoute.CredentialDetail(credentialId.value))
                            },
                            onNavigateToCreate = {
                                navigate(VaultRoute.CredentialCreate())
                            },
                            onNavigateToGenerator = {
                                navigate(GeneratorRoute.Generator)
                            },
                            onNavigateToTwoFactorCodes = {
                                navigate(TwoFactorRoute.Codes)
                            },
                            onNavigateToSettings = {
                                navigate(SettingsRoute.Settings)
                            },
                            onLock = {
                                if (!vaultRepository.lockWithBoundedRetry()) {
                                    vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnLockFailed)
                                }
                            },
                            showActionDock = !compact,
                            modifier = contentModifier,
                        )
                    }

                    if (!compact) {
                        vaultContent(Modifier.fillMaxSize())
                    } else {
                        VaultTabShell(
                            selectedTab = selectedVaultTab,
                            onSelectedTabChanged = ::selectVaultTab,
                            onAdd = {
                                navigate(VaultRoute.CredentialCreate())
                            },
                            vaultContent = vaultContent,
                            generatorContent = { contentModifier ->
                                val viewModel: GeneratorViewModel = koinInject()
                                val state by viewModel.state.collectAsState()
                                LaunchedEffect(viewModel) {
                                    viewModel.ensureGenerated()
                                    viewModel.effect.collect { effect ->
                                        val password = when (effect) {
                                            is GeneratorViewModel.GeneratorEffect.CopyToClipboard ->
                                                effect.password
                                            is GeneratorViewModel.GeneratorEffect.UsePassword ->
                                                effect.password
                                        }
                                        copyGeneratedValue(viewModel, password)
                                    }
                                }
                                com.passvault.feature.generator.ui.GeneratorScreen(
                                    state = state,
                                    onEvent = viewModel::onEvent,
                                    onNavigateBack = {},
                                    onNavigateToHealth = { navigate(HealthRoute.Health) },
                                    showBackButton = false,
                                    modifier = contentModifier,
                                )
                            },
                            twoFactorCodesContent = { contentModifier ->
                                val viewModel: TwoFactorCodesViewModel = koinInject()
                                val state by viewModel.state.collectAsState()
                                ObserveTwoFactorCodesEffects(
                                    viewModel = viewModel,
                                    onBack = {},
                                    onCredential = { id ->
                                        navigate(VaultRoute.CredentialDetail(id.value))
                                    },
                                    onCopyCode = { code ->
                                        try {
                                            copySensitive(code)
                                            true
                                        } catch (cancel: CancellationException) {
                                            throw cancel
                                        } catch (_: Exception) {
                                            false
                                        }
                                    },
                                )
                                com.passvault.feature.vault.ui.TwoFactorCodesScreen(
                                    state = state,
                                    onEvent = viewModel::onEvent,
                                    showBackButton = false,
                                    modifier = contentModifier,
                                )
                            },
                            settingsContent = { contentModifier ->
                                val viewModel: SettingsViewModel = koinInject()
                                val state by viewModel.state.collectAsState()
                                ObserveSettingsEffects(
                                    viewModel = viewModel,
                                    onBack = {},
                                    onSecurity = { navigate(SettingsRoute.Security) },
                                    onAppearance = { navigate(SettingsRoute.Appearance) },
                                    onData = { navigate(SettingsRoute.Data) },
                                    onLock = { replaceRoot(AuthRoute.Unlock) },
                                    onExport = { navigate(BackupRoute.Export) },
                                    onImport = { navigate(BackupRoute.Import) },
                                    onBackup = { navigate(BackupRoute.Backup) },
                                )
                                com.passvault.feature.settings.ui.SettingsScreen(
                                    state = state,
                                    onEvent = viewModel::onEvent,
                                    showBackButton = false,
                                    modifier = contentModifier,
                                )
                            },
                        )
                    }
                }
            }

            entry<VaultRoute.CredentialDetail> { route ->
                com.passvault.feature.credential.ui.CredentialDetailScreen(
                    viewModel = koinInject(),
                    credentialId = CredentialId(route.credentialId),
                    onNavigateBack = {
                        vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnRefresh)
                        credentialViewModel.clearForLock()
                        popBack()
                    },
                    onNavigateToEdit = { id ->
                        navigate(VaultRoute.CredentialEdit(id.value))
                    },
                    onCopyToClipboard = { text ->
                        try {
                            copySensitive(text)
                            true
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (_: Exception) {
                            false
                        }
                    },
                )
            }

            entry<VaultRoute.CredentialCreate> {
                com.passvault.feature.credential.ui.CredentialEditScreen(
                    viewModel = koinInject(),
                    credentialId = null,
                    onNavigateBack = {
                        credentialViewModel.clearForLock()
                        popBack()
                    },
                    onSaveSuccess = { id ->
                        vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnRefresh)
                        credentialViewModel.clearForLock()
                        popBack()
                        id?.let {
                            navigate(VaultRoute.CredentialDetail(it.value))
                        }
                    },
                )
            }

            entry<VaultRoute.CredentialEdit> { route ->
                com.passvault.feature.credential.ui.CredentialEditScreen(
                    viewModel = koinInject(),
                    credentialId = CredentialId(route.credentialId),
                    onNavigateBack = {
                        credentialViewModel.clearForLock()
                        popBack()
                    },
                    onSaveSuccess = {
                        vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnRefresh)
                        credentialViewModel.clearForLock()
                        popBack()
                        if (backStack.lastOrNull() !is VaultRoute.CredentialDetail) {
                            navigate(VaultRoute.CredentialDetail(route.credentialId))
                        }
                    },
                )
            }

            entry<GeneratorRoute.Generator> {
                val viewModel: GeneratorViewModel = koinInject()
                val credentialViewModel: CredentialViewModel = koinInject()
                val state by viewModel.state.collectAsState()

                LaunchedEffect(viewModel) {
                    viewModel.ensureGenerated()
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is GeneratorViewModel.GeneratorEffect.CopyToClipboard ->
                                copyGeneratedValue(viewModel, effect.password)
                            is GeneratorViewModel.GeneratorEffect.UsePassword -> {
                                when (backStack.dropLast(1).lastOrNull()) {
                                    is VaultRoute.CredentialCreate,
                                    is VaultRoute.CredentialEdit,
                                    -> {
                                        if (
                                            vaultRepository.getSessionState().first() !is
                                            VaultSessionState.Unlocked
                                        ) {
                                            return@collect
                                        }
                                        credentialViewModel.onEvent(
                                            CredentialViewModel.CredentialEvent.OnPasswordChanged(
                                                effect.password
                                            )
                                        )
                                        generatorViewModel.clearForLock()
                                        popBack()
                                    }
                                    else -> {
                                        copyGeneratedValue(viewModel, effect.password)
                                    }
                                }
                            }
                        }
                    }
                }

                com.passvault.feature.generator.ui.GeneratorScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onNavigateBack = {
                        generatorViewModel.clearForLock()
                        popBack()
                    },
                    onNavigateToHealth = { navigate(HealthRoute.Health) },
                )
            }

            entry<HealthRoute.Health> {
                val viewModel: HealthViewModel = koinInject()
                val state by viewModel.state.collectAsState()
                LaunchedEffect(viewModel) {
                    viewModel.onEvent(HealthViewModel.HealthEvent.OnRefreshScan)
                }
                ObserveHealthEffects(
                    viewModel = viewModel,
                    onBack = {
                        healthViewModel.clearForLock()
                        popBack()
                    },
                    onCredential = { id ->
                        navigate(VaultRoute.CredentialDetail(id.value))
                    },
                    onEditCredential = { id ->
                        navigate(VaultRoute.CredentialEdit(id.value))
                    },
                    onCopySummary = { report ->
                        try {
                            clipboardService.copy(report)
                            true
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (_: Exception) {
                            false
                        }
                    },
                )
                com.passvault.feature.health.ui.HealthScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                )
            }

            entry<TwoFactorRoute.Codes> {
                val viewModel: TwoFactorCodesViewModel = koinInject()
                val state by viewModel.state.collectAsState()
                ObserveTwoFactorCodesEffects(
                    viewModel = viewModel,
                    onBack = {
                        viewModel.clearForLock()
                        popBack()
                    },
                    onCredential = { id ->
                        navigate(VaultRoute.CredentialDetail(id.value))
                    },
                    onCopyCode = { code ->
                        try {
                            copySensitive(code)
                            true
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (_: Exception) {
                            false
                        }
                    },
                )
                com.passvault.feature.vault.ui.TwoFactorCodesScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                )
            }

            entry<SettingsRoute.Settings> {
                val viewModel: SettingsViewModel = koinInject()
                val state by viewModel.state.collectAsState()
                ObserveSettingsEffects(
                    viewModel = viewModel,
                    onBack = ::popBack,
                    onSecurity = { navigate(SettingsRoute.Security) },
                    onAppearance = { navigate(SettingsRoute.Appearance) },
                    onData = { navigate(SettingsRoute.Data) },
                    onLock = { replaceRoot(AuthRoute.Unlock) },
                    onExport = { navigate(BackupRoute.Export) },
                    onImport = { navigate(BackupRoute.Import) },
                    onBackup = { navigate(BackupRoute.Backup) },
                )
                com.passvault.feature.settings.ui.SettingsScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                )
            }

            entry<SettingsRoute.Security> {
                val viewModel: SettingsViewModel = koinInject()
                val state by viewModel.state.collectAsState()
                ObserveSettingsEffects(
                    viewModel = viewModel,
                    onBack = ::popBack,
                    onSecurity = {},
                    onAppearance = { navigate(SettingsRoute.Appearance) },
                    onData = { navigate(SettingsRoute.Data) },
                    onLock = { replaceRoot(AuthRoute.Unlock) },
                    onExport = { navigate(BackupRoute.Export) },
                    onImport = { navigate(BackupRoute.Import) },
                    onBackup = { navigate(BackupRoute.Backup) },
                )
                com.passvault.feature.settings.ui.SecuritySettingsScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                )
            }

            entry<SettingsRoute.Appearance> {
                val viewModel: SettingsViewModel = koinInject()
                val state by viewModel.state.collectAsState()
                ObserveSettingsEffects(
                    viewModel = viewModel,
                    onBack = ::popBack,
                    onSecurity = { navigate(SettingsRoute.Security) },
                    onAppearance = {},
                    onData = { navigate(SettingsRoute.Data) },
                    onLock = { replaceRoot(AuthRoute.Unlock) },
                    onExport = { navigate(BackupRoute.Export) },
                    onImport = { navigate(BackupRoute.Import) },
                    onBackup = { navigate(BackupRoute.Backup) },
                )
                com.passvault.feature.settings.ui.AppearanceSettingsScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                )
            }

            entry<SettingsRoute.Data> {
                com.passvault.feature.settings.ui.DataSettingsScreen(
                    viewModel = koinInject(),
                    onNavigateBack = ::popBack,
                    onNavigateToExport = {
                        navigate(BackupRoute.Export)
                    },
                    onNavigateToImport = {
                        navigate(BackupRoute.Import)
                    },
                    onNavigateToBackup = {
                        navigate(BackupRoute.Backup)
                    },
                )
            }

            entry<BackupRoute.Backup> {
                val viewModel: BackupViewModel = koinInject()
                val state by viewModel.state.collectAsState()
                LaunchedEffect(viewModel) { viewModel.refresh() }
                ObserveBackupEffects(
                    viewModel = viewModel,
                    onBack = {
                        backupViewModel.clearForLock()
                        popBack()
                    },
                    onImportSuccess = { replaceRoot(AuthRoute.Unlock) },
                )
                com.passvault.feature.backup.ui.BackupScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                )
            }

            entry<BackupRoute.Export> {
                val viewModel: BackupViewModel = koinInject()
                LaunchedEffect(viewModel) { viewModel.refresh() }
                com.passvault.feature.backup.ui.ExportScreen(
                    viewModel = viewModel,
                    onNavigateBack = ::leaveBackupIfIdle,
                )
            }

            entry<BackupRoute.Import> {
                val viewModel: BackupViewModel = koinInject()
                LaunchedEffect(viewModel) { viewModel.refresh() }
                com.passvault.feature.backup.ui.ImportScreen(
                    viewModel = viewModel,
                    onNavigateBack = ::leaveBackupIfIdle,
                    onImportComplete = {
                        replaceRoot(AuthRoute.Unlock)
                    },
                )
            }
        },
        onBack = ::handleBack,
    )
}

internal sealed interface InitialRouteState {
    data object Loading : InitialRouteState
    data object Error : InitialRouteState
    data class Ready(val route: PassVaultRoute) : InitialRouteState
}

internal fun resolveInitialRoute(vaultExists: Result<Boolean>): InitialRouteState =
    vaultExists.fold(
        onSuccess = { exists ->
            InitialRouteState.Ready(if (exists) AuthRoute.Unlock else AuthRoute.Onboarding)
        },
        onFailure = { InitialRouteState.Error },
    )

internal fun shouldRedirectToUnlock(
    route: PassVaultRoute,
    sessionState: VaultSessionState,
): Boolean = route.requiresUnlockedVault() && sessionState !is VaultSessionState.Unlocked

internal fun shouldGuardUnlockedRoutes(sessionState: VaultSessionState): Boolean =
    sessionState is VaultSessionState.Locking || sessionState is VaultSessionState.Locked

internal enum class SessionPhase {
    UNINITIALIZED,
    LOCKED,
    UNLOCKING,
    UNLOCKED,
    LOCKING,
}

internal fun VaultSessionState.toSessionPhase(): SessionPhase = when (this) {
    VaultSessionState.Uninitialized -> SessionPhase.UNINITIALIZED
    is VaultSessionState.Locked -> SessionPhase.LOCKED
    VaultSessionState.Unlocking -> SessionPhase.UNLOCKING
    is VaultSessionState.Unlocked -> SessionPhase.UNLOCKED
    is VaultSessionState.Locking -> SessionPhase.LOCKING
}

internal data class SessionCleanupPolicy(
    val clearSensitiveUiState: Boolean,
    val clearUnlockUiState: Boolean,
    val preserveBackupRestore: Boolean,
)

/**
 * Only a real lock transition scrubs singleton feature state. StateFlow may
 * conflate the repository's synchronous Locking -> Locked emissions, so the
 * terminal state retains the completed lock reason. A Locked state after
 * initialization or a failed unlock has no reason and must not scrub feedback
 * produced by that operation.
 */
internal fun sessionCleanupPolicy(
    sessionState: VaultSessionState,
    previousSessionPhase: SessionPhase = SessionPhase.UNINITIALIZED,
    restoreInProgress: Boolean = false,
): SessionCleanupPolicy {
    val completedLock = sessionState as? VaultSessionState.Locked
    val isUnobservedCompletedLock = completedLock?.reason != null &&
        previousSessionPhase != SessionPhase.LOCKING
    return when {
        sessionState is VaultSessionState.Locking -> SessionCleanupPolicy(
            clearSensitiveUiState = true,
            clearUnlockUiState = true,
            preserveBackupRestore = sessionState.reason == LockReason.Restore && restoreInProgress,
        )
        isUnobservedCompletedLock -> SessionCleanupPolicy(
            clearSensitiveUiState = true,
            clearUnlockUiState = true,
            preserveBackupRestore = completedLock.reason == LockReason.Restore && restoreInProgress,
        )
        else -> SessionCleanupPolicy(
            clearSensitiveUiState = false,
            clearUnlockUiState = false,
            preserveBackupRestore = false,
        )
    }
}

@Composable
private fun ObserveTwoFactorCodesEffects(
    viewModel: TwoFactorCodesViewModel,
    onBack: () -> Unit,
    onCredential: (CredentialId) -> Unit,
    onCopyCode: suspend (String) -> Boolean,
) {
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                TwoFactorCodesViewModel.TwoFactorCodesEffect.NavigateBack -> onBack()
                is TwoFactorCodesViewModel.TwoFactorCodesEffect.NavigateToCredential ->
                    onCredential(effect.credentialId)
                is TwoFactorCodesViewModel.TwoFactorCodesEffect.CopyCode -> {
                    val copied = try {
                        onCopyCode(effect.code)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        false
                    }
                    viewModel.onEvent(TwoFactorCodesViewModel.TwoFactorCodesEvent.OnCopyResult(copied))
                }
            }
        }
    }
}

@Composable
private fun ObserveSettingsEffects(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onSecurity: () -> Unit,
    onAppearance: () -> Unit,
    onData: () -> Unit,
    onLock: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onBackup: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsViewModel.SettingsEffect.NavigateBack -> onBack()
                SettingsViewModel.SettingsEffect.NavigateToSecurity -> onSecurity()
                SettingsViewModel.SettingsEffect.NavigateToAppearance -> onAppearance()
                SettingsViewModel.SettingsEffect.NavigateToData -> onData()
                SettingsViewModel.SettingsEffect.LockVault -> onLock()
                SettingsViewModel.SettingsEffect.ShowExportDialog -> onExport()
                SettingsViewModel.SettingsEffect.ShowImportDialog -> onImport()
                SettingsViewModel.SettingsEffect.ShowBackupDialog -> onBackup()
            }
        }
    }
}

@Composable
private fun ObserveHealthEffects(
    viewModel: HealthViewModel,
    onBack: () -> Unit,
    onCredential: (CredentialId) -> Unit,
    onEditCredential: (CredentialId) -> Unit,
    onCopySummary: suspend (String) -> Boolean,
) {
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HealthViewModel.HealthEffect.NavigateBack -> onBack()
                is HealthViewModel.HealthEffect.NavigateToCredential ->
                    onCredential(effect.credentialId)
                is HealthViewModel.HealthEffect.NavigateToEditCredential ->
                    onEditCredential(effect.credentialId)
                is HealthViewModel.HealthEffect.CopySummary -> {
                    val copied = onCopySummary(effect.report.resolveSuspending())
                    viewModel.onEvent(HealthViewModel.HealthEvent.OnCopySummaryResult(copied))
                }
            }
        }
    }
}

@Composable
private fun ObserveBackupEffects(
    viewModel: BackupViewModel,
    onBack: () -> Unit,
    onImportSuccess: () -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                BackupViewModel.BackupEffect.NavigateBack -> onBack()
                BackupViewModel.BackupEffect.ShowImportSuccess -> onImportSuccess()
            }
        }
    }
}
