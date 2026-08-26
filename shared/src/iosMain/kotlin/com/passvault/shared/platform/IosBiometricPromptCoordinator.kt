package com.passvault.shared.platform

import com.passvault.core.security.BiometricPromptController
import platform.Foundation.NSLock

/**
 * Owns the single iOS authentication context that may currently complete.
 *
 * An operation snapshots the cancellation epoch before doing preparatory work.
 * Locking the vault advances that epoch, so an operation cannot present a new
 * prompt after the lock request. Context completion is claimed atomically;
 * callbacks or Keychain results from invalidated contexts are therefore stale.
 */
internal class IosBiometricPromptCoordinator<Context>(
    private val invalidateContext: (Context) -> Unit,
) : BiometricPromptController {
    private val stateLock = NSLock()
    private var cancellationEpoch = Any()
    private var currentOperation: ReservedOperation? = null

    suspend fun <T> withOperation(
        onBusy: () -> T,
        block: suspend (Operation) -> T,
    ): T {
        val operation = beginOperation() ?: return onBusy()
        return try {
            block(operation)
        } finally {
            releaseOperation(operation)
        }
    }

    fun beginOperation(): Operation? = withStateLock {
        if (currentOperation != null) {
            null
        } else {
            Operation(identity = Any(), cancellationEpoch = cancellationEpoch).also { operation ->
                currentOperation = ReservedOperation(operation)
            }
        }
    }

    fun activate(
        operation: Operation,
        context: Context,
        reportCancelled: () -> Unit,
    ): Boolean {
        val activeContext = ActiveContext(context, reportCancelled)
        val accepted = withStateLock {
            val current = currentOperation
            if (
                operation.cancellationEpoch !== cancellationEpoch ||
                current?.operation?.identity !== operation.identity ||
                current.activeContext != null
            ) {
                false
            } else {
                current.activeContext = activeContext
                true
            }
        }
        if (!accepted) activeContext.cancel()
        return accepted
    }

    /** Claims an authoritative context, then completes without holding the state lock. */
    fun finishPrompt(
        operation: Operation,
        context: Context,
        complete: () -> Unit,
    ): Boolean {
        val completedContext = withStateLock {
            val current = currentOperation
            val active = current?.activeContext
            if (
                current?.operation?.identity === operation.identity &&
                active?.context === context
            ) {
                current.activeContext = null
                active
            } else {
                null
            }
        } ?: return false
        completedContext.invalidate()
        complete()
        return true
    }

    /** Cancels only this operation, as used by coroutine cancellation. */
    fun cancel(operation: Operation) {
        takeOperation(operation)?.activeContext?.cancel()
    }

    /** Invalidates a visible context and any operation that has not presented yet. */
    override fun cancelActive() {
        val current = withStateLock {
            cancellationEpoch = Any()
            currentOperation.also { currentOperation = null }
        }
        current?.activeContext?.cancel()
    }

    private fun takeOperation(operation: Operation): ReservedOperation? = withStateLock {
        currentOperation
            ?.takeIf { current -> current.operation.identity === operation.identity }
            ?.also { currentOperation = null }
    }

    private fun releaseOperation(operation: Operation) {
        takeOperation(operation)?.activeContext?.invalidate()
    }

    private inline fun <T> withStateLock(block: () -> T): T {
        stateLock.lock()
        return try {
            block()
        } finally {
            stateLock.unlock()
        }
    }

    internal class Operation internal constructor(
        internal val identity: Any,
        internal val cancellationEpoch: Any,
    )

    private inner class ReservedOperation(
        val operation: Operation,
        var activeContext: ActiveContext? = null,
    )

    private inner class ActiveContext(
        val context: Context,
        val reportCancelled: () -> Unit,
    ) {
        fun invalidate() {
            runCatching { invalidateContext(context) }
        }

        fun cancel() {
            invalidate()
            runCatching(reportCancelled)
        }
    }
}
