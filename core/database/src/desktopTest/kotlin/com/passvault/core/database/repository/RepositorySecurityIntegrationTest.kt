package com.passvault.core.database.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.DerivedKey
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.crypto.PaddedPayload
import com.passvault.core.crypto.WrappedKey
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.backup.VaultBackupService
import com.passvault.core.database.dao.VaultMetadataDao
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.domain.model.AttachmentAvailability
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
import com.passvault.core.domain.model.VaultSessionState
import com.passvault.core.domain.repository.LockReason
import com.passvault.core.security.BiometricAvailability
import com.passvault.core.security.BiometricCapability
import com.passvault.core.security.BiometricFailureReason
import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricKeyStoreException
import com.passvault.core.security.BiometricOperationResult
import com.passvault.core.security.BiometricPromptController
import com.passvault.core.security.BiometricType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.time.Instant
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val TEST_MASTER_PASSWORD = "correct horse battery staple"
private const val TEST_TOTP_SECRET = "JBSWY3DPEHPK3PXP"

/**
 * Exercises the real Room + libsodium repository boundary. Fakes cannot
 * prove that encrypted records survive a lock, reject tampering, or preserve
 * all fields through serialization.
 */
@Suppress("TooManyFunctions") // Cohesive real-Room and real-libsodium security integration fixture.
class RepositorySecurityIntegrationTest : RepositorySecurityIntegrationFixture() {

    @Test
    fun `vault creation rejects a master password outside the shared policy`() = runTest {
        val password = SensitiveText.from("too-short")

        try {
            assertTrue(vaultRepository.create(password).isFailure)
            assertFalse(database.vaultMetadataDao().exists())
        } finally {
            password.clear()
        }
    }

    @Test
    fun `vault creation rejects a predictable policy-length password without persistence`() = runTest {
        val password = SensitiveText.from("passwordpass")

        try {
            assertTrue(vaultRepository.create(password).isFailure)
            assertFalse(database.vaultMetadataDao().exists())
        } finally {
            password.clear()
        }
    }

    @Test
    fun `master password change rejects a predictable replacement without changing the vault`() = runTest {
        createAndUnlockVault()
        val current = SensitiveText.from(TEST_MASTER_PASSWORD)
        val weak = SensitiveText.from("Summer2024!!")

        try {
            assertTrue(vaultRepository.changeMasterPassword(current, weak).isFailure)
            assertTrue(vaultRepository.lock().isSuccess)
            assertTrue(vaultRepository.unlock(current).isSuccess)
        } finally {
            current.clear()
            weak.clear()
        }
    }

    @Test
    fun `vault created by an older version with a weak password remains unlockable`() = runTest {
        val originalPassword = SensitiveText.from(TEST_MASTER_PASSWORD)
        val legacyPassword = SensitiveText.from("passwordpass")
        var passwordBytes: ByteArray? = null
        var vaultKey: ByteArray? = null
        var derivedKey: DerivedKey? = null
        var wrappedKey: WrappedKey? = null

        try {
            assertTrue(vaultRepository.create(originalPassword).isSuccess)
            assertTrue(vaultRepository.unlock(originalPassword).isSuccess)
            val activeVaultKey = vaultRepository.withUnlockedSession { it.copyOf() }
            vaultKey = activeVaultKey
            assertTrue(vaultRepository.lock().isSuccess)

            val metadata = requireNotNull(database.vaultMetadataDao().get())
            val encodedPassword = legacyPassword.toUtf8ByteArray()
            passwordBytes = encodedPassword
            val derived = cryptoEngine.deriveKey(
                password = encodedPassword,
                salt = metadata.argon2Salt,
                opsLimit = metadata.argon2OpsLimit,
                memLimit = metadata.argon2MemLimit,
            ).getOrThrow()
            derivedKey = derived
            val wrapped = com.passvault.core.crypto.VaultKeyHierarchy(cryptoEngine)
                .wrapVEK(activeVaultKey, derived.key)
                .getOrThrow()
            wrappedKey = wrapped
            database.vaultMetadataDao().update(
                metadata.copy(
                    wrappedVek = wrapped.ciphertext.copyOf(),
                    vekNonce = wrapped.nonce.copyOf(),
                ),
            )

            assertTrue(vaultRepository.unlock(legacyPassword).isSuccess)
        } finally {
            passwordBytes?.let(cryptoEngine::secureWipe)
            vaultKey?.let(cryptoEngine::secureWipe)
            derivedKey?.clear()
            wrappedKey?.clear()
            originalPassword.clear()
            legacyPassword.clear()
        }
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
    fun `unlock rejects a non-exact wrapped VEK envelope`() = runTest {
        createAndUnlockVault()
        assertTrue(vaultRepository.lock().isSuccess)
        val metadata = requireNotNull(database.vaultMetadataDao().get())
        database.vaultMetadataDao().update(
            metadata.copy(wrappedVek = metadata.wrappedVek + byteArrayOf(0)),
        )
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)

        try {
            assertTrue(vaultRepository.unlock(password).isFailure)
            assertFalse(vaultRepository.isUnlocked())
        } finally {
            password.clear()
        }
    }

    @Test
    fun `unlock normalizes the authenticated duplicated-tag envelope written by legacy builds`() = runTest {
        createAndUnlockVault()
        assertTrue(vaultRepository.lock().isSuccess)
        val metadata = requireNotNull(database.vaultMetadataDao().get())
        val duplicatedTag = metadata.wrappedVek + metadata.wrappedVek.takeLast(16)
        database.vaultMetadataDao().update(metadata.copy(wrappedVek = duplicatedTag))
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)

