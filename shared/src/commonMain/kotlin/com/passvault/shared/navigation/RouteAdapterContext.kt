package com.passvault.shared.navigation

import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.navigation.AppNavigator
import com.passvault.core.navigation.NavigationMutation
import com.passvault.core.navigation.NavigationRejection
import com.passvault.core.navigation.NavigationToken
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.security.ClipboardService
import com.passvault.feature.backup.presentation.BackupViewModel
import com.passvault.feature.onboarding.presentation.OnboardingViewModel
import com.passvault.feature.settings.presentation.SettingsViewModel
import com.passvault.feature.unlock.presentation.UnlockViewModel
import com.passvault.feature.vault.presentation.VaultViewModel
import com.passvault.shared.platform.preservesSensitiveClipboardOnBackgroundLock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

internal class RouteAdapterContext(
    val navigator: AppNavigator,
    val backCoordinator: NavigationBackCoordinator,
    val vaultRepository: VaultRepository,
    val clipboardService: ClipboardService,
    val vaultViewModel: VaultViewModel,
    val settingsViewModel: SettingsViewModel,
    val backupViewModel: BackupViewModel,
    val onboardingViewModel: OnboardingViewModel,
    val unlockViewModel: UnlockViewModel,
) {
    fun push(route: PassVaultRoute, token: NavigationToken) {
        if (!backCoordinator.canLeaveForForwardNavigation()) return
        navigator.pushSingleTop(route, token).checkExpected()
    }

    fun popAfterGuard(token: NavigationToken) {
        navigator.popAfterGuard(token).checkExpected()
    }

    fun replaceCurrent(route: PassVaultRoute, token: NavigationToken) {
        navigator.replaceCurrentWith(route, token).checkExpected()
    }

    fun popThenEnsure(route: PassVaultRoute, token: NavigationToken) {
        navigator.popThenEnsure(route, token).checkExpected()
    }

    suspend fun copySensitive(text: String): Boolean = try {
        if (vaultRepository.getSessionState().first() !is VaultSessionState.Unlocked) return false
        com.passvault.shared.copySensitiveWhileUnlocked(
            sessionState = vaultRepository.getSessionState(),
            clipboardService = clipboardService,
            text = text,
            timeoutMs = settingsViewModel.state.value.clipboardClearSeconds * 1_000L,
            preserveClipboardOnBackgroundLock = preservesSensitiveClipboardOnBackgroundLock(),
        )
        true
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        false
    }

    fun openCredential(id: CredentialId, token: NavigationToken) {
        push(com.passvault.core.navigation.VaultRoute.CredentialDetail(id.value), token)
    }
}

internal fun NavigationMutation.checkExpected() {
    if (this !is NavigationMutation.Rejected) return
    when (reason) {
        NavigationRejection.Duplicate,
        NavigationRejection.AtRoot,
        NavigationRejection.HostInactive,
        NavigationRejection.EntryInactive,
        NavigationRejection.StaleSession,
        NavigationRejection.VaultLocked,
        NavigationRejection.StaleExternalDelivery,
        -> Unit
        NavigationRejection.InvalidDestination,
        NavigationRejection.UnauthorizedExternalInput,
        NavigationRejection.DuplicateExternalDelivery,
        -> error("Unexpected navigation rejection: $reason")
    }
}
