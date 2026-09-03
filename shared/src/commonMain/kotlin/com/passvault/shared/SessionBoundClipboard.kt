package com.passvault.shared

import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
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
 * iOS may retain a local-only, OS-expiring item across a background lock so
 * the user can complete a cross-app paste.
 */
internal suspend fun copySensitiveWhileUnlocked(
    sessionState: Flow<VaultSessionState>,
    clipboardService: ClipboardService,
    text: String,
    timeoutMs: Long,
    preserveClipboardOnBackgroundLock: Boolean = false,
) {
    currentCoroutineContext().ensureActive()
    val copyInvalidated = withContext(NonCancellable) {
        val activeSession = sessionState.first() as? VaultSessionState.Unlocked
        check(activeSession != null) {
            "Vault must be unlocked before copying sensitive text"
        }
        clipboardService.copySensitive(text, timeoutMs)
        val currentSession = sessionState.first()
        val preserveAfterBackgroundLock = preserveClipboardOnBackgroundLock &&
            currentSession.isBackgroundLockTransition()
        val copyInvalidated = currentSession != activeSession && !preserveAfterBackgroundLock
        if (copyInvalidated) {
            clipboardService.clear()
        }
        copyInvalidated
    }
    currentCoroutineContext().ensureActive()
    check(!copyInvalidated) {
        "Vault session changed during sensitive clipboard copy"
    }
}

private fun VaultSessionState.isBackgroundLockTransition(): Boolean = when (this) {
    is VaultSessionState.Locking -> reason == LockReason.Background
    is VaultSessionState.Locked -> reason == LockReason.Background
    else -> false
}
