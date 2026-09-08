package com.passvault.core.database.repository

import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.repository.CredentialHealthInput
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Real encrypted records; fault injection is restricted to the completed-result boundary. */
class CredentialResultOwnershipTest : RepositorySecurityIntegrationFixture() {
    @Test
    fun `credential is wiped when cancellation rejects a completed session result`() = runTest {
        createAndUnlockVault()
        val input = sampleCredential()
        val session = RejectingResultSession(vaultRepository, CancellationException("synthetic handoff cancellation"))
        try {
            credentialRepository.save(input).getOrThrow()
            assertFailsWith<CancellationException> { repository(session).getById(input.id) }
            assertCredentialCleared(assertIs<Credential>(session.produced))
        } finally {
            input.clearSensitiveValues()
            (session.produced as? Credential)?.clearSensitiveValues()
        }
    }

    @Test
    fun `health batch is wiped when cancellation rejects a completed session result`() = runTest {
        createAndUnlockVault()
        val input = sampleCredential()
        val session = RejectingResultSession(vaultRepository, CancellationException("synthetic handoff cancellation"))
        try {
            credentialRepository.save(input).getOrThrow()
            assertFailsWith<CancellationException> { repository(session).getCredentialsForHealthAnalysis() }
            assertHealthCleared(session.healthInputs().single())
        } finally {
            input.clearSensitiveValues()
            session.healthInputs().forEach(::clearHealthInput)
        }
    }

    @Test
    fun `ordinary post-result failure wipes credentials and health inputs`() = runTest {
        createAndUnlockVault()
        val input = sampleCredential()
        val session = RejectingResultSession(vaultRepository, IllegalStateException("synthetic revocation"))
        try {
            credentialRepository.save(input).getOrThrow()
            assertTrue(repository(session).getById(input.id).isFailure)
            assertCredentialCleared(assertIs<Credential>(session.produced))
            assertTrue(repository(session).getCredentialsForHealthAnalysis().isFailure)
            assertHealthCleared(session.healthInputs().single())
        } finally {
            input.clearSensitiveValues()
            (session.produced as? Credential)?.clearSensitiveValues()
            session.healthInputs().forEach(::clearHealthInput)
        }
    }

    @Test
    fun `successful delivery transfers live values to the consumer`() = runTest {
        createAndUnlockVault()
        val input = sampleCredential()
        var delivered: Credential? = null
        var health: List<CredentialHealthInput> = emptyList()
        try {
            credentialRepository.save(input).getOrThrow()
            delivered = assertNotNull(credentialRepository.getById(input.id).getOrThrow())
            health = credentialRepository.getCredentialsForHealthAnalysis().getOrThrow()
            assertEquals("hunter2", delivered.password?.toStringUnsafe())
            assertEquals("hunter2", health.single().password?.toStringUnsafe())
        } finally {
            input.clearSensitiveValues()
            delivered?.clearSensitiveValues()
            health.forEach(::clearHealthInput)
        }
    }

    @Test
    fun `real session lease release failure cannot abandon completed secret results`() = runTest {
        createAndUnlockVault()
        val input = sampleCredential()
        val password = SensitiveText.from("correct horse battery staple")
        val releaseEngine = LeaseReleaseFailureEngine(cryptoEngine)
        val releaseRepository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = releaseEngine,
            keyHierarchy = VaultKeyHierarchy(releaseEngine),
        )
        val session = object : VaultSessionManager by releaseRepository {
            var produced: Any? = null

            override suspend fun <T> withUnlockedSession(block: suspend (ByteArray) -> T): T =
                releaseRepository.withUnlockedSession { key ->
                    block(key).also { value ->
                        produced = value
                        releaseEngine.failReleaseOf = key
                    }
                }
        }
        try {
            credentialRepository.save(input).getOrThrow()
            vaultRepository.lock().getOrThrow()
            releaseRepository.unlock(password).getOrThrow()
            val guarded = repository(session)
            assertTrue(guarded.getById(input.id).isFailure)
            assertCredentialCleared(assertIs<Credential>(session.produced))
            assertTrue(guarded.getCredentialsForHealthAnalysis().isFailure)
            val batch = assertIs<List<*>>(session.produced).filterIsInstance<CredentialHealthInput>()
            assertHealthCleared(batch.single())
            assertEquals(2, releaseEngine.failedReleases)
        } finally {
            releaseEngine.failReleaseOf = null
            releaseRepository.lock()
            input.clearSensitiveValues()
            password.clear()
            (session.produced as? Credential)?.clearSensitiveValues()
            (session.produced as? List<*>)?.filterIsInstance<CredentialHealthInput>()?.forEach(::clearHealthInput)
        }
    }

    private fun repository(session: VaultSessionManager) = CredentialRepositoryImpl(
        credentialDao = database.credentialDao(),
        folderDao = database.folderDao(),
        tagDao = database.tagDao(),
        attachmentDao = database.attachmentDao(),
        passwordHistoryDao = database.passwordHistoryDao(),
        cryptoEngine = cryptoEngine,
        sessionManager = session,
    )

    private fun assertCredentialCleared(credential: Credential) {
        val values = listOf(credential.username, credential.email, credential.password, credential.notes) +
            credential.recoveryCodes + credential.apiKeys + credential.licenseKeys +
            credential.customFields.map { it.value } + credential.passwordHistory.map { it.password } +
            listOf(credential.totp?.secret)
        values.filterNotNull().forEach(::assertCleared)
    }

    private fun assertHealthCleared(input: CredentialHealthInput) {
        listOfNotNull(input.username, input.email, input.password).forEach(::assertCleared)
    }

    private fun assertCleared(value: SensitiveText) {
        value.withExposed { chars -> assertTrue(chars.all { it == '\u0000' }) }
    }

    private fun clearHealthInput(input: CredentialHealthInput) {
        input.username?.clear()
        input.email?.clear()
        input.password?.clear()
    }

    private class RejectingResultSession(
        private val delegate: VaultSessionManager,
        private val failure: Exception,
    ) : VaultSessionManager by delegate {
        var produced: Any? = null

        override suspend fun <T> withUnlockedSession(block: suspend (ByteArray) -> T): T =
            delegate.withUnlockedSession { key ->
                produced = block(key)
                throw failure
            }

        fun healthInputs(): List<CredentialHealthInput> =
            (produced as? List<*>)?.filterIsInstance<CredentialHealthInput>().orEmpty()
    }

    private class LeaseReleaseFailureEngine(private val delegate: CryptoEngine) : CryptoEngine by delegate {
        var failReleaseOf: ByteArray? = null
        var failedReleases = 0

        override fun secureWipe(data: ByteArray) {
            delegate.secureWipe(data)
            if (data === failReleaseOf) {
                failReleaseOf = null
                failedReleases++
                error("synthetic lease release failure")
            }
        }
    }
}
