package com.passvault.shared.security

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Waits for a full inactivity interval and crosses the auto-lock boundary.
 *
 * Session and settings ownership stays with the caller. A new invocation is
 * created only when those authoritative values change; ordinary activity is
 * handled inside this loop without restarting a Compose effect.
 */
internal class AutoLockTimer(
    private val activitySignal: UserActivitySignal,
    private val clock: AutoLockClock = SystemAutoLockClock,
    private val timeoutMillis: Long,
    private val retryDelayMillis: Long = AUTO_LOCK_RETRY_DELAY_MILLIS,
    private val lock: suspend () -> Boolean,
    private val onLockFailed: () -> Unit,
) {
    init {
        require(timeoutMillis > 0L) { "Auto-lock timeout must be positive" }
        require(retryDelayMillis > 0L) { "Auto-lock retry delay must be positive" }
    }

    suspend fun run() {
        activitySignal.discardPendingActivity()
        var deadline = deadlineAfter(clock.nowMillis())
        try {
            while (true) {
                val remaining = remainingUntil(deadline, clock.nowMillis())
                val activity = if (remaining > 0L) {
                    withTimeoutOrNull(remaining) { activitySignal.receiveActivity() }
                } else {
                    null
                }
                val pendingActivity = activitySignal.takeLatestActivity()
                val latestActivity = pendingActivity ?: activity
                if (latestActivity != null && latestActivity < deadline) {
                    deadline = deadlineAfter(latestActivity)
                } else {
                    if (lock()) return
                    onLockFailed()

                    // A failed auto-lock must not wait for the activity that
                    // auto-lock is intended to protect against. Activity that
                    // arrived during the lock attempt still earns a normal
                    // inactivity interval; otherwise retry on our own timer.
                    deadline = latestActivity?.let(::deadlineAfter)
                        ?: retryDeadlineAfter(clock.nowMillis())
                }
            }
        } catch (_: ClosedReceiveChannelException) {
            // Composition disposal closes the signal after cancelling the
            // owning effect. A close that wins that race is ordinary teardown.
        }
    }

    private fun deadlineAfter(activityMillis: Long): Long =
        if (activityMillis > Long.MAX_VALUE - timeoutMillis) {
            Long.MAX_VALUE
        } else {
            activityMillis + timeoutMillis
        }

    private fun remainingUntil(deadlineMillis: Long, nowMillis: Long): Long =
        if (nowMillis >= deadlineMillis) 0L else deadlineMillis - nowMillis

    private fun retryDeadlineAfter(nowMillis: Long): Long =
        if (nowMillis > Long.MAX_VALUE - retryDelayMillis) Long.MAX_VALUE else nowMillis + retryDelayMillis

    private companion object {
        const val AUTO_LOCK_RETRY_DELAY_MILLIS = 5_000L
    }
}
