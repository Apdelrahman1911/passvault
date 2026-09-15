package com.passvault.core.database.repository

import com.passvault.core.crypto.CiphertextAuthenticationException
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricFailureReason
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricKeyStoreException
import com.passvault.core.security.BiometricOperationResult
import com.passvault.core.security.BiometricType
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * PVA-038 routing/ownership regressions: real Room and vault crypto, synthetic
 * protected-key release. Injected failures do NOT reproduce cold native loading;
 * the separately opted-in provider test covers that distinct evidence boundary.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BiometricVerificationFailureIntegrationTest : RepositorySecurityIntegrationFixture() {
    @Test
    fun `provider result failure preserves enrollment for a later verified unlock`() = runTest {
        withVerificationFixture {
            val failure = RuntimeException("Synthetic provider initialization failure", IOException("Synthetic IO"))
            engine.verificationResult = Result.failure(failure)

            assertEquals(internalFailure(), service.unlock())
            assertSame(failure, engine.lastFailure)
            assertEnrollmentPreserved()
            assertEquals(lastAccessedBeforeAttempt, database.vaultMetadataDao().get()?.lastAccessedAt)

            engine.verificationResult = null
            assertEquals(BiometricOperationResult.Success, service.unlock())
            assertTrue(repository.isUnlocked())
            assertTrue(keyStore.isEnrolled)
            assertEquals(0, keyStore.deleteCalls)
            assertTransferredMaterialWiped()
        }
    }

    @Test
    fun `thrown provider failure is not a rejected biometric key`() = runTest {
        withVerificationFixture {
            val failure = IllegalStateException("Synthetic provider failure")
            engine.thrownFailure = failure

            assertEquals(internalFailure(), service.unlock())
            assertSame(failure, engine.lastFailure)
            assertEnrollmentPreserved()
        }
    }

    @Test
    fun `generic argument failure is not an authentication rejection`() = runTest {
        withVerificationFixture {
            val failure = IllegalArgumentException("Synthetic provider argument failure")
            engine.verificationResult = Result.failure(failure)

            assertEquals(internalFailure(), service.unlock())
            assertSame(failure, engine.lastFailure)
            assertEnrollmentPreserved()
        }
    }

    @Test
    fun `verification cancellation propagates without deleting enrollment`() = runTest {
        withVerificationFixture {
            val cancellation = CancellationException("Synthetic verifier cancellation")
            engine.verificationResult = Result.failure(cancellation)

            assertSame(cancellation, assertFailsWith<CancellationException> { service.unlock() })
            assertEnrollmentPreserved()
        }
    }

    @Test
    fun `explicit malformed verification plaintext still invalidates and is wiped`() = runTest {
        withVerificationFixture {
            // A conforming real AEAD cannot return 31 bytes for this fixed envelope.
            // This preserves the existing defensive policy, not a new format claim.
            val malformed = ByteArray(31) { 0x6a }
            engine.verificationResult = Result.success(malformed)

            assertEquals(invalidatedFailure(), service.unlock())
            assertEnrollmentRejected()
            assertTrue(malformed.all { it == 0.toByte() })
            assertSame(malformed, engine.lastPlaintext)
        }
    }

    @Test
    fun `real wrong key authentication failure still invalidates enrollment`() = runTest {
        withVerificationFixture {
            keyStore.corruptKey()

            assertEquals(invalidatedFailure(), service.unlock())
            assertTrue(engine.lastFailure is CiphertextAuthenticationException)
            assertEnrollmentRejected()
        }
    }

    @Test
    fun `real tampered verification tag still invalidates enrollment`() = runTest {
        withVerificationFixture {
            val metadata = assertNotNull(database.vaultMetadataDao().get())
            val tampered = metadata.encryptedVerificationRecord.copyOf()
            tampered[tampered.lastIndex] = (tampered.last().toInt() xor 1).toByte()
            database.vaultMetadataDao().update(metadata.copy(encryptedVerificationRecord = tampered))

            assertEquals(invalidatedFailure(), service.unlock())
            assertTrue(engine.lastFailure is CiphertextAuthenticationException)
            assertEnrollmentRejected()
        }
    }

    @Test
    fun `malformed metadata remains outside the invalidating verifier boundary`() = runTest {
        withVerificationFixture {
            val stateBeforeAttempt = repository.getSessionState().first()
            val metadata = assertNotNull(database.vaultMetadataDao().get())
            database.vaultMetadataDao().update(metadata.copy(verificationNonce = ByteArray(23)))

            assertEquals(internalFailure(), service.unlock())
            // The first service metadata lookup rejects before OS retrieval or
            // the repository's unlock transition; the initial state is unchanged.
            assertEquals(stateBeforeAttempt, repository.getSessionState().first())
            assertFalse(repository.isUnlocked())
            assertFalse(observedStates.any { it is VaultSessionState.Unlocked })
            assertTrue(keyStore.isEnrolled)
            assertEquals(0, keyStore.deleteCalls)
            assertEquals(0, keyStore.retrieveCalls)
            assertEquals(0, engine.verificationCalls)
            assertNull(keyStore.lastReturnedKey)
            assertNull(engine.lastCandidate)
        }
    }

    @Test
    fun `provider failure keeps password fallback usable without reenrollment`() = runTest {
        withVerificationFixture {
            engine.verificationResult = Result.failure(IOException("Synthetic provider failure"))
            assertEquals(internalFailure(), service.unlock())
            assertEnrollmentPreserved()
            engine.verificationResult = null
            val password = SensitiveText.from("correct horse battery staple")
            try {
                assertTrue(repository.unlock(password).isSuccess)
                assertTrue(repository.isUnlocked())
                assertTrue(keyStore.isEnrolled)
                assertEquals(0, keyStore.deleteCalls)
                assertTransferredMaterialWiped()
            } finally {
                password.clear()
            }
        }
    }

    private suspend fun TestScope.withVerificationFixture(block: suspend VerificationFixture.() -> Unit) {
        val keyStore = SyntheticBiometricVerificationKeyStore()
        var repository: VaultRepositoryImpl? = null
        try {
            createAndUnlockVault()
            val enrollment = DefaultBiometricUnlockService(vaultRepository, vaultRepository, keyStore, cryptoEngine)
            assertEquals(BiometricOperationResult.Success, enrollment.enable())
            vaultRepository.lock().getOrThrow()
            val engine = RecordingBiometricVerificationCryptoEngine(cryptoEngine)
            val verificationRepository = VaultRepositoryImpl(
                database.vaultMetadataDao(), engine, VaultKeyHierarchy(engine),
            )
            repository = verificationRepository
            val states = mutableListOf<VaultSessionState>()
            val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                verificationRepository.getSessionState().collect(states::add)
            }
            try {
                VerificationFixture(
                    verificationRepository,
                    DefaultBiometricUnlockService(verificationRepository, verificationRepository, keyStore, engine),
                    keyStore,
                    engine,
                    states,
                    database.vaultMetadataDao().get()?.lastAccessedAt,
                ).block()
            } finally {
                withContext(NonCancellable) { collector.cancelAndJoin() }
            }
        } finally {
            try {
                repository?.lock()?.getOrThrow()
            } finally {
                keyStore.dispose()
            }
        }
    }

    private class VerificationFixture(
        val repository: VaultRepositoryImpl,
        val service: DefaultBiometricUnlockService,
        val keyStore: SyntheticBiometricVerificationKeyStore,
        val engine: RecordingBiometricVerificationCryptoEngine,
        val observedStates: List<VaultSessionState>,
        val lastAccessedBeforeAttempt: Long?,
    ) {
        suspend fun assertEnrollmentPreserved() {
            assertLockedWithoutPublication()
            assertTrue(keyStore.isEnrolled)
            assertEquals(0, keyStore.deleteCalls)
            assertTransferredMaterialWiped()
        }

        suspend fun assertEnrollmentRejected() {
            assertLockedWithoutPublication()
            assertFalse(keyStore.isEnrolled)
            assertEquals(1, keyStore.deleteCalls)
            assertTransferredMaterialWiped()
        }

        fun assertTransferredMaterialWiped() {
            val released = assertNotNull(keyStore.lastReturnedKey)
            assertTrue(released.all { it == 0.toByte() })
            val candidate = assertNotNull(engine.lastCandidate)
            assertTrue(candidate !== released)
            assertTrue(candidate.all { it == 0.toByte() })
            engine.lastPlaintext?.let { assertTrue(it.all { byte -> byte == 0.toByte() }) }
        }

        private suspend fun assertLockedWithoutPublication() {
            assertFalse(repository.isUnlocked())
            assertTrue(repository.getSessionState().first() is VaultSessionState.Locked)
            assertFalse(observedStates.any { it is VaultSessionState.Unlocked })
        }
    }

    private fun internalFailure() = BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR)
    private fun invalidatedFailure() = BiometricOperationResult.Failure(BiometricFailureReason.INVALIDATED)
}

