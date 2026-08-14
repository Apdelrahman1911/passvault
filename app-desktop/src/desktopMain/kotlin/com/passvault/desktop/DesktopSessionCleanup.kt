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
    val lockSucceeded = lockVaultForShutdown(vaultRepository)
    clearClipboardForShutdown(clipboardService)
    lockSucceeded
}

/**
 * Crosses the repository's normal lock boundary without allowing caller
 * cancellation to skip key wiping. Desktop process shutdown races this work
 * against a separate terminal deadline; this function deliberately keeps the
 * stronger non-cancellable semantics used by ordinary background locking.
 */
internal suspend fun lockVaultForShutdown(vaultRepository: VaultRepository): Boolean =
    withContext(NonCancellable) {
        try {
            vaultRepository.lockWithBoundedRetry()
        } catch (_: Exception) {
            false
        }
    }

/** Clears only clipboard content still owned by PassVault. */
internal suspend fun clearClipboardForShutdown(clipboardService: ClipboardService) =
    withContext(NonCancellable) {
        try {
            clipboardService.clear()
        } catch (_: Exception) {
            // Clipboard cleanup is best effort when the native provider is unavailable.
        }
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
