package com.passvault.core.domain.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/**
 * Makes a bounded number of attempts to cross the vault lock boundary.
 *
 * A failed `Result` and an ordinary implementation exception are both
 * retryable. Coroutine cancellation is never converted into a lock result so
 * platform lifecycle owners can keep their native privacy surface fail-closed
 * and decide when to start a replacement cleanup.
 */
suspend fun VaultRepository.lockWithBoundedRetry(reason: LockReason = LockReason.Manual): Boolean {
    repeat(MAX_VAULT_LOCK_ATTEMPTS) { attemptIndex ->
        val locked = try {
            lock(reason).isSuccess
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            false
        }
        if (locked) return true

        if (attemptIndex < MAX_VAULT_LOCK_ATTEMPTS - 1) {
            delay(VAULT_LOCK_RETRY_DELAY_MS * (attemptIndex + 1L))
        }
    }
    return false
}

private const val MAX_VAULT_LOCK_ATTEMPTS = 3
private const val VAULT_LOCK_RETRY_DELAY_MS = 100L
