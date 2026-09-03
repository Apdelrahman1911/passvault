@file:OptIn(kotlin.io.encoding.ExperimentalEncodingApi::class)

package com.passvault.core.database.backup

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.dao.VaultBackupDao
import com.passvault.core.database.dao.VaultBackupEntities
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.database.entity.VaultMetadataEntity
import com.passvault.core.database.repository.MAX_FOLDER_ENCRYPTED_PAYLOAD_BYTES
import com.passvault.core.database.repository.VaultSessionManager
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricKeyStoreException
import com.passvault.core.security.BiometricType
import com.passvault.core.testing.fakes.FakeVaultRepository
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@Suppress(
    "LargeClass",
    "TooManyFunctions",
) // Legacy compatibility and biometric rollback cases share one strict snapshot fixture.
class VaultBackupServiceTest {

    private lateinit var database: VaultDatabase
    private lateinit var backupDao: VaultBackupDao
    private lateinit var cryptoEngine: DesktopCryptoEngine
    private lateinit var vaultRepository: FakeVaultRepository
    private lateinit var sessionManager: TestVaultSessionManager
    private lateinit var biometricKeyStore: RecordingBiometricKeyStore
    private lateinit var service: VaultBackupService

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<VaultDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        backupDao = database.vaultBackupDao()
        cryptoEngine = DesktopCryptoEngine()
        vaultRepository = FakeVaultRepository().apply { setupExistingVault() }
        sessionManager = TestVaultSessionManager(vaultRepository)
        biometricKeyStore = RecordingBiometricKeyStore()
        service = VaultBackupService(
            backupDao,
            cryptoEngine,
            vaultRepository,
            sessionManager,
            biometricKeyStore,
        )
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
    fun `legacy export derives entry count from credential rows`() = runTest {
        val staleSnapshot = validSnapshot("stale-count-vault", "credential-one").let { snapshot ->
            snapshot.copy(metadata = snapshot.metadata.copy(entryCount = 7))
        }
        insertSnapshot(staleSnapshot)
        val password = SensitiveText.from("stale entry count backup password")
        var backup: ByteArray? = null
        var plaintext: ByteArray? = null

        try {
            backup = service.createBackup(password).getOrThrow()
            plaintext = decryptLegacyPayload(backup, password)
            val metadata = Json.parseToJsonElement(plaintext.decodeToString())
                .jsonObject.getValue("metadata").jsonObject

            assertEquals(1, metadata.getValue("entryCount").jsonPrimitive.content.toInt())
            assertEquals(1, service.inspectBackup(backup, password).getOrThrow().credentialCount)
            assertEquals(7, backupDao.getVaultMetadata()?.entryCount)
        } finally {
            backup?.let(cryptoEngine::secureWipe)
            plaintext?.let(cryptoEngine::secureWipe)
            password.clear()
        }
    }

    @Test
    fun `snapshot validation still rejects a mismatched imported entry count`() = runTest {
        val snapshot = validSnapshot("mismatched-count-vault", "credential-one")

        assertFailsWith<IllegalArgumentException> {
            service.validateSnapshot(snapshot.copy(metadata = snapshot.metadata.copy(entryCount = 0)))
        }
    }

    @Test
    fun `new legacy backup omits title hash while an old backup still restores`() = runTest {
        insertSnapshot(validSnapshot("legacy-title-index-vault", "credential-one"))
        val password = SensitiveText.from("legacy title index compatibility password")
        var plaintext: ByteArray? = null
        var oldBackup: ByteArray? = null
        try {
            val newBackup = service.createBackup(password).getOrThrow()
            plaintext = decryptLegacyPayload(newBackup, password)
            assertFalse(plaintext.decodeToString().contains("\"titleHash\""))

            oldBackup = addLegacyTitleHash(newBackup, plaintext, password)
            replaceWithEmptyVault("sentinel-vault")

            service.restoreBackup(oldBackup, password).getOrThrow()
            assertEquals("credential-one", backupDao.readSnapshot().credentials.single().id)
        } finally {
            plaintext?.let(cryptoEngine::secureWipe)
            oldBackup?.let(cryptoEngine::secureWipe)
            password.clear()
        }
    }

