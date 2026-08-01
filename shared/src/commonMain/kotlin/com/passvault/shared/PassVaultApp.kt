package com.passvault.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.pointer.pointerInput
import com.passvault.core.designsystem.theme.PassVaultTheme
import com.passvault.core.designsystem.components.LoadingState
import com.passvault.core.designsystem.tokens.Breakpoints
import com.passvault.core.designsystem.text.resolveSuspending
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.navigation.AuthRoute
import com.passvault.core.navigation.AppCommand
import com.passvault.core.navigation.AppCommandDispatcher
import com.passvault.core.navigation.BackupRoute
import com.passvault.core.navigation.GeneratorRoute
import com.passvault.core.navigation.HealthRoute
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.SettingsRoute
import com.passvault.core.navigation.VaultRoute
import com.passvault.core.navigation.requiresUnlockedVault
import com.passvault.core.security.ClipboardService
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
import com.passvault.feature.vault.ui.VaultScreenRoute
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
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

    PassVaultTheme(
        darkTheme = useDarkTheme,
        accent = settingsState.accentColor,
    ) {
        AppContent()
    }
}

@Composable
private fun AppContent() {
    val vaultRepository: VaultRepository = koinInject()
    val initialRoute by produceState<PassVaultRoute?>(null, vaultRepository) {
        value = if (vaultRepository.exists().getOrDefault(false)) {
            AuthRoute.Unlock
        } else {
            AuthRoute.Onboarding
        }
    }

    val route = initialRoute
    if (route == null) {
        LoadingState()
        return
    }

    AppNavigation(
        initialRoute = route,
        vaultRepository = vaultRepository,
        clipboardService = koinInject(),
    )
}

