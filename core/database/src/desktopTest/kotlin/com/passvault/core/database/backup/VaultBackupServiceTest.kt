package com.passvault.core.database.backup

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.dao.VaultBackupDao
import com.passvault.core.database.dao.VaultBackupEntities
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.VaultMetadataEntity
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.testing.fakes.FakeVaultRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VaultBackupServiceTest {

    private lateinit var database: VaultDatabase
    private lateinit var backupDao: VaultBackupDao
    private lateinit var cryptoEngine: DesktopCryptoEngine
    private lateinit var vaultRepository: FakeVaultRepository
    private lateinit var service: VaultBackupService

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<VaultDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        backupDao = database.vaultBackupDao()
        cryptoEngine = DesktopCryptoEngine()
        vaultRepository = FakeVaultRepository().apply { setupExistingVault() }
        service = VaultBackupService(backupDao, cryptoEngine, vaultRepository)
    }

    @AfterTest
    fun tearDown() {
        database.close()
        vaultRepository.reset()
    }

    @Test
    fun `encrypted backup round trip validates before replacing the vault`() = runTest {
        val original = validSnapshot("original-vault", "credential-one")
        insertSnapshot(original)
        val password = SensitiveText.from("independent backup password")

        try {
            val backup = service.createBackup(password).getOrThrow()
            val inspection = service.inspectBackup(backup, password).getOrThrow()

            assertEquals(1, inspection.credentialCount)
            assertFalse(backup.decodeToString().contains("private-title"))

            replaceWithEmptyVault("sentinel-vault")
            val restored = service.restoreBackup(backup, password).getOrThrow()

            assertEquals(1, restored.credentialCount)
            assertSnapshotEquals(original, backupDao.readSnapshot())
        } finally {
            password.clear()
        }
    }

    @Test
    fun `wrong backup password does not modify current data`() = runTest {
        val original = validSnapshot("original-vault", "credential-one")
        insertSnapshot(original)
        val correct = SensitiveText.from("correct backup password")
        val wrong = SensitiveText.from("wrong backup password")

        try {
            val backup = service.createBackup(correct).getOrThrow()
            replaceWithEmptyVault("sentinel-vault")
            val before = backupDao.readSnapshot()

            val result = service.restoreBackup(backup, wrong)

            assertTrue(result.isFailure)
            assertEquals(
                "The backup password is incorrect or the backup is corrupt.",
                result.exceptionOrNull()?.message,
            )
            assertSnapshotEquals(before, backupDao.readSnapshot())
        } finally {
            correct.clear()
            wrong.clear()
        }
    }

    @Test
    fun `tampered and truncated backups are rejected without replacement`() = runTest {
        val original = validSnapshot("original-vault", "credential-one")
        insertSnapshot(original)
        val password = SensitiveText.from("backup password for tamper test")

        try {
            val backup = service.createBackup(password).getOrThrow()
            replaceWithEmptyVault("sentinel-vault")
            val before = backupDao.readSnapshot()
            val tampered = backup.copyOf().also { bytes ->
                val index = bytes.lastIndex - 8
                bytes[index] = (bytes[index].toInt() xor 1).toByte()
            }

            assertTrue(service.restoreBackup(tampered, password).isFailure)
            assertSnapshotEquals(before, backupDao.readSnapshot())

            assertTrue(service.restoreBackup(backup.copyOf(24), password).isFailure)
            assertSnapshotEquals(before, backupDao.readSnapshot())
        } finally {
            password.clear()
        }
    }

    @Test
    fun `unsupported envelope version is rejected before decryption`() = runTest {
        insertSnapshot(validSnapshot("original-vault", "credential-one"))
        val password = SensitiveText.from("backup version password")

        try {
            val backup = service.createBackup(password).getOrThrow()
            val unsupported = backup.decodeToString()
                .replace("\"formatVersion\":1", "\"formatVersion\":99")
                .encodeToByteArray()

            assertTrue(service.inspectBackup(unsupported, password).isFailure)
        } finally {
            password.clear()
        }
    }

    @Test
    fun `backup creation uses a fresh salt and nonce each time`() = runTest {
        insertSnapshot(validSnapshot("original-vault", "credential-one"))
        val password = SensitiveText.from("nonce uniqueness password")

        try {
            val first = service.createBackup(password).getOrThrow()
            val second = service.createBackup(password).getOrThrow()

            assertFalse(first.contentEquals(second))
        } finally {
            password.clear()
        }
    }

    @Test
    fun `restore aborts when the active session cannot be locked`() = runTest {
        val original = validSnapshot("original-vault", "credential-one")
        insertSnapshot(original)
        val password = SensitiveText.from("lock failure password")

        try {
            val backup = service.createBackup(password).getOrThrow()
            replaceWithEmptyVault("sentinel-vault")
            val before = backupDao.readSnapshot()
            vaultRepository.setShouldFail(IllegalStateException("lock failed"))

            assertTrue(service.restoreBackup(backup, password).isFailure)
            assertSnapshotEquals(before, backupDao.readSnapshot())
        } finally {
            password.clear()
        }
    }

    @Test
    fun `attachment file packaging is rejected explicitly`() = runTest {
        insertSnapshot(validSnapshot("original-vault", "credential-one"))
        val password = SensitiveText.from("attachment backup password")

        try {
            val result = service.createBackup(password, includeAttachments = true)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("cannot be packaged"))
        } finally {
            password.clear()
        }
    }

    private suspend fun validSnapshot(
        vaultId: String,
        credentialId: String,
    ): VaultBackupEntities {
        val key = ByteArray(32) { index -> (index + 1).toByte() }
        try {
            val wrappedVek = encryptedPayload(ByteArray(32) { 7 }, key, "VEK_WRAP")
            val verification = encryptedPayload("verification".encodeToByteArray(), key, "verification")
            val summary = encryptedPayload("private-title".encodeToByteArray(), key, "summary")
            val secret = encryptedPayload("private-secret".encodeToByteArray(), key, "secret")

            return VaultBackupEntities(
                metadata = VaultMetadataEntity(
                    vaultFormatVersion = 1,
                    cryptoFormatVersion = 2,
                    vaultId = vaultId,
                    argon2AlgorithmId = "Argon2id",
                    argon2Salt = ByteArray(16) { 3 },
                    argon2OpsLimit = 2,
                    argon2MemLimit = 32 * 1024 * 1024,
                    argon2Parallelism = 1,
                    wrappedVek = wrappedVek.payload,
                    vekNonce = wrappedVek.nonce,
                    encryptedVerificationRecord = verification.payload,
                    verificationNonce = verification.nonce,
                    createdAt = 1_000,
                    lastAccessedAt = null,
                    entryCount = 1,
                ),
                credentials = listOf(
                    CredentialRecordEntity(
                        id = credentialId,
                        type = "login",
                        titleHash = ByteArray(32) { 9 },
                        summaryPayload = summary.payload,
                        summaryNonce = summary.nonce,
                        secretPayload = secret.payload,
                        secretNonce = secret.nonce,
                        folderId = null,
                        isFavorite = false,
                        createdAt = 1_000,
                        updatedAt = 1_000,
                        lastUsedAt = null,
                    ),
                ),
                folders = emptyList(),
                tags = emptyList(),
                credentialFolderReferences = emptyList(),
                credentialTagReferences = emptyList(),
                attachments = emptyList(),
                passwordHistory = emptyList(),
            )
        } finally {
            cryptoEngine.secureWipe(key)
        }
    }

    private suspend fun encryptedPayload(
        plaintext: ByteArray,
        key: ByteArray,
        associatedData: String,
    ): StoredPayload {
        val encrypted = cryptoEngine.encrypt(
            plaintext = plaintext,
            key = key,
            associatedData = associatedData.encodeToByteArray(),
        ).getOrThrow()
        return try {
            StoredPayload(
                payload = CryptoEnvelope.encode(encrypted),
                nonce = encrypted.nonce.copyOf(),
            )
        } finally {
            encrypted.clear()
            cryptoEngine.secureWipe(plaintext)
        }
    }

    private suspend fun insertSnapshot(snapshot: VaultBackupEntities) {
        backupDao.insertVaultMetadata(snapshot.metadata)
        backupDao.insertCredentials(snapshot.credentials)
    }

    private suspend fun replaceWithEmptyVault(vaultId: String) {
        backupDao.deleteCredentialFolderReferences()
        backupDao.deleteCredentialTagReferences()
        backupDao.deletePasswordHistory()
        backupDao.deleteAttachments()
        backupDao.deleteCredentials()
        backupDao.deleteFolders()
        backupDao.deleteTags()
        backupDao.deleteVaultMetadata()
        backupDao.insertVaultMetadata(
            validSnapshot(vaultId, "temporary").metadata.copy(entryCount = 0),
        )
    }

    private fun assertSnapshotEquals(
        expected: VaultBackupEntities,
        actual: VaultBackupEntities,
    ) {
        assertEquals(expected.metadata, actual.metadata)
        assertEquals(expected.credentials, actual.credentials)
        assertEquals(expected.folders, actual.folders)
        assertEquals(expected.tags, actual.tags)
        assertEquals(expected.credentialFolderReferences, actual.credentialFolderReferences)
        assertEquals(expected.credentialTagReferences, actual.credentialTagReferences)
        assertEquals(expected.attachments, actual.attachments)
        assertEquals(expected.passwordHistory, actual.passwordHistory)
        assertContentEquals(expected.metadata.argon2Salt, actual.metadata.argon2Salt)
    }

    private data class StoredPayload(
        val payload: ByteArray,
        val nonce: ByteArray,
    )
}
