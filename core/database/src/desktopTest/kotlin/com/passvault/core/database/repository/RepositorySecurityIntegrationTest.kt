package com.passvault.core.database.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.crypto.CryptoEnvelope
import com.passvault.core.crypto.CryptoEngine
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.database.VaultDatabase
import com.passvault.core.database.backup.VaultBackupService
import com.passvault.core.database.dao.VaultMetadataDao
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.database.entity.TagRecordEntity
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
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
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

private const val TEST_MASTER_PASSWORD = "correct horse battery staple"
private const val TEST_TOTP_SECRET = "JBSWY3DPEHPK3PXP"

/**
 * Exercises the real Room + libsodium repository boundary. Fakes cannot
 * prove that encrypted records survive a lock, reject tampering, or preserve
 * all fields through serialization.
 */
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
    fun `master password change rejects a weak replacement without changing the vault`() = runTest {
        createAndUnlockVault()
        val current = SensitiveText.from(TEST_MASTER_PASSWORD)
        val weak = SensitiveText.from("too-short")

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
    fun `credential read rejects an authenticated attachment filename with bidi controls`() = runTest {
        createAndUnlockVault()
        val credential = sampleCredential()
        try {
            assertTrue(credentialRepository.save(credential).isSuccess)
            insertEncryptedAttachment(credential.id, "invoice\u202Efdp.exe")

            assertTrue(credentialRepository.getById(credential.id).isFailure)
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

            val input = credentialRepository.getCredentialsForTotpDisplay().getOrThrow().single()
            assertEquals(withTotp.id, input.id)
            assertEquals("GitHub", input.title)
            assertEquals("alice@example.com", input.displayUsername)
            assertEquals(TEST_TOTP_SECRET, input.configuration.secret.toStringUnsafe())
            assertEquals("Example", input.configuration.issuer)

            input.clear()
            assertTrue(input.configuration.secret.toStringUnsafe().all { it == '\u0000' })
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

class RepositoryBiometricSecurityIntegrationTest : RepositorySecurityIntegrationFixture() {
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
    fun `lock waits for an active session lease and wipes its key`() = runTest {
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

        val lockStarted = CompletableDeferred<Unit>()
        val lockOperation = async(Dispatchers.Default) {
            lockStarted.complete(Unit)
            vaultRepository.lock()
        }
        lockStarted.await()
        yield()
        assertFalse(lockOperation.isCompleted, "Lock must wait for the active lease")

        releaseLease.complete(Unit)
        operation.await()
        assertTrue(lockOperation.await().isSuccess)
        assertTrue(requireNotNull(leasedKey).all { it == 0.toByte() })
        assertFalse(vaultRepository.isUnlocked())
    }
}

class VaultLockFailureIntegrationTest : RepositorySecurityIntegrationFixture() {

    @Test
    fun `lock cancels a platform prompt before waiting for an active session lease`() = runTest {
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

            val locking = async(Dispatchers.Default) { repository.lock(LockReason.AutoLock) }
            cancellationObserved.await()
            assertFalse(locking.isCompleted, "Lock must still wait for key-lease cleanup")

            releaseLease.complete(Unit)
            lease.await()
            assertTrue(locking.await().isSuccess)
            assertFalse(repository.isUnlocked())
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

    protected class FailingLastAccessVaultMetadataDao(
        private val delegate: VaultMetadataDao,
    ) : VaultMetadataDao by delegate {
        override suspend fun updateLastAccessed(timestamp: Long) {
            throw IllegalStateException("Simulated metadata write failure")
        }
    }

}
