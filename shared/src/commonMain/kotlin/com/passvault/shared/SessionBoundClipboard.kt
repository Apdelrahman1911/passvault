package com.passvault.shared

import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.security.ClipboardService
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * Copies a secret only while the vault session is active.
 *
 * Lock cleanup and a platform clipboard write can otherwise cross: cleanup
 * may finish first, followed by the pending write restoring the secret. The
 * post-copy session check closes that race. Once authorized, the copy and
 * post-check are non-cancellable so caller teardown cannot skip the cleanup.
 */
internal suspend fun copySensitiveWhileUnlocked(
    sessionState: Flow<VaultSessionState>,
    clipboardService: ClipboardService,
    text: String,
    timeoutMs: Long,
) {
    currentCoroutineContext().ensureActive()
    val sessionChangedDuringCopy = withContext(NonCancellable) {
        val activeSession = sessionState.first() as? VaultSessionState.Unlocked
        check(activeSession != null) {
            "Vault must be unlocked before copying sensitive text"
        }
        clipboardService.copySensitive(text, timeoutMs)
        val sessionChanged = sessionState.first() != activeSession
        if (sessionChanged) {
            clipboardService.clear()
        }
        sessionChanged
    }
    currentCoroutineContext().ensureActive()
    check(!sessionChangedDuringCopy) {
        "Vault session changed during sensitive clipboard copy"
    }
}
