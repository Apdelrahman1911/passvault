package com.passvault.shared

import com.passvault.core.domain.repository.LockReason
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.domain.repository.lockWithBoundedRetry
import com.passvault.core.security.VaultUiSecurityCoordinator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
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
            pendingCleanup = launchBackgroundLock()
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
    }

    fun applicationDidEnterBackground() {
        if (!backgroundCleanupEpisode.requestCleanup()) return

        launchBackgroundLock()?.let(backgroundCleanupEpisode::attachCleanup)
    }

    private fun launchBackgroundLock(): Deferred<Boolean>? {
        val koin = KoinPlatform.getKoinOrNull()
        val applicationScope = resolveApplicationScope()
        return if (koin == null || applicationScope == null) null else applicationScope.async {
            val lockSucceeded = koin.get<VaultRepository>()
                .lockWithBoundedRetry(LockReason.Background)
            if (!lockSucceeded) {
                false
            } else {
                val coordinator = koin.get<VaultUiSecurityCoordinator>()
                val requestEpoch = coordinator.requestAcknowledgement()
                withTimeoutOrNull(UI_SECURITY_ACK_TIMEOUT_MS) {
                    coordinator.awaitAcknowledgement(requestEpoch)
                    true
                } == true
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
                    val replacement = launchBackgroundLock()
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
