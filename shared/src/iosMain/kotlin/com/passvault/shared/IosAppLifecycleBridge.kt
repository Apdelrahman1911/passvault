package com.passvault.shared

import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.domain.repository.lockWithBoundedRetry
import com.passvault.core.security.VaultUiSecurityCoordinator
import com.passvault.shared.di.AppDatabaseLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.context.stopKoin
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
    private val protectedDataScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    private val protectedDataRuntimeResolver: () -> IosProtectedDataRuntimeDependencies? =
        ::resolveIosProtectedDataRuntime,
    private val protectedDataRuntimeShutdown: suspend (IosProtectedDataRuntimeDependencies?) -> Boolean =
        ::shutdownIosAppRuntime,
) {
    private var inactiveEpisode = false
    private val backgroundCleanupEpisode = BackgroundCleanupEpisode<Deferred<IosBackgroundCleanupOutcome>>()
    // Main-thread-confined episode state; duplicate active callbacks may add
    // observers, but cannot reset the budget of an already attached cleanup.
    private var cleanupRetryState = IosBackgroundCleanupRetryState()
    private var protectedDataAvailable = true
    private var protectedDataPhase = IosProtectedDataPhase.AVAILABLE
    private var protectedDataCleanup: Deferred<IosBackgroundCleanupOutcome>? = null
    private var protectedDataRuntime: IosProtectedDataRuntimeDependencies? = null

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
        if (!protectedDataAvailable || protectedDataPhase != IosProtectedDataPhase.AVAILABLE) return
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
        if (
            !protectedDataAvailable ||
            protectedDataPhase != IosProtectedDataPhase.AVAILABLE ||
            !backgroundCleanupEpisode.requestCleanup()
        ) {
            return
        }

        cleanupRetryState = IosBackgroundCleanupRetryState()
        val applicationScope = resolveApplicationScope() ?: return
        launchBackgroundLock(applicationScope)?.let(backgroundCleanupEpisode::attachCleanup)
    }

    /**
     * Starts the terminal cleanup required before iOS revokes access to Complete-protected files.
     * The native host has already installed an opaque cover and must detach Compose when either
     * callback runs. Runtime teardown is intentionally separate so Room is not closed while its
     * controller can still launch work.
     */
    fun applicationProtectedDataWillBecomeUnavailable(
        onRuntimeTeardownRequired: () -> Unit,
        onRecoveryRequired: () -> Unit,
    ) {
        protectedDataAvailable = false
        if (protectedDataPhase != IosProtectedDataPhase.AVAILABLE) return
        protectedDataPhase = IosProtectedDataPhase.SECURING

        val runtime = protectedDataRuntime ?: protectedDataRuntimeResolver()
        protectedDataRuntime = runtime
        if (runtime?.repository == null || runtime.coordinator == null) {
            dispatchProtectedDataCleanup(
                outcome = IosBackgroundCleanupOutcome.RuntimeUnavailable,
                onRuntimeTeardownRequired = onRuntimeTeardownRequired,
                onRecoveryRequired = onRecoveryRequired,
            )
            return
        }

        val cleanup = protectedDataScope.async {
            secureRuntimeForProtectedDataLoss(runtime)
        }
        protectedDataCleanup = cleanup
        protectedDataScope.launch {
            val outcome = try {
                cleanup.await()
            } catch (_: CancellationException) {
                currentCoroutineContext().ensureActive()
                IosBackgroundCleanupOutcome.RuntimeUnavailable
            } catch (_: Exception) {
                IosBackgroundCleanupOutcome.RuntimeUnavailable
            }
            dispatchProtectedDataCleanup(
                cleanup = cleanup,
                outcome = outcome,
                onRuntimeTeardownRequired = onRuntimeTeardownRequired,
                onRecoveryRequired = onRecoveryRequired,
            )
        }
    }

    /** Records that iOS can open Complete-protected files again. */
    fun applicationProtectedDataDidBecomeAvailable() {
        protectedDataAvailable = true
        if (protectedDataPhase == IosProtectedDataPhase.UNAVAILABLE) {
            protectedDataPhase = IosProtectedDataPhase.RUNTIME_STOPPED
        }
    }

    /**
     * Completes terminal runtime shutdown after SwiftUI has dismantled the Compose controller.
     */
    fun composeRuntimeDidDetach(
        onRuntimeStopped: () -> Unit,
        onRecoveryRequired: () -> Unit,
    ) {
        if (protectedDataPhase != IosProtectedDataPhase.AWAITING_CONTROLLER_DETACH) return
        protectedDataPhase = IosProtectedDataPhase.STOPPING_RUNTIME
        // Resolve once more after controller disposal. This closes a narrow startup race where
        // Koin can appear after the protected-data callback but before SwiftUI unmounts Compose.
        val runtime = protectedDataRuntime ?: protectedDataRuntimeResolver()

        // Queue once beyond UIViewControllerRepresentable.dismantle so Compose has released its
        // hierarchy before application work is cancelled and Room is checkpointed and closed.
        dispatchToMain {
            protectedDataScope.launch {
                val stoppedCleanly = try {
                    protectedDataRuntimeShutdown(runtime)
                } catch (_: CancellationException) {
                    currentCoroutineContext().ensureActive()
                    false
                } catch (_: Exception) {
                    false
                }
                dispatchToMain completion@{
                    if (protectedDataPhase != IosProtectedDataPhase.STOPPING_RUNTIME) return@completion
                    protectedDataRuntime = null
                    protectedDataPhase = if (protectedDataAvailable) {
                        IosProtectedDataPhase.RUNTIME_STOPPED
                    } else {
                        IosProtectedDataPhase.UNAVAILABLE
                    }
                    if (stoppedCleanly) onRuntimeStopped() else onRecoveryRequired()
                }
            }
        }
    }

    /** Resets lifecycle bookkeeping only after a newly-created Compose/Koin runtime exists. */
    fun composeRuntimeDidStart() {
        if (
            !protectedDataAvailable ||
            (protectedDataPhase != IosProtectedDataPhase.AVAILABLE &&
                protectedDataPhase != IosProtectedDataPhase.RUNTIME_STOPPED)
        ) {
            return
        }
        protectedDataCleanup = null
        // Capture the shutdown dependencies while Complete-protected storage is available. If a
        // later notification races with dependency construction, teardown can still close Room.
        protectedDataRuntime = protectedDataRuntimeResolver()
        backgroundCleanupEpisode.reset()
        cleanupRetryState = IosBackgroundCleanupRetryState()
        protectedDataPhase = IosProtectedDataPhase.AVAILABLE
    }

    private suspend fun secureRuntimeForProtectedDataLoss(
        runtime: IosProtectedDataRuntimeDependencies,
    ): IosBackgroundCleanupOutcome = try {
        val repository = runtime.repository ?: return IosBackgroundCleanupOutcome.RuntimeUnavailable
        val coordinator = runtime.coordinator ?: return IosBackgroundCleanupOutcome.RuntimeUnavailable
        if (repository.lock(LockReason.Background).isFailure) {
            IosBackgroundCleanupOutcome.LockFailed
        } else {
            val requestEpoch = coordinator.requestAcknowledgement()
            awaitUiAcknowledgement(
                coordinator = coordinator,
                requestEpoch = requestEpoch,
                timeoutMillis = minOf(acknowledgementTimeoutMillis, PROTECTED_DATA_ACK_TIMEOUT_MS),
            )
        }
    } catch (cancel: CancellationException) {
        throw cancel
    } catch (_: Exception) {
        IosBackgroundCleanupOutcome.RuntimeUnavailable
    }

    private fun dispatchProtectedDataCleanup(
        outcome: IosBackgroundCleanupOutcome,
        onRuntimeTeardownRequired: () -> Unit,
        onRecoveryRequired: () -> Unit,
        cleanup: Deferred<IosBackgroundCleanupOutcome>? = null,
    ) {
        dispatchToMain {
            if (protectedDataPhase != IosProtectedDataPhase.SECURING) return@dispatchToMain
            if (cleanup != null && protectedDataCleanup !== cleanup) return@dispatchToMain
            protectedDataCleanup = null
            protectedDataPhase = IosProtectedDataPhase.AWAITING_CONTROLLER_DETACH
            if (outcome == IosBackgroundCleanupOutcome.Succeeded) {
                onRuntimeTeardownRequired()
            } else {
                onRecoveryRequired()
            }
        }
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
        timeoutMillis: Long = acknowledgementTimeoutMillis,
    ): IosBackgroundCleanupOutcome =
        if (
            withTimeoutOrNull(timeoutMillis) {
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
        const val PROTECTED_DATA_ACK_TIMEOUT_MS = 1_000L
    }
}

