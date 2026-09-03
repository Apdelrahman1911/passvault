package com.passvault.shared

internal sealed interface IosBackgroundCleanupOutcome {
    data object Succeeded : IosBackgroundCleanupOutcome
    data object LockFailed : IosBackgroundCleanupOutcome
    data class AcknowledgementTimedOut(val requestEpoch: Long) : IosBackgroundCleanupOutcome
    data object RuntimeUnavailable : IosBackgroundCleanupOutcome
}

internal data class IosBackgroundCleanupRetryState(
    val lockFailures: Int = 0,
    val acknowledgementTimeouts: Int = 0,
)

internal sealed interface IosBackgroundCleanupResolution {
    data object Complete : IosBackgroundCleanupResolution
    data object RecoveryRequired : IosBackgroundCleanupResolution
    data class RetryLock(
        val delayMillis: Long,
        val retryState: IosBackgroundCleanupRetryState,
    ) : IosBackgroundCleanupResolution

    data class RetryAcknowledgement(
        val requestEpoch: Long,
        val delayMillis: Long,
        val retryState: IosBackgroundCleanupRetryState,
    ) : IosBackgroundCleanupResolution
}

/** Keeps lock failures and UI acknowledgement stalls on separate bounded budgets. */
internal class IosBackgroundCleanupRetryPolicy(
    private val maximumLockAttempts: Int = DEFAULT_MAXIMUM_ATTEMPTS,
    private val maximumAcknowledgementAttempts: Int = DEFAULT_MAXIMUM_ATTEMPTS,
    private val baseRetryDelayMillis: Long = DEFAULT_RETRY_DELAY_MILLIS,
) {
    init {
        require(maximumLockAttempts > 0)
        require(maximumAcknowledgementAttempts > 0)
        require(baseRetryDelayMillis > 0L)
        require(baseRetryDelayMillis <= Long.MAX_VALUE / maxOf(maximumLockAttempts, maximumAcknowledgementAttempts))
    }

    fun resolve(
        outcome: IosBackgroundCleanupOutcome,
        retryState: IosBackgroundCleanupRetryState,
    ): IosBackgroundCleanupResolution = when (outcome) {
        IosBackgroundCleanupOutcome.Succeeded -> IosBackgroundCleanupResolution.Complete
        IosBackgroundCleanupOutcome.RuntimeUnavailable -> IosBackgroundCleanupResolution.RecoveryRequired
        IosBackgroundCleanupOutcome.LockFailed -> resolveLockFailure(retryState)
        is IosBackgroundCleanupOutcome.AcknowledgementTimedOut -> {
            resolveAcknowledgementTimeout(outcome.requestEpoch, retryState)
        }
    }

    private fun resolveLockFailure(
        retryState: IosBackgroundCleanupRetryState,
    ): IosBackgroundCleanupResolution {
        val failures = retryState.lockFailures + 1
        return if (failures >= maximumLockAttempts) {
            IosBackgroundCleanupResolution.RecoveryRequired
        } else {
            IosBackgroundCleanupResolution.RetryLock(
                delayMillis = retryDelay(failures),
                retryState = retryState.copy(lockFailures = failures),
            )
        }
    }

    private fun resolveAcknowledgementTimeout(
        requestEpoch: Long,
        retryState: IosBackgroundCleanupRetryState,
    ): IosBackgroundCleanupResolution {
        require(requestEpoch > 0L)
        val timeouts = retryState.acknowledgementTimeouts + 1
        return if (timeouts >= maximumAcknowledgementAttempts) {
            IosBackgroundCleanupResolution.RecoveryRequired
        } else {
            IosBackgroundCleanupResolution.RetryAcknowledgement(
                requestEpoch = requestEpoch,
                delayMillis = retryDelay(timeouts),
                retryState = retryState.copy(acknowledgementTimeouts = timeouts),
            )
        }
    }

    private fun retryDelay(failureCount: Int): Long = baseRetryDelayMillis * failureCount

    private companion object {
        const val DEFAULT_MAXIMUM_ATTEMPTS = 3
        const val DEFAULT_RETRY_DELAY_MILLIS = 250L
    }
}
