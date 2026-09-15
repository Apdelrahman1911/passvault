package com.passvault.android.picker

import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AndroidPickerHostStateTest {
    private val ownerThread = Thread.currentThread()

    @Test
    fun attachPublishesOnlyACompleteLauncherSet() {
        val state = state()
        val activity = FakeActivity()
        val launchers = FakeLaunchers()

        state.attach(activity) {
            assertNull(state.launchersOrNull())
            launchers
        }

        assertSame(launchers, state.launchersOrNull())
    }

    @Test
    fun failedRegistrationDoesNotPublishAPartialHost() {
        val state = state()

        assertFailsWith<IllegalStateException> {
            state.attach(FakeActivity()) { error("registration failed") }
        }

        assertNull(state.launchersOrNull())
        state.attach(FakeActivity()) { FakeLaunchers() }
    }

    @Test
    fun failedReplacementKeepsTheFinishingHostConsistent() {
        val state = state()
        val oldActivity = FakeActivity()
        val oldLaunchers = FakeLaunchers()
        state.attach(oldActivity) { oldLaunchers }
        oldActivity.finishing = true

        assertFailsWith<IllegalStateException> {
            state.attach(FakeActivity()) { error("registration failed") }
        }

        assertSame(oldLaunchers, state.launchersOrNull())
        assertTrue(state.detach(oldActivity, isChangingConfigurations = false).cancelPending)
    }

    @Test
    fun duplicateAttachDoesNotRegisterLaunchersTwice() {
        val state = state()
        val activity = FakeActivity()
        var registrations = 0

        state.attach(activity) {
            registrations++
            FakeLaunchers()
        }
        state.attach(activity) {
            registrations++
            FakeLaunchers()
        }

        assertEquals(1, registrations)
    }

    @Test
    fun liveActivityCannotBeReplaced() {
        val state = state()
        state.attach(FakeActivity()) { FakeLaunchers() }

        assertFailsWith<IllegalStateException> {
            state.attach(FakeActivity()) { FakeLaunchers() }
        }
    }

    @Test
    fun staleDetachCannotClearAConfigurationReplacement() {
        val state = state()
        val oldActivity = FakeActivity()
        val newActivity = FakeActivity()
        val replacementLaunchers = FakeLaunchers()
        state.attach(oldActivity) { FakeLaunchers() }
        oldActivity.finishing = true
        state.attach(newActivity) { replacementLaunchers }

        val staleDecision = state.detach(oldActivity, isChangingConfigurations = true)

        assertFalse(staleDecision.detached)
        assertFalse(staleDecision.cancelPending)
        assertSame(replacementLaunchers, state.launchersOrNull())
    }

    @Test
    fun configurationDetachPreservesThePendingPickerForReattachment() {
        val state = state()
        val oldActivity = FakeActivity()
        state.attach(oldActivity) { FakeLaunchers() }

        val decision = state.detach(oldActivity, isChangingConfigurations = true)

        assertTrue(decision.detached)
        assertFalse(decision.cancelPending)
        assertNull(state.launchersOrNull())
        state.attach(FakeActivity()) { FakeLaunchers() }
    }

    @Test
    fun finalDetachRequestsPendingPickerCancellation() {
        val state = state()
        val activity = FakeActivity()
        state.attach(activity) { FakeLaunchers() }

        val decision = state.detach(activity, isChangingConfigurations = false)

        assertTrue(decision.detached)
        assertTrue(decision.cancelPending)
        assertNull(state.launchersOrNull())
    }

    @Test
    fun accessFromAnotherThreadFailsBeforeMutation() {
        val state = state()
        val failure = AtomicReference<Throwable?>()

        thread {
            failure.set(runCatching { state.attach(FakeActivity()) { FakeLaunchers() } }.exceptionOrNull())
        }.join()

        assertTrue(failure.get() is IllegalStateException)
        assertNull(state.launchersOrNull())
    }

    private fun state() = AndroidPickerHostState<FakeActivity, FakeLaunchers>(
        description = "test picker",
        isFinishing = FakeActivity::finishing,
        assertOwnerThread = {
            check(Thread.currentThread() === ownerThread) { "wrong thread" }
        },
    )

    private class FakeActivity(var finishing: Boolean = false)

    private class FakeLaunchers
}
