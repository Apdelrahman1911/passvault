package com.passvault.shared

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.passvault.core.database.VaultDatabaseBootstrap
import com.passvault.core.database.VaultDatabaseBootstrapResult
import com.passvault.core.designsystem.components.ErrorState
import com.passvault.core.designsystem.components.LoadingState
import com.passvault.core.designsystem.platform.KeyboardDismissButton
import com.passvault.core.designsystem.theme.PassVaultAccent
import com.passvault.core.designsystem.theme.PassVaultTheme
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.AccentColorPreference
import com.passvault.core.domain.repository.AppSettings
import com.passvault.core.domain.repository.AppSettingsStore
import com.passvault.core.domain.repository.LanguagePreference
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.ThemePreference
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.navigation.AuthRoute
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.requiresUnlockedVault
import com.passvault.core.security.VaultUiSecurityCoordinator
import com.passvault.feature.settings.presentation.SettingsViewModel
import com.passvault.shared.navigation.PassVaultNavigationHost
import com.passvault.shared.platform.AppLanguageProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PassVaultApp() {
    val databaseBootstrap: VaultDatabaseBootstrap = koinInject()
    val appSettingsStore: AppSettingsStore = koinInject()
    var bootstrapAttempt by remember { mutableIntStateOf(0) }
    val startupState by produceState<StartupState>(
        initialValue = StartupState.Loading,
        databaseBootstrap,
        appSettingsStore,
        bootstrapAttempt,
    ) {
        val settings = try {
            appSettingsStore.load().getOrDefault(AppSettings()).normalized()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            AppSettings()
        }
        value = StartupState.Resolved(
            settings = settings,
            database = databaseBootstrap.openAndVerify(),
        )
    }

    when (val state = startupState) {
        StartupState.Loading -> StartupTheme { LoadingState() }
        is StartupState.Resolved -> StartupTheme(state.settings) {
            DatabaseBootstrapContent(
                state = state.database,
                databaseBootstrap = databaseBootstrap,
                onRetry = { bootstrapAttempt++ },
            )
        }
    }
}

@Composable
private fun StartupTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit,
) {
    val useDarkTheme = when (settings.theme) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
    AppLanguageProvider(settings.language.toAppLanguage()) {
        PassVaultTheme(
            darkTheme = useDarkTheme,
            accent = settings.accentColor.toPassVaultAccent(),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
                content = content,
            )
        }
    }
}

@Composable
private fun DatabaseBootstrapContent(
    state: VaultDatabaseBootstrapResult,
    databaseBootstrap: VaultDatabaseBootstrap,
    onRetry: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isPreserving by remember(state) { mutableStateOf(false) }
    var preservationFailed by remember(state) { mutableStateOf(false) }

    when (state) {
        VaultDatabaseBootstrapResult.Ready -> VerifiedPassVaultApp()
        VaultDatabaseBootstrapResult.Unavailable -> ErrorState(onAction = onRetry)
        is VaultDatabaseBootstrapResult.RecoveryRequired -> DatabaseRecoveryState(
            canPreserveAndReset = state.canPreserveAndReset,
            isPreserving = isPreserving,
            preservationFailed = preservationFailed,
            onRetry = onRetry,
            onPreserveAndReset = {
                if (!isPreserving) {
                    isPreserving = true
                    preservationFailed = false
                    scope.launch {
                        val result = databaseBootstrap.preserveAndReset()
                        isPreserving = false
                        if (result.isSuccess) onRetry() else preservationFailed = true
                    }
                }
            },
        )
    }
}

@Composable
private fun VerifiedPassVaultApp() {
    val settingsViewModel: SettingsViewModel = koinInject()
    val settingsState by settingsViewModel.state.collectAsState()
    val useDarkTheme = when (settingsState.theme) {
        SettingsViewModel.AppTheme.LIGHT -> false
        SettingsViewModel.AppTheme.DARK -> true
        SettingsViewModel.AppTheme.SYSTEM -> isSystemInDarkTheme()
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
                KeyboardDismissButton(modifier = Modifier.align(Alignment.BottomEnd))
            }
        }
    }
}

