package com.passvault.shared

import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.domain.repository.lockWithBoundedRetry
import com.passvault.core.security.ClipboardService
import com.passvault.core.security.VaultUiSecurityCoordinator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.mp.KoinPlatform
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Bridges SwiftUI scene transitions to the shared security boundary.
 *
 * `inactive` immediately clears an owned sensitive clipboard without locking
 * during system prompts (camera permission and biometrics). Entering the real
 * background locks once per scene episode. Session-state observers in the
 * shared application own navigation and UI scrubbing.
 */
class IosAppLifecycleBridge {
    private var inactiveEpisode = false
    private val backgroundCleanupEpisode = BackgroundCleanupEpisode<Deferred<Boolean>>()

    /**
     * Keeps SwiftUI's privacy cover in place until any serialized background
     * lock has finished. This closes the fast background/foreground race where
     * an unlock operation may still own the repository mutex.
     */
    fun applicationDidBecomeActive(onReady: () -> Unit) {
        inactiveEpisode = false
        val applicationScope = resolveApplicationScope()
        if (applicationScope == null) {
            // The native privacy cover is the last security boundary when the
            // dependency graph is unavailable. Never reveal fail-open.
            return
        }
        var pendingCleanup = backgroundCleanupEpisode.currentCleanup
        if (backgroundCleanupEpisode.cleanupRequested && pendingCleanup == null) {
            // A background transition may precede shared-controller/Koin
            // initialization. Retry the required lock instead of treating the
            // missing job as successful cleanup.
            pendingCleanup = launchCleanup(lockVault = true)
            pendingCleanup?.let(backgroundCleanupEpisode::attachCleanup)
        }
        if (pendingCleanup == null) {
            if (!backgroundCleanupEpisode.cleanupRequested) dispatchReadyWithoutCleanup(onReady)
            return
        }

        observeCleanup(
            applicationScope = applicationScope,
            cleanup = pendingCleanup,
            onReady = onReady,
            allowReplacement = true,
        )
    }

    fun applicationWillResignActive() {
        if (inactiveEpisode) return
        inactiveEpisode = true

        launchCleanup(lockVault = false)
    }

    fun applicationDidEnterBackground() {
        if (!backgroundCleanupEpisode.requestCleanup()) return

        launchCleanup(lockVault = true)?.let(backgroundCleanupEpisode::attachCleanup)
    }

    private fun launchCleanup(lockVault: Boolean): Deferred<Boolean>? {
        val koin = KoinPlatform.getKoinOrNull()
        val applicationScope = resolveApplicationScope()
        return if (koin == null || applicationScope == null) null else applicationScope.async {
            var lockSucceeded = true
            try {
                if (lockVault) {
                    lockSucceeded = koin.get<VaultRepository>()
                        .lockWithBoundedRetry(LockReason.Background)
                }
            } finally {
                withContext(NonCancellable) {
                    try {
                        koin.get<ClipboardService>().clear()
                    } catch (_: CancellationException) {
                        // NonCancellable cleanup is best effort at process exit.
                    } catch (_: Exception) {
                        // Clipboard ownership can change while iOS backgrounds the app.
                    }
                }
            }
            when {
                !lockSucceeded -> false
                !lockVault -> true
                else -> {
                    val coordinator = koin.get<VaultUiSecurityCoordinator>()
                    val requestEpoch = coordinator.requestAcknowledgement()
                    withTimeoutOrNull(UI_SECURITY_ACK_TIMEOUT_MS) {
                        coordinator.awaitAcknowledgement(requestEpoch)
                        true
                    } == true
                }
            }
        }
    }

    private fun observeCleanup(
        applicationScope: CoroutineScope,
        cleanup: Deferred<Boolean>,
        onReady: () -> Unit,
        allowReplacement: Boolean,
    ) {
        applicationScope.launch {
            cleanup.join()
            val succeeded = !cleanup.isCancelled && cleanup.await()
            dispatchCleanupCompletion(cleanup, succeeded, onReady, allowReplacement)
        }
    }

    private fun dispatchCleanupCompletion(
        completedCleanup: Deferred<Boolean>,
        succeeded: Boolean,
        onReady: () -> Unit,
        allowReplacement: Boolean,
    ) {
        dispatch_async(dispatch_get_main_queue()) {
            if (succeeded && backgroundCleanupEpisode.completeIfCurrent(completedCleanup)) {
                onReady()
            } else if (backgroundCleanupEpisode.markRetryableIfCurrent(completedCleanup)) {
                if (allowReplacement) {
                    val replacement = launchCleanup(lockVault = true)
                    if (replacement != null) {
                        backgroundCleanupEpisode.attachCleanup(replacement)
                        resolveApplicationScope()?.let { applicationScope ->
                            observeCleanup(
                                applicationScope = applicationScope,
                                cleanup = replacement,
                                onReady = onReady,
                                allowReplacement = false,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun dispatchReadyWithoutCleanup(onReady: () -> Unit) {
        dispatch_async(dispatch_get_main_queue()) {
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

    private companion object {
        const val UI_SECURITY_ACK_TIMEOUT_MS = 5_000L
    }
}
