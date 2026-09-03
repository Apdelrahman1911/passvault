package com.passvault.shared

import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.domain.repository.lockWithBoundedRetry
import com.passvault.core.security.VaultUiSecurityCoordinator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.mp.KoinPlatform
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Bridges SwiftUI scene transitions to the shared security boundary.
 *
 * `inactive` only installs the native privacy cover. Entering the real
 * background locks once per scene episode. The iOS pasteboard keeps its
 * OS-enforced expiration and ownership timer across those transitions so a
 * user can switch apps and paste. Session-state observers own navigation, UI
 * scrubbing, and clipboard cleanup for stronger lock reasons.
 */
class IosAppLifecycleBridge internal constructor(
    private val dispatchToMain: ((() -> Unit) -> Unit),
    private val acknowledgementTimeoutMillis: Long,
    private val retryPolicy: IosBackgroundCleanupRetryPolicy,
) {
    private var inactiveEpisode = false
    private val backgroundCleanupEpisode = BackgroundCleanupEpisode<Deferred<IosBackgroundCleanupOutcome>>()
    // Main-thread-confined episode state; duplicate active callbacks may add
    // observers, but cannot reset the budget of an already attached cleanup.
    private var cleanupRetryState = IosBackgroundCleanupRetryState()

    constructor() : this(
        dispatchToMain = { block ->
            dispatch_async(dispatch_get_main_queue()) { block() }
        },
        acknowledgementTimeoutMillis = UI_SECURITY_ACK_TIMEOUT_MS,
        retryPolicy = IosBackgroundCleanupRetryPolicy(),
    )

    init {
        require(acknowledgementTimeoutMillis > 0L)
    }

    /**
     * Keeps SwiftUI's privacy cover in place until any serialized background
     * lock has finished. This closes the fast background/foreground race where
     * an unlock operation may still own the repository mutex.
     */
    fun applicationDidBecomeActive(
        onReady: () -> Unit,
        onRecoveryRequired: () -> Unit,
    ) {
        inactiveEpisode = false
        val applicationScope = resolveApplicationScope()
        if (applicationScope == null) {
            // The native privacy cover is the last security boundary when the
            // dependency graph is unavailable. Never reveal fail-open.
            if (backgroundCleanupEpisode.cleanupRequested) {
                dispatchRecoveryIfPending(onRecoveryRequired)
            }
        } else {
            observePendingCleanup(applicationScope, onReady, onRecoveryRequired)
        }
    }

    private fun observePendingCleanup(
        applicationScope: CoroutineScope,
        onReady: () -> Unit,
        onRecoveryRequired: () -> Unit,
    ) {
        var pendingCleanup = backgroundCleanupEpisode.currentCleanup
        if (backgroundCleanupEpisode.cleanupRequested && pendingCleanup == null) {
            // A background transition may precede shared-controller/Koin
            // initialization. Retry the required lock instead of treating the
            // missing job as successful cleanup.
            cleanupRetryState = IosBackgroundCleanupRetryState()
            pendingCleanup = launchBackgroundLock(applicationScope)
            pendingCleanup?.let(backgroundCleanupEpisode::attachCleanup)
        }
        when {
            pendingCleanup != null -> observeCleanup(
                applicationScope = applicationScope,
                cleanup = pendingCleanup,
                onReady = onReady,
                onRecoveryRequired = onRecoveryRequired,
            )
            backgroundCleanupEpisode.cleanupRequested -> dispatchRecoveryIfPending(onRecoveryRequired)
            else -> dispatchReadyWithoutCleanup(onReady)
        }
    }

    fun applicationWillResignActive() {
        if (inactiveEpisode) return
        inactiveEpisode = true
    }

    fun applicationDidEnterBackground() {
        if (!backgroundCleanupEpisode.requestCleanup()) return

        cleanupRetryState = IosBackgroundCleanupRetryState()
        val applicationScope = resolveApplicationScope() ?: return
        launchBackgroundLock(applicationScope)?.let(backgroundCleanupEpisode::attachCleanup)
    }

    private fun launchBackgroundLock(
        applicationScope: CoroutineScope,
        delayMillis: Long = 0L,
    ): Deferred<IosBackgroundCleanupOutcome>? = resolveCleanupDependencies()?.let { dependencies ->
        applicationScope.async {
            try {
                if (delayMillis > 0L) delay(delayMillis)
                if (!dependencies.repository.lockWithBoundedRetry(LockReason.Background)) {
                    IosBackgroundCleanupOutcome.LockFailed
                } else {
                    val requestEpoch = dependencies.coordinator.requestAcknowledgement()
                    awaitUiAcknowledgement(dependencies.coordinator, requestEpoch)
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                IosBackgroundCleanupOutcome.RuntimeUnavailable
            }
        }
    }

    private fun launchAcknowledgementRetry(
        applicationScope: CoroutineScope,
        requestEpoch: Long,
        delayMillis: Long,
    ): Deferred<IosBackgroundCleanupOutcome>? = resolveUiSecurityCoordinator()?.let { coordinator ->
        applicationScope.async {
            try {
                if (delayMillis > 0L) delay(delayMillis)
                awaitUiAcknowledgement(coordinator, requestEpoch)
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                IosBackgroundCleanupOutcome.RuntimeUnavailable
            }
        }
    }

    private suspend fun awaitUiAcknowledgement(
        coordinator: VaultUiSecurityCoordinator,
        requestEpoch: Long,
    ): IosBackgroundCleanupOutcome =
        if (
            withTimeoutOrNull(acknowledgementTimeoutMillis) {
                coordinator.awaitAcknowledgement(requestEpoch)
                true
            } == true
        ) {
            IosBackgroundCleanupOutcome.Succeeded
        } else {
            IosBackgroundCleanupOutcome.AcknowledgementTimedOut(requestEpoch)
        }

    private fun launchRetry(
        applicationScope: CoroutineScope,
        resolution: IosBackgroundCleanupResolution,
    ): Deferred<IosBackgroundCleanupOutcome>? = when (resolution) {
        IosBackgroundCleanupResolution.Complete,
        IosBackgroundCleanupResolution.RecoveryRequired,
        -> null

        is IosBackgroundCleanupResolution.RetryLock -> launchBackgroundLock(
            applicationScope = applicationScope,
            delayMillis = resolution.delayMillis,
        )

        is IosBackgroundCleanupResolution.RetryAcknowledgement -> launchAcknowledgementRetry(
            applicationScope = applicationScope,
            requestEpoch = resolution.requestEpoch,
            delayMillis = resolution.delayMillis,
        )
    }

    private fun observeCleanup(
        applicationScope: CoroutineScope,
        cleanup: Deferred<IosBackgroundCleanupOutcome>,
        onReady: () -> Unit,
        onRecoveryRequired: () -> Unit,
    ) {
        applicationScope.launch {
            val outcome = try {
                cleanup.await()
            } catch (_: CancellationException) {
                currentCoroutineContext().ensureActive()
                // The cleanup alone was cancelled; the still-active observer
                // must keep the cover closed and surface recovery.
                IosBackgroundCleanupOutcome.RuntimeUnavailable
            } catch (_: Exception) {
                IosBackgroundCleanupOutcome.RuntimeUnavailable
            }
            dispatchCleanupCompletion(
                applicationScope = applicationScope,
                completedCleanup = cleanup,
                outcome = outcome,
                onReady = onReady,
                onRecoveryRequired = onRecoveryRequired,
            )
        }
    }

    private fun dispatchCleanupCompletion(
        applicationScope: CoroutineScope,
        completedCleanup: Deferred<IosBackgroundCleanupOutcome>,
        outcome: IosBackgroundCleanupOutcome,
        onReady: () -> Unit,
        onRecoveryRequired: () -> Unit,
    ) {
        dispatchToMain {
            if (outcome == IosBackgroundCleanupOutcome.Succeeded) {
                if (backgroundCleanupEpisode.completeIfCurrent(completedCleanup)) {
                    cleanupRetryState = IosBackgroundCleanupRetryState()
                    onReady()
                }
                return@dispatchToMain
            }
            if (!backgroundCleanupEpisode.markRetryableIfCurrent(completedCleanup)) return@dispatchToMain

            val resolution = retryPolicy.resolve(outcome, cleanupRetryState)
            if (resolution == IosBackgroundCleanupResolution.RecoveryRequired) {
                onRecoveryRequired()
                return@dispatchToMain
            }
            cleanupRetryState = resolution.retryState()
            val replacement = launchRetry(applicationScope, resolution)
            if (replacement == null) {
                onRecoveryRequired()
                return@dispatchToMain
            }
            backgroundCleanupEpisode.attachCleanup(replacement)
            observeCleanup(
                applicationScope = applicationScope,
                cleanup = replacement,
                onReady = onReady,
                onRecoveryRequired = onRecoveryRequired,
            )
        }
    }

    private fun IosBackgroundCleanupResolution.retryState(): IosBackgroundCleanupRetryState = when (this) {
        is IosBackgroundCleanupResolution.RetryLock -> retryState
        is IosBackgroundCleanupResolution.RetryAcknowledgement -> retryState
        IosBackgroundCleanupResolution.Complete,
        IosBackgroundCleanupResolution.RecoveryRequired,
        -> error("A terminal cleanup resolution cannot be retried")
    }

    private fun dispatchRecoveryIfPending(onRecoveryRequired: () -> Unit) {
        dispatchToMain {
            if (backgroundCleanupEpisode.cleanupRequested) onRecoveryRequired()
        }
    }

    private fun dispatchReadyWithoutCleanup(onReady: () -> Unit) {
        dispatchToMain {
            if (!backgroundCleanupEpisode.cleanupRequested) onReady()
        }
    }

    private fun resolveApplicationScope(): CoroutineScope? {
        val koin = KoinPlatform.getKoinOrNull() ?: return null
        return try {
            koin.get<CoroutineScope>()
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveCleanupDependencies(): IosBackgroundCleanupDependencies? {
        val koin = KoinPlatform.getKoinOrNull() ?: return null
        return try {
            IosBackgroundCleanupDependencies(
                repository = koin.get(),
                coordinator = koin.get(),
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun resolveUiSecurityCoordinator(): VaultUiSecurityCoordinator? {
        val koin = KoinPlatform.getKoinOrNull() ?: return null
        return try {
            koin.get()
        } catch (_: Exception) {
            null
        }
    }

    private companion object {
        const val UI_SECURITY_ACK_TIMEOUT_MS = 5_000L
    }
}

private data class IosBackgroundCleanupDependencies(
    val repository: VaultRepository,
    val coordinator: VaultUiSecurityCoordinator,
)