private sealed interface StartupState {
    data object Loading : StartupState

    data class Resolved(
        val settings: AppSettings,
        val database: VaultDatabaseBootstrapResult,
    ) : StartupState
}

private fun LanguagePreference.toAppLanguage(): SettingsViewModel.AppLanguage = when (this) {
    LanguagePreference.SYSTEM -> SettingsViewModel.AppLanguage.SYSTEM
    LanguagePreference.ENGLISH -> SettingsViewModel.AppLanguage.ENGLISH
    LanguagePreference.ARABIC -> SettingsViewModel.AppLanguage.ARABIC
}

private fun AccentColorPreference.toPassVaultAccent(): PassVaultAccent = when (this) {
    AccentColorPreference.NEUTRAL -> PassVaultAccent.NEUTRAL
    AccentColorPreference.SAGE -> PassVaultAccent.SAGE
    AccentColorPreference.BLUE -> PassVaultAccent.BLUE
    AccentColorPreference.PURPLE -> PassVaultAccent.PURPLE
    AccentColorPreference.ROSE -> PassVaultAccent.ROSE
    AccentColorPreference.AMBER -> PassVaultAccent.AMBER
}

@Composable
private fun AppContent() {
    val vaultRepository: VaultRepository = koinInject()
    val securityCoordinator: VaultUiSecurityCoordinator = koinInject()
    val requestedSecurityEpoch by securityCoordinator.requestedEpoch.collectAsState()
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
        is InitialRouteState.Ready -> PassVaultNavigationHost(
            initialRoute = state.route,
            vaultRepository = vaultRepository,
            clipboardService = koinInject(),
            vaultUiSecurityCoordinator = securityCoordinator,
            requestedSecurityEpoch = requestedSecurityEpoch,
        )
    }
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

/**
 * Non-sensitive state retained by the session cleanup observer.
 *
 * The lock reason distinguishes a normal `Locking(reason) -> Locked(reason)`
 * completion from a superseding lock whose intermediate state was conflated.
 */
internal data class SessionCleanupObservation(
    val phase: SessionPhase = SessionPhase.UNINITIALIZED,
    val lockingReason: LockReason? = null,
)

internal fun VaultSessionState.toSessionCleanupObservation(): SessionCleanupObservation =
    SessionCleanupObservation(
        phase = toSessionPhase(),
        lockingReason = (this as? VaultSessionState.Locking)?.reason,
    )

internal data class SessionCleanupPolicy(
    val clearSensitiveUiState: Boolean,
    val clearUnlockUiState: Boolean,
    val clearClipboard: Boolean,
    val preserveBackupRestore: Boolean,
)

/**
 * StateFlow may conflate Locking -> Locked, so a terminal lock reason also
 * proves a completed security transition and requires UI scrubbing.
 */
internal fun sessionCleanupPolicy(
    sessionState: VaultSessionState,
    previousSessionObservation: SessionCleanupObservation = SessionCleanupObservation(),
    restoreInProgress: Boolean = false,
    preserveClipboardOnBackgroundLock: Boolean = false,
): SessionCleanupPolicy {
    val completedLockReason = (sessionState as? VaultSessionState.Locked)?.reason
    val observedCompletedLock = completedLockReason != null &&
        previousSessionObservation.phase == SessionPhase.LOCKING &&
        previousSessionObservation.lockingReason == completedLockReason
    val cleanupReason = when {
        sessionState is VaultSessionState.Locking -> sessionState.reason
        completedLockReason != null && !observedCompletedLock -> completedLockReason
        else -> null
    }
    return if (cleanupReason != null) {
        SessionCleanupPolicy(
            clearSensitiveUiState = true,
            clearUnlockUiState = true,
            clearClipboard = !preserveClipboardOnBackgroundLock || cleanupReason != LockReason.Background,
            preserveBackupRestore = cleanupReason == LockReason.Restore && restoreInProgress,
        )
    } else {
        SessionCleanupPolicy(
            clearSensitiveUiState = false,
            clearUnlockUiState = false,
            clearClipboard = false,
            preserveBackupRestore = false,
        )
    }
}
