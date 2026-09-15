package com.passvault.shared

/**
 * Tracks one native background-cleanup episode by token identity.
 *
 * Native lifecycle callbacks are confined to their platform main thread. A
 * completion queued by an older episode must never clear a replacement cleanup
 * or reveal the native privacy cover for the newer episode.
 */
internal class BackgroundCleanupEpisode<T : Any> {
    var cleanupRequested: Boolean = false
        private set

    var currentCleanup: T? = null
        private set

    fun requestCleanup(): Boolean {
        if (cleanupRequested) return false

        cleanupRequested = true
        currentCleanup = null
        return true
    }

    fun attachCleanup(cleanup: T) {
        check(cleanupRequested) { "A cleanup can only be attached to a requested episode" }
        check(currentCleanup == null) { "An active cleanup cannot be replaced without an identity check" }
        currentCleanup = cleanup
    }

    fun markRetryableIfCurrent(failedCleanup: T): Boolean {
        if (!cleanupRequested || currentCleanup !== failedCleanup) return false

        currentCleanup = null
        return true
    }

    fun completeIfCurrent(completedCleanup: T): Boolean {
        if (!cleanupRequested || currentCleanup !== completedCleanup) return false

        currentCleanup = null
        cleanupRequested = false
        return true
    }

    /** Discards tokens owned by a runtime that has already been torn down. */
    fun reset() {
        cleanupRequested = false
        currentCleanup = null
    }
}