        try {
            assertTrue(vaultRepository.unlock(password).isSuccess)
            assertTrue(vaultRepository.isUnlocked())
        } finally {
            password.clear()
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
    fun `credential and password history payloads hide exact plaintext length in buckets`() = runTest {
        createAndUnlockVault()
        val shortCredential = sampleCredential().copy(
            id = CredentialId("credential-short-secret"),
            username = SensitiveText.from("a"),
            email = null,
            password = SensitiveText.from("1234"),
            urls = emptyList(),
            notes = null,
            recoveryCodes = emptyList(),
            apiKeys = emptyList(),
            licenseKeys = emptyList(),
            customFields = emptyList(),
        )
        val longerCredential = shortCredential.copy(
            id = CredentialId("credential-longer-secret"),
            password = SensitiveText.from("123456789012"),
        )
        val changedShort = shortCredential.copy(password = SensitiveText.from("different"))
        val changedLonger = longerCredential.copy(password = SensitiveText.from("different"))

        try {
            assertTrue(credentialRepository.save(shortCredential).isSuccess)
            assertTrue(credentialRepository.save(longerCredential).isSuccess)
            val shortStored = requireNotNull(database.credentialDao().getById(shortCredential.id.value))
            val longerStored = requireNotNull(database.credentialDao().getById(longerCredential.id.value))
            assertTrue(CryptoEnvelope.isPaddedPayload(shortStored.summaryPayload))
            assertTrue(CryptoEnvelope.isPaddedPayload(shortStored.secretPayload))
            assertEquals(shortStored.summaryPayload.size, longerStored.summaryPayload.size)
            assertEquals(shortStored.secretPayload.size, longerStored.secretPayload.size)

            assertTrue(credentialRepository.save(changedShort).isSuccess)
            assertTrue(credentialRepository.save(changedLonger).isSuccess)
            val shortHistory = database.passwordHistoryDao().getByCredential(shortCredential.id.value).single()
            val longerHistory = database.passwordHistoryDao().getByCredential(longerCredential.id.value).single()
            assertTrue(CryptoEnvelope.isPaddedPayload(shortHistory.encryptedPassword))
            assertEquals(shortHistory.encryptedPassword.size, longerHistory.encryptedPassword.size)
        } finally {
            shortCredential.clearSensitiveValuesForTest()
            longerCredential.clearSensitiveValuesForTest()
            changedShort.clearSensitiveValuesForTest()
            changedLonger.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `legacy credential payloads decrypt and are padded on the next save`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential()
        val replacement = credential.copy(password = SensitiveText.from("replacement-password"))
        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            val stored = requireNotNull(database.credentialDao().getById(credential.id.value))
            val legacySummary = reencryptAsLegacy(
                stored.summaryPayload,
                stored.summaryNonce,
                "record:${stored.id}",
                "passvault:credential:${stored.id}:summary:v1",
            )
            val legacySecret = reencryptAsLegacy(
                stored.secretPayload,
                stored.secretNonce,
                "record:${stored.id}",
                "passvault:credential:${stored.id}:secret:v1",
            )
            database.credentialDao().update(
                stored.copy(
                    summaryPayload = legacySummary.first,
                    summaryNonce = legacySummary.second,
                    secretPayload = legacySecret.first,
                    secretNonce = legacySecret.second,
                ),
            )

            val legacyRead = credentialRepository.getById(credential.id).getOrThrow()
            assertEquals("hunter2", legacyRead?.password?.toStringUnsafe())
            legacyRead?.clearSensitiveValuesForTest()
            assertTrue(credentialRepository.save(replacement).isSuccess)
            val rewritten = requireNotNull(database.credentialDao().getById(credential.id.value))
            assertTrue(CryptoEnvelope.isPaddedPayload(rewritten.summaryPayload))
            assertTrue(CryptoEnvelope.isPaddedPayload(rewritten.secretPayload))
            assertTrue(
                CryptoEnvelope.isPaddedPayload(
                    database.passwordHistoryDao().getByCredential(credential.id.value).single().encryptedPassword,
                ),
            )
        } finally {
            credential.clearSensitiveValuesForTest()
            replacement.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `padded credential framing tamper fails closed`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential()
        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            val stored = requireNotNull(database.credentialDao().getById(credential.id.value))
            val vek = vaultRepository.withUnlockedSession { it.copyOf() }
            var key: ByteArray? = null
            var decrypted: ByteArray? = null
            var tampered: com.passvault.core.crypto.EncryptedData? = null
            val versionedAad =
                "passvault:padded-payload:v1\u0000passvault:credential:${stored.id}:secret:v1".encodeToByteArray()
            try {
                key = cryptoEngine.deriveSubkey(vek, "record:${stored.id}", 32).getOrThrow()
                decrypted = cryptoEngine.decrypt(
                    CryptoEnvelope.normalize(stored.secretPayload),
                    stored.secretNonce,
                    key,
                    versionedAad,
                ).getOrThrow()
                decrypted[decrypted.lastIndex] = 1
                tampered = cryptoEngine.encrypt(decrypted, key, versionedAad).getOrThrow()
                database.credentialDao().update(
                    stored.copy(
                        secretPayload = CryptoEnvelope.markPadded(CryptoEnvelope.encode(tampered)),
                        secretNonce = tampered.nonce.copyOf(),
                    ),
                )
            } finally {
                tampered?.clear()
                decrypted?.let(cryptoEngine::secureWipe)
                key?.let(cryptoEngine::secureWipe)
                cryptoEngine.secureWipe(versionedAad)
                cryptoEngine.secureWipe(vek)
            }

            assertTrue(credentialRepository.getById(credential.id).isFailure)
        } finally {
            credential.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `legacy password history remains readable`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential()
        val changed = credential.copy(password = SensitiveText.from("changed-password"))
        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            assertTrue(credentialRepository.save(changed).isSuccess)
            val history = database.passwordHistoryDao().getByCredential(credential.id.value).single()
            val legacy = reencryptAsLegacy(
                history.encryptedPassword,
                history.passwordNonce,
                "history:${history.id}",
                "passvault:history:${history.id}:${credential.id.value}:v2",
            )
            database.vaultBackupDao().deletePasswordHistory()
            database.passwordHistoryDao().insert(
                history.copy(encryptedPassword = legacy.first, passwordNonce = legacy.second),
            )

            val restored = requireNotNull(credentialRepository.getById(credential.id).getOrThrow())
            try {
                assertEquals("hunter2", restored.passwordHistory.single().password.toStringUnsafe())
            } finally {
                restored.clearSensitiveValuesForTest()
            }
        } finally {
            credential.clearSensitiveValuesForTest()
            changed.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `credential edits preserve relations attachments history and reset password health`() = runTest {
        createAndUnlockVault()
        val folder = Folder(
            id = FolderId("folder-integrity"),
            parentId = null,
            name = "Integrity",
            icon = null,
            sortOrder = 0,
            createdAt = Instant.fromEpochMilliseconds(100),
        )
        val tag = Tag(TagId("tag-integrity"), "Integrity", "#123456")
        val original = sampleCredential().copy(folderId = folder.id, tagIds = setOf(tag.id))
        val firstChange = original.copy(
            password = SensitiveText.from("first-new-password"),
            createdAt = Instant.fromEpochMilliseconds(999_999),
        )
        val secondChange = original.copy(password = SensitiveText.from("second-new-password"))
        var restored: Credential? = null

        try {
            assertTrue(folderRepository.save(folder).isSuccess)
            assertTrue(tagRepository.save(tag).isSuccess)
            assertTrue(credentialRepository.save(original).isSuccess)
            insertEncryptedAttachment(original.id)

            assertTrue(credentialRepository.save(firstChange).isSuccess)
            assertTrue(credentialRepository.save(secondChange).isSuccess)

            assertEquals(1, database.attachmentDao().getByCredential(original.id.value).size)
            assertEquals(2, database.passwordHistoryDao().getByCredential(original.id.value).size)
            assertEquals(
                listOf(tag.id.value),
                database.credentialDao().getTagCrossRefsForCredential(original.id.value).map { it.tagId },
            )
            assertEquals(folder.id.value, database.credentialDao().getById(original.id.value)?.folderId)
            restored = credentialRepository.getById(original.id).getOrThrow()
            val restoredCredential = assertNotNull(restored)
            assertEquals(Instant.fromEpochMilliseconds(100), restoredCredential.createdAt)
            assertEquals(PasswordHealth.UNKNOWN, restoredCredential.passwordHealth)
            assertEquals(2, restoredCredential.passwordHistory.size)
            assertEquals(1, restoredCredential.attachments.size)
            val summary = credentialRepository.getAllSummaries().getOrThrow().single()
            assertEquals(PasswordHealth.UNKNOWN, summary.passwordHealth)
        } finally {
            restored?.clearSensitiveValuesForTest()
            original.clearSensitiveValuesForTest()
            firstChange.clearSensitiveValuesForTest()
            secondChange.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `tag edits preserve creation time and credential references`() = runTest {
        createAndUnlockVault()
        val tag = Tag(TagId("tag-edit"), "Before", "#111111")
        val credential = sampleCredential().copy(tagIds = setOf(tag.id))
        var restored: Credential? = null

        try {
            assertTrue(tagRepository.save(tag).isSuccess)
            assertTrue(credentialRepository.save(credential).isSuccess)
            val createdAt = requireNotNull(database.tagDao().getById(tag.id.value)).createdAt

            assertTrue(tagRepository.save(tag.copy(name = "After", color = "#222222")).isSuccess)

            assertEquals(createdAt, requireNotNull(database.tagDao().getById(tag.id.value)).createdAt)
            assertEquals(1, database.tagDao().getCredentialCount(tag.id.value))
            restored = credentialRepository.getById(credential.id).getOrThrow()
            assertEquals(setOf(tag.id), assertNotNull(restored).tagIds)
        } finally {
            restored?.clearSensitiveValuesForTest()
            credential.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `deleting a folder keeps its credentials and moves them to root`() = runTest {
        createAndUnlockVault()
        val grandparent = Folder(
            id = FolderId("folder-grandparent"),
            parentId = null,
            name = "Grandparent",
            icon = null,
            sortOrder = 0,
            createdAt = Instant.fromEpochMilliseconds(50),
        )
        val folder = Folder(
            id = FolderId("folder-personal"),
            parentId = grandparent.id,
            name = "Personal",
            icon = null,
            sortOrder = 0,
            createdAt = Instant.fromEpochMilliseconds(100),
        )
        val child = Folder(
            id = FolderId("folder-child"),
            parentId = folder.id,
            name = "Child",
            icon = null,
            sortOrder = 0,
            createdAt = Instant.fromEpochMilliseconds(150),
        )
        val credential = sampleCredential().copy(folderId = folder.id)
        var restored: Credential? = null
        try {
            assertTrue(folderRepository.save(grandparent).isSuccess)
            assertTrue(folderRepository.save(folder).isSuccess)
            assertTrue(folderRepository.save(child).isSuccess)
            assertTrue(credentialRepository.save(credential).isSuccess)
            val originalCreatedAt = requireNotNull(database.folderDao().getById(folder.id.value)).createdAt
            assertTrue(
                folderRepository.save(
                    folder.copy(
                        name = "Personal edited",
                        createdAt = Instant.fromEpochMilliseconds(999_999),
                    ),
                ).isSuccess,
            )
            assertEquals(
                originalCreatedAt,
                requireNotNull(database.folderDao().getById(folder.id.value)).createdAt,
            )
            assertEquals(1, database.credentialDao().getByFolderCrossRef(folder.id.value).size)

            assertTrue(folderRepository.delete(folder.id).isSuccess)

            assertNull(folderRepository.getById(folder.id).getOrThrow())
            restored = credentialRepository.getById(credential.id).getOrThrow()
            val restoredCredential = assertNotNull(restored)
            assertNull(restoredCredential.folderId)
            assertEquals(grandparent.id, folderRepository.getById(child.id).getOrThrow()?.parentId)
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
    fun `health analysis does not decrypt unrelated attachment or password history secrets`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential()
        var healthInput: com.passvault.core.domain.repository.CredentialHealthInput? = null
        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            database.attachmentDao().insert(
                AttachmentRecordEntity(
                    id = "tampered-attachment",
                    credentialId = credential.id.value,
                    encryptedFilename = byteArrayOf(1),
                    filenameNonce = ByteArray(24),
                    mimeType = "application/octet-stream",
                    sizeBytes = 1,
                    storagePath = "attachments/tampered.enc",
                    keyDerivationContext = "tampered-context",
                    createdAt = 200,
                ),
            )
            database.passwordHistoryDao().insert(
                PasswordHistoryRecordEntity(
                    id = "tampered-history",
                    credentialId = credential.id.value,
                    encryptedPassword = byteArrayOf(1),
                    passwordNonce = ByteArray(24),
                    changedAt = 200,
                ),
            )

            assertTrue(credentialRepository.getById(credential.id).isFailure)
            healthInput = credentialRepository.getCredentialsForHealthAnalysis().getOrThrow().single()

            assertEquals(credential.id, healthInput.id)
            assertEquals("alice@example.com", healthInput.username?.toStringUnsafe())
            assertEquals("hunter2", healthInput.password?.toStringUnsafe())
            assertNotNull(healthInput.passwordChangedAt)
        } finally {
            healthInput?.username?.clear()
            healthInput?.email?.clear()
            healthInput?.password?.clear()
            credential.clearSensitiveValuesForTest()
        }
    }

}

class VaultMasterPasswordMatcherIntegrationTest : RepositorySecurityIntegrationFixture() {

    @Test
    fun `master password matcher is exact and unavailable while locked`() = runTest {
        createAndUnlockVault()
        val currentPassword = SensitiveText.from(TEST_MASTER_PASSWORD)
        val distinctPassword = SensitiveText.from(TEST_MASTER_PASSWORD.dropLast(1) + "!")

        try {
            assertTrue(vaultRepository.matchesMasterPassword(currentPassword))
            assertFalse(vaultRepository.matchesMasterPassword(distinctPassword))
            assertTrue(vaultRepository.lock().isSuccess)
            assertFailsWith<VaultSessionLockedException> {
                vaultRepository.matchesMasterPassword(currentPassword)
            }
        } finally {
            currentPassword.clear()
            distinctPassword.clear()
        }
    }

    @Test
    fun `master password matcher clears candidate derivation material`() = runTest {
        val engine = PasswordMatchWipeRecordingCryptoEngine(DesktopCryptoEngine())
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = engine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(engine),
        )
        val currentPassword = SensitiveText.from(TEST_MASTER_PASSWORD)
        val wrongPassword = SensitiveText.from(TEST_MASTER_PASSWORD.dropLast(1) + "!")

        try {
            assertTrue(repository.create(currentPassword).isSuccess)
            assertTrue(repository.unlock(currentPassword).isSuccess)

            engine.captureNextMatch()
            assertTrue(repository.matchesMasterPassword(currentPassword))
            engine.assertCapturedMaterialCleared(expectUnwrappedKey = true)
            assertEquals(1, engine.constantTimeComparisonCount)

            engine.captureNextMatch()
            assertFalse(repository.matchesMasterPassword(wrongPassword))
            engine.assertCapturedMaterialCleared(expectUnwrappedKey = false)
        } finally {
            repository.lock()
            currentPassword.clear()
            wrongPassword.clear()
        }
    }

    @Test
    fun `cancelling master password matching releases its session lease`() = runTest {
        val engine = GatedUnlockCryptoEngine(DesktopCryptoEngine())
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = engine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(engine),
        )
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)

        try {
            assertTrue(repository.create(password).isSuccess)
            assertTrue(repository.unlock(password).isSuccess)
            val gate = engine.gateNextPasswordDerivation()
            val matching = async { repository.matchesMasterPassword(password) }
            gate.started.await()

            matching.cancelAndJoin()

            assertTrue(matching.isCancelled)
            assertTrue(repository.isUnlocked())
            assertTrue(repository.lock().isSuccess)
        } finally {
            password.clear()
        }
    }

    @Test
    fun `master password matcher propagates non authentication crypto failures`() = runTest {
        val engine = PasswordMatchFailureCryptoEngine(DesktopCryptoEngine())
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = engine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(engine),
        )
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)

        try {
            assertTrue(repository.create(password).isSuccess)
            assertTrue(repository.unlock(password).isSuccess)
            engine.failNextPasswordDerivation()

            assertFailsWith<IllegalStateException> {
                repository.matchesMasterPassword(password)
            }
            engine.failNextVaultKeyUnwrap()
            assertFailsWith<IllegalStateException> {
                repository.matchesMasterPassword(password)
            }
            assertTrue(repository.isUnlocked())
        } finally {
            repository.lock()
            password.clear()
        }
    }
}

class RepositoryMutationIntegrityIntegrationTest : RepositorySecurityIntegrationFixture() {

    @Test
    fun `credential mutations reject a missing credential instead of silently succeeding`() = runTest {
        createAndUnlockVault()
        val missingId = CredentialId("missing-credential")

        assertTrue(credentialRepository.updateFavorite(missingId, isFavorite = true).isFailure)
        assertTrue(credentialRepository.removeTag(missingId, TagId("missing-tag")).isFailure)
    }

    @Test
    fun `credential read rejects password history beyond the retained limit before decrypting it`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential()
        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            repeat(11) { index ->
                database.passwordHistoryDao().insert(
                    PasswordHistoryRecordEntity(
                        id = "excess-history-$index",
                        credentialId = credential.id.value,
                        encryptedPassword = byteArrayOf(1),
                        passwordNonce = ByteArray(24),
                        changedAt = index.toLong(),
                    ),
                )
            }

            val result = credentialRepository.getById(credential.id)
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("history", ignoreCase = true))
        } finally {
            credential.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `adding tags preserves idempotency at the limit and rejects a new tag beyond it`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential()
        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            val tags = (0..100).map { index ->
                TagRecordEntity(
                    id = "limit-tag-$index",
                    nameHash = ByteArray(32) { index.toByte() },
                    encryptedPayload = byteArrayOf(1),
                    payloadNonce = ByteArray(24),
                    color = null,
                    createdAt = index.toLong(),
                )
            }
            database.vaultBackupDao().insertTags(tags)
            database.credentialDao().addTagCrossRefs(
                tags.take(100).map { tag ->
                    CredentialTagCrossRef(
                        credentialId = credential.id.value,
                        tagId = tag.id,
                    )
                },
            )

            assertTrue(credentialRepository.addTag(credential.id, TagId(tags.first().id)).isSuccess)
            assertTrue(credentialRepository.addTag(credential.id, TagId(tags.last().id)).isFailure)
            assertEquals(100, database.credentialDao().getTagCrossRefsForCredential(credential.id.value).size)
        } finally {
            credential.clearSensitiveValuesForTest()
        }
    }
}