    @Test
    fun `streaming legacy backup remains compatible without whole document buffering`() = runTest {
        insertSnapshot(validSnapshot("legacy-stream-vault", "credential-one"))
        val password = SensitiveText.from("legacy streaming compatibility password")

        try {
            val backup = service.createBackup(password).getOrThrow()
            val source = TrackingBackupSource(backup, maximumChunkBytes = 17)

            val inspection = service.inspectBackup(source, password).getOrThrow()

            assertEquals(1, inspection.credentialCount)
            assertTrue(source.closed)
            assertEquals(backup.size.toLong(), source.bytesRead)
            assertTrue(source.maximumRequestedBytes <= 8 * 1024)
        } finally {
            password.clear()
        }
    }

    @Test
    fun `non backup input is rejected after a bounded prefix instead of being drained`() = runTest {
        val hostile = ByteArray(1024 * 1024) { 'x'.code.toByte() }
        val source = TrackingBackupSource(hostile, declaredSizeBytes = null)
        val password = SensitiveText.from("bounded legacy parser password")

        try {
            assertTrue(service.inspectBackup(source, password).isFailure)
            assertTrue(source.closed)
            assertEquals(8, source.bytesRead)
        } finally {
            password.clear()
            hostile.fill(0)
        }
    }

    @Test
    fun `legacy size admission is derived from the largest compatible v1 payload`() = runTest {
        val source = TrackingBackupSource(
            bytes = ByteArray(16) { 'x'.code.toByte() },
            declaredSizeBytes = 128L * 1024L * 1024L,
        )
        val password = SensitiveText.from("legacy compatibility ceiling password")

        try {
            assertTrue(BackupLimits.LEGACY_MAX_BACKUP_BYTES < 128L * 1024L * 1024L)
            assertTrue(service.inspectBackup(source, password).isFailure)
            assertTrue(source.closed)
            assertEquals(8, source.bytesRead)
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
    fun `new backup rejects a passphrase below the shared minimum`() = runTest {
        insertSnapshot(validSnapshot("policy-vault", "credential-one"))
        val password = SensitiveText.from("too-short")

        try {
            assertTrue(service.createBackup(password).isFailure)
        } finally {
            password.clear()
        }
    }

    @Test
    fun `secure-store failure reports restore activation failure and preserves current data`() = runTest {
        insertSnapshot(validSnapshot("source-vault", "credential-one"))
        val password = SensitiveText.from("independent backup password")

        try {
            val backup = service.createBackup(password).getOrThrow()
            replaceWithEmptyVault("sentinel-vault")
            val before = backupDao.readSnapshot()
            biometricKeyStore.failDeletion = true

            val result = service.restoreBackup(backup, password)

            assertTrue(result.isFailure)
            assertEquals("The validated backup could not be restored.", result.exceptionOrNull()?.message)
            assertSnapshotEquals(before, backupDao.readSnapshot())
        } finally {
            password.clear()
        }
    }

    @Test
    fun `database rollback restores a previously enabled biometric enrollment`() = runTest {
        insertSnapshot(validSnapshot("source-vault", "credential-one"))
        val password = SensitiveText.from("biometric rollback backup password")

        try {
            val backup = service.createBackup(password).getOrThrow()
            replaceWithEmptyVault("sentinel-vault")
            val before = backupDao.readSnapshot()
            biometricKeyStore.enabled = true
            val failingService = VaultBackupService(
                FailingReplaceBackupDao(backupDao),
                cryptoEngine,
                vaultRepository,
                sessionManager,
                biometricKeyStore,
            )

            val result = failingService.restoreBackup(backup, password)

            assertTrue(result.isFailure)
            assertTrue(biometricKeyStore.enabled)
            assertEquals(listOf("test-vault-123"), biometricKeyStore.enrolledVaultIds)
            assertSnapshotEquals(before, backupDao.readSnapshot())
        } finally {
            password.clear()
        }
    }

    @Test
    fun `successful restore removes the previous vault biometric key`() = runTest {
        val original = validSnapshot("original-vault", "credential-one")
        insertSnapshot(original)
        val password = SensitiveText.from("independent backup password")

        try {
            val backup = service.createBackup(password).getOrThrow()
            replaceWithEmptyVault("sentinel-vault")

            service.restoreBackup(backup, password).getOrThrow()

            assertEquals(listOf("test-vault-123"), biometricKeyStore.deletedVaultIds)
        } finally {
            password.clear()
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
    fun `vault format two backup survives inspection and restore`() = runTest {
        val original = validSnapshot("totp-vault", "credential-with-totp", vaultFormatVersion = 2)
        insertSnapshot(original)
        val password = SensitiveText.from("format two backup password")

        try {
            val backup = service.createBackup(password).getOrThrow()
            assertEquals(1, service.inspectBackup(backup, password).getOrThrow().credentialCount)

            replaceWithEmptyVault("sentinel-vault")
            service.restoreBackup(backup, password).getOrThrow()

            assertEquals(2, backupDao.readSnapshot().metadata.vaultFormatVersion)
            assertSnapshotEquals(original, backupDao.readSnapshot())
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

    @Test
    fun `attachment accounting rejects backups that claim to package files or contain phantom rows`() {
        assertFailsWith<IllegalArgumentException> {
            validateBackupAttachmentAccounting(
                attachmentsIncluded = true,
                attachmentRowCount = 0,
                omittedAttachmentCount = 0,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            validateBackupAttachmentAccounting(
                attachmentsIncluded = false,
                attachmentRowCount = 1,
                omittedAttachmentCount = 0,
            )
        }

        validateBackupAttachmentAccounting(
            attachmentsIncluded = false,
            attachmentRowCount = 0,
            omittedAttachmentCount = 1,
        )
    }

    @Test
    fun `metadata-only backup reports but does not restore unusable attachment rows`() = runTest {
        val base = validSnapshot("attachment-vault", "credential-one")
        val source = base.copy(attachments = listOf(validAttachment("credential-one")))
        insertSnapshot(source)
        val password = SensitiveText.from("metadata-only attachment backup")

        try {
            val backup = service.createBackup(password).getOrThrow()
            val preview = service.inspectBackup(backup, password).getOrThrow()
            assertEquals(1, preview.attachmentCount)
            assertEquals(
                listOf(VaultBackupService.BackupWarning.ATTACHMENT_FILES_NOT_INCLUDED_IN_PREVIEW),
                preview.warnings,
            )

            replaceWithEmptyVault("sentinel-vault")
            val restored = service.restoreBackup(backup, password).getOrThrow()

            assertEquals(1, restored.attachmentCount)
            assertEquals(
                listOf(VaultBackupService.BackupWarning.ATTACHMENT_FILES_NOT_INCLUDED_AFTER_RESTORE),
                restored.warnings,
            )
            assertTrue(backupDao.readSnapshot().attachments.isEmpty())
        } finally {
            password.clear()
        }
    }

    @Test
    fun `snapshot validation rejects non-production credential types`() = runTest {
        val snapshot = validSnapshot("type-vault", "credential-one")

        assertFailsWith<IllegalArgumentException> {
            service.validateSnapshot(
                snapshot.copy(
                    credentials = snapshot.credentials.map { it.copy(type = "login") },
                ),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.validateSnapshot(
                snapshot.copy(
                    credentials = snapshot.credentials.map { it.copy(type = "Custom:") },
                ),
            )
        }
    }

    @Test
    fun `snapshot validation rejects non-exact fixed secret envelopes`() = runTest {
        val snapshot = validSnapshot("envelope-vault", "credential-one")

        assertFailsWith<IllegalArgumentException> {
            service.validateSnapshot(
                snapshot.copy(
                    metadata = snapshot.metadata.copy(
                        wrappedVek = snapshot.metadata.wrappedVek + byteArrayOf(0),
                    ),
                ),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.validateSnapshot(
                snapshot.copy(
                    metadata = snapshot.metadata.copy(
                        encryptedVerificationRecord = snapshot.metadata.encryptedVerificationRecord.copyOf(36),
                    ),
                ),
            )
        }
    }

    @Test
    fun `snapshot validation rejects encrypted metadata payloads repositories cannot read`() = runTest {
        val snapshot = validSnapshot("payload-vault", "credential-one")
        val credential = snapshot.credentials.single()
        val oversizedPayload = credential.summaryPayload.copyOf(MAX_FOLDER_ENCRYPTED_PAYLOAD_BYTES + 1)
        val folder = FolderRecordEntity(
            id = "folder-one",
            parentId = null,
            nameHash = ByteArray(32),
            encryptedPayload = oversizedPayload,
            payloadNonce = credential.summaryNonce.copyOf(),
            icon = null,
            sortOrder = 0,
            createdAt = 1,
            updatedAt = 1,
        )
        val tag = TagRecordEntity(
            id = "tag-one",
            nameHash = ByteArray(32),
            encryptedPayload = oversizedPayload.copyOf(),
            payloadNonce = credential.summaryNonce.copyOf(),
            color = null,
            createdAt = 1,
        )

        assertFailsWith<IllegalArgumentException> {
            service.validateSnapshot(snapshot.copy(folders = listOf(folder)))
        }
        assertFailsWith<IllegalArgumentException> {
            service.validateSnapshot(snapshot.copy(tags = listOf(tag)))
        }
    }

    @Test
    fun `snapshot validation bounds credential relations`() = runTest {
        val snapshot = validSnapshot("relation-vault", "credential-one")
        val credential = snapshot.credentials.single()
        val tags = (0..100).map { index ->
            TagRecordEntity(
                id = "tag-$index",
                nameHash = ByteArray(32) { index.toByte() },
                encryptedPayload = credential.summaryPayload.copyOf(),
                payloadNonce = credential.summaryNonce.copyOf(),
                color = null,
                createdAt = index.toLong(),
            )
        }
        val tagReferences = tags.map { tag ->
            CredentialTagCrossRef(credentialId = credential.id, tagId = tag.id)
        }
        assertFailsWith<IllegalArgumentException> {
            service.validateSnapshot(
                snapshot.copy(tags = tags, credentialTagReferences = tagReferences),
            )
        }

        val history = (0..10).map { index ->
            PasswordHistoryRecordEntity(
                id = "history-$index",
                credentialId = credential.id,
                encryptedPassword = credential.secretPayload.copyOf(),
                passwordNonce = credential.secretNonce.copyOf(),
                changedAt = index.toLong(),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            service.validateSnapshot(snapshot.copy(passwordHistory = history))
        }
    }

    @Test
    fun `snapshot validation rejects folder cycles and accepts a deep acyclic hierarchy`() = runTest {
        val snapshot = validSnapshot("folder-vault", "credential-one")
        val encryptedFolder = encryptedPayload("folder".encodeToByteArray(), ByteArray(32) { 9 }, "folder")
        val deepFolders = (0 until 5_000).map { index ->
            FolderRecordEntity(
                id = "folder-$index",
                parentId = if (index == 0) null else "folder-${index - 1}",
                nameHash = ByteArray(32) { 1 },
                encryptedPayload = encryptedFolder.payload.copyOf(),
                payloadNonce = encryptedFolder.nonce.copyOf(),
                icon = null,
                sortOrder = index,
                createdAt = index.toLong(),
                updatedAt = index.toLong(),
            )
        }

        service.validateSnapshot(snapshot.copy(folders = deepFolders))
        assertFailsWith<IllegalArgumentException> {
            service.validateSnapshot(
                snapshot.copy(
                    folders = deepFolders.take(2).mapIndexed { index, folder ->
                        folder.copy(parentId = "folder-${1 - index}")
                    },
                ),
            )
        }
    }

    @Test
    fun `snapshot validation rejects unsafe unencrypted metadata`() = runTest {
        val snapshot = validSnapshot("metadata-vault", "credential-one")
        val attachment = validAttachment("credential-one")
        val key = ByteArray(32) { 4 }
        try {
            val folderPayload = encryptedPayload("folder".encodeToByteArray(), key, "folder")
            val folder = FolderRecordEntity(
                id = "folder-one",
                parentId = null,
                nameHash = ByteArray(32) { 1 },
                encryptedPayload = folderPayload.payload,
                payloadNonce = folderPayload.nonce,
                icon = null,
                sortOrder = 0,
                createdAt = 1,
                updatedAt = 1,
            )
            val tagPayload = encryptedPayload("tag".encodeToByteArray(), key, "tag")
            val tag = TagRecordEntity(
                id = "tag-one",
                nameHash = ByteArray(32) { 1 },
                encryptedPayload = tagPayload.payload,
                payloadNonce = tagPayload.nonce,
                color = null,
                createdAt = 1,
            )

            assertFailsWith<IllegalArgumentException> {
                val invalidAttachment = attachment.copy(storagePath = "../secret")
                service.validateSnapshot(snapshot.copy(attachments = listOf(invalidAttachment)))
            }
            assertFailsWith<IllegalArgumentException> {
                val invalidAttachment = attachment.copy(mimeType = "text/\u0000plain")
                service.validateSnapshot(snapshot.copy(attachments = listOf(invalidAttachment)))
            }
            assertFailsWith<IllegalArgumentException> {
                val invalidAttachment = attachment.copy(storagePath = "attachments/invoice\u202Efdp.enc")
                service.validateSnapshot(snapshot.copy(attachments = listOf(invalidAttachment)))
            }
            assertFailsWith<IllegalArgumentException> {
                service.validateSnapshot(snapshot.copy(folders = listOf(folder.copy(sortOrder = -1))))
            }
            assertFailsWith<IllegalArgumentException> {
                service.validateSnapshot(snapshot.copy(folders = listOf(folder.copy(icon = "\u0000"))))
            }
            assertFailsWith<IllegalArgumentException> {
                service.validateSnapshot(snapshot.copy(tags = listOf(tag.copy(color = "\u0000"))))
            }
        } finally {
            cryptoEngine.secureWipe(key)
        }
    }

    private suspend fun validSnapshot(
        vaultId: String,
        credentialId: String,
        vaultFormatVersion: Int = 1,
    ): VaultBackupEntities {
        val key = ByteArray(32) { index -> (index + 1).toByte() }
        try {
            val wrappedVek = encryptedPayload(ByteArray(32) { 7 }, key, "VEK_WRAP")
            val verification = encryptedPayload(ByteArray(32) { 8 }, key, "verification")
            val summary = encryptedPayload("private-title".encodeToByteArray(), key, "summary")
            val secret = encryptedPayload("private-secret".encodeToByteArray(), key, "secret")

            return VaultBackupEntities(
                metadata = VaultMetadataEntity(
                    vaultFormatVersion = vaultFormatVersion,
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
                        type = "Login",
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
        if (snapshot.folders.isNotEmpty()) backupDao.insertFolders(snapshot.folders)
        if (snapshot.tags.isNotEmpty()) backupDao.insertTags(snapshot.tags)
        backupDao.insertCredentials(snapshot.credentials)
        if (snapshot.credentialFolderReferences.isNotEmpty()) {
            backupDao.insertCredentialFolderReferences(snapshot.credentialFolderReferences)
        }
        if (snapshot.credentialTagReferences.isNotEmpty()) {
            backupDao.insertCredentialTagReferences(snapshot.credentialTagReferences)
        }
        if (snapshot.attachments.isNotEmpty()) backupDao.insertAttachments(snapshot.attachments)
        if (snapshot.passwordHistory.isNotEmpty()) backupDao.insertPasswordHistory(snapshot.passwordHistory)
    }

    private suspend fun decryptLegacyPayload(backup: ByteArray, password: SensitiveText): ByteArray {
        val envelope = Json.parseToJsonElement(backup.decodeToString()).jsonObject
        val salt = Base64.decode(envelope.getValue("salt").jsonPrimitive.content)
        val nonce = Base64.decode(envelope.getValue("nonce").jsonPrimitive.content)
        val ciphertext = Base64.decode(envelope.getValue("ciphertext").jsonPrimitive.content)
        val passwordBytes = password.toUtf8ByteArray()
        val key = cryptoEngine.deriveKey(
            passwordBytes,
            salt,
            envelope.getValue("argon2OpsLimit").jsonPrimitive.content.toInt(),
            envelope.getValue("argon2MemLimit").jsonPrimitive.content.toInt(),
        ).getOrThrow()
        return try {
            cryptoEngine.decrypt(
                ciphertext,
                nonce,
                key.key,
                "passvault:backup:v1".encodeToByteArray(),
            ).getOrThrow()
        } finally {
            passwordBytes.fill(0)
            salt.fill(0)
            nonce.fill(0)
            ciphertext.fill(0)
            key.clear()
        }
    }

    private suspend fun addLegacyTitleHash(
        backup: ByteArray,
        plaintext: ByteArray,
        password: SensitiveText,
    ): ByteArray {
        val envelope = Json.parseToJsonElement(backup.decodeToString()).jsonObject
        val snapshot = Json.parseToJsonElement(plaintext.decodeToString()).jsonObject
        val credentials = snapshot.getValue("credentials").jsonArray
        val legacyCredential = JsonObject(
            credentials.single().jsonObject.toMutableMap().apply {
                put("titleHash", JsonPrimitive(Base64.encode(ByteArray(32) { 9 })))
            },
        )
        val legacyPlaintext = JsonObject(
            snapshot.toMutableMap().apply { put("credentials", JsonArray(listOf(legacyCredential))) },
        ).toString().encodeToByteArray()
        val salt = Base64.decode(envelope.getValue("salt").jsonPrimitive.content)
        val passwordBytes = password.toUtf8ByteArray()
        val key = cryptoEngine.deriveKey(
            passwordBytes,
            salt,
            envelope.getValue("argon2OpsLimit").jsonPrimitive.content.toInt(),
            envelope.getValue("argon2MemLimit").jsonPrimitive.content.toInt(),
        ).getOrThrow()
        val encrypted = cryptoEngine.encrypt(
            legacyPlaintext,
            key.key,
            "passvault:backup:v1".encodeToByteArray(),
        ).getOrThrow()
        return try {
            JsonObject(
                envelope.toMutableMap().apply {
                    put("nonce", JsonPrimitive(Base64.encode(encrypted.nonce)))
                    put("ciphertext", JsonPrimitive(Base64.encode(CryptoEnvelope.encode(encrypted))))
                },
            ).toString().encodeToByteArray()
        } finally {
            legacyPlaintext.fill(0)
            salt.fill(0)
            passwordBytes.fill(0)
            key.clear()
            encrypted.clear()
        }
    }

    private suspend fun validAttachment(credentialId: String): AttachmentRecordEntity {
        val key = ByteArray(32) { 5 }
        return try {
            val filename = encryptedPayload("document.txt".encodeToByteArray(), key, "attachment")
            AttachmentRecordEntity(
                id = "attachment-one",
                credentialId = credentialId,
                encryptedFilename = filename.payload,
                filenameNonce = filename.nonce,
                mimeType = "text/plain",
                sizeBytes = 12,
                storagePath = "attachments/document.enc",
                keyDerivationContext = "attachment-context",
                createdAt = 1_000,
            )
        } finally {
            cryptoEngine.secureWipe(key)
        }
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

    private class TrackingBackupSource(
        private val bytes: ByteArray,
        override val declaredSizeBytes: Long? = bytes.size.toLong(),
        private val maximumChunkBytes: Int = Int.MAX_VALUE,
    ) : BackupContentSource {
        private var offset = 0
        var bytesRead = 0L
            private set
        var maximumRequestedBytes = 0
            private set
        var closed = false
            private set

        override suspend fun read(buffer: ByteArray): Int {
            maximumRequestedBytes = maxOf(maximumRequestedBytes, buffer.size)
            if (offset == bytes.size) return -1
            val count = minOf(buffer.size, maximumChunkBytes, bytes.size - offset)
            bytes.copyInto(buffer, destinationOffset = 0, startIndex = offset, endIndex = offset + count)
            offset += count
            bytesRead += count
            return count
        }

        override suspend fun rewind() {
            offset = 0
            closed = false
        }

        override suspend fun close() {
            closed = true
        }
    }

    private class TestVaultSessionManager(
        private val vaultRepository: FakeVaultRepository,
    ) : VaultSessionManager {
        private var unlocked = true
        private var vek = ByteArray(32) { 7 }

        override suspend fun <T> withUnlockedSession(block: suspend (ByteArray) -> T): T {
            check(unlocked) { "Vault not unlocked" }
            val leasedVek = vek.copyOf()
            return try {
                block(leasedVek)
            } finally {
                leasedVek.fill(0)
            }
        }

        override suspend fun <T> lockAndRun(reason: LockReason, block: suspend () -> T): T {
            check(unlocked) { "Vault not unlocked" }
            vaultRepository.lock(reason).getOrThrow()
            unlocked = false
            vek.fill(0)
            return block()
        }

    }

    private class RecordingBiometricKeyStore : BiometricKeyStore {
        val deletedVaultIds = mutableListOf<String>()
        val enrolledVaultIds = mutableListOf<String>()
        var failDeletion = false
        var enabled = false

        override suspend fun getCapability(): BiometricCapability = BiometricCapability(
            type = BiometricType.GENERIC,
            availability = BiometricAvailability.AVAILABLE,
        )

        override suspend fun contains(vaultId: String): Boolean = enabled

        override suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit> {
            enrolledVaultIds += vaultId
            enabled = true
            return Result.success(Unit)
        }

        override suspend fun retrieve(vaultId: String): Result<ByteArray> =
            Result.failure(BiometricKeyStoreException.NotEnabled())

        override suspend fun delete(vaultId: String): Result<Unit> {
            deletedVaultIds += vaultId
            return if (failDeletion) {
                Result.failure(BiometricKeyStoreException.Invalidated())
            } else {
                enabled = false
                Result.success(Unit)
            }
        }
    }

    private class FailingReplaceBackupDao(
        delegate: VaultBackupDao,
    ) : VaultBackupDao by delegate {
        override suspend fun replaceVault(snapshot: VaultBackupEntities) {
            error("simulated Room replacement failure")
        }
    }
}
