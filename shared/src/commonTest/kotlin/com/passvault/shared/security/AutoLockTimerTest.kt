package com.passvault.shared.security

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AutoLockTimerTest {
    @Test
    fun `inactivity locks exactly at the configured deadline`() = runTest {
        val signal = UserActivitySignal(SchedulerClock(testScheduler))
        var lockCalls = 0
        val timer = launch {
            timer(
                signal = signal,
                scheduler = testScheduler,
                lock = {
                    lockCalls++
                    true
                },
            ).run()
        }

        runCurrent()
        advanceTimeBy(TIMEOUT_MILLIS - 1L)
        runCurrent()
        assertEquals(0, lockCalls)

        advanceTimeBy(1L)
        runCurrent()
        assertEquals(1, lockCalls)
        assertTrue(timer.isCompleted)
    }

    @Test
    fun `activity before the boundary grants a full new interval`() = runTest {
        val signal = UserActivitySignal(SchedulerClock(testScheduler))
        var lockCalls = 0
        val timer = launch {
            timer(
                signal = signal,
                scheduler = testScheduler,
                lock = {
                    lockCalls++
                    true
                },
            ).run()
        }
        runCurrent()

        advanceTimeBy(750L)
        assertTrue(signal.recordActivity())
        runCurrent()
        advanceTimeBy(TIMEOUT_MILLIS - 1L)
        runCurrent()
        assertEquals(0, lockCalls)

        advanceTimeBy(1L)
        runCurrent()
        assertEquals(1, lockCalls)
        assertTrue(timer.isCompleted)
    }

    @Test
    fun `activity timestamp at the boundary cannot postpone locking`() = runTest {
        val activityClock = MutableClock(TIMEOUT_MILLIS)
        val signal = UserActivitySignal(activityClock)
        var lockCalls = 0
        val timer = launch {
            AutoLockTimer(
                activitySignal = signal,
                clock = SchedulerClock(testScheduler),
                timeoutMillis = TIMEOUT_MILLIS,
                lock = {
                    lockCalls++
                    true
                },
                onLockFailed = {},
            ).run()
        }
        runCurrent()

        advanceTimeBy(TIMEOUT_MILLIS / 2L)
        assertTrue(signal.recordActivity())
        runCurrent()

        assertEquals(1, lockCalls)
        assertTrue(timer.isCompleted)
    }

    @Test
    fun `stale activity from a prior session is discarded`() = runTest {
        val signal = UserActivitySignal(SchedulerClock(testScheduler))
        assertTrue(signal.recordActivity())
        advanceTimeBy(500L)
        var lockCalls = 0
        val timer = launch {
            timer(
                signal = signal,
                scheduler = testScheduler,
                lock = {
                    lockCalls++
                    true
                },
            ).run()
        }
        runCurrent()

        advanceTimeBy(TIMEOUT_MILLIS - 1L)
        runCurrent()
        assertEquals(0, lockCalls)

        advanceTimeBy(1L)
        runCurrent()
        assertEquals(1, lockCalls)
        assertTrue(timer.isCompleted)
    }

    @Test
    fun `high volume activity is conflated to one latest timestamp`() {
        val clock = MutableClock(0L)
        val signal = UserActivitySignal(clock)

        repeat(ACTIVITY_BURST_SIZE) { index ->
            clock.now = index.toLong()
            assertTrue(signal.recordActivity())
        }

        assertEquals((ACTIVITY_BURST_SIZE - 1).toLong(), signal.takeLatestActivity())
        assertNull(signal.takeLatestActivity())
    }

    @Test
    fun `lock failure is reported once and rearms only after new activity`() = runTest {
        val signal = UserActivitySignal(SchedulerClock(testScheduler))
        var lockCalls = 0
        var failures = 0
        val timer = launch {
            timer(
                signal = signal,
                scheduler = testScheduler,
                lock = {
                    lockCalls++
                    lockCalls > 1
                },
                onLockFailed = { failures++ },
            ).run()
        }
        runCurrent()

        advanceTimeBy(TIMEOUT_MILLIS)
        runCurrent()
        assertEquals(1, lockCalls)
        assertEquals(1, failures)

        advanceTimeBy(TIMEOUT_MILLIS * 2L)
        runCurrent()
        assertEquals(1, lockCalls)

        assertTrue(signal.recordActivity())
        runCurrent()
        advanceTimeBy(TIMEOUT_MILLIS)
        runCurrent()
        assertEquals(2, lockCalls)
        assertEquals(1, failures)
        assertTrue(timer.isCompleted)
    }

    @Test
    fun `activity arriving during a failed lock attempt rearms from its timestamp`() = runTest {
        val signal = UserActivitySignal(SchedulerClock(testScheduler))
        val allowFirstAttempt = CompletableDeferred<Unit>()
        var lockCalls = 0
        var failures = 0
        val timer = launch {
            timer(
                signal = signal,
                scheduler = testScheduler,
                lock = {
                    lockCalls++
                    if (lockCalls == 1) {
                        allowFirstAttempt.await()
                        false
                    } else {
                        true
                    }
                },
                onLockFailed = { failures++ },
            ).run()
        }
        runCurrent()

        advanceTimeBy(TIMEOUT_MILLIS)
        runCurrent()
        assertEquals(1, lockCalls)
        advanceTimeBy(100L)
        assertTrue(signal.recordActivity())
        allowFirstAttempt.complete(Unit)
        runCurrent()
        assertEquals(1, failures)

        advanceTimeBy(TIMEOUT_MILLIS - 1L)
        runCurrent()
        assertEquals(1, lockCalls)
        advanceTimeBy(1L)
        runCurrent()
        assertEquals(2, lockCalls)
        assertTrue(timer.isCompleted)
    }

    @Test
    fun `cancellation releases the timer and closed signals do not leak waiters`() = runTest {
        val signal = UserActivitySignal(SchedulerClock(testScheduler))
        val cancelledTimer = launch {
            timer(signal = signal, scheduler = testScheduler).run()
        }
        runCurrent()
        cancelledTimer.cancelAndJoin()
        assertTrue(cancelledTimer.isCancelled)

        val closedSignal = UserActivitySignal(SchedulerClock(testScheduler))
        val closedTimer = launch {
            timer(signal = closedSignal, scheduler = testScheduler).run()
        }
        runCurrent()
        closedSignal.close()
        runCurrent()
        assertTrue(closedTimer.isCompleted)
        assertFalse(closedTimer.isCancelled)
    }

    private fun timer(
        signal: UserActivitySignal,
        scheduler: TestCoroutineScheduler,
        lock: suspend () -> Boolean = { true },
        onLockFailed: () -> Unit = {},
    ): AutoLockTimer = AutoLockTimer(
        activitySignal = signal,
        clock = SchedulerClock(scheduler),
        timeoutMillis = TIMEOUT_MILLIS,
        lock = lock,
        onLockFailed = onLockFailed,
    )

    private companion object {
        const val TIMEOUT_MILLIS = 1_000L
        const val ACTIVITY_BURST_SIZE = 100_000
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
private class SchedulerClock(
    private val scheduler: TestCoroutineScheduler,
) : AutoLockClock {
    override fun nowMillis(): Long = scheduler.currentTime
}

private class MutableClock(
    var now: Long,
) : AutoLockClock {
    override fun nowMillis(): Long = now
}
