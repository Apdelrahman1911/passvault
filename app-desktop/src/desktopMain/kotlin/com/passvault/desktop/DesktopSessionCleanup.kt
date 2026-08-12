package com.passvault.desktop

import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.domain.repository.lockWithBoundedRetry
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.VaultUiSecurityCoordinator
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

internal fun areDesktopSessionActionsEnabled(sessionState: VaultSessionState): Boolean =
    sessionState is VaultSessionState.Unlocked

internal suspend fun lockAndClear(
    vaultRepository: VaultRepository,
    clipboardService: ClipboardService,
): Boolean = withContext(NonCancellable) {
    val lockSucceeded = try {
        vaultRepository.lockWithBoundedRetry()
    } catch (_: Exception) {
        false
    } finally {
        try {
            clipboardService.clear()
        } catch (_: Exception) {
            // Clipboard cleanup is best effort when the provider is unavailable.
        }
    }
    lockSucceeded
}

internal suspend fun lockClearAndAwaitUiSecurity(
    vaultRepository: VaultRepository,
    clipboardService: ClipboardService,
    vaultUiSecurityCoordinator: VaultUiSecurityCoordinator,
): Boolean {
    if (!lockAndClear(vaultRepository, clipboardService)) return false
    val requestEpoch = vaultUiSecurityCoordinator.requestAcknowledgement()
    return withTimeoutOrNull(UI_SECURITY_ACK_TIMEOUT_MS) {
        vaultUiSecurityCoordinator.awaitAcknowledgement(requestEpoch)
        true
    } == true
}

private const val UI_SECURITY_ACK_TIMEOUT_MS = 5_000L