class RepositoryMetadataSecurityIntegrationTest : RepositorySecurityIntegrationFixture() {

    @Test
    fun `vault metadata fixes Argon2 parallelism at one and rejects another value`() = runTest {
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)

        try {
            assertTrue(vaultRepository.create(password).isSuccess)
            assertEquals(1, requireNotNull(database.vaultMetadataDao().get()).argon2Parallelism)
            assertTrue(vaultRepository.lock().isSuccess)

            val metadata = requireNotNull(database.vaultMetadataDao().get())
            database.vaultMetadataDao().update(metadata.copy(argon2Parallelism = 2))

            assertTrue(vaultRepository.unlock(password).isFailure)
            assertFalse(vaultRepository.isUnlocked())
        } finally {
            password.clear()
        }
    }

    @Test
    fun `folder read rejects an oversized encrypted payload before authentication`() = runTest {
        createAndUnlockVault()
        database.folderDao().insertOrUpdate(
            FolderRecordEntity(
                id = "oversized-folder",
                parentId = null,
                nameHash = ByteArray(32),
                encryptedPayload = ByteArray(65 * 1024),
                payloadNonce = ByteArray(24),
                icon = null,
                sortOrder = 0,
                createdAt = 1,
                updatedAt = 1,
            ),
        )

        assertTrue(folderRepository.getById(FolderId("oversized-folder")).isFailure)
    }

    @Test
    fun `credential save rejects format controls in display labels`() = runTest {
        createAndUnlockVault()
        val unsafeTitle = sampleCredential().copy(title = "invoice\u202Efdp.exe")
        val unsafeUsernameSource = sampleCredential()
        val unsafeUsername = unsafeUsernameSource.let { credential ->
            credential.username?.clear()
            credential.copy(
                id = CredentialId("credential-unsafe-username"),
                username = SensitiveText.from("alice\u202E@example.com"),
            )
        }
        val unsafeFieldName = sampleCredential().let { credential ->
            credential.copy(
                id = CredentialId("credential-unsafe-field"),
                customFields = credential.customFields.map { field ->
                    field.copy(name = "token\u200Bhidden")
                },
            )
        }
        try {
            assertTrue(credentialRepository.save(unsafeTitle).isFailure)
            assertTrue(credentialRepository.save(unsafeUsername).isFailure)
            assertTrue(credentialRepository.save(unsafeFieldName).isFailure)
        } finally {
            unsafeTitle.clearSensitiveValuesForTest()
            unsafeUsername.clearSensitiveValuesForTest()
            unsafeFieldName.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `credential read quarantines an authenticated attachment filename with bidi controls`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential()
        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            insertEncryptedAttachment(credential.id, "invoice\u202Efdp.exe")

            val loaded = assertNotNull(credentialRepository.getById(credential.id).getOrThrow())
            assertEquals(AttachmentAvailability.CORRUPTED_FILENAME, loaded.attachments.single().availability)
            assertEquals("attachment-one", loaded.attachments.single().id.value)
        } finally {
            credential.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `credential summary rejects an empty custom type identifier`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential()
        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            val stored = requireNotNull(database.credentialDao().getById(credential.id.value))
            database.credentialDao().update(stored.copy(type = "Custom:"))

            assertTrue(credentialRepository.getAllSummaries().isFailure)
        } finally {
            credential.clearSensitiveValuesForTest()
        }
    }
}

