package com.passvault.android.lifecycle

import com.passvault.core.domain.repository.LockReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidLifecycleLockPolicyTest {

    @Test
    fun configurationChangesDoNotCoverOrLockTheVault() {
        val policy = AndroidLifecycleLockPolicy()

        val decision = policy.onActivityStopped(isChangingConfigurations = true)

        assertEquals(AndroidLifecycleLockDecision(), decision)
        assertFalse(policy.privacyCoverVisible)
    }

    @Test
    fun ordinaryBackgroundTransitionCoversAndRequestsExactlyOneLock() {
        val policy = AndroidLifecycleLockPolicy()

        val first = policy.onActivityStopped(isChangingConfigurations = false)
        val repeated = policy.onActivityStopped(isChangingConfigurations = false)

        assertTrue(policy.privacyCoverVisible)
        assertTrue(first.cancelGracePeriod)
        assertTrue(first.startLock)
        assertEquals(LockReason.Background, first.lockReason)
        assertFalse(repeated.startLock)
    }

    @Test
    fun resumeKeepsCoverUntilInFlightLockCompletes() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onActivityStopped(isChangingConfigurations = false)

        val resumed = policy.onActivityResumed()

        assertTrue(resumed.cancelGracePeriod)
        assertTrue(policy.privacyCoverVisible)

        policy.onLockFinished()
        assertTrue(policy.privacyCoverVisible)
        policy.onSensitiveContentSecured()
        assertFalse(policy.privacyCoverVisible)
    }

    @Test
    fun completedBackgroundLockRemainsCoveredUntilResume() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onActivityStopped(isChangingConfigurations = false)

        policy.onLockFinished()
        policy.onSensitiveContentSecured()

        assertTrue(policy.privacyCoverVisible)
        policy.onActivityResumed()
        assertFalse(policy.privacyCoverVisible)
    }

    @Test
    fun resumedCoverWaitsWhenContentIsSecuredBeforeRepositoryLockFinishes() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onActivityStopped(isChangingConfigurations = false)
        policy.onActivityResumed()

        policy.onSensitiveContentSecured()
        assertTrue(policy.privacyCoverVisible)

        policy.onLockFinished()
        assertFalse(policy.privacyCoverVisible)
    }

    @Test
    fun resumedCoverWaitsWhenRepositoryLockFinishesBeforeContentIsSecured() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onActivityStopped(isChangingConfigurations = false)
        policy.onActivityResumed()

        policy.onLockFinished()
        assertTrue(policy.privacyCoverVisible)

        policy.onSensitiveContentSecured()
        assertFalse(policy.privacyCoverVisible)
    }

    @Test
    fun staleSecuredStateCannotReleaseALaterLockEpisode() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onActivityStopped(isChangingConfigurations = false)
        policy.onSensitiveContentSecured()
        policy.onLockFinished()
        policy.onActivityResumed()
        assertFalse(policy.privacyCoverVisible)

        policy.onActivityStopped(isChangingConfigurations = false)
        policy.onActivityResumed()
        policy.onLockFinished()

        assertTrue(policy.privacyCoverVisible)
        policy.onSensitiveContentSecured()
        assertFalse(policy.privacyCoverVisible)
    }

    @Test
    fun foregroundMemoryPressureCoversUntilLockAndUIAreBothSecured() {
        val policy = AndroidLifecycleLockPolicy()

        val decision = policy.onMemoryPressure()

        assertTrue(decision.startLock)
        assertEquals(LockReason.MemoryPressure, decision.lockReason)
        assertTrue(policy.privacyCoverVisible)
        policy.onLockFinished()
        assertTrue(policy.privacyCoverVisible)
        policy.onSensitiveContentSecured()
        assertFalse(policy.privacyCoverVisible)
    }

    @Test
    fun failedBackgroundSecurityBoundaryRemainsCoveredAndRetriesOnResume() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onActivityStopped(isChangingConfigurations = false)
        policy.onActivityResumed()

        policy.onSecurityFailed(LockReason.Background)

        assertTrue(policy.privacyCoverVisible)
        val retry = policy.onActivityResumed()
        assertTrue(retry.startLock)
        assertEquals(LockReason.Background, retry.lockReason)
        assertTrue(policy.privacyCoverVisible)
    }

    @Test
    fun failedMemoryPressureBoundaryRetainsItsReasonForLifecycleRetry() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onMemoryPressure()

        policy.onSecurityFailed(LockReason.MemoryPressure)

        val retry = policy.onActivityResumed()
        assertTrue(retry.startLock)
        assertEquals(LockReason.MemoryPressure, retry.lockReason)
        assertTrue(policy.privacyCoverVisible)
    }

    @Test
    fun staleCompletionCannotRevealAnEpisodeWhoseSecurityBoundaryFailed() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onMemoryPressure()
        policy.onSecurityFailed(LockReason.MemoryPressure)

        policy.onLockFinished()
        policy.onSensitiveContentSecured()

        assertTrue(policy.privacyCoverVisible)
    }

    @Test
    fun returningSystemFlowCancelsGraceWithoutLocking() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onSystemFlowStarted()
        val stopped = policy.onActivityStopped(isChangingConfigurations = false)

        val returned = policy.onSystemFlowEnded(returningToActivity = true)

        assertTrue(stopped.scheduleGracePeriod)
        assertFalse(stopped.startLock)
        assertTrue(returned.cancelGracePeriod)
        assertFalse(returned.startLock)
        // Activity-result delivery precedes onResume, so the opaque cover must
        // remain until the actual resumed transition.
        assertTrue(policy.privacyCoverVisible)
        policy.onActivityResumed()
        assertFalse(policy.privacyCoverVisible)
    }

    @Test
    fun firstAndLastSystemFlowsOwnScreenOffObservation() {
        val policy = AndroidLifecycleLockPolicy()

        val firstStarted = policy.onSystemFlowStarted()
        val nestedStarted = policy.onSystemFlowStarted()
        val nestedEnded = policy.onSystemFlowEnded(returningToActivity = false)
        val lastEnded = policy.onSystemFlowEnded(returningToActivity = false)

        assertTrue(firstStarted.startScreenOffObservation)
        assertFalse(nestedStarted.startScreenOffObservation)
        assertFalse(nestedEnded.stopScreenOffObservation)
        assertTrue(lastEnded.stopScreenOffObservation)
    }

    @Test
    fun screenOffImmediatelyLocksAStoppedSystemFlow() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onSystemFlowStarted()
        policy.onActivityStopped(isChangingConfigurations = false)

        val screenOff = policy.onScreenOff()

        assertTrue(screenOff.cancelGracePeriod)
        assertTrue(screenOff.startLock)
        assertEquals(LockReason.Background, screenOff.lockReason)
        assertTrue(policy.privacyCoverVisible)
    }

    @Test
    fun screenOffBeforeStopIsRememberedWithoutLockingInForeground() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onSystemFlowStarted()

        val screenOff = policy.onScreenOff()
        val stopped = policy.onActivityStopped(isChangingConfigurations = false)

        assertEquals(AndroidLifecycleLockDecision(), screenOff)
        assertFalse(stopped.scheduleGracePeriod)
        assertTrue(stopped.cancelGracePeriod)
        assertTrue(stopped.startLock)
        assertEquals(LockReason.Background, stopped.lockReason)
    }

    @Test
    fun resumeClearsAnUnconsumedForegroundScreenOffSignal() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onSystemFlowStarted()
        policy.onScreenOff()
        policy.onActivityResumed()

        val stopped = policy.onActivityStopped(isChangingConfigurations = false)

        assertTrue(stopped.scheduleGracePeriod)
        assertFalse(stopped.startLock)
    }

    @Test
    fun screenOffWithoutAnActiveSystemFlowIsInert() {
        val policy = AndroidLifecycleLockPolicy()

        assertEquals(AndroidLifecycleLockDecision(), policy.onScreenOff())
    }

    @Test
    fun unavailableScreenOffObservationMakesTheNextStopFailClosed() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onSystemFlowStarted()

        assertEquals(AndroidLifecycleLockDecision(), policy.onScreenOffObservationUnavailable())
        policy.onActivityResumed()

        val stopped = policy.onActivityStopped(isChangingConfigurations = false)
        assertFalse(stopped.scheduleGracePeriod)
        assertTrue(stopped.startLock)
        assertEquals(LockReason.Background, stopped.lockReason)
    }

    @Test
    fun graceExpiryLocksWhileSystemFlowRemainsInBackground() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onSystemFlowStarted()
        policy.onActivityStopped(isChangingConfigurations = false)

        val expired = policy.onGracePeriodElapsed()

        assertTrue(expired.cancelGracePeriod)
        assertTrue(expired.startLock)
        assertTrue(policy.privacyCoverVisible)
    }

    @Test
    fun graceExpiryIsInertAfterActivityResumes() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onSystemFlowStarted()
        policy.onActivityStopped(isChangingConfigurations = false)
        policy.onActivityResumed()

        assertFalse(policy.onGracePeriodElapsed().startLock)
    }

    @Test
    fun onlyFinalNestedSystemFlowCanRequestBackgroundLock() {
        val policy = AndroidLifecycleLockPolicy()
        policy.onSystemFlowStarted()
        policy.onSystemFlowStarted()
        policy.onActivityStopped(isChangingConfigurations = false)

        val first = policy.onSystemFlowEnded(returningToActivity = false)
        val last = policy.onSystemFlowEnded(returningToActivity = false)

        assertFalse(first.startLock)
        assertTrue(last.cancelGracePeriod)
        assertTrue(last.startLock)
    }

    @Test
    fun unbalancedSystemFlowCompletionFailsClosed() {
        val policy = AndroidLifecycleLockPolicy()

        assertFailsWith<IllegalStateException> {
            policy.onSystemFlowEnded(returningToActivity = false)
        }
    }
}
