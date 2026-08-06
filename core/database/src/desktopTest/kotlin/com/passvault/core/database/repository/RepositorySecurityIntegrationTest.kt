package com.passvault.core.database.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.backup.VaultBackupService
import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.Folder
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.Tag
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.model.TotpAlgorithm
import com.passvault.core.domain.model.TotpConfiguration
import com.passvault.core.domain.model.UrlValue
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricKeyStoreException
import com.passvault.core.security.BiometricOperationResult
import com.passvault.core.security.BiometricType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Exercises the real Room + libsodium repository boundary. Fakes cannot
 * prove that encrypted records survive a lock, reject tampering, or preserve
 * all fields through serialization.
 */
class RepositorySecurityIntegrationTest {

    private lateinit var database: VaultDatabase
    private lateinit var cryptoEngine: DesktopCryptoEngine
    private lateinit var vaultRepository: VaultRepositoryImpl
    private lateinit var credentialRepository: CredentialRepositoryImpl
    private lateinit var folderRepository: FolderRepositoryImpl
    private lateinit var tagRepository: TagRepositoryImpl
    private lateinit var applicationScope: CoroutineScope

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<VaultDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        cryptoEngine = DesktopCryptoEngine()
        applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        vaultRepository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = cryptoEngine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(cryptoEngine),
            applicationScope = applicationScope,
        )
        credentialRepository = CredentialRepositoryImpl(
            credentialDao = database.credentialDao(),
            folderDao = database.folderDao(),
            tagDao = database.tagDao(),
            attachmentDao = database.attachmentDao(),
            passwordHistoryDao = database.passwordHistoryDao(),
            cryptoEngine = cryptoEngine,
            sessionManager = vaultRepository,
        )
        folderRepository = FolderRepositoryImpl(
            folderDao = database.folderDao(),
            cryptoEngine = cryptoEngine,
            sessionManager = vaultRepository,
        )
        tagRepository = TagRepositoryImpl(
            tagDao = database.tagDao(),
            cryptoEngine = cryptoEngine,
            sessionManager = vaultRepository,
        )
    }

    @AfterTest
    fun tearDown() {
        runBlocking { vaultRepository.lock() }
        applicationScope.cancel()
        database.close()
    }

    @Test
    fun `real vault lifecycle rejects wrong password and blocks reads while locked`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential()
        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            assertTrue(vaultRepository.lock().isSuccess)
            assertTrue(credentialRepository.getById(credential.id).isFailure)

            val wrongPassword = SensitiveText.from("not-the-master-password")
            try {
                val wrongUnlock = vaultRepository.unlock(wrongPassword)
                assertTrue(wrongUnlock.isFailure)
                assertTrue(wrongUnlock.exceptionOrNull()?.message.orEmpty().contains("unlock", ignoreCase = true))
            } finally {
                wrongPassword.clear()
            }

            val correctPassword = SensitiveText.from(TEST_MASTER_PASSWORD)
            try {
                assertTrue(vaultRepository.unlock(correctPassword).isSuccess)
                val restored = credentialRepository.getById(credential.id).getOrThrow()
                assertNotNull(restored)
                assertEquals("GitHub", restored.title)
                assertEquals("alice@example.com", restored.username?.toStringUnsafe())
            } finally {
                correctPassword.clear()
            }
        } finally {
            credential.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `credential payload is encrypted, lossless, and uses fresh nonces`() = runTest {
        createAndUnlockVault()
        val folder = Folder(
            id = FolderId("folder-work"),
            parentId = null,
            name = "Work",
            icon = "briefcase",
            sortOrder = 1,
            createdAt = Instant.fromEpochMilliseconds(100),
        )
        val tag = Tag(TagId("tag-important"), "Important", "#B45309")
        assertTrue(folderRepository.save(folder).isSuccess)
        assertTrue(tagRepository.save(tag).isSuccess)

        val credential = sampleCredential().copy(
            folderId = folder.id,
            tagIds = setOf(tag.id),
        )
        val changedCredential = credential.copy(
            password = SensitiveText.from("new-password-456"),
            updatedAt = Instant.fromEpochMilliseconds(2_000),
        )

        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            val firstEntity = requireNotNull(database.credentialDao().getById(credential.id.value))
            assertFalse(firstEntity.summaryPayload.decodeToString().contains("alice@example.com"))
            assertFalse(firstEntity.secretPayload.decodeToString().contains("hunter2"))
            assertFalse(firstEntity.secretPayload.decodeToString().contains("private recovery code"))

            val firstRead = credentialRepository.getById(credential.id).getOrThrow()
            assertNotNull(firstRead)
            assertEquals("alice@example.com", firstRead.username?.toStringUnsafe())
            assertEquals("hunter2", firstRead.password?.toStringUnsafe())
            assertEquals("private note", firstRead.notes?.toStringUnsafe())
            assertEquals(setOf(tag.id), firstRead.tagIds)
            assertEquals(folder.id, firstRead.folderId)
            assertEquals("private recovery code", firstRead.recoveryCodes.single().toStringUnsafe())
            assertEquals("custom-secret", firstRead.customFields.single().value.toStringUnsafe())

            assertTrue(credentialRepository.save(changedCredential).isSuccess)
            val secondEntity = requireNotNull(database.credentialDao().getById(credential.id.value))
            assertFalse(firstEntity.summaryNonce.contentEquals(secondEntity.summaryNonce))
            assertFalse(firstEntity.secretNonce.contentEquals(secondEntity.secretNonce))

            val updated = credentialRepository.getById(credential.id).getOrThrow()
            assertNotNull(updated)
            assertEquals("new-password-456", updated.password?.toStringUnsafe())
            assertEquals("hunter2", updated.passwordHistory.single().password.toStringUnsafe())

            val summary = credentialRepository.getAllSummaries().getOrThrow().single()
            assertEquals("alice@example.com", summary.displayUsername)
            assertEquals("GitHub", summary.title)
        } finally {
            credential.clearSensitiveValuesForTest()
            changedCredential.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `deleting a folder keeps its credentials and moves them to root`() = runTest {
        createAndUnlockVault()
        val folder = Folder(
            id = FolderId("folder-personal"),
            parentId = null,
            name = "Personal",
            icon = null,
            sortOrder = 0,
            createdAt = Instant.fromEpochMilliseconds(100),
        )
        val credential = sampleCredential().copy(folderId = folder.id)
        var restored: Credential? = null
        try {
            assertTrue(folderRepository.save(folder).isSuccess)
            assertTrue(credentialRepository.save(credential).isSuccess)

            assertTrue(folderRepository.delete(folder.id).isSuccess)

            assertNull(folderRepository.getById(folder.id).getOrThrow())
            restored = credentialRepository.getById(credential.id).getOrThrow()
            val restoredCredential = assertNotNull(restored)
            assertNull(restoredCredential.folderId)
        } finally {
            restored?.clearSensitiveValuesForTest()
            credential.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `tampered credential ciphertext is rejected without plaintext fallback`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential()
        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            val original = requireNotNull(database.credentialDao().getById(credential.id.value))
            val tamperedPayload = original.secretPayload.copyOf().also { bytes ->
                val index = bytes.lastIndex
                bytes[index] = (bytes[index].toInt() xor 1).toByte()
            }
            database.credentialDao().update(original.copy(secretPayload = tamperedPayload))

            val result = credentialRepository.getById(credential.id)
            assertTrue(result.isFailure)
            assertFalse(result.exceptionOrNull()?.message.orEmpty().contains("hunter2"))
        } finally {
            credential.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `TOTP configuration is encrypted and marks the vault format`() = runTest {
        createAndUnlockVault()
        assertEquals(1, database.vaultMetadataDao().getVaultFormatVersion())
        val credential = sampleCredential().copy(
            totp = TotpConfiguration(
                secret = SensitiveText.from(TEST_TOTP_SECRET),
                issuer = "Example",
                accountName = "alice@example.com",
                algorithm = TotpAlgorithm.SHA256,
                digits = 8,
                periodSeconds = 60,
            ),
        )

        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            assertEquals(2, database.vaultMetadataDao().getVaultFormatVersion())
            val stored = requireNotNull(database.credentialDao().getById(credential.id.value))
            assertFalse(stored.secretPayload.decodeToString().contains(TEST_TOTP_SECRET))
            assertFalse(stored.secretPayload.decodeToString().contains("alice@example.com"))

            val restored = requireNotNull(credentialRepository.getById(credential.id).getOrThrow())
            try {
                assertEquals(TEST_TOTP_SECRET, restored.totp?.secret?.toStringUnsafe())
                assertEquals("Example", restored.totp?.issuer)
                assertEquals("alice@example.com", restored.totp?.accountName)
                assertEquals(TotpAlgorithm.SHA256, restored.totp?.algorithm)
                assertEquals(8, restored.totp?.digits)
                assertEquals(60, restored.totp?.periodSeconds)
            } finally {
                restored.clearSensitiveValuesForTest()
            }
        } finally {
            credential.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `encrypted backup restores TOTP configuration`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential().copy(
            totp = TotpConfiguration(
                secret = SensitiveText.from(TEST_TOTP_SECRET),
                issuer = "Example",
                accountName = "alice@example.com",
            ),
        )
        val backupPassword = SensitiveText.from("separate backup password")
        var backup: ByteArray? = null

        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            val backupService = VaultBackupService(
                database.vaultBackupDao(),
                cryptoEngine,
                vaultRepository,
            )
            backup = backupService.createBackup(backupPassword).getOrThrow()
            assertTrue(credentialRepository.delete(credential.id).isSuccess)

            backupService.restoreBackup(requireNotNull(backup), backupPassword).getOrThrow()
            val masterPassword = SensitiveText.from(TEST_MASTER_PASSWORD)
            try {
                assertTrue(vaultRepository.unlock(masterPassword).isSuccess)
            } finally {
                masterPassword.clear()
            }

            val restored = credentialRepository.getById(credential.id).getOrThrow()
            try {
                assertEquals(TEST_TOTP_SECRET, restored?.totp?.secret?.toStringUnsafe())
                assertEquals("Example", restored?.totp?.issuer)
                assertEquals("alice@example.com", restored?.totp?.accountName)
            } finally {
                restored?.clearSensitiveValuesForTest()
            }
        } finally {
            backup?.fill(0)
            backupPassword.clear()
            credential.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `TOTP configuration is rejected for non-login credentials without upgrading format`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential().copy(
            type = CredentialType.SecureNote,
            totp = TotpConfiguration(secret = SensitiveText.from(TEST_TOTP_SECRET)),
        )

        try {
            assertTrue(credentialRepository.save(credential).isFailure)
            assertFalse(database.credentialDao().exists(credential.id.value))
            assertEquals(1, database.vaultMetadataDao().getVaultFormatVersion())
        } finally {
            credential.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `noncanonical TOTP secret is rejected before persistence`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential().copy(
            totp = TotpConfiguration(secret = SensitiveText.from("AAAAAAAAAAAAAAAAA")),
        )

        try {
            assertTrue(credentialRepository.save(credential).isFailure)
            assertFalse(database.credentialDao().exists(credential.id.value))
            assertEquals(1, database.vaultMetadataDao().getVaultFormatVersion())
        } finally {
            credential.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `biometric key opens a session only after vault verification succeeds`() = runTest {
        createAndUnlockVault()
        val vaultKey = assertNotNull(vaultRepository.getCurrentVek())
        try {
            assertTrue(vaultRepository.lock().isSuccess)

            assertTrue(vaultRepository.unlockWithBiometricKey(vaultKey).isSuccess)
            assertTrue(vaultRepository.isUnlocked())
        } finally {
            cryptoEngine.secureWipe(vaultKey)
        }
    }

    @Test
    fun `invalid biometric key fails closed and leaves vault locked`() = runTest {
        createAndUnlockVault()
        assertTrue(vaultRepository.lock().isSuccess)
        val invalidKey = ByteArray(32) { 0x5a }
        try {
            assertTrue(vaultRepository.unlockWithBiometricKey(invalidKey).isFailure)
            assertFalse(vaultRepository.isUnlocked())
            assertNull(vaultRepository.getCurrentVek())
        } finally {
            cryptoEngine.secureWipe(invalidKey)
        }
    }

    @Test
    fun `biometric service enrolls the active VEK and reopens the verified vault`() = runTest {
        createAndUnlockVault()
        val keyStore = InMemoryBiometricKeyStore()
        val service = DefaultBiometricUnlockService(
            vaultRepository = vaultRepository,
            sessionManager = vaultRepository,
            keyStore = keyStore,
            cryptoEngine = cryptoEngine,
        )
        try {
            assertEquals(BiometricOperationResult.Success, service.enable())
            assertTrue(vaultRepository.lock().isSuccess)

            assertEquals(BiometricOperationResult.Success, service.unlock())
            assertTrue(vaultRepository.isUnlocked())
        } finally {
            keyStore.clear()
        }
    }

    private suspend fun createAndUnlockVault() {
        val masterPassword = SensitiveText.from(TEST_MASTER_PASSWORD)
        try {
            assertTrue(vaultRepository.create(masterPassword).isSuccess)
            assertTrue(vaultRepository.unlock(masterPassword).isSuccess)
        } finally {
            masterPassword.clear()
        }
    }

    private fun sampleCredential(): Credential = Credential(
        id = CredentialId("credential-github"),
        type = CredentialType.Login,
        title = "GitHub",
        username = SensitiveText.from("alice@example.com"),
        email = SensitiveText.from("alice@example.com"),
        password = SensitiveText.from("hunter2"),
        urls = listOf(UrlValue("https://github.com/login")),
        notes = SensitiveText.from("private note"),
        recoveryCodes = listOf(SensitiveText.from("private recovery code")),
        apiKeys = listOf(SensitiveText.from("api-secret")),
        licenseKeys = listOf(SensitiveText.from("license-secret")),
        customFields = listOf(
            CustomField(
                id = CustomFieldId("field-one"),
                name = "token",
                value = SensitiveText.from("custom-secret"),
                isSecret = true,
            ),
        ),
        folderId = null,
        tagIds = emptySet(),
        isFavorite = true,
        attachments = emptyList(),
        passwordHistory = emptyList(),
        createdAt = Instant.fromEpochMilliseconds(100),
        updatedAt = Instant.fromEpochMilliseconds(100),
        lastUsedAt = null,
        passwordHealth = PasswordHealth(
            score = PasswordScore.GOOD,
            isDuplicate = false,
            isWeak = false,
            isOld = false,
            ageDays = 4,
        ),
    )

    private fun Credential.clearSensitiveValuesForTest() {
        username?.clear()
        email?.clear()
        password?.clear()
        notes?.clear()
        recoveryCodes.forEach(SensitiveText::clear)
        apiKeys.forEach(SensitiveText::clear)
        licenseKeys.forEach(SensitiveText::clear)
        customFields.forEach { it.value.clear() }
        passwordHistory.forEach { it.password.clear() }
        totp?.clear()
    }

    private class InMemoryBiometricKeyStore : BiometricKeyStore {
        private var key: ByteArray? = null

        override suspend fun getCapability(): BiometricCapability = BiometricCapability(
            type = BiometricType.GENERIC,
            availability = BiometricAvailability.AVAILABLE,
        )

        override suspend fun contains(vaultId: String): Boolean = key != null

        override suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit> {
            clear()
            key = vaultKey.copyOf()
            return Result.success(Unit)
        }

        override suspend fun retrieve(vaultId: String): Result<ByteArray> = key?.copyOf()
            ?.let { Result.success(it) }
            ?: Result.failure(BiometricKeyStoreException.NotEnabled())

        override suspend fun delete(vaultId: String): Result<Unit> {
            clear()
            return Result.success(Unit)
        }

        fun clear() {
            key?.fill(0)
            key = null
        }
    }

    private companion object {
        const val TEST_MASTER_PASSWORD = "correct horse battery staple"
        const val TEST_TOTP_SECRET = "JBSWY3DPEHPK3PXP"
    }
}
