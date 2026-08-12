package com.passvault.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.database.dao.CredentialDao
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.database.entity.VaultMetadataEntity
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CredentialDaoMutationTest : CredentialDaoFixture() {

    @Test
    fun `upsert writes and replaces one encrypted record`() = runTest {
        val original = credential("credential-one", type = "Login")
        credentialDao.insertOrUpdate(original)
        credentialDao.insertOrUpdate(original.copy(type = "SecureNote", updatedAt = 2))

        assertEquals("SecureNote", credentialDao.getById(original.id)?.type)
    }

    @Test
    fun `summary query returns newest records first`() = runTest {
        credentialDao.insertOrUpdate(credential("older", updatedAt = 10))
        credentialDao.insertOrUpdate(credential("newer", updatedAt = 20))

        assertEquals(listOf("newer", "older"), credentialDao.getAllSummaries().map { it.id })
    }

    @Test
    fun `targeted updates change only requested columns`() = runTest {
        val original = credential("credential-one")
        val updatedSummary = byteArrayOf(8, 9)
        val updatedNonce = ByteArray(24) { 7 }
        credentialDao.insertOrUpdate(original)

        credentialDao.updateFavorite(original.id, true)
        credentialDao.updateLastUsed(original.id, 42)
        credentialDao.updateEncryptedSummary(original.id, updatedSummary, updatedNonce)

        val updated = assertNotNull(credentialDao.getById(original.id))
        assertTrue(updated.isFavorite)
        assertEquals(42, updated.lastUsedAt)
        assertContentEquals(updatedSummary, updated.summaryPayload)
        assertContentEquals(updatedNonce, updated.summaryNonce)
        assertContentEquals(original.secretPayload, updated.secretPayload)
    }

    @Test
    fun `full update replaces the authenticated record`() = runTest {
        val original = credential("credential-one")
        credentialDao.insertOrUpdate(original)

        credentialDao.update(original.copy(secretPayload = byteArrayOf(4, 5, 6)))

        assertContentEquals(byteArrayOf(4, 5, 6), credentialDao.getById(original.id)?.secretPayload)
    }

    @Test
    fun `exists and delete reflect record presence`() = runTest {
        credentialDao.insertOrUpdate(credential("credential-one"))
        assertTrue(credentialDao.exists("credential-one"))

        credentialDao.deleteById("credential-one")

        assertFalse(credentialDao.exists("credential-one"))
    }
}

class CredentialDaoTransactionTest : CredentialDaoFixture() {

    @Test
    fun `tag replacement removes stale references`() = runTest {
        credentialDao.insertOrUpdate(credential("credential-one"))
        insertTags("tag-one", "tag-two", "tag-three")
        credentialDao.replaceTagsForCredential("credential-one", listOf("tag-one", "tag-two"))

        credentialDao.replaceTagsForCredential("credential-one", listOf("tag-three"))

        assertEquals(
            listOf("tag-three"),
            credentialDao.getTagCrossRefsForCredential("credential-one").map { it.tagId },
        )
    }

    @Test
    fun `folder update keeps canonical column and cross reference aligned`() = runTest {
        insertFolder("folder-one")
        credentialDao.insertOrUpdate(credential("credential-one"))

        credentialDao.updateFolderAndCrossReference("credential-one", "folder-one")

        assertEquals("folder-one", credentialDao.getById("credential-one")?.folderId)
        assertEquals(listOf("credential-one"), credentialDao.getByFolderCrossRef("folder-one").map { it.id })
    }

    @Test
    fun `full save transaction updates history count and format`() = runTest {
        insertVaultMetadata()
        val entity = credential("credential-one")
        val history = passwordHistory("history-one", entity.id, changedAt = 10)

        credentialDao.updateCredentialWithTagsAndHistory(
            entity = entity,
            tagIds = emptyList(),
            history = history,
            requiredVaultFormatVersion = 2,
        )

        assertEquals(listOf(history), database.passwordHistoryDao().getByCredential(entity.id))
        val metadata = assertNotNull(database.vaultMetadataDao().get())
        assertEquals(1, metadata.entryCount)
        assertEquals(2, metadata.vaultFormatVersion)
    }

    @Test
    fun `full save transaction retains only ten newest history rows`() = runTest {
        insertVaultMetadata()
        val entity = credential("credential-one")
        repeat(12) { index ->
            credentialDao.updateCredentialWithTagsAndHistory(
                entity = entity.copy(updatedAt = index.toLong()),
                tagIds = emptyList(),
                history = passwordHistory("history-$index", entity.id, changedAt = index.toLong()),
            )
        }

        val history = database.passwordHistoryDao().getByCredential(entity.id)
        assertEquals(10, history.size)
        assertEquals((11 downTo 2).map { "history-$it" }, history.map { it.id })
    }

