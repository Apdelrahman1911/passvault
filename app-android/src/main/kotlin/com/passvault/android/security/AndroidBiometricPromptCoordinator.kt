package com.passvault.android.security

import com.passvault.core.security.BiometricPromptController

/**
 * Owns the single Android biometric prompt that may currently complete.
 *
 * An operation snapshots the cancellation epoch before doing any preparatory
 * work. A lock or host detach advances that epoch, so a prompt that has not
 * yet been shown is rejected just like one that is already visible. Terminal
 * callbacks must claim their operation before publishing a result; callbacks
 * from cancelled or superseded prompts are therefore inert.
 */
internal class AndroidBiometricPromptCoordinator(
    private val dispatchToMain: (() -> Unit) -> Unit,
) : BiometricPromptController {
    private val stateLock = Any()
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

    fun beginOperation(): Operation? = synchronized(stateLock) {
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
        cancelAuthentication: () -> Unit,
        reportCancelled: () -> Unit,
    ): Boolean {
        val cancellation = ActiveCancellation(
            cancelAuthentication = cancelAuthentication,
            reportCancelled = reportCancelled,
        )
        val accepted = synchronized(stateLock) {
            val current = currentOperation
            if (
                operation.cancellationEpoch !== cancellationEpoch ||
                current?.operation?.identity !== operation.identity ||
                current.activeCancellation != null
            ) {
                false
            } else {
                current.activeCancellation = cancellation
                true
            }
        }
        if (!accepted) cancellation.cancel()
        return accepted
    }

    /** Claims a terminal prompt callback. False means the callback is stale. */
    fun finishPrompt(operation: Operation): Boolean = synchronized(stateLock) {
        val current = currentOperation
        if (current?.operation?.identity === operation.identity && current.activeCancellation != null) {
            current.activeCancellation = null
            true
        } else {
            false
        }
    }

    /** Cancels only this operation, as used by coroutine cancellation. */
    fun cancel(operation: Operation) {
        takeOperation(operation)?.activeCancellation?.cancel()
    }

    /** Invalidates both the visible prompt and any operation not shown yet. */
    override fun cancelActive() {
        val current = synchronized(stateLock) {
            cancellationEpoch = Any()
            currentOperation.also { currentOperation = null }
        }
        current?.activeCancellation?.cancel()
    }

    private fun takeOperation(operation: Operation): ReservedOperation? = synchronized(stateLock) {
        currentOperation
            ?.takeIf { current -> current.operation.identity === operation.identity }
            ?.also { currentOperation = null }
    }

    private fun releaseOperation(operation: Operation) {
        takeOperation(operation)
    }

    internal class Operation internal constructor(
        internal val identity: Any,
        internal val cancellationEpoch: Any,
    )

    private class ReservedOperation(
        val operation: Operation,
        var activeCancellation: ActiveCancellation? = null,
    )

    private inner class ActiveCancellation(
        val cancelAuthentication: () -> Unit,
        val reportCancelled: () -> Unit,
    ) {
        fun cancel() {
            runCatching {
                dispatchToMain {
                    runCatching(cancelAuthentication)
                }
            }
            reportCancelled()
        }
    }
}