class RepositoryUnicodeSecurityIntegrationTest : RepositorySecurityIntegrationFixture() {
    @Test
    fun `vault creation rejects a malformed Unicode master password`() = runTest {
        val password = SensitiveText.from("correct horse\uD800")

        try {
            assertTrue(vaultRepository.create(password).isFailure)
            assertFalse(database.vaultMetadataDao().exists())
        } finally {
            password.clear()
        }
    }

    @Test
    fun `metadata limits count supplementary characters without splitting them`() = runTest {
        createAndUnlockVault()
        val accepted = sampleCredential().copy(
            id = CredentialId("credential-unicode-boundary"),
            title = "🔐".repeat(200),
        )
        val rejected = sampleCredential().copy(
            id = CredentialId("credential-unicode-overflow"),
            title = "🔐".repeat(201),
        )
        try {
            assertTrue(credentialRepository.save(accepted).isSuccess)
            assertTrue(credentialRepository.save(rejected).isFailure)
        } finally {
            accepted.clearSensitiveValuesForTest()
            rejected.clearSensitiveValuesForTest()
        }
    }

    @Test
    fun `credential save rejects malformed Unicode in encrypted secret fields`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential().copy(
            notes = SensitiveText.from("private\uD800note"),
        )
        try {
            assertTrue(credentialRepository.save(credential).isFailure)
            assertNull(database.credentialDao().getById(credential.id.value))
        } finally {
            credential.clearSensitiveValuesForTest()
        }
    }
}

class RepositoryTotpSecurityIntegrationTest : RepositorySecurityIntegrationFixture() {
    @Test
    fun `TOTP display read returns only authenticator accounts and transfers clearable seed ownership`() = runTest {
        createAndUnlockVault()
        val withTotp = sampleCredential().copy(
            totp = TotpConfiguration(
                secret = SensitiveText.from(TEST_TOTP_SECRET),
                issuer = "Example",
                accountName = "alice@example.com",
            ),
        )
        val withoutTotp = sampleCredential().copy(
            id = CredentialId("credential-without-totp"),
            title = "No authenticator",
            totp = null,
        )

        try {
            assertTrue(credentialRepository.save(withTotp).isSuccess)
            assertTrue(credentialRepository.save(withoutTotp).isSuccess)

            val lease = credentialRepository.getCredentialsForTotpDisplay().getOrThrow()
            val inputs = assertNotNull(lease.take())
            try {
                val input = inputs.single()
                assertEquals(withTotp.id, input.id)
                assertEquals("GitHub", input.title)
                assertEquals("alice@example.com", input.displayUsername)
                assertEquals(TEST_TOTP_SECRET, input.configuration.secret.toStringUnsafe())
                assertEquals("Example", input.configuration.issuer)
            } finally {
                inputs.forEach { it.clear() }
            }
            assertTrue(inputs.single().configuration.secret.toStringUnsafe().all { it == '\u0000' })
        } finally {
            withTotp.clearSensitiveValuesForTest()
            withoutTotp.clearSensitiveValuesForTest()
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
}

@OptIn(ExperimentalCoroutinesApi::class)
class VaultUnlockPreemptionIntegrationTest : RepositorySecurityIntegrationFixture() {

    @Test
    fun `lock intent during last-access write is linearized before session publication`() = runTest {
        val metadataDao = GatedLastAccessVaultMetadataDao(database.vaultMetadataDao())
        val engine = GatedUnlockCryptoEngine(cryptoEngine)
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = metadataDao,
            cryptoEngine = engine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(engine),
        )
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)
        val observedStates = mutableListOf<VaultSessionState>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.getSessionState().collect(observedStates::add)
        }

