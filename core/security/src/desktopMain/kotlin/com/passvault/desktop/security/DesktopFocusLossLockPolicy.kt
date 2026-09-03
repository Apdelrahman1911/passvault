package com.passvault.desktop.security

internal sealed interface DesktopFocusLossLockDecision {
    data object None : DesktopFocusLossLockDecision
    data object Lock : DesktopFocusLossLockDecision
    data class Schedule(val delayMillis: Long) : DesktopFocusLossLockDecision {
        init {
            require(delayMillis > 0L)
        }
    }
}

/**
 * Accumulates time spent outside PassVault across focus changes.
 *
 * A native biometric prompt may defer a due focus lock, but only for one
 * bounded interval measured from the first focus loss observed while that
 * prompt is active.
 */
internal class DesktopFocusLossLockPolicy(
    private val monotonicTimeMillis: () -> Long,
) {
    private var enabled = false
    private var focusLossDelayMillis = 0L
    private var maximumSuppressedFocusLossMillis = 0L
    private var accumulatedFocusLossMillis = 0L
    private var currentFocusLossStartedAtMillis: Long? = null
    private var suppressionStartedAtMillis: Long? = null
    private var hasObservedFocusLoss = false

    fun configure(
        enabled: Boolean,
        focusLossDelayMillis: Long,
        maximumSuppressedFocusLossMillis: Long,
        suppressed: Boolean,
    ): DesktopFocusLossLockDecision {
        val wasEnabled = this.enabled
        this.enabled = enabled
        this.focusLossDelayMillis = focusLossDelayMillis.coerceAtLeast(0L)
        this.maximumSuppressedFocusLossMillis = maximumSuppressedFocusLossMillis.coerceAtLeast(0L)
        if (!enabled) {
            resetTiming()
            return DesktopFocusLossLockDecision.None
        }
        if (!wasEnabled) resetTiming()
        return evaluate(monotonicTimeMillis(), suppressed)
    }

    fun onFocusLost(suppressed: Boolean): DesktopFocusLossLockDecision {
        if (!enabled) return DesktopFocusLossLockDecision.None
        val now = monotonicTimeMillis()
        hasObservedFocusLoss = true
        if (currentFocusLossStartedAtMillis == null) currentFocusLossStartedAtMillis = now
        if (suppressed && suppressionStartedAtMillis == null) suppressionStartedAtMillis = now
        return evaluate(now, suppressed)
    }

    fun onFocusGained(suppressed: Boolean): DesktopFocusLossLockDecision {
        if (!enabled) return DesktopFocusLossLockDecision.None
        val now = monotonicTimeMillis()
        accumulatedFocusLossMillis = elapsedFocusLossMillis(now)
        currentFocusLossStartedAtMillis = null
        return evaluate(now, suppressed)
    }

    fun onSuppressionEnded(): DesktopFocusLossLockDecision {
        suppressionStartedAtMillis = null
        return if (enabled) evaluate(monotonicTimeMillis(), suppressed = false) else DesktopFocusLossLockDecision.None
    }

    fun onTimerFired(suppressed: Boolean): DesktopFocusLossLockDecision =
        if (enabled) evaluate(monotonicTimeMillis(), suppressed) else DesktopFocusLossLockDecision.None

    fun resetEpisode() {
        resetTiming()
    }

    fun reset() {
        enabled = false
        focusLossDelayMillis = 0L
        maximumSuppressedFocusLossMillis = 0L
        resetTiming()
    }

    private fun evaluate(
        now: Long,
        suppressed: Boolean,
    ): DesktopFocusLossLockDecision {
        val elapsedFocusLoss = elapsedFocusLossMillis(now)
        return when {
            !hasObservedFocusLoss -> DesktopFocusLossLockDecision.None
            elapsedFocusLoss < focusLossDelayMillis -> currentFocusLossStartedAtMillis?.let {
                DesktopFocusLossLockDecision.Schedule(focusLossDelayMillis - elapsedFocusLoss)
            } ?: DesktopFocusLossLockDecision.None
            !suppressed -> {
                suppressionStartedAtMillis = null
                DesktopFocusLossLockDecision.Lock
            }
            else -> suppressedDecision(now)
        }
    }

    private fun suppressedDecision(now: Long): DesktopFocusLossLockDecision {
        val suppressionStart = suppressionStartedAtMillis
            ?: currentFocusLossStartedAtMillis
            ?: now
        suppressionStartedAtMillis = suppressionStart
        val suppressedFor = elapsedSince(suppressionStart, now)
        return if (suppressedFor >= maximumSuppressedFocusLossMillis) {
            DesktopFocusLossLockDecision.Lock
        } else {
            DesktopFocusLossLockDecision.Schedule(maximumSuppressedFocusLossMillis - suppressedFor)
        }
    }

    private fun elapsedFocusLossMillis(now: Long): Long {
        val currentSegment = currentFocusLossStartedAtMillis?.let { startedAt ->
            elapsedSince(startedAt, now)
        } ?: 0L
        return saturatedAdd(accumulatedFocusLossMillis, currentSegment)
    }

    private fun resetTiming() {
        accumulatedFocusLossMillis = 0L
        currentFocusLossStartedAtMillis = null
        suppressionStartedAtMillis = null
        hasObservedFocusLoss = false
    }
}

private fun elapsedSince(startedAt: Long, now: Long): Long = (now - startedAt).coerceAtLeast(0L)

private fun saturatedAdd(left: Long, right: Long): Long =
    if (right > Long.MAX_VALUE - left) Long.MAX_VALUE else left + right
