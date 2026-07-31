package com.passvault.core.database.repository

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.crypto.DesktopCryptoEngine
import com.passvault.core.database.VaultDatabase
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
import com.passvault.core.domain.model.UrlValue
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
    }

    private companion object {
        const val TEST_MASTER_PASSWORD = "correct horse battery staple"
    }
}