        try {
            assertTrue(repository.create(password).isSuccess)
            observedStates.clear()
            val unlock = async { repository.unlock(password) }
            metadataDao.updateStarted.await()

            val locking = async { repository.lock(LockReason.Background) }
            runCurrent()
            assertFalse(locking.isCompleted)
            metadataDao.allowUpdate.complete(Unit)

            assertTrue(unlock.await().isFailure)
            assertTrue(locking.await().isSuccess)
            assertFalse(observedStates.any { it is VaultSessionState.Unlocked })
            assertTrue(requireNotNull(engine.lastUnwrappedVaultKey).all { it == 0.toByte() })
        } finally {
            metadataDao.allowUpdate.complete(Unit)
            collector.cancel()
            repository.lock()
            password.clear()
        }
    }

    @Test
    fun `password unlock cannot publish a session after lock intent is registered`() = runTest {
        val engine = GatedUnlockCryptoEngine(cryptoEngine)
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = engine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(engine),
        )
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)
        val observedStates = mutableListOf<VaultSessionState>()
        val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.getSessionState().collect(observedStates::add)
        }

        try {
            assertTrue(repository.create(password).isSuccess)
            observedStates.clear()
            val gate = engine.gateNextPasswordDerivation()
            val unlock = async { repository.unlock(password) }
            gate.started.await()

            val locking = async { repository.lock(LockReason.Background) }
            runCurrent()
            assertFalse(locking.isCompleted)
            gate.release.complete(Unit)

            assertTrue(unlock.await().isFailure)
            assertTrue(locking.await().isSuccess)
            assertFalse(repository.isUnlocked())
            assertEquals(
                VaultSessionState.Locked(LockReason.Background),
                repository.getSessionState().first(),
            )
            assertFalse(observedStates.any { it is VaultSessionState.Unlocked })
            assertTrue(requireNotNull(engine.lastUnwrappedVaultKey).all { it == 0.toByte() })
        } finally {
            collector.cancel()
            repository.lock()
            password.clear()
        }
    }

    @Test
    fun `biometric unlock cannot publish a session after lock intent is registered`() = runTest {
        val engine = GatedUnlockCryptoEngine(cryptoEngine)
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = engine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(engine),
        )
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)
        var vaultKey: ByteArray? = null
        val observedStates = mutableListOf<VaultSessionState>()
        try {
            assertTrue(repository.create(password).isSuccess)
            assertTrue(repository.unlock(password).isSuccess)
            vaultKey = repository.withUnlockedSession { it.copyOf() }
            assertTrue(repository.lock().isSuccess)
            val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                repository.getSessionState().collect(observedStates::add)
            }
            observedStates.clear()

            val gate = engine.gateNextVerification()
            val unlock = async {
                repository.unlockWithBiometricKey(requireNotNull(vaultKey))
            }
            gate.started.await()
            val locking = async { repository.lock(LockReason.AutoLock) }
            runCurrent()
            assertFalse(locking.isCompleted)
            gate.release.complete(Unit)

            assertTrue(unlock.await().isFailure)
            assertTrue(locking.await().isSuccess)
            assertFalse(repository.isUnlocked())
            assertEquals(
                VaultSessionState.Locked(LockReason.AutoLock),
                repository.getSessionState().first(),
            )
            assertFalse(observedStates.any { it is VaultSessionState.Unlocked })
            assertTrue(requireNotNull(engine.lastVerificationKey).all { it == 0.toByte() })
            collector.cancel()
        } finally {
            vaultKey?.let(cryptoEngine::secureWipe)
            repository.lock()
            password.clear()
        }
    }

    @Test
    fun `lock and run preempts an in-flight unlock before protected work`() = runTest {
        val engine = GatedUnlockCryptoEngine(cryptoEngine)
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = engine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(engine),
        )
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)
        var protectedWorkRan = false
        try {
            assertTrue(repository.create(password).isSuccess)
            val gate = engine.gateNextPasswordDerivation()
            val unlock = async { repository.unlock(password) }
            gate.started.await()
            val lockAndRun = async {
                runCatching {
                    repository.lockAndRun(LockReason.Restore) {
                        protectedWorkRan = true
                    }
                }
            }
            runCurrent()
            assertFalse(lockAndRun.isCompleted)
            gate.release.complete(Unit)

            assertTrue(unlock.await().isFailure)
            assertTrue(lockAndRun.await().isFailure)
            assertFalse(protectedWorkRan)
            assertEquals(
                VaultSessionState.Locked(LockReason.Restore),
                repository.getSessionState().first(),
            )
        } finally {
            repository.lock()
            password.clear()
        }
    }

    @Test
    fun `overlapping lock intents both preempt unlock and are fully released`() = runTest {
        val engine = GatedUnlockCryptoEngine(cryptoEngine)
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = engine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(engine),
        )
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)
        try {
            assertTrue(repository.create(password).isSuccess)
            val gate = engine.gateNextPasswordDerivation()
            val unlock = async { repository.unlock(password) }
            gate.started.await()
            val backgroundLock = async { repository.lock(LockReason.Background) }
            val autoLock = async { repository.lock(LockReason.AutoLock) }
            runCurrent()
            assertFalse(backgroundLock.isCompleted)
            assertFalse(autoLock.isCompleted)
            gate.release.complete(Unit)

            assertTrue(unlock.await().isFailure)
            assertTrue(backgroundLock.await().isSuccess)
            assertTrue(autoLock.await().isSuccess)
            assertEquals(
                VaultSessionState.Locked(LockReason.AutoLock),
                repository.getSessionState().first(),
            )
            assertTrue(repository.unlock(password).isSuccess)
        } finally {
            repository.lock()
            password.clear()
        }
    }

    @Test
    fun `lock reason remains authoritative when the preempted unlock also fails authentication`() = runTest {
        val engine = GatedUnlockCryptoEngine(cryptoEngine)
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = engine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(engine),
        )
        val correctPassword = SensitiveText.from(TEST_MASTER_PASSWORD)
        val wrongPassword = SensitiveText.from("wrong password value")
        try {
            assertTrue(repository.create(correctPassword).isSuccess)
            val gate = engine.gateNextPasswordDerivation()
            val unlock = async { repository.unlock(wrongPassword) }
            gate.started.await()
            val locking = async { repository.lock(LockReason.Background) }
            runCurrent()
            assertFalse(locking.isCompleted)
            gate.release.complete(Unit)

            assertTrue(unlock.await().isFailure)
            assertTrue(locking.await().isSuccess)
            assertEquals(
                VaultSessionState.Locked(LockReason.Background),
                repository.getSessionState().first(),
            )
        } finally {
            repository.lock()
            correctPassword.clear()
            wrongPassword.clear()
        }
    }

    @Test
    fun `lock during password throttle preempts unlock without adding a failed attempt`() = runTest {
        val wrongPassword = SensitiveText.from("wrong password value")
        val correctPassword = SensitiveText.from(TEST_MASTER_PASSWORD)
        val observedStates = mutableListOf<VaultSessionState>()
        try {
            assertTrue(vaultRepository.create(correctPassword).isSuccess)
            repeat(3) {
                assertTrue(vaultRepository.unlock(wrongPassword).isFailure)
            }
            val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                vaultRepository.getSessionState().collect(observedStates::add)
            }
            observedStates.clear()

            val unlock = async { vaultRepository.unlock(correctPassword) }
            runCurrent()
            assertFalse(unlock.isCompleted)
            val locking = async { vaultRepository.lock(LockReason.AutoLock) }
            runCurrent()
            assertFalse(locking.isCompleted)

            advanceTimeBy(501)
            advanceUntilIdle()
            assertTrue(unlock.await().isFailure)
            assertTrue(locking.await().isSuccess)
            assertFalse(observedStates.any { it is VaultSessionState.Unlocked })

            val nextUnlock = async { vaultRepository.unlock(correctPassword) }
            runCurrent()
            assertFalse(nextUnlock.isCompleted)
            advanceTimeBy(501)
            runCurrent()
            assertTrue(
                nextUnlock.isCompleted || vaultRepository.getSessionState().first() is VaultSessionState.Unlocking,
                "A preempted unlock must not add another failed-attempt delay",
            )
            assertTrue(nextUnlock.await().isSuccess)
            collector.cancel()
        } finally {
            vaultRepository.lock()
            wrongPassword.clear()
            correctPassword.clear()
        }
    }

}

