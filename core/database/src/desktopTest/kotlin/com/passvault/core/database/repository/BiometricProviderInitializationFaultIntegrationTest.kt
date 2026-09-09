package com.passvault.core.database.repository

import com.passvault.core.crypto.CiphertextAuthenticationException
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.DerivedKey
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.crypto.EncryptedData
import com.passvault.core.crypto.VaultKeyHierarchy
import com.passvault.core.crypto.WrappedKey
import com.passvault.core.database.dao.VaultMetadataDao
import com.passvault.core.database.entity.VaultMetadataEntity
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.security.BiometricFailureReason
import com.passvault.core.security.BiometricOperationResult
import java.io.IOException
import java.lang.management.ManagementFactory
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assume.assumeTrue
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * PVA-038 actual-provider regression DESIGN. No subprocesses are launched here.
 * Default execution is explicitly SKIPPED, never counted as provider evidence.
 *
 * Admission must select exactly ONE method in each new JDK-17 test JVM, with
 * PASSVAULT_PVA038_MODE=prepare/success/wrong-key/loader-io respectively. Keep
 * the compact PVA038_FIXTURE output from prepare and supply exactly that value
 * as PASSVAULT_PVA038_FIXTURE to all three consumers. Never create/enroll a real
 * vault or run another crypto test in a consumer JVM before this method.
 *
 * A separately reviewed owner must create/own PASSVAULT_PVA038_RUN_ROOT, its
 * empty home/jna/tmp directories, and (loader-io only) zero-byte tmp-blocker file.
 * The worker's STARTUP -Duser.home, -Djna.tmpdir, -Djava.io.tmpdir must name those
 * exact children (tmp-blocker instead of tmp for loader-io). Setting a Gradle
 * client's -D alone does not establish worker properties. The owner supplies
 * dependency/source identity, serial admission, resource bounds and post-JVM
 * native/temp cleanup; these source checks do not replace lifecycle admission.
 *
 * Metadata/OS-key storage is deliberately in-memory: SQLite/JNA startup must
 * not consume the targeted fault first. Actual crypto is not replaced or fault-
 * injected. A Linux result is JVM crypto/service evidence, NOT Linux biometrics,
 * Keychain, Hello or physical-device evidence. Other Errors fail, not pass.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BiometricProviderInitializationFaultIntegrationTest {
    @Test
    fun `prepare public synthetic fixture in a separate provider process`() = runTest {
        requireDedicatedLaunch("prepare")
        assertFalse(providerIsInitialized(), "Fixture producer must also start in a fresh JVM")
        val engine = DesktopCryptoEngine()
        val key = syntheticKey()
        val salt = syntheticSalt()
        val password = SYNTHETIC_PASSWORD.encodeToByteArray()
        val plaintext = ByteArray(32) { (it + 0x60).toByte() }
        var derived: DerivedKey? = null
        var wrapped: WrappedKey? = null
        var verification: EncryptedData? = null
        var envelope: ByteArray? = null
        try {
            val derivedKey = engine.deriveKey(password, salt, 2, 32 * 1024 * 1024).getOrThrow()
            derived = derivedKey
            val wrappedKey = VaultKeyHierarchy(engine).wrapVEK(key, derivedKey.key).getOrThrow()
            wrapped = wrappedKey
            val encrypted = engine.encrypt(plaintext, key, "verification".encodeToByteArray()).getOrThrow()
            verification = encrypted
            val stored = CryptoEnvelope.encode(encrypted)
            envelope = stored
            assertTrue(providerIsInitialized())
            // Only public test-key ciphertext/nonces, never an application export.
            println(
                "PVA038_FIXTURE=pva038-v1:" + listOf(
                    wrappedKey.ciphertext, wrappedKey.nonce, stored, encrypted.nonce,
                ).joinToString(":") { it.toHex() },
            )
        } finally {
            envelope?.fill(0)
            verification?.clear()
            wrapped?.clear()
            derived?.clear()
            plaintext.fill(0)
            password.fill(0)
            salt.fill(0)
            key.fill(0)
        }
    }

    @Test
    fun `fresh real provider authenticates the fixture and retains password fallback`() = runTest {
        requireDedicatedLaunch("success")
        withColdFixture {
            assertEquals(BiometricOperationResult.Success, service.unlock())
            assertTrue(providerIsInitialized())
            assertEquals(1, engine.verificationCalls)
            assertNull(engine.lastFailure)
            assertEquals(1, metadata.lastAccessWrites)
            assertTrue(repository.isUnlocked())
            assertTrue(keyStore.isEnrolled)
            assertEquals(0, keyStore.deleteCalls)
            assertTransferredMaterialWiped()

            repository.lock().getOrThrow()
            val password = SensitiveText.from(SYNTHETIC_PASSWORD)
            val expectedKey = syntheticKey()
            try {
                repository.unlock(password).getOrThrow()
                repository.withUnlockedSession { assertContentEquals(expectedKey, it) }
            } finally {
                expectedKey.fill(0)
                password.clear()
            }
        }
    }

    @Test
    fun `fresh real provider rejects a wrong key with typed authentication failure`() = runTest {
        requireDedicatedLaunch("wrong-key")
        withColdFixture {
            keyStore.corruptKey()
            assertEquals(BiometricOperationResult.Failure(BiometricFailureReason.INVALIDATED), service.unlock())
            assertTrue(providerIsInitialized())
            assertTrue(engine.lastFailure is CiphertextAuthenticationException)
            assertEquals(1, engine.verificationCalls)
            assertFalse(keyStore.isEnrolled)
            assertEquals(1, keyStore.deleteCalls)
            assertNeverPublished()
            assertTransferredMaterialWiped()
        }
    }

    @Test
    fun `actual cold resource loader IO failure keeps a valid enrollment`() = runTest {
        requireDedicatedLaunch("loader-io")
        withColdFixture {
            assertEquals(BiometricOperationResult.Failure(BiometricFailureReason.INTERNAL_ERROR), service.unlock())
            val failure = assertNotNull(engine.lastFailure)
            assertEquals("com.goterl.resourceloader.ResourceLoaderException", failure.javaClass.name)
            val cause = assertNotNull(failure.cause)
            assertTrue(cause is IOException, "The witness must be loader IO, not native linkage or bad fixture data")
            assertTrue(cause.stackTrace.any {
                it.className == "com.goterl.resourceloader.ResourceLoader" &&
                    it.methodName == "createMainTempDirectory"
            })
            assertTrue(cause.stackTrace.any {
                it.className == "java.nio.file.Files" && it.methodName == "createTempDirectory"
            })
            assertFalse(providerIsInitialized())
            assertEquals(1, engine.verificationCalls)
            assertNull(engine.lastPlaintext)
            assertTrue(keyStore.isEnrolled)
            assertEquals(0, keyStore.deleteCalls)
            assertNeverPublished()
            assertTransferredMaterialWiped()
            println("PVA038_PROVIDER_FAULT=ResourceLoaderException/IOException/createMainTempDirectory")
        }
    }

    private fun requireDedicatedLaunch(mode: String) {
        val requestedMode = System.getenv("PASSVAULT_PVA038_MODE")
        assumeTrue(
            "PVA-038 actual-provider verification requires dedicated admission; SKIPPED is not PASS",
            requestedMode != null,
        )
        assertEquals(mode, requestedMode, "Select exactly one matching method in a fresh worker JVM")
        assertEquals("17", System.getProperty("java.specification.version"))
        val root = Path.of(requireNotNull(System.getenv("PASSVAULT_PVA038_RUN_ROOT")))
        assertTrue(root.isAbsolute)
        assertEquals(root, root.normalize())
        assertEquals(root, root.toRealPath())
        assertTrue(Files.isDirectory(root, NOFOLLOW_LINKS))
        assertTrue(root.fileName.toString().startsWith("pva038-"))
        val temporary = root.resolve(if (mode == "loader-io") "tmp-blocker" else "tmp")
        if (mode == "loader-io") {
            assertTrue(Files.isRegularFile(temporary, NOFOLLOW_LINKS))
            assertEquals(0L, Files.size(temporary))
        } else {
            assertTrue(Files.isDirectory(temporary, NOFOLLOW_LINKS))
        }
        val required = mapOf(
            "java.io.tmpdir" to temporary,
            "jna.tmpdir" to root.resolve("jna"),
            "user.home" to root.resolve("home"),
        )
        val arguments = ManagementFactory.getRuntimeMXBean().inputArguments
        required.forEach { (property, path) ->
            assertEquals(path, path.toRealPath())
            if (property != "java.io.tmpdir") assertTrue(Files.isDirectory(path, NOFOLLOW_LINKS))
            assertEquals(path.toString(), System.getProperty(property))
            assertTrue(arguments.contains("-D$property=$path"), "Required at JVM startup, not via setProperty")
        }
    }

    private suspend fun TestScope.withColdFixture(block: suspend ProviderFixture.() -> Unit) {
        assertFalse(providerIsInitialized(), "A warm/global provider is not the admitted cold-start witness")
        val metadata = SyntheticProviderMetadataDao(readFixture())
        val engine = RecordingBiometricVerificationCryptoEngine(DesktopCryptoEngine())
        val repository = VaultRepositoryImpl(metadata, engine, VaultKeyHierarchy(engine))
        val keyStore = SyntheticBiometricVerificationKeyStore()
        val key = syntheticKey()
        val observed = mutableListOf<VaultSessionState>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.getSessionState().collect(observed::add)
        }
        try {
            keyStore.enroll(SYNTHETIC_VAULT_ID, key).getOrThrow()
            assertNull(engine.verificationResult)
            assertNull(engine.thrownFailure)
            assertFalse(providerIsInitialized())
            ProviderFixture(
                repository, DefaultBiometricUnlockService(repository, repository, keyStore, engine),
                metadata, engine, keyStore, observed,
            ).block()
        } finally {
            try {
                withContext(NonCancellable) {
                    try {
                        collector.cancelAndJoin()
                    } finally {
                        repository.lock().getOrThrow()
                    }
                }
            } finally {
                key.fill(0)
                keyStore.dispose()
                metadata.dispose()
            }
        }
    }

    private class ProviderFixture(
        val repository: VaultRepositoryImpl,
        val service: DefaultBiometricUnlockService,
        val metadata: SyntheticProviderMetadataDao,
        val engine: RecordingBiometricVerificationCryptoEngine,
        val keyStore: SyntheticBiometricVerificationKeyStore,
        val observed: List<VaultSessionState>,
    ) {
        suspend fun assertNeverPublished() {
            assertFalse(repository.isUnlocked())
            assertFalse(observed.any { it is VaultSessionState.Unlocked })
            assertEquals(0, metadata.lastAccessWrites)
        }

        fun assertTransferredMaterialWiped() {
            val released = assertNotNull(keyStore.lastReturnedKey)
            val candidate = assertNotNull(engine.lastCandidate)
            assertTrue(candidate !== released)
            assertTrue(released.all { it == 0.toByte() })
            assertTrue(candidate.all { it == 0.toByte() })
            engine.lastPlaintext?.let { assertTrue(it.all { byte -> byte == 0.toByte() }) }
        }
    }

    private fun readFixture(): VaultMetadataEntity {
        val encoded = requireNotNull(System.getenv("PASSVAULT_PVA038_FIXTURE"))
        // Version token + four exact bounded hex fields. No file or private vault reads.
        assertEquals(317, encoded.length)
        val parts = encoded.split(':')
        assertEquals(5, parts.size)
        assertEquals("pva038-v1", parts[0])
        return VaultMetadataEntity(
            vaultFormatVersion = 1,
            cryptoFormatVersion = 2,
            vaultId = SYNTHETIC_VAULT_ID,
            argon2AlgorithmId = "Argon2id",
            argon2Salt = syntheticSalt(),
            argon2OpsLimit = 2,
            argon2MemLimit = 32 * 1024 * 1024,
            argon2Parallelism = 1,
            wrappedVek = parts[1].fromHex(52),
            vekNonce = parts[2].fromHex(24),
            encryptedVerificationRecord = parts[3].fromHex(52),
            verificationNonce = parts[4].fromHex(24),
            createdAt = 0,
            lastAccessedAt = null,
            entryCount = 0,
        )
    }

    // Reflection observes the runtime dependency without adding an implementation
    // dependency to this module or replacing the initializer/loader/AEAD operation.
    private fun providerIsInitialized(): Boolean {
        val type = Class.forName("com.ionspin.kotlin.crypto.LibsodiumInitializer")
        val instance = type.getField("INSTANCE").get(null)
        return type.getMethod("isInitialized").invoke(instance) as Boolean
    }

    private fun syntheticKey(): ByteArray = ByteArray(32) { (it + 1).toByte() }
    private fun syntheticSalt(): ByteArray = ByteArray(16) { (it + 0x20).toByte() }
    private fun ByteArray.toHex(): String = joinToString("") { (it.toInt() and 0xff).toString(16).padStart(2, '0') }

    private fun String.fromHex(bytes: Int): ByteArray {
        assertEquals(bytes * 2, length)
        assertTrue(all { it in '0'..'9' || it in 'a'..'f' })
        return ByteArray(bytes) { substring(it * 2, it * 2 + 2).toInt(16).toByte() }
    }

    private companion object {
        const val SYNTHETIC_VAULT_ID = "pva038-synthetic-vault"
        const val SYNTHETIC_PASSWORD = "Pva038-only synthetic master passphrase!"
    }
}

