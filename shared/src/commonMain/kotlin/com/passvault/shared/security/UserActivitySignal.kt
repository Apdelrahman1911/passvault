package com.passvault.shared.security

import kotlinx.coroutines.channels.Channel
import kotlin.time.TimeSource

/** Monotonic time source used by user-activity and auto-lock scheduling. */
internal fun interface AutoLockClock {
    fun nowMillis(): Long
}

/**
 * Non-Compose, bounded signal for user activity.
 *
 * The channel retains only the newest activity timestamp. Producers never
 * suspend, and input bursts therefore cannot build an event backlog.
 */
internal class UserActivitySignal(
    private val clock: AutoLockClock = SystemAutoLockClock,
) {
    private val events = Channel<Long>(capacity = Channel.CONFLATED)

    fun recordActivity(): Boolean = events.trySend(clock.nowMillis()).isSuccess

    suspend fun receiveActivity(): Long = events.receive()

    fun takeLatestActivity(): Long? {
        var latest: Long? = null
        while (true) {
            val next = events.tryReceive().getOrNull() ?: return latest
            latest = next
        }
    }

    fun discardPendingActivity() {
        takeLatestActivity()
    }

    fun close() {
        events.close()
    }
}

internal object SystemAutoLockClock : AutoLockClock {
    private val origin = TimeSource.Monotonic.markNow()

    override fun nowMillis(): Long = origin.elapsedNow().inWholeMilliseconds
}