class RepositoryBiometricSecurityIntegrationTest : RepositorySecurityIntegrationFixture() {
    @Test
    fun `biometric status reconciles surviving platform items when no vault exists`() = runTest {
        val keyStore = InMemoryBiometricKeyStore()
        val service = DefaultBiometricUnlockService(
            vaultRepository = vaultRepository,
            sessionManager = vaultRepository,
            keyStore = keyStore,
            cryptoEngine = cryptoEngine,
        )

        val status = service.getStatus()

        assertFalse(status.isEnabled)
        assertEquals(listOf<String?>(null), keyStore.reconciledVaultIds)
    }

    @Test
    fun `biometric key opens a session only after vault verification succeeds`() = runTest {
        createAndUnlockVault()
        val vaultKey = vaultRepository.withUnlockedSession { it.copyOf() }
        try {
            assertTrue(vaultRepository.lock().isSuccess)

            assertTrue(vaultRepository.unlockWithBiometricKey(vaultKey).isSuccess)
            assertTrue(vaultRepository.isUnlocked())
        } finally {
            cryptoEngine.secureWipe(vaultKey)
        }
    }

    @Test
    fun `lock cancels biometric enrollment that is holding a vault key lease`() = runTest {
        createAndUnlockVault()
        val enrollmentEntered = CompletableDeferred<Unit>()
        var enrollmentKey: ByteArray? = null
        val keyStore = object : BiometricKeyStore {
            override suspend fun getCapability(): BiometricCapability = BiometricCapability(
                type = BiometricType.GENERIC,
                availability = BiometricAvailability.AVAILABLE,
            )

            override suspend fun contains(vaultId: String): Boolean = false

            override suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit> {
                enrollmentKey = vaultKey
                enrollmentEntered.complete(Unit)
                awaitCancellation()
            }

            override suspend fun retrieve(vaultId: String): Result<ByteArray> =
                Result.failure(BiometricKeyStoreException.NotEnabled())

            override suspend fun delete(vaultId: String): Result<Unit> = Result.success(Unit)
        }
        val service = DefaultBiometricUnlockService(
            vaultRepository = vaultRepository,
            sessionManager = vaultRepository,
            keyStore = keyStore,
            cryptoEngine = cryptoEngine,
        )

        val enrollment = async(Dispatchers.Default) { service.enable() }
        enrollmentEntered.await()

        assertTrue(
            withContext(Dispatchers.Default) {
                withTimeout(1_000) { vaultRepository.lock(LockReason.AutoLock) }
            }.isSuccess,
        )
        enrollment.join()
        assertTrue(enrollment.isCancelled)
        assertTrue(requireNotNull(enrollmentKey).all { it == 0.toByte() })
        assertFalse(vaultRepository.isUnlocked())
    }

    @Test
    fun `invalid biometric key fails closed and leaves vault locked`() = runTest {
        createAndUnlockVault()
        assertTrue(vaultRepository.lock().isSuccess)
        val invalidKey = ByteArray(32) { 0x5a }
        try {
            assertTrue(vaultRepository.unlockWithBiometricKey(invalidKey).isFailure)
            assertFalse(vaultRepository.isUnlocked())
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

    @Test
    fun `biometric unlock racing an existing session keeps the enrolled key`() = runTest {
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

            assertEquals(BiometricOperationResult.Success, service.unlock())
            val vaultId = vaultRepository.getMetadata().getOrThrow().id.value
            assertTrue(keyStore.contains(vaultId))
            assertTrue(vaultRepository.isUnlocked())
        } finally {
            keyStore.clear()
        }
    }

    @Test
    fun `transient vault activation failure keeps the enrolled biometric key`() = runTest {
        createAndUnlockVault()
        val keyStore = InMemoryBiometricKeyStore()
        val enrollmentService = DefaultBiometricUnlockService(
            vaultRepository = vaultRepository,
            sessionManager = vaultRepository,
            keyStore = keyStore,
            cryptoEngine = cryptoEngine,
        )
        assertEquals(BiometricOperationResult.Success, enrollmentService.enable())
        val vaultId = vaultRepository.getMetadata().getOrThrow().id.value
        assertTrue(vaultRepository.lock().isSuccess)

        val failingRepository = VaultRepositoryImpl(
            vaultMetadataDao = FailingLastAccessVaultMetadataDao(database.vaultMetadataDao()),
            cryptoEngine = cryptoEngine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(cryptoEngine),
        )
        val failingService = DefaultBiometricUnlockService(
            vaultRepository = failingRepository,
            sessionManager = failingRepository,
            keyStore = keyStore,
            cryptoEngine = cryptoEngine,
        )
        try {
            assertEquals(
                BiometricOperationResult.Failure(
                    com.passvault.core.security.BiometricFailureReason.INTERNAL_ERROR,
                ),
                failingService.unlock(),
            )
            assertTrue(keyStore.contains(vaultId))
            assertFalse(failingRepository.isUnlocked())
        } finally {
            keyStore.clear()
        }
    }

    @Test
    fun `biometric enrollment reports a locked vault without masking platform failures`() = runTest {
        createAndUnlockVault()
        assertTrue(vaultRepository.lock().isSuccess)
        val keyStore = InMemoryBiometricKeyStore()
        val service = DefaultBiometricUnlockService(
            vaultRepository = vaultRepository,
            sessionManager = vaultRepository,
            keyStore = keyStore,
            cryptoEngine = cryptoEngine,
        )

        assertEquals(
            BiometricOperationResult.Failure(
                com.passvault.core.security.BiometricFailureReason.VAULT_LOCKED,
            ),
            service.enable(),
        )
    }

    @Test
    fun `biometric unlock preserves invalidation reported while checking enrollment`() = runTest {
        createAndUnlockVault()
        assertTrue(vaultRepository.lock().isSuccess)
        val keyStore = object : BiometricKeyStore {
            override suspend fun getCapability(): BiometricCapability = BiometricCapability(
                type = BiometricType.TOUCH_ID,
                availability = BiometricAvailability.AVAILABLE,
            )

            override suspend fun contains(vaultId: String): Boolean =
                throw BiometricKeyStoreException.Invalidated()

            override suspend fun enroll(vaultId: String, vaultKey: ByteArray): Result<Unit> =
                Result.failure(BiometricKeyStoreException.Invalidated())

            override suspend fun retrieve(vaultId: String): Result<ByteArray> =
                Result.failure(BiometricKeyStoreException.Invalidated())

            override suspend fun delete(vaultId: String): Result<Unit> = Result.success(Unit)
        }
        val service = DefaultBiometricUnlockService(
            vaultRepository = vaultRepository,
            sessionManager = vaultRepository,
            keyStore = keyStore,
            cryptoEngine = cryptoEngine,
        )

        assertEquals(
            BiometricOperationResult.Failure(BiometricFailureReason.INVALIDATED),
            service.unlock(),
        )
        assertFalse(vaultRepository.isUnlocked())
    }

    @Test
    fun `lock cancels an active session lease and wipes its key without waiting for caller work`() = runTest {
        createAndUnlockVault()
        val leaseEntered = CompletableDeferred<Unit>()
        val releaseLease = CompletableDeferred<Unit>()
        var leasedKey: ByteArray? = null

        val operation = async(Dispatchers.Default) {
            vaultRepository.withUnlockedSession { vek ->
                leasedKey = vek
                leaseEntered.complete(Unit)
                releaseLease.await()
            }
        }
        leaseEntered.await()

        val lockResult = withContext(Dispatchers.Default) {
            withTimeout(1_000) { vaultRepository.lock() }
        }
        operation.join()
        assertTrue(lockResult.isSuccess)
        assertTrue(operation.isCancelled)
        assertTrue(requireNotNull(leasedKey).all { it == 0.toByte() })
        assertFalse(vaultRepository.isUnlocked())
        releaseLease.complete(Unit)
    }

    @Test
    fun `lock and run revokes an active lease before running protected work`() = runTest {
        createAndUnlockVault()
        val leaseEntered = CompletableDeferred<Unit>()
        var leasedKey: ByteArray? = null
        val operation = async(Dispatchers.Default) {
            vaultRepository.withUnlockedSession { key ->
                leasedKey = key
                leaseEntered.complete(Unit)
                awaitCancellation()
            }
        }
        leaseEntered.await()

        var blockObservedLocked = false
        val blockResult = withContext(Dispatchers.Default) {
            withTimeout(1_000) {
                vaultRepository.lockAndRun(LockReason.Restore) {
                    blockObservedLocked = !vaultRepository.isUnlocked()
                    "restored"
                }
            }
        }

        operation.join()
        assertEquals("restored", blockResult)
        assertTrue(blockObservedLocked)
        assertTrue(operation.isCancelled)
        assertTrue(requireNotNull(leasedKey).all { it == 0.toByte() })
    }
}

class VaultLockFailureIntegrationTest : RepositorySecurityIntegrationFixture() {

