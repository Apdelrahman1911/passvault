package com.passvault.android.lifecycle

import android.os.Handler
import android.os.Looper
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.domain.repository.lockWithBoundedRetry
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.VaultUiSecurityCoordinator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Distinguishes a real app background transition from a configuration change
 * and from a short, app-initiated system UI flow such as the Storage Access
 * Framework picker.
 *
 * Known system flows receive a bounded grace period so their Activity result
 * can return without the lock cancelling the operation. If the user leaves the
 * system UI in the background, the grace timeout still locks the vault. Screen
 * off revokes that grace immediately, regardless of broadcast/onStop ordering.
 */
class AndroidLifecycleLockCoordinator internal constructor(
    private val vaultRepository: VaultRepository,
    private val clipboardService: ClipboardService,
    private val applicationScope: CoroutineScope,
    private val vaultUiSecurityCoordinator: VaultUiSecurityCoordinator,
    private val screenOffObserver: ScreenOffObserver,
    private val handler: Handler = Handler(Looper.getMainLooper()),
) {
    private val policy = AndroidLifecycleLockPolicy()
    private val _privacyCoverVisible = MutableStateFlow(false)

    /** Opaque UI cover held until repository and rendered-UI lock boundaries complete. */
    val privacyCoverVisible = _privacyCoverVisible.asStateFlow()

    private val deferredSystemFlowLock = Runnable {
        synchronized(this) {
            applyDecisionLocked(policy.onGracePeriodElapsed())
        }
    }

    @Synchronized
    fun onActivityResumed() {
        applyDecisionLocked(policy.onActivityResumed())
    }

    @Synchronized
    fun onActivityStopped(isChangingConfigurations: Boolean) {
        applyDecisionLocked(policy.onActivityStopped(isChangingConfigurations))
    }

    @Synchronized
    fun onMemoryPressure() {
        applyDecisionLocked(policy.onMemoryPressure())
    }

    @Synchronized
    private fun onScreenOff() {
        applyDecisionLocked(policy.onScreenOff())
    }

    @Synchronized
    fun beginSystemFlow(): SystemFlowToken {
        applyDecisionLocked(policy.onSystemFlowStarted())
        return SystemFlowToken(this)
    }

    @Synchronized
    private fun endSystemFlow(returningToActivity: Boolean) {
        applyDecisionLocked(policy.onSystemFlowEnded(returningToActivity))
    }

    private fun applyDecisionLocked(decision: AndroidLifecycleLockDecision) {
        _privacyCoverVisible.value = policy.privacyCoverVisible
        if (decision.cancelGracePeriod) {
            handler.removeCallbacks(deferredSystemFlowLock)
        }
        if (decision.stopScreenOffObservation) {
            screenOffObserver.stop()
        }
        if (decision.startScreenOffObservation) {
            if (!screenOffObserver.start(::onScreenOff)) {
                // A missing observer must not silently restore an unbounded sleep-time grace. Mark
                // this flow unsafe so its next stopped transition requests the normal lock.
                applyDecisionLocked(policy.onScreenOffObservationUnavailable())
            }
        }
        if (decision.scheduleGracePeriod) {
            handler.postDelayed(deferredSystemFlowLock, SYSTEM_FLOW_GRACE_MS)
        }
        if (!decision.startLock) return
        val lockReason = checkNotNull(decision.lockReason) {
            "A requested lifecycle lock must retain its originating reason"
        }
        // The security boundary must not depend on a transient UI collector:
        // SharedFlow commands can legitimately have no subscriber while the
        // Activity is being torn down. Session state will drive navigation and
        // UI scrubbing when the app is next composed.
        applicationScope.launch {
            val lockSucceeded = try {
                try {
                    vaultRepository.lockWithBoundedRetry(lockReason)
                } finally {
                    withContext(NonCancellable) {
                        try {
                            clipboardService.clear()
                        } catch (_: Exception) {
                            // Clipboard providers can disappear while the app is
                            // leaving the foreground; the lock remains authoritative.
                        }
                    }
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                false
            }
            if (!lockSucceeded) {
                synchronized(this@AndroidLifecycleLockCoordinator) {
                    applyDecisionLocked(policy.onSecurityFailed(lockReason))
                }
                return@launch
            }

            synchronized(this@AndroidLifecycleLockCoordinator) {
                applyDecisionLocked(policy.onLockFinished())
            }
            val requestEpoch = vaultUiSecurityCoordinator.requestAcknowledgement()
            val contentSecured = withTimeoutOrNull(UI_SECURITY_ACK_TIMEOUT_MS) {
                vaultUiSecurityCoordinator.awaitAcknowledgement(requestEpoch)
                true
            } == true
            synchronized(this@AndroidLifecycleLockCoordinator) {
                if (contentSecured) {
                    applyDecisionLocked(policy.onSensitiveContentSecured())
                } else {
                    applyDecisionLocked(policy.onSecurityFailed(lockReason))
                }
            }
        }
    }

    class SystemFlowToken internal constructor(
        private val owner: AndroidLifecycleLockCoordinator,
    ) : AutoCloseable {
        private val closed = AtomicBoolean(false)

        override fun close() {
            finish(returningToActivity = false)
        }

        fun returnedToActivity() {
            finish(returningToActivity = true)
        }

        private fun finish(returningToActivity: Boolean) {
            if (closed.compareAndSet(false, true)) owner.endSystemFlow(returningToActivity)
        }
    }

    private companion object {
        const val SYSTEM_FLOW_GRACE_MS = 60_000L
        const val UI_SECURITY_ACK_TIMEOUT_MS = 5_000L
    }
}

/** Pure lifecycle state used by host tests; Android scheduling stays in the coordinator. */
internal class AndroidLifecycleLockPolicy {
    private var activeSystemFlows = 0
    private var appStopped = false
    private var screenOffSinceResume = false
    private var screenOffObservationUnavailable = false
    private var lockRequestedForEpisode = false
    private var lockFinishedForEpisode = false
    private var sensitiveContentSecuredForEpisode = false
    private var retryReason: LockReason? = null

    var privacyCoverVisible: Boolean = false
        private set

    fun onActivityResumed(): AndroidLifecycleLockDecision {
        appStopped = false
        screenOffSinceResume = false
        retryReason?.let { reason ->
            return requestLock(reason, allowForeground = true)
        }
        releaseCoverIfSafe()
        return AndroidLifecycleLockDecision(cancelGracePeriod = true)
    }

    fun onActivityStopped(isChangingConfigurations: Boolean): AndroidLifecycleLockDecision {
        if (isChangingConfigurations) return AndroidLifecycleLockDecision()
        appStopped = true
        // The repository lock can wait behind an in-progress unlock. Keep the
        // native Activity covered until that serialized lock has completed.
        privacyCoverVisible = true
        return if (
            activeSystemFlows == 0 ||
            screenOffSinceResume ||
            screenOffObservationUnavailable
        ) {
            requestLock(LockReason.Background)
        } else {
            AndroidLifecycleLockDecision(cancelGracePeriod = true, scheduleGracePeriod = true)
        }
    }

    fun onSystemFlowStarted(): AndroidLifecycleLockDecision {
        activeSystemFlows++
        if (activeSystemFlows == 1) screenOffObservationUnavailable = false
        return AndroidLifecycleLockDecision(
            startScreenOffObservation = activeSystemFlows == 1,
        )
    }

    fun onSystemFlowEnded(returningToActivity: Boolean): AndroidLifecycleLockDecision {
        check(activeSystemFlows > 0) { "System-flow tracking became unbalanced" }
        activeSystemFlows--
        if (returningToActivity) {
            // Activity-result delivery can precede onResume. Treat it as the
            // foreground handoff so the result is not cancelled by a lock.
            appStopped = false
            screenOffSinceResume = false
        }
        return if (activeSystemFlows == 0) {
            screenOffSinceResume = false
            screenOffObservationUnavailable = false
            requestLock(LockReason.Background).copy(
                cancelGracePeriod = true,
                stopScreenOffObservation = true,
            )
        } else {
            AndroidLifecycleLockDecision()
        }
    }

    fun onScreenOff(): AndroidLifecycleLockDecision {
        if (activeSystemFlows == 0) return AndroidLifecycleLockDecision()
        screenOffSinceResume = true
        return if (appStopped) {
            requestLock(LockReason.Background)
        } else {
            AndroidLifecycleLockDecision()
        }
    }

    fun onScreenOffObservationUnavailable(): AndroidLifecycleLockDecision {
        if (activeSystemFlows == 0) return AndroidLifecycleLockDecision()
        screenOffObservationUnavailable = true
        return if (appStopped) {
            requestLock(LockReason.Background)
        } else {
            AndroidLifecycleLockDecision()
        }
    }

    fun onGracePeriodElapsed(): AndroidLifecycleLockDecision =
        if (appStopped && activeSystemFlows > 0) {
            requestLock(LockReason.Background)
        } else {
            AndroidLifecycleLockDecision()
        }

    fun onMemoryPressure(): AndroidLifecycleLockDecision {
        privacyCoverVisible = true
        return requestLock(LockReason.MemoryPressure, allowForeground = true)
    }

    fun onLockFinished(): AndroidLifecycleLockDecision {
        if (!lockRequestedForEpisode) return AndroidLifecycleLockDecision()
        lockFinishedForEpisode = true
        releaseCoverIfSafe()
        return AndroidLifecycleLockDecision()
    }

    fun onSensitiveContentSecured(): AndroidLifecycleLockDecision {
        if (lockRequestedForEpisode) {
            sensitiveContentSecuredForEpisode = true
            releaseCoverIfSafe()
        }
        return AndroidLifecycleLockDecision()
    }

    fun onSecurityFailed(reason: LockReason): AndroidLifecycleLockDecision {
        if (!lockRequestedForEpisode) return AndroidLifecycleLockDecision()
        lockRequestedForEpisode = false
        lockFinishedForEpisode = false
        sensitiveContentSecuredForEpisode = false
        retryReason = reason
        privacyCoverVisible = true
        return AndroidLifecycleLockDecision()
    }

    private fun requestLock(
        reason: LockReason,
        allowForeground: Boolean = false,
    ): AndroidLifecycleLockDecision {
        if ((!appStopped && !allowForeground) || lockRequestedForEpisode) return AndroidLifecycleLockDecision()
        lockRequestedForEpisode = true
        lockFinishedForEpisode = false
        sensitiveContentSecuredForEpisode = false
        retryReason = null
        return AndroidLifecycleLockDecision(
            cancelGracePeriod = true,
            startLock = true,
            lockReason = reason,
        )
    }

    private fun releaseCoverIfSafe() {
        if (appStopped) return
        if (
            lockRequestedForEpisode &&
            (!lockFinishedForEpisode || !sensitiveContentSecuredForEpisode)
        ) {
            return
        }

        lockRequestedForEpisode = false
        lockFinishedForEpisode = false
        sensitiveContentSecuredForEpisode = false
        privacyCoverVisible = false
    }
}

internal data class AndroidLifecycleLockDecision(
    val cancelGracePeriod: Boolean = false,
    val scheduleGracePeriod: Boolean = false,
    val startScreenOffObservation: Boolean = false,
    val stopScreenOffObservation: Boolean = false,
    val startLock: Boolean = false,
    val lockReason: LockReason? = null,
)