private data class IosBackgroundCleanupDependencies(
    val repository: VaultRepository,
    val coordinator: VaultUiSecurityCoordinator,
)

internal data class IosProtectedDataRuntimeDependencies(
    val repository: VaultRepository?,
    val coordinator: VaultUiSecurityCoordinator?,
    val applicationScope: CoroutineScope?,
    val databaseLifecycle: AppDatabaseLifecycle?,
)

private enum class IosProtectedDataPhase {
    AVAILABLE,
    SECURING,
    AWAITING_CONTROLLER_DETACH,
    STOPPING_RUNTIME,
    UNAVAILABLE,
    RUNTIME_STOPPED,
}

private fun resolveIosProtectedDataRuntime(): IosProtectedDataRuntimeDependencies? {
    val koin = KoinPlatform.getKoinOrNull() ?: return null
    return IosProtectedDataRuntimeDependencies(
        repository = try {
            koin.get()
        } catch (_: Exception) {
            null
        },
        coordinator = try {
            koin.get()
        } catch (_: Exception) {
            null
        },
        applicationScope = try {
            koin.get()
        } catch (_: Exception) {
            null
        },
        databaseLifecycle = try {
            koin.get()
        } catch (_: Exception) {
            null
        },
    )
}

internal suspend fun shutdownIosAppRuntime(
    runtime: IosProtectedDataRuntimeDependencies?,
    stopRuntime: () -> Unit = ::stopKoin,
    runtimeIsStopped: () -> Boolean = { KoinPlatform.getKoinOrNull() == null },
): Boolean {
    val applicationScopeCancelled = if (runtime == null) {
        true
    } else {
        runtime.applicationScope?.let { scope ->
            try {
                scope.cancel()
                true
            } catch (_: Exception) {
                false
            }
        } ?: false
    }
    val databaseClosed = if (runtime == null) {
        true
    } else {
        runtime.databaseLifecycle?.let { databaseLifecycle ->
            try {
                databaseLifecycle.close().isSuccess
            } catch (_: Exception) {
                false
            }
        } ?: false
    }
    val runtimeStopped = try {
        stopRuntime()
        runtimeIsStopped()
    } catch (_: Exception) {
        false
    }
    return applicationScopeCancelled && databaseClosed && runtimeStopped
}