    @Test
    fun `lock cancels a platform prompt and its active session lease`() = runTest {
        val cancellationObserved = CompletableDeferred<Unit>()
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = cryptoEngine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(cryptoEngine),
            biometricPromptController = BiometricPromptController {
                cancellationObserved.complete(Unit)
            },
        )
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)
        val leaseEntered = CompletableDeferred<Unit>()
        val releaseLease = CompletableDeferred<Unit>()

        try {
            assertTrue(repository.create(password).isSuccess)
            assertTrue(repository.unlock(password).isSuccess)
            val lease = async(Dispatchers.Default) {
                repository.withUnlockedSession {
                    leaseEntered.complete(Unit)
                    releaseLease.await()
                }
            }
            leaseEntered.await()

            val lockResult = withContext(Dispatchers.Default) {
                withTimeout(1_000) { repository.lock(LockReason.AutoLock) }
            }
            assertTrue(cancellationObserved.isCompleted)
            lease.join()
            assertTrue(lease.isCancelled)
            assertTrue(lockResult.isSuccess)
            assertFalse(repository.isUnlocked())
        } finally {
            releaseLease.complete(Unit)
            password.clear()
        }
    }

    @Test
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun `lock reaches locked state after the hard deadline when lease code suppresses cancellation`() = runTest {
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = cryptoEngine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(cryptoEngine),
        )
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)
        val leaseEntered = CompletableDeferred<Unit>()
        val releaseLease = CompletableDeferred<Unit>()
        var leasedKey: ByteArray? = null

        try {
            assertTrue(repository.create(password).isSuccess)
            assertTrue(repository.unlock(password).isSuccess)
            val lease = async {
                repository.withUnlockedSession { key ->
                    leasedKey = key
                    leaseEntered.complete(Unit)
                    withContext(NonCancellable) { releaseLease.await() }
                }
            }
            leaseEntered.await()

            val locking = async { repository.lock(LockReason.Background) }
            yield()
            assertEquals(
                VaultSessionState.Locking(LockReason.Background),
                repository.getSessionState().first(),
            )

            testScheduler.advanceTimeBy(2_001)
            assertTrue(locking.await().isSuccess)
            assertEquals(
                VaultSessionState.Locked(LockReason.Background),
                repository.getSessionState().first(),
            )
            assertTrue(requireNotNull(leasedKey).all { it == 0.toByte() })

            releaseLease.complete(Unit)
            lease.join()
            assertTrue(lease.isCancelled)
        } finally {
            releaseLease.complete(Unit)
            password.clear()
        }
    }

    @Test
    fun `platform prompt cancellation failure cannot prevent vault locking`() = runTest {
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = cryptoEngine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(cryptoEngine),
            biometricPromptController = BiometricPromptController {
                error("simulated platform cancellation failure")
            },
        )
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)

        try {
            assertTrue(repository.create(password).isSuccess)
            assertTrue(repository.unlock(password).isSuccess)
            assertTrue(repository.lockAndRun(LockReason.Restore) { true })
            assertFalse(repository.isUnlocked())
        } finally {
            password.clear()
        }
    }

    @Test
    fun `failed key wipe still removes the live key and reaches terminal locked state`() = runTest {
        val engine = ThrowOnceWipeCryptoEngine(DesktopCryptoEngine())
        val repository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = engine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(engine),
        )
        val password = SensitiveText.from(TEST_MASTER_PASSWORD)

        try {
            assertTrue(repository.create(password).isSuccess)
            assertTrue(repository.unlock(password).isSuccess)
            engine.failNextWipe()

            assertTrue(repository.lock(LockReason.Background).isFailure)
            assertFalse(repository.isUnlocked())
            assertEquals(
                VaultSessionState.Locked(LockReason.Background),
                repository.getSessionState().first(),
            )
            assertTrue(repository.lock(LockReason.Background).isSuccess)
        } finally {
            password.clear()
        }
    }
}

private class PasswordMatchWipeRecordingCryptoEngine(
    private val delegate: CryptoEngine,
) : CryptoEngine by delegate {
    private var captureMatch = false
    private var passwordBytes: ByteArray? = null
    private var derivedKeyBytes: ByteArray? = null
    private var derivedSaltBytes: ByteArray? = null
    private var unwrappedVek: ByteArray? = null
    var constantTimeComparisonCount = 0
        private set

    fun captureNextMatch() {
        captureMatch = true
        passwordBytes = null
        derivedKeyBytes = null
        derivedSaltBytes = null
        unwrappedVek = null
        constantTimeComparisonCount = 0
    }

    override suspend fun deriveKey(
        password: ByteArray,
        salt: ByteArray,
        opsLimit: Int,
        memLimit: Int,
    ): Result<DerivedKey> {
        if (captureMatch) passwordBytes = password
        return delegate.deriveKey(password, salt, opsLimit, memLimit).also { result ->
            if (captureMatch) {
                result.getOrNull()?.let { derived ->
                    derivedKeyBytes = derived.key
                    derivedSaltBytes = derived.salt
                }
            }
        }
    }

    override suspend fun decrypt(
        ciphertext: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<ByteArray> = delegate.decrypt(ciphertext, nonce, key, associatedData).also { result ->
        if (captureMatch && associatedData?.contentEquals(VEK_WRAP_ASSOCIATED_DATA) == true) {
            unwrappedVek = result.getOrNull()
            captureMatch = false
        }
    }

    override suspend fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        constantTimeComparisonCount++
        return delegate.constantTimeEquals(a, b)
    }

    fun assertCapturedMaterialCleared(expectUnwrappedKey: Boolean) {
        assertTrue(requireNotNull(passwordBytes).all { it == 0.toByte() })
        assertTrue(requireNotNull(derivedKeyBytes).all { it == 0.toByte() })
        assertTrue(requireNotNull(derivedSaltBytes).all { it == 0.toByte() })
        if (expectUnwrappedKey) {
            assertTrue(requireNotNull(unwrappedVek).all { it == 0.toByte() })
        } else {
            assertNull(unwrappedVek)
        }
    }

    private companion object {
        val VEK_WRAP_ASSOCIATED_DATA = "VEK_WRAP".encodeToByteArray()
    }
}

