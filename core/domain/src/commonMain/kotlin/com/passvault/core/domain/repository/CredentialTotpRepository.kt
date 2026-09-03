package com.passvault.core.domain.repository

import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.TotpConfiguration
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Narrow decrypted-data boundary for displaying current authenticator codes.
 *
 * The producer owns returned TOTP secrets until the caller atomically takes
 * them from the lease. Callers that take a batch must clear every input when
 * the authenticator screen is hidden or the vault locks.
 */
interface CredentialTotpRepository {
    suspend fun getCredentialsForTotpDisplay(): Result<CredentialTotpInputLease>
}

/**
 * Cancellation-safe ownership handoff for a decrypted TOTP input batch.
 *
 * The coroutine awaiting production retains cleanup responsibility until
 * [take] wins. If that owner completes before transfer, its completion handler
 * clears the batch. Once [take] succeeds, the caller becomes the sole owner.
 */
@OptIn(ExperimentalAtomicApi::class)
class CredentialTotpInputLease private constructor(
    inputs: List<CredentialTotpInput>,
    ownerJob: Job,
) {
    private val ownedInputs = AtomicReference<List<CredentialTotpInput>?>(inputs)

    init {
        ownerJob.invokeOnCompletion { clear() }
    }

    /** Returns the batch only when this call wins ownership of it. */
    fun take(): List<CredentialTotpInput>? = ownedInputs.exchange(null)

    /** Wipes the batch only when ownership has not already transferred. */
    fun clear() {
        ownedInputs.exchange(null)?.forEach(CredentialTotpInput::clear)
    }

    companion object {
        /**
         * Binds [inputs] to [ownerJob]. Producers that create work in a child
         * coroutine must pass the job of the coroutine awaiting that work.
         */
        fun ownedByCoroutine(
            inputs: List<CredentialTotpInput>,
            ownerJob: Job,
        ): CredentialTotpInputLease = CredentialTotpInputLease(inputs, ownerJob)

        /** Binds [inputs] to the coroutine invoking this factory. */
        suspend fun ownedByCurrentCoroutine(
            inputs: List<CredentialTotpInput>,
        ): CredentialTotpInputLease {
            val ownerJob = currentCoroutineContext()[Job]
            if (ownerJob == null) {
                inputs.forEach(CredentialTotpInput::clear)
                error("A TOTP input lease requires a coroutine Job")
            }
            return ownedByCoroutine(inputs, ownerJob)
        }
    }
}

data class CredentialTotpInput(
    val id: CredentialId,
    val title: String,
    val displayUsername: String?,
    val configuration: TotpConfiguration,
) {
    fun clear() {
        configuration.clear()
    }
}
