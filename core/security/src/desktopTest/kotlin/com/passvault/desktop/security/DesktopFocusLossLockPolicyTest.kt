package com.passvault.desktop.security

import kotlin.test.Test
import kotlin.test.assertEquals

class DesktopFocusLossLockPolicyTest {
    @Test
    fun `configuration does not lock before an external focus loss`() {
        val clock = TestMonotonicClock()
        val policy = DesktopFocusLossLockPolicy(clock::read)

        val configured = policy.configure(
            enabled = true,
            focusLossDelayMillis = 0L,
            maximumSuppressedFocusLossMillis = 60L,
            suppressed = false,
        )

        assertEquals(DesktopFocusLossLockDecision.None, configured)
        assertEquals(DesktopFocusLossLockDecision.Lock, policy.onFocusLost(suppressed = false))
    }

    @Test
    fun `separate focus losses consume one cumulative budget`() {
        val clock = TestMonotonicClock()
        val policy = configuredPolicy(clock)

        assertEquals(DesktopFocusLossLockDecision.Schedule(30L), policy.onFocusLost(suppressed = false))
        clock.nowMillis = 20L
        assertEquals(DesktopFocusLossLockDecision.None, policy.onFocusGained(suppressed = false))

        clock.nowMillis = 1_000L
        assertEquals(DesktopFocusLossLockDecision.Schedule(10L), policy.onFocusLost(suppressed = false))
        clock.nowMillis = 1_010L
        assertEquals(DesktopFocusLossLockDecision.Lock, policy.onFocusGained(suppressed = false))
    }

    @Test
    fun `focused time does not consume the focus loss budget`() {
        val clock = TestMonotonicClock()
        val policy = configuredPolicy(clock)

        policy.onFocusLost(suppressed = false)
        clock.nowMillis = 10L
        policy.onFocusGained(suppressed = false)
        clock.nowMillis = 10_000L

        assertEquals(DesktopFocusLossLockDecision.Schedule(20L), policy.onFocusLost(suppressed = false))
    }

    @Test
    fun `active biometric suppression cannot exceed its absolute deadline`() {
        val clock = TestMonotonicClock()
        val policy = configuredPolicy(clock)

        assertEquals(DesktopFocusLossLockDecision.Schedule(30L), policy.onFocusLost(suppressed = true))
        clock.nowMillis = 30L
        assertEquals(DesktopFocusLossLockDecision.Schedule(30L), policy.onTimerFired(suppressed = true))
        clock.nowMillis = 59L
        assertEquals(DesktopFocusLossLockDecision.Schedule(1L), policy.onTimerFired(suppressed = true))
        clock.nowMillis = 60L
        assertEquals(DesktopFocusLossLockDecision.Lock, policy.onTimerFired(suppressed = true))
    }

    @Test
    fun `ending suppression enforces an already exhausted focus loss budget`() {
        val clock = TestMonotonicClock()
        val policy = configuredPolicy(clock)

        policy.onFocusLost(suppressed = true)
        clock.nowMillis = 35L

        assertEquals(DesktopFocusLossLockDecision.Lock, policy.onSuppressionEnded())
    }

    @Test
    fun `disabling focus locking clears elapsed time from the prior session`() {
        val clock = TestMonotonicClock()
        val policy = configuredPolicy(clock)

        policy.onFocusLost(suppressed = false)
        clock.nowMillis = 20L
        policy.onFocusGained(suppressed = false)
        policy.configure(
            enabled = false,
            focusLossDelayMillis = 30L,
            maximumSuppressedFocusLossMillis = 60L,
            suppressed = false,
        )
        policy.configure(
            enabled = true,
            focusLossDelayMillis = 30L,
            maximumSuppressedFocusLossMillis = 60L,
            suppressed = false,
        )

        assertEquals(DesktopFocusLossLockDecision.Schedule(30L), policy.onFocusLost(suppressed = false))
    }

    private fun configuredPolicy(clock: TestMonotonicClock): DesktopFocusLossLockPolicy =
        DesktopFocusLossLockPolicy(clock::read).also { policy ->
            assertEquals(
                DesktopFocusLossLockDecision.None,
                policy.configure(
                    enabled = true,
                    focusLossDelayMillis = 30L,
                    maximumSuppressedFocusLossMillis = 60L,
                    suppressed = false,
                ),
            )
        }
}

private class TestMonotonicClock {
    var nowMillis: Long = 0L

    fun read(): Long = nowMillis
}