private class PasswordMatchFailureCryptoEngine(
    private val delegate: CryptoEngine,
) : CryptoEngine by delegate {
    private var failNextDerivation = false
    private var failNextUnwrap = false

    fun failNextPasswordDerivation() {
        failNextDerivation = true
    }

    fun failNextVaultKeyUnwrap() {
        failNextUnwrap = true
    }

    override suspend fun deriveKey(
        password: ByteArray,
        salt: ByteArray,
        opsLimit: Int,
        memLimit: Int,
    ): Result<DerivedKey> {
        if (failNextDerivation) {
            failNextDerivation = false
            return Result.failure(IllegalStateException("simulated KDF failure"))
        }
        return delegate.deriveKey(password, salt, opsLimit, memLimit)
    }

    override suspend fun decrypt(
        ciphertext: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<ByteArray> {
        if (failNextUnwrap && associatedData?.contentEquals(VEK_WRAP_ASSOCIATED_DATA) == true) {
            failNextUnwrap = false
            return Result.failure(IllegalStateException("simulated VEK unwrap failure"))
        }
        return delegate.decrypt(ciphertext, nonce, key, associatedData)
    }

    private companion object {
        val VEK_WRAP_ASSOCIATED_DATA = "VEK_WRAP".encodeToByteArray()
    }
}

private class GatedUnlockCryptoEngine(
    private val delegate: CryptoEngine,
) : CryptoEngine by delegate {
    private var derivedKeyCalls = 0
    private var passwordGate: UnlockGate? = null
    private var verificationGate: UnlockGate? = null

    var lastUnwrappedVaultKey: ByteArray? = null
        private set
    var lastVerificationKey: ByteArray? = null
        private set

    fun gateNextPasswordDerivation(): UnlockGate = UnlockGate().also { passwordGate = it }

    fun gateNextVerification(): UnlockGate = UnlockGate().also { verificationGate = it }

    override suspend fun deriveKey(
        password: ByteArray,
        salt: ByteArray,
        opsLimit: Int,
        memLimit: Int,
    ): Result<DerivedKey> {
        derivedKeyCalls++
        passwordGate?.takeIf { derivedKeyCalls > 1 }?.let { gate ->
            passwordGate = null
            gate.started.complete(Unit)
            gate.release.await()
        }
        return delegate.deriveKey(password, salt, opsLimit, memLimit)
    }

    override suspend fun decrypt(
        ciphertext: ByteArray,
        nonce: ByteArray,
        key: ByteArray,
        associatedData: ByteArray?,
    ): Result<ByteArray> {
        if (associatedData?.contentEquals(VERIFICATION_ASSOCIATED_DATA) == true) {
            lastVerificationKey = key
            verificationGate?.let { gate ->
                verificationGate = null
                gate.started.complete(Unit)
                gate.release.await()
            }
        }
        val result = delegate.decrypt(ciphertext, nonce, key, associatedData)
        if (associatedData?.contentEquals(VEK_WRAP_ASSOCIATED_DATA) == true) {
            lastUnwrappedVaultKey = result.getOrNull()
        }
        return result
    }

    class UnlockGate {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
    }

    private companion object {
        val VEK_WRAP_ASSOCIATED_DATA = "VEK_WRAP".encodeToByteArray()
        val VERIFICATION_ASSOCIATED_DATA = "verification".encodeToByteArray()
    }
}

private class GatedLastAccessVaultMetadataDao(
    private val delegate: VaultMetadataDao,
) : VaultMetadataDao by delegate {
    val updateStarted = CompletableDeferred<Unit>()
    val allowUpdate = CompletableDeferred<Unit>()

    override suspend fun updateLastAccessed(timestamp: Long) {
        updateStarted.complete(Unit)
        allowUpdate.await()
        delegate.updateLastAccessed(timestamp)
    }
}

private class ThrowOnceWipeCryptoEngine(
    private val delegate: CryptoEngine,
) : CryptoEngine by delegate {
    private var shouldFailNextWipe = false

    fun failNextWipe() {
        shouldFailNextWipe = true
    }

    override fun secureWipe(data: ByteArray) {
        if (shouldFailNextWipe) {
            shouldFailNextWipe = false
            throw IllegalStateException("Simulated secure-wipe failure")
        }
        delegate.secureWipe(data)
    }
}

abstract class RepositorySecurityIntegrationFixture {
    protected lateinit var database: VaultDatabase
    protected lateinit var cryptoEngine: DesktopCryptoEngine
    protected lateinit var vaultRepository: VaultRepositoryImpl
    protected lateinit var credentialRepository: CredentialRepositoryImpl
    protected lateinit var folderRepository: FolderRepositoryImpl
    protected lateinit var tagRepository: TagRepositoryImpl

    @BeforeTest
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<VaultDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        cryptoEngine = DesktopCryptoEngine()
        vaultRepository = VaultRepositoryImpl(
            vaultMetadataDao = database.vaultMetadataDao(),
            cryptoEngine = cryptoEngine,
            keyHierarchy = com.passvault.core.crypto.VaultKeyHierarchy(cryptoEngine),
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
        database.close()
    }

    protected suspend fun createAndUnlockVault() {
        val masterPassword = SensitiveText.from(TEST_MASTER_PASSWORD)
        try {
            assertTrue(vaultRepository.create(masterPassword).isSuccess)
            assertTrue(vaultRepository.unlock(masterPassword).isSuccess)
        } finally {
            masterPassword.clear()
        }
    }

    protected fun sampleCredential(): Credential = Credential(
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

    protected suspend fun insertEncryptedAttachment(
        credentialId: CredentialId,
        filenameValue: String = "document.txt",
    ) {
        val vek = vaultRepository.withUnlockedSession { it.copyOf() }
        val context = "attachment-context"
        var attachmentKey: ByteArray? = null
        val filename = filenameValue.encodeToByteArray()
        try {
            attachmentKey = cryptoEngine.deriveSubkey(vek, "attachment:$context", 32).getOrThrow()
            val encrypted = cryptoEngine.encrypt(
                plaintext = filename,
                key = attachmentKey,
                associatedData =
                    "passvault:attachment:attachment-one:${credentialId.value}:filename:v1".encodeToByteArray(),
            ).getOrThrow()
            try {
                database.attachmentDao().insert(
                    AttachmentRecordEntity(
                        id = "attachment-one",
                        credentialId = credentialId.value,
                        encryptedFilename = CryptoEnvelope.encode(encrypted),
                        filenameNonce = encrypted.nonce.copyOf(),
                        mimeType = "text/plain",
                        sizeBytes = 12,
                        storagePath = "attachments/document.enc",
                        keyDerivationContext = context,
                        createdAt = 1_000,
                    ),
                )
            } finally {
                encrypted.clear()
            }
        } finally {
            cryptoEngine.secureWipe(filename)
            attachmentKey?.let { cryptoEngine.secureWipe(it) }
            cryptoEngine.secureWipe(vek)
        }
    }

    protected suspend fun reencryptAsLegacy(
        paddedCiphertext: ByteArray,
        nonce: ByteArray,
        keyContext: String,
        associatedDataValue: String,
    ): Pair<ByteArray, ByteArray> {
        val vek = vaultRepository.withUnlockedSession { it.copyOf() }
        var key: ByteArray? = null
        var plaintext: ByteArray? = null
        val associatedData = associatedDataValue.encodeToByteArray()
        return try {
            key = cryptoEngine.deriveSubkey(vek, keyContext, 32).getOrThrow()
            plaintext = PaddedPayload.decrypt(
                cryptoEngine = cryptoEngine,
                storedCiphertext = paddedCiphertext,
                nonce = nonce,
                key = key,
                associatedData = associatedData,
                maxPlaintextBytes = MAX_CREDENTIAL_PLAINTEXT_BYTES,
            ).getOrThrow()
            val encrypted = cryptoEngine.encrypt(plaintext, key, associatedData).getOrThrow()
            try {
                CryptoEnvelope.encode(encrypted) to encrypted.nonce.copyOf()
            } finally {
                encrypted.clear()
            }
        } finally {
            plaintext?.let(cryptoEngine::secureWipe)
            key?.let(cryptoEngine::secureWipe)
            cryptoEngine.secureWipe(associatedData)
            cryptoEngine.secureWipe(vek)
        }
    }

    protected fun Credential.clearSensitiveValuesForTest() {
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

    protected class InMemoryBiometricKeyStore : BiometricKeyStore {
        private var key: ByteArray? = null
        val reconciledVaultIds = mutableListOf<String?>()

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

        override suspend fun reconcile(activeVaultId: String?): Result<Unit> {
            reconciledVaultIds += activeVaultId
            return Result.success(Unit)
        }

        fun clear() {
            key?.fill(0)
            key = null
        }
    }

    protected class FailingLastAccessVaultMetadataDao(
        private val delegate: VaultMetadataDao,
    ) : VaultMetadataDao by delegate {
        override suspend fun updateLastAccessed(timestamp: Long) {
            throw IllegalStateException("Simulated metadata write failure")
        }
    }

}
