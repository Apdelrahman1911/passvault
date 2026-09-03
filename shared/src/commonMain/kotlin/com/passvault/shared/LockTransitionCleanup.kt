package com.passvault.shared

import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.navigation.PassVaultRoute
import com.passvault.core.navigation.requiresUnlockedVault
import com.passvault.core.security.ClipboardService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

/**
 * Clears clipboard state when the platform lock policy requires it.
 * The session-keyed Compose effect is cancelled when the terminal state
 * arrives, so this small security boundary must outlive that cancellation.
 */
internal suspend fun ClipboardService.clearForLockTransition() {
    withContext(NonCancellable) {
        try {
            withTimeout(LOCK_CLIPBOARD_CLEAR_TIMEOUT_MS) { clear() }
        } catch (_: CancellationException) {
            // A bounded timeout is best effort; session and UI state are
            // already fail-closed even if the platform clipboard disappears.
        } catch (_: Exception) {
            // Clipboard providers can disappear while the app backgrounds.
        }
    }
}

internal fun shouldAcknowledgeVaultUiSecurity(
    requestedEpoch: Long,
    acknowledgedEpoch: Long,
    sessionState: VaultSessionState,
    route: PassVaultRoute?,
): Boolean = requestedEpoch > acknowledgedEpoch &&
    sessionState is VaultSessionState.Locked &&
    route?.requiresUnlockedVault() != true

private const val LOCK_CLIPBOARD_CLEAR_TIMEOUT_MS = 2_000L