/** No Room/native bootstrap in the fault worker; no persistent storage of any kind. */
private class SyntheticProviderMetadataDao(private var metadata: VaultMetadataEntity) : VaultMetadataDao {
    var lastAccessWrites = 0
        private set

    override suspend fun get(): VaultMetadataEntity = metadata
    override suspend fun getVaultFormatVersion(): Int = metadata.vaultFormatVersion
    override suspend fun exists(): Boolean = true
    override suspend fun insert(entity: VaultMetadataEntity): Long = error("Fixture is precomputed")
    override suspend fun update(entity: VaultMetadataEntity): Unit = error("Fixture is immutable")

    override suspend fun updateLastAccessed(timestamp: Long) {
        lastAccessWrites++
        metadata = metadata.copy(lastAccessedAt = timestamp)
    }

    override suspend fun updateEncryptionParameters(
        salt: ByteArray,
        opsLimit: Int,
        memLimit: Int,
        parallelism: Int,
        wrappedVek: ByteArray,
        vekNonce: ByteArray,
        verificationRecord: ByteArray,
        verificationNonce: ByteArray,
        lastAccessedAt: Long,
    ): Unit = error("Fixture is immutable")

    fun dispose() {
        metadata.argon2Salt.fill(0)
        metadata.wrappedVek.fill(0)
        metadata.vekNonce.fill(0)
        metadata.encryptedVerificationRecord.fill(0)
        metadata.verificationNonce.fill(0)
    }
}
