package com.passvault.desktop

import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.security.ClipboardService
import com.passvault.desktop.security.biometric.DesktopBiometricHost
import com.passvault.shared.di.AppDatabaseLifecycle
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.Koin

/**
 * Owns the terminal Desktop shutdown transition.
 *
 * The window is hidden immediately after the first close request. Vault,
 * clipboard, database, and native-biometric cleanup then run independently so
 * one blocked provider cannot prevent the other security boundaries from
 * executing. [awaitCleanup] is intentionally a Java latch wait: the main
 * thread can impose a hard deadline without becoming a structured parent of a
 * non-cooperative native or cryptographic operation.
 */
internal class DesktopShutdownCoordinator(
    private val scope: CoroutineScope,
    private val operations: DesktopShutdownOperations,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val requested = AtomicBoolean(false)
    private val cleanupComplete = CountDownLatch(CLEANUP_TASK_COUNT)
    private val failures = AtomicInteger(0)

    val isShutdownRequested: Boolean
        get() = requested.get()

    /**
     * Starts cleanup exactly once, secures/hides the window, then closes the
     * Compose application immediately. Cleanup never sits in front of the UI
     * exit callback.
     */
    fun requestClose(
        prepareWindowForExit: () -> Unit,
        exitApplication: () -> Unit,
    ) {
        if (!beginShutdown()) return
        try {
            prepareWindowForExit()
        } catch (_: Exception) {
            failures.incrementAndGet()
        } finally {
            launchCleanupTasks()
            exitApplication()
        }
    }

    /** Starts the same terminal cleanup if application composition exits abnormally. */
    fun requestShutdown(): Boolean {
        if (!beginShutdown()) return false
        launchCleanupTasks()
        return true
    }

    fun awaitCleanup(timeoutMillis: Long): DesktopShutdownReport {
        require(timeoutMillis >= 0L) { "Desktop shutdown timeout cannot be negative" }
        val completed = cleanupComplete.await(timeoutMillis, TimeUnit.MILLISECONDS)
        return DesktopShutdownReport(
            completed = completed,
            failureCount = failures.get(),
        )
    }

    private fun beginShutdown(): Boolean {
        if (!requested.compareAndSet(false, true)) return false
        try {
            operations.cancelBiometricPrompt()
        } catch (_: Exception) {
            failures.incrementAndGet()
        }
        return true
    }

    private fun launchCleanupTasks() {
        launchTracked {
            try {
                if (!operations.lockVault()) failures.incrementAndGet()
            } finally {
                try {
                    operations.closeDatabase()
                } catch (_: Exception) {
                    failures.incrementAndGet()
                }
            }
        }
        launchTracked {
            operations.clearClipboard()
        }
        launchTracked(ioDispatcher) {
            operations.closeBiometricHost()
        }
    }

    private fun launchTracked(
        dispatcher: CoroutineDispatcher? = null,
        operation: suspend () -> Unit,
    ) {
        val job = if (dispatcher == null) {
            scope.launch { runCleanupOperation(operation) }
        } else {
            scope.launch(dispatcher) { runCleanupOperation(operation) }
        }
        job.completeCleanupTaskWhenFinished()
    }

    private suspend fun runCleanupOperation(operation: suspend () -> Unit) {
        try {
            operation()
        } catch (_: Exception) {
            failures.incrementAndGet()
        }
    }

    private fun Job.completeCleanupTaskWhenFinished() {
        invokeOnCompletion { cleanupComplete.countDown() }
    }

    private companion object {
        const val CLEANUP_TASK_COUNT = 3
    }
}

internal data class DesktopShutdownOperations(
    val cancelBiometricPrompt: () -> Unit,
    val lockVault: suspend () -> Boolean,
    val clearClipboard: suspend () -> Unit,
    val closeBiometricHost: () -> Unit,
    val closeDatabase: () -> Unit,
)

internal data class DesktopShutdownReport(
    val completed: Boolean,
    val failureCount: Int,
)

internal fun createDesktopShutdownCoordinator(koin: Koin): DesktopShutdownCoordinator {
    val vaultRepository = koin.get<VaultRepository>()
    val clipboardService = koin.get<ClipboardService>()
    val biometricHost = koin.get<DesktopBiometricHost>()
    val databaseLifecycle = koin.get<AppDatabaseLifecycle>()
    return DesktopShutdownCoordinator(
        scope = koin.get(),
        operations = DesktopShutdownOperations(
            cancelBiometricPrompt = biometricHost::cancelActive,
            lockVault = { lockVaultForShutdown(vaultRepository) },
            clearClipboard = { clearClipboardForShutdown(clipboardService) },
            closeBiometricHost = biometricHost::close,
            closeDatabase = databaseLifecycle::close,
        ),
    )
}