@Composable
private fun AppNavigation(
    initialRoute: PassVaultRoute,
    vaultRepository: VaultRepository,
    clipboardService: ClipboardService,
) {
    val scope = rememberCoroutineScope()
    val commandDispatcher: AppCommandDispatcher = koinInject()
    val unlockViewModel: UnlockViewModel = koinInject()
    val vaultViewModel: VaultViewModel = koinInject()
    val settingsViewModel: SettingsViewModel = koinInject()
    val credentialViewModel: CredentialViewModel = koinInject()
    val generatorViewModel: GeneratorViewModel = koinInject()
    val backupViewModel: BackupViewModel = koinInject()
    val healthViewModel: HealthViewModel = koinInject()
    val onboardingViewModel: OnboardingViewModel = koinInject()
    val settingsState by settingsViewModel.state.collectAsState()
    var activityGeneration by remember { mutableIntStateOf(0) }
    val backStack = remember { mutableStateListOf<PassVaultRoute>(initialRoute) }
    val sessionState by vaultRepository.getSessionState()
        .collectAsState(initial = VaultSessionState.Uninitialized)

    fun popBack() {
        // removeLast() can compile to a Java 21 List method that is absent on
        // older Android runtimes. removeAt() is supported on every minSdk.
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun replaceRoot(route: PassVaultRoute) {
        backStack.clear()
        backStack.add(route)
    }

    suspend fun copySensitive(text: String) {
        clipboardService.copySensitive(
            text = text,
            timeoutMs = settingsViewModel.state.value.clipboardClearSeconds * 1_000L,
        )
    }

    fun clearSensitiveUiState() {
        vaultViewModel.clearForLock()
        unlockViewModel.clearForLock()
        credentialViewModel.clearForLock()
        generatorViewModel.clearForLock()
        settingsViewModel.clearForLock()
        backupViewModel.clearForLock()
        healthViewModel.clearForLock()
        onboardingViewModel.clearForLock()
    }

    LaunchedEffect(sessionState) {
        when (sessionState) {
            is VaultSessionState.Locking,
            is VaultSessionState.Locked,
            is VaultSessionState.FatalError,
            -> {
                clearSensitiveUiState()
                try {
                    clipboardService.clear()
                } catch (cancel: CancellationException) {
                    throw cancel
                } catch (_: Exception) {
                    // Clipboard providers can disappear while the app is
                    // backgrounded; failure must not block session cleanup.
                }
                if (backStack.lastOrNull()?.requiresUnlockedVault() == true) {
                    replaceRoot(AuthRoute.Unlock)
                }
            }
            is VaultSessionState.Unlocked -> {
                vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnRefresh)
                if (backStack.lastOrNull() == AuthRoute.Unlock) {
                    replaceRoot(VaultRoute.Vault)
                }
            }
            else -> Unit
        }
    }

    LaunchedEffect(commandDispatcher, sessionState) {
        commandDispatcher.commands.collect { command ->
            if (command == AppCommand.LOCK) {
                clearSensitiveUiState()
                vaultRepository.lock()
                replaceRoot(AuthRoute.Unlock)
                return@collect
            }
            if (sessionState !is VaultSessionState.Unlocked) return@collect

            when (command) {
                AppCommand.NEW_CREDENTIAL -> backStack.add(VaultRoute.CredentialCreate())
                AppCommand.SEARCH -> {
                    replaceRoot(VaultRoute.Vault)
                    vaultViewModel.onEvent(VaultViewModel.VaultEvent.OnSearchClick)
                }
                AppCommand.GENERATOR -> backStack.add(GeneratorRoute.Generator)
                AppCommand.HEALTH -> backStack.add(HealthRoute.Health)
                AppCommand.SETTINGS -> backStack.add(SettingsRoute.Settings)
                AppCommand.TOGGLE_THEME -> {
                    val nextTheme =
                        if (settingsViewModel.state.value.theme == SettingsViewModel.AppTheme.DARK) {
                            SettingsViewModel.AppTheme.LIGHT
                        } else {
                            SettingsViewModel.AppTheme.DARK
                        }
                    settingsViewModel.onEvent(SettingsViewModel.SettingsEvent.OnThemeChanged(nextTheme))
                }
                AppCommand.IMPORT -> backStack.add(BackupRoute.Import)
                AppCommand.EXPORT -> backStack.add(BackupRoute.Export)
                AppCommand.HELP -> {
                    settingsViewModel.onEvent(SettingsViewModel.SettingsEvent.OnHelpClick)
                    backStack.add(SettingsRoute.Settings)
                }
                AppCommand.ABOUT -> {
                    settingsViewModel.onEvent(SettingsViewModel.SettingsEvent.OnVaultInfoClick)
                    backStack.add(SettingsRoute.Settings)
                }
                AppCommand.CLEAR_CLIPBOARD -> clipboardService.clear()
                AppCommand.BACK -> popBack()
                AppCommand.LOCK -> Unit
            }
        }
    }

    LaunchedEffect(
        sessionState,
        activityGeneration,
        settingsState.autoLockTimeoutMinutes,
    ) {
        if (sessionState is VaultSessionState.Unlocked) {
            delay(settingsState.autoLockTimeoutMinutes * 60_000L)
            vaultRepository.lock()
        }
    }

    NavDisplay(
        modifier = Modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        activityGeneration++
                    }
                }
            }
            .onPreviewKeyEvent {
                activityGeneration++
                false
            },
        backStack = backStack,
        entryProvider = entryProvider {
            entry<AuthRoute.Onboarding> {
                OnboardingScreen(
                    viewModel = koinInject(),
                    onNavigateToCreatePassword = {
                        backStack.add(AuthRoute.CreatePassword)
                    },
                    onNavigateToUnlock = {
                        replaceRoot(AuthRoute.Unlock)
                    },
                )
            }

            entry<AuthRoute.CreatePassword> {
                CreatePasswordScreen(
                    viewModel = koinInject(),
                    onNavigateToConfirm = {
                        backStack.add(AuthRoute.ConfirmPassword)
                    },
                    onNavigateBack = ::popBack,
                )
            }

            entry<AuthRoute.ConfirmPassword> {
                ConfirmPasswordScreen(
                    viewModel = koinInject(),
                    onNavigateToSecurity = {
                        backStack.add(AuthRoute.SecurityExplanation)
                    },
                    onNavigateToComplete = {
                        replaceRoot(VaultRoute.Vault)
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
                    val compact = maxWidth < Breakpoints.mediumMin
                    val vaultContent: @Composable (Modifier) -> Unit = { contentModifier ->
                        VaultScreenRoute(
                            viewModel = vaultViewModel,
                            onNavigateToCredential = { credentialId ->
                                backStack.add(VaultRoute.CredentialDetail(credentialId.value))
                            },
                            onNavigateToCreate = {
                                backStack.add(VaultRoute.CredentialCreate())
                            },
                            onNavigateToGenerator = {
                                backStack.add(GeneratorRoute.Generator)
                            },
                            onNavigateToHealth = {
                                backStack.add(HealthRoute.Health)
                            },
                            onNavigateToSettings = {
                                backStack.add(SettingsRoute.Settings)
                            },
                            onLock = {
                                scope.launch { vaultRepository.lock() }
                                replaceRoot(AuthRoute.Unlock)
                            },
                            showActionDock = !compact,
                            modifier = contentModifier,
                        )
                    }

                    if (!compact) {
                        vaultContent(Modifier.fillMaxSize())
                    } else {
                        VaultTabShell(
                            onAdd = {
                                backStack.add(VaultRoute.CredentialCreate())
                            },
                            vaultContent = vaultContent,
                            generatorContent = { contentModifier ->
                                val viewModel: GeneratorViewModel = koinInject()
                                val state by viewModel.state.collectAsState()
                                LaunchedEffect(viewModel) {
                                    viewModel.effect.collect { effect ->
                                        val password = when (effect) {
                                            is GeneratorViewModel.GeneratorEffect.CopyToClipboard ->
                                                effect.password
                                            is GeneratorViewModel.GeneratorEffect.UsePassword ->
                                                effect.password
                                        }
                                        try {
                                            copySensitive(password)
                                        } catch (cancel: CancellationException) {
                                            throw cancel
                                        } catch (_: Exception) {
                                            // Clipboard failure leaves the generated value intact.
                                        }
                                    }
                                }
                                com.passvault.feature.generator.ui.GeneratorScreen(
                                    state = state,
                                    onEvent = viewModel::onEvent,
                                    onNavigateBack = {},
                                    showBackButton = false,
                                    modifier = contentModifier,
                                )
                            },
                            healthContent = { contentModifier ->
                                val viewModel: HealthViewModel = koinInject()
                                val state by viewModel.state.collectAsState()
                                LaunchedEffect(viewModel) {
                                    viewModel.onEvent(HealthViewModel.HealthEvent.OnRefreshScan)
                                }
                                ObserveHealthEffects(
                                    viewModel = viewModel,
                                    onBack = {},
                                    onCredential = { id ->
                                        backStack.add(VaultRoute.CredentialDetail(id.value))
                                    },
                                    onEditCredential = { id ->
                                        backStack.add(VaultRoute.CredentialEdit(id.value))
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
                                    onSecurity = { backStack.add(SettingsRoute.Security) },
                                    onAppearance = { backStack.add(SettingsRoute.Appearance) },
                                    onData = { backStack.add(SettingsRoute.Data) },
                                    onLock = { replaceRoot(AuthRoute.Unlock) },
                                    onExport = { backStack.add(BackupRoute.Export) },
                                    onImport = { backStack.add(BackupRoute.Import) },
                                    onBackup = { backStack.add(BackupRoute.Backup) },
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
                        backStack.add(VaultRoute.CredentialEdit(id.value))
                    },
                    onCopyToClipboard = { text ->
                        scope.launch {
                            try {
                                copySensitive(text)
                            } catch (cancel: CancellationException) {
                                throw cancel
                            } catch (_: Exception) {
                                // The detail screen presents the value only
                                // after an explicit action; a clipboard
                                // provider failure is safely non-fatal.
                            }
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
                            backStack.add(VaultRoute.CredentialDetail(it.value))
                        }
                    },
                    onNavigateToGenerator = {
                        backStack.add(GeneratorRoute.Generator)
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
                            backStack.add(VaultRoute.CredentialDetail(route.credentialId))
                        }
                    },
                    onNavigateToGenerator = {
                        backStack.add(GeneratorRoute.Generator)
                    },
                )
            }

            entry<GeneratorRoute.Generator> {
                val viewModel: GeneratorViewModel = koinInject()
                val credentialViewModel: CredentialViewModel = koinInject()
                val state by viewModel.state.collectAsState()

                LaunchedEffect(viewModel) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is GeneratorViewModel.GeneratorEffect.CopyToClipboard -> {
                                try {
                                    copySensitive(effect.password)
                                } catch (cancel: CancellationException) {
                                    throw cancel
                                } catch (_: Exception) {
                                    // Clipboard failure does not alter the
                                    // generated value or vault state.
                                }
                            }
                            is GeneratorViewModel.GeneratorEffect.UsePassword -> {
                                when (backStack.dropLast(1).lastOrNull()) {
                                    is VaultRoute.CredentialCreate,
                                    is VaultRoute.CredentialEdit,
                                    -> {
                                        credentialViewModel.onEvent(
                                            CredentialViewModel.CredentialEvent.OnPasswordChanged(
                                                effect.password
                                            )
                                        )
                                        popBack()
                                    }
                                    else -> {
                                        try {
                                            copySensitive(effect.password)
                                        } catch (cancel: CancellationException) {
                                            throw cancel
                                        } catch (_: Exception) {
                                            // Clipboard failure is recoverable.
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                com.passvault.feature.generator.ui.GeneratorScreen(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onNavigateBack = ::popBack,
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
                    onBack = ::popBack,
                    onCredential = { id ->
                        backStack.add(VaultRoute.CredentialDetail(id.value))
                    },
                    onEditCredential = { id ->
                        backStack.add(VaultRoute.CredentialEdit(id.value))
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

            entry<SettingsRoute.Settings> {
                val viewModel: SettingsViewModel = koinInject()
                val state by viewModel.state.collectAsState()
                ObserveSettingsEffects(
                    viewModel = viewModel,
                    onBack = ::popBack,
                    onSecurity = { backStack.add(SettingsRoute.Security) },
                    onAppearance = { backStack.add(SettingsRoute.Appearance) },
                    onData = { backStack.add(SettingsRoute.Data) },
                    onLock = { replaceRoot(AuthRoute.Unlock) },
                    onExport = { backStack.add(BackupRoute.Export) },
                    onImport = { backStack.add(BackupRoute.Import) },
                    onBackup = { backStack.add(BackupRoute.Backup) },
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
                    onAppearance = { backStack.add(SettingsRoute.Appearance) },
                    onData = { backStack.add(SettingsRoute.Data) },
                    onLock = { replaceRoot(AuthRoute.Unlock) },
                    onExport = { backStack.add(BackupRoute.Export) },
                    onImport = { backStack.add(BackupRoute.Import) },
                    onBackup = { backStack.add(BackupRoute.Backup) },
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
                    onSecurity = { backStack.add(SettingsRoute.Security) },
                    onAppearance = {},
                    onData = { backStack.add(SettingsRoute.Data) },
                    onLock = { replaceRoot(AuthRoute.Unlock) },
                    onExport = { backStack.add(BackupRoute.Export) },
                    onImport = { backStack.add(BackupRoute.Import) },
                    onBackup = { backStack.add(BackupRoute.Backup) },
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
                        backStack.add(BackupRoute.Export)
                    },
                    onNavigateToImport = {
                        backStack.add(BackupRoute.Import)
                    },
                    onNavigateToBackup = {
                        backStack.add(BackupRoute.Backup)
                    },
                )
            }

            entry<BackupRoute.Backup> {
                val viewModel: BackupViewModel = koinInject()
                val state by viewModel.state.collectAsState()
                LaunchedEffect(viewModel) { viewModel.refresh() }
                ObserveBackupEffects(
                    viewModel = viewModel,
                    onBack = ::popBack,
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
                    onNavigateBack = ::popBack,
                )
            }

            entry<BackupRoute.Import> {
                val viewModel: BackupViewModel = koinInject()
                LaunchedEffect(viewModel) { viewModel.refresh() }
                com.passvault.feature.backup.ui.ImportScreen(
                    viewModel = viewModel,
                    onNavigateBack = ::popBack,
                    onImportComplete = {
                        replaceRoot(AuthRoute.Unlock)
                    },
                )
            }
        },
        onBack = ::popBack,
    )
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
                is SettingsViewModel.SettingsEffect.ShowMessage -> Unit
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
                BackupViewModel.BackupEffect.ShowExportSuccess -> Unit
            }
        }
    }
}