    @Test
    fun `history retention is deterministic when timestamps are equal`() = runTest {
        insertVaultMetadata()
        val entity = credential("credential-one")
        repeat(12) { index ->
            val id = "history-${index.toString().padStart(2, '0')}"
            credentialDao.updateCredentialWithTagsAndHistory(
                entity = entity,
                tagIds = emptyList(),
                history = passwordHistory(id, entity.id, changedAt = 10),
            )
        }

        assertEquals(
            (11 downTo 2).map { index -> "history-${index.toString().padStart(2, '0')}" },
            database.passwordHistoryDao().getByCredential(entity.id).map { it.id },
        )
    }

    @Test
    fun `health query returns only login records`() = runTest {
        credentialDao.insertOrUpdate(credential("login-one", type = "Login"))
        credentialDao.insertOrUpdate(credential("note-one", type = "SecureNote"))
        credentialDao.insertOrUpdate(credential("login-two", type = "Login"))

        assertEquals(
            setOf("login-one", "login-two"),
            credentialDao.getLoginsForHealthAnalysis().map { it.id }.toSet(),
        )
    }

    @Test
    fun `delete transaction cascades relations and refreshes count`() = runTest {
        insertVaultMetadata()
        insertTags("tag-one")
        val entity = credential("credential-one")
        credentialDao.updateCredentialWithTagsAndHistory(
            entity = entity,
            tagIds = listOf("tag-one"),
            history = passwordHistory("history-one", entity.id, changedAt = 1),
        )

        credentialDao.deleteCredentialAndRefreshCount(entity.id)

        assertFalse(credentialDao.exists(entity.id))
        assertTrue(credentialDao.getTagCrossRefsForCredential(entity.id).isEmpty())
        assertTrue(database.passwordHistoryDao().getByCredential(entity.id).isEmpty())
        assertEquals(0, database.vaultMetadataDao().get()?.entryCount)
    }
}

abstract class CredentialDaoFixture {
    protected lateinit var database: VaultDatabase
    protected lateinit var credentialDao: CredentialDao

    @BeforeTest
    fun setUpDatabase() {
        database = Room.inMemoryDatabaseBuilder<VaultDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        credentialDao = database.credentialDao()
    }

    @AfterTest
    fun closeDatabase() {
        database.close()
    }

    protected fun credential(
        id: String,
        type: String = "Login",
        folderId: String? = null,
        updatedAt: Long = 1,
    ): CredentialRecordEntity = CredentialRecordEntity(
        id = id,
        type = type,
        titleHash = ByteArray(32) { 1 },
        summaryPayload = byteArrayOf(1, 2),
        summaryNonce = ByteArray(24) { 2 },
        secretPayload = byteArrayOf(3, 4),
        secretNonce = ByteArray(24) { 3 },
        folderId = folderId,
        isFavorite = false,
        createdAt = 1,
        updatedAt = updatedAt,
        lastUsedAt = null,
    )

    protected suspend fun insertTags(vararg ids: String) {
        ids.forEachIndexed { index, id ->
            database.tagDao().insertOrUpdate(
                TagRecordEntity(
                    id = id,
                    nameHash = ByteArray(32) { index.toByte() },
                    encryptedPayload = byteArrayOf(1),
                    payloadNonce = ByteArray(24),
                    color = null,
                    createdAt = index.toLong(),
                ),
            )
        }
    }

    protected suspend fun insertFolder(id: String) {
        database.folderDao().insertOrUpdate(
            FolderRecordEntity(
                id = id,
                parentId = null,
                nameHash = ByteArray(32),
                encryptedPayload = byteArrayOf(1),
                payloadNonce = ByteArray(24),
                icon = null,
                sortOrder = 0,
                createdAt = 1,
                updatedAt = 1,
            ),
        )
    }

    protected suspend fun insertVaultMetadata() {
        database.vaultMetadataDao().insert(
            VaultMetadataEntity(
                vaultFormatVersion = 1,
                cryptoFormatVersion = 2,
                vaultId = "test-vault",
                argon2AlgorithmId = "Argon2id",
                argon2Salt = ByteArray(16),
                argon2OpsLimit = 3,
                argon2MemLimit = 64 * 1024 * 1024,
                argon2Parallelism = 1,
                wrappedVek = ByteArray(52),
                vekNonce = ByteArray(24),
                encryptedVerificationRecord = ByteArray(52),
                verificationNonce = ByteArray(24),
                createdAt = 1,
                lastAccessedAt = null,
                entryCount = 0,
            ),
        )
    }

    protected fun passwordHistory(
        id: String,
        credentialId: String,
        changedAt: Long,
    ): PasswordHistoryRecordEntity = PasswordHistoryRecordEntity(
        id = id,
        credentialId = credentialId,
        encryptedPassword = byteArrayOf(1, 2),
        passwordNonce = ByteArray(24),
        changedAt = changedAt,
    )
}
