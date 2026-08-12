package com.passvault.core.security

/**
 * Interface for secure clipboard operations.
 * Automatically clears sensitive data after a timeout.
 */
interface ClipboardService {
    /**
     * Copy sensitive text to clipboard.
     * The text will be automatically cleared after the timeout.
     *
     * @param text The sensitive text to copy
     * @param timeoutMs Time in milliseconds before clearing (default: 30 seconds)
     * The expiry timer is deliberately implementation-owned so callers cannot
     * accidentally cancel the security cleanup while leaving the secret in
     * the clipboard.
     */
    suspend fun copySensitive(text: String, timeoutMs: Long = 30_000L)

    /**
     * Copy text to clipboard without auto-clear.
     * Use for non-sensitive data only.
     */
    suspend fun copy(text: String)

    /**
     * Clear the clipboard.
     */
    suspend fun clear()

    /**
     * Check if clipboard contains potentially sensitive data.
     */
    suspend fun containsSensitive(): Boolean
}