/** Default mode observes real decrypt unchanged; only the routing tests set injection fields. */
internal class RecordingBiometricVerificationCryptoEngine(
    private val delegate: CryptoEngine,
) : CryptoEngine by delegate {
    var verificationResult: Result<ByteArray>? = null
    var thrownFailure: Exception? = null
    var verificationCalls = 0
        private set
    var lastCandidate: ByteArray? = null
        private set
    var lastPlaintext: ByteArray? = null
        private set
    var lastFailure: Throwable? = null
        private set

    override suspend fun decrypt(
        ciphertext: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<ByteArray> {
        if (associatedData?.decodeToString() != "verification") {
            return delegate.decrypt(ciphertext, nonce, key, associatedData)
        }
        verificationCalls++
        lastCandidate = key
        lastPlaintext = null
        lastFailure = null
        thrownFailure?.let {
            lastFailure = it
            throw it
        }
        val result = verificationResult ?: delegate.decrypt(ciphertext, nonce, key, associatedData)
        lastFailure = result.exceptionOrNull()
        lastPlaintext = result.getOrNull()
        return result
    }
}

/** An in-memory stand-in for OS release/deletion, never an OS security assertion. */
internal class SyntheticBiometricVerificationKeyStore : BiometricKeyStore {
    private var key: ByteArray? = null
    private var enrolledVaultId: String? = null
    val isEnrolled: Boolean get() = key != null
    var deleteCalls = 0
        private set
    var retrieveCalls = 0
        private set
    var lastReturnedKey: ByteArray? = null
        private set

    override suspend fun getCapability() = BiometricCapability(
        type = BiometricType.GENERIC,
        availability = BiometricAvailability.AVAILABLE,
    )

    override suspend fun contains(vaultId: String): Boolean = isEnrolled && enrolledVaultId == vaultId

    override suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit> {
        dispose()
        enrolledVaultId = vaultId
        key = vaultKey.copyOf()
        return Result.success(Unit)
    }

    override suspend fun retrieve(vaultId: String): Result<ByteArray> {
        retrieveCalls++
        if (!contains(vaultId)) return Result.failure(BiometricKeyStoreException.NotEnabled())
        return Result.success(assertNotNull(key).copyOf().also { lastReturnedKey = it })
    }

    override suspend fun delete(vaultId: String): Result<Unit> {
        check(enrolledVaultId == vaultId)
        deleteCalls++
        dispose()
        return Result.success(Unit)
    }

    fun corruptKey() {
        val candidate = assertNotNull(key)
        candidate[0] = (candidate[0].toInt() xor 1).toByte()
    }

    fun dispose() {
        key?.fill(0)
        key = null
        enrolledVaultId = null
    }
}
