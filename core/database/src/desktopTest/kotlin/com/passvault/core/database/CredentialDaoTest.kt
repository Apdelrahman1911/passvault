package com.passvault.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.passvault.core.database.dao.CredentialDao
import com.passvault.core.database.entity.CredentialFolderCrossRef
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.entity.TagRecordEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.time.Clock
import kotlin.test.*

/**
 * Comprehensive test suite for CredentialDao.
 *
 * Tests all CRUD operations, queries, and cross-reference operations
 * for credential records.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CredentialDaoTest {

    private lateinit var database: VaultDatabase
    private lateinit var credentialDao: CredentialDao

    @BeforeTest
    fun setUp() {
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder<VaultDatabase>()
            .setDriver(BundledSQLiteDriver())
            .build()
        credentialDao = database.credentialDao()
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    // ==================== Insert Operations ====================

    @Test
    fun `insert creates new credential`() = runTest {
        val entity = createTestCredential("cred-1", "Test Credential")

        val id = credentialDao.insert(entity)

        assertTrue(id > 0)
        val retrieved = credentialDao.getById("cred-1")
        assertNotNull(retrieved)
        assertEquals("cred-1", retrieved?.id)
    }

    @Test
    fun `insertOrUpdate updates existing credential`() = runTest {
        val entity = createTestCredential("cred-1", "Original Title")
        credentialDao.insert(entity)

        val updated = entity.copy(type = "Updated Type")
        credentialDao.insertOrUpdate(updated)

        val retrieved = credentialDao.getById("cred-1")
        assertNotNull(retrieved)
        assertEquals("Updated Type", retrieved?.type)
    }

    @Test
    fun `insertAll creates multiple credentials`() = runTest {
        val entities = listOf(
            createTestCredential("cred-1", "Credential 1"),
            createTestCredential("cred-2", "Credential 2"),
            createTestCredential("cred-3", "Credential 3")
        )

        val ids = credentialDao.insertAll(entities)

        assertEquals(3, ids.size)
        assertEquals(3, credentialDao.getCount())
    }

    @Test
    fun `insertOrUpdateAll handles mixed new and existing`() = runTest {
        val entity1 = createTestCredential("cred-1", "Credential 1")
        credentialDao.insert(entity1)

        val entities = listOf(
            entity1.copy(type = "Updated"),
            createTestCredential("cred-2", "Credential 2")
        )

        credentialDao.insertOrUpdateAll(entities)

        assertEquals(2, credentialDao.getCount())
        assertEquals("Updated", credentialDao.getById("cred-1")?.type)
    }

    // ==================== Update Operations ====================

    @Test
    fun `update modifies credential`() = runTest {
        val entity = createTestCredential("cred-1", "Test Credential")
        credentialDao.insert(entity)

        val updated = entity.copy(type = "Modified Type", isFavorite = true)
        credentialDao.update(updated)

        val retrieved = credentialDao.getById("cred-1")
        assertNotNull(retrieved)
        assertEquals("Modified Type", retrieved?.type)
        assertTrue(retrieved?.isFavorite == true)
    }

    @Test
    fun `updateAll modifies multiple credentials`() = runTest {
        val entities = listOf(
            createTestCredential("cred-1", "Credential 1"),
            createTestCredential("cred-2", "Credential 2")
        )
        credentialDao.insertAll(entities)

        val updated = entities.map { it.copy(isFavorite = true) }
        credentialDao.updateAll(updated)

        val all = credentialDao.getAll()
        assertTrue(all.all { it.isFavorite })
    }

    @Test
    fun `updateFavorite changes favorite status`() = runTest {
        val entity = createTestCredential("cred-1", "Test Credential", isFavorite = false)
        credentialDao.insert(entity)

        credentialDao.updateFavorite("cred-1", true)

        val retrieved = credentialDao.getById("cred-1")
        assertTrue(retrieved?.isFavorite == true)
    }

    @Test
    fun `updateFolder changes folder`() = runTest {
        val entity = createTestCredential("cred-1", "Test Credential")
        credentialDao.insert(entity)

        credentialDao.updateFolder("cred-1", "folder-123")

        val retrieved = credentialDao.getById("cred-1")
        assertEquals("folder-123", retrieved?.folderId)
    }

    @Test
    fun `updateFolder sets folder to null`() = runTest {
        val entity = createTestCredential("cred-1", "Test Credential", folderId = "folder-123")
        credentialDao.insert(entity)

        credentialDao.updateFolder("cred-1", null)

        val retrieved = credentialDao.getById("cred-1")
        assertNull(retrieved?.folderId)
    }

    @Test
    fun `updateLastUsed sets timestamp`() = runTest {
        val entity = createTestCredential("cred-1", "Test Credential")
        credentialDao.insert(entity)

        val timestamp = 1234567890L
        credentialDao.updateLastUsed("cred-1", timestamp)

        val retrieved = credentialDao.getById("cred-1")
        assertEquals(timestamp, retrieved?.lastUsedAt)
    }

    // ==================== Delete Operations ====================

    @Test
    fun `delete removes credential`() = runTest {
        val entity = createTestCredential("cred-1", "Test Credential")
        credentialDao.insert(entity)

        credentialDao.delete(entity)

        val retrieved = credentialDao.getById("cred-1")
        assertNull(retrieved)
    }

    @Test
    fun `deleteById removes credential`() = runTest {
        val entity = createTestCredential("cred-1", "Test Credential")
        credentialDao.insert(entity)

        credentialDao.deleteById("cred-1")

        assertNull(credentialDao.getById("cred-1"))
    }

    @Test
    fun `deleteByIds removes multiple credentials`() = runTest {
        val entities = listOf(
            createTestCredential("cred-1", "Credential 1"),
            createTestCredential("cred-2", "Credential 2"),
            createTestCredential("cred-3", "Credential 3")
        )
        credentialDao.insertAll(entities)

        credentialDao.deleteByIds(listOf("cred-1", "cred-3"))

        assertNull(credentialDao.getById("cred-1"))
        assertNotNull(credentialDao.getById("cred-2"))
        assertNull(credentialDao.getById("cred-3"))
    }

    @Test
    fun `deleteAll removes all credentials`() = runTest {
        val entities = listOf(
            createTestCredential("cred-1", "Credential 1"),
            createTestCredential("cred-2", "Credential 2")
        )
        credentialDao.insertAll(entities)

        credentialDao.deleteAll()

        assertEquals(0, credentialDao.getCount())
    }

    // ==================== Query Operations ====================

    @Test
    fun `getById returns correct credential`() = runTest {
        val entity = createTestCredential("cred-1", "Test Credential")
        credentialDao.insert(entity)

        val retrieved = credentialDao.getById("cred-1")

        assertNotNull(retrieved)
        assertEquals("cred-1", retrieved?.id)
        assertEquals("Test Credential", retrieved?.type) // Using type as title substitute in test
    }

    @Test
    fun `getById returns null for non-existent`() = runTest {
        val retrieved = credentialDao.getById("non-existent")

        assertNull(retrieved)
    }

    @Test
    fun `getAll returns all credentials`() = runTest {
        val entities = listOf(
            createTestCredential("cred-1", "Credential 1"),
            createTestCredential("cred-2", "Credential 2")
        )
        credentialDao.insertAll(entities)

        val all = credentialDao.getAll()

        assertEquals(2, all.size)
    }

    @Test
    fun `getAll returns empty when no credentials`() = runTest {
        val all = credentialDao.getAll()

        assertTrue(all.isEmpty())
    }

    @Test
    fun `getAllSummaries returns summaries`() = runTest {
        val entity = createTestCredential("cred-1", "Test Credential")
        credentialDao.insert(entity)

        val summaries = credentialDao.getAllSummaries()

        assertEquals(1, summaries.size)
        assertEquals("cred-1", summaries[0].id)
    }

    @Test
    fun `getSummariesByFolder returns credentials in folder`() = runTest {
        val entities = listOf(
            createTestCredential("cred-1", "Credential 1", folderId = "folder-1"),
            createTestCredential("cred-2", "Credential 2", folderId = "folder-1"),
            createTestCredential("cred-3", "Credential 3", folderId = "folder-2")
        )
        credentialDao.insertAll(entities)

        val summaries = credentialDao.getSummariesByFolder("folder-1")

        assertEquals(2, summaries.size)
        assertTrue(summaries.all { it.folderId == "folder-1" })
    }

    @Test
    fun `getFavoriteSummaries returns only favorites`() = runTest {
        val entities = listOf(
            createTestCredential("cred-1", "Credential 1", isFavorite = true),
            createTestCredential("cred-2", "Credential 2", isFavorite = false),
            createTestCredential("cred-3", "Credential 3", isFavorite = true)
        )
        credentialDao.insertAll(entities)

        val favorites = credentialDao.getFavoriteSummaries()

        assertEquals(2, favorites.size)
        assertTrue(favorites.all { it.isFavorite })
    }

    // ==================== Count Operations ====================

    @Test
    fun `getCount returns correct count`() = runTest {
        assertEquals(0, credentialDao.getCount())

        credentialDao.insert(createTestCredential("cred-1", "Credential 1"))
        assertEquals(1, credentialDao.getCount())

        credentialDao.insert(createTestCredential("cred-2", "Credential 2"))
        assertEquals(2, credentialDao.getCount())
    }

    @Test
    fun `getCountByFolder returns correct count`() = runTest {
        val entities = listOf(
            createTestCredential("cred-1", "Credential 1", folderId = "folder-1"),
            createTestCredential("cred-2", "Credential 2", folderId = "folder-1"),
            createTestCredential("cred-3", "Credential 3", folderId = "folder-2")
        )
        credentialDao.insertAll(entities)

        assertEquals(2, credentialDao.getCountByFolder("folder-1"))
        assertEquals(1, credentialDao.getCountByFolder("folder-2"))
        assertEquals(0, credentialDao.getCountByFolder("folder-3"))
    }

    @Test
    fun `getFavoriteCount returns correct count`() = runTest {
        val entities = listOf(
            createTestCredential("cred-1", "Credential 1", isFavorite = true),
            createTestCredential("cred-2", "Credential 2", isFavorite = true),
            createTestCredential("cred-3", "Credential 3", isFavorite = false)
        )
        credentialDao.insertAll(entities)

        assertEquals(2, credentialDao.getFavoriteCount())
    }

    @Test
    fun `exists returns true for existing credential`() = runTest {
        credentialDao.insert(createTestCredential("cred-1", "Credential 1"))

        assertTrue(credentialDao.exists("cred-1"))
    }

    @Test
    fun `exists returns false for non-existent credential`() = runTest {
        assertFalse(credentialDao.exists("non-existent"))
    }

    // ==================== Flow Operations ====================

    @Test
    fun `observeById emits updates`() = runTest {
        val entity = createTestCredential("cred-1", "Credential 1")
        credentialDao.insert(entity)

        val flow = credentialDao.observeById("cred-1")
        val first = flow.first()

        assertNotNull(first)
        assertEquals("cred-1", first?.id)
    }

    @Test
    fun `observeAllSummaries emits all summaries`() = runTest {
        credentialDao.insert(createTestCredential("cred-1", "Credential 1"))

        val flow = credentialDao.observeAllSummaries()
        val summaries = flow.first()

        assertEquals(1, summaries.size)
    }

    @Test
    fun `observeCount emits count updates`() = runTest {
        val flow = credentialDao.observeCount()

        var count = flow.first()
        assertEquals(0, count)

        credentialDao.insert(createTestCredential("cred-1", "Credential 1"))
        count = flow.first()
        assertEquals(1, count)
    }

    // ==================== Cross-Reference Operations ====================

    @Test
    fun `addTagCrossRef creates relationship`() = runTest {
        val entity = createTestCredential("cred-1", "Credential 1")
        credentialDao.insert(entity)
        insertTestTags("tag-1")

        val crossRef = CredentialTagCrossRef(credentialId = "cred-1", tagId = "tag-1")
        credentialDao.addTagCrossRef(crossRef)

        val tagRefs = credentialDao.getTagCrossRefsForCredential("cred-1")
        assertEquals(1, tagRefs.size)
        assertEquals("tag-1", tagRefs[0].tagId)
    }

    @Test
    fun `addTagCrossRefs creates multiple relationships`() = runTest {
        val entity = createTestCredential("cred-1", "Credential 1")
        credentialDao.insert(entity)
        insertTestTags("tag-1", "tag-2")

        val crossRefs = listOf(
            CredentialTagCrossRef(credentialId = "cred-1", tagId = "tag-1"),
            CredentialTagCrossRef(credentialId = "cred-1", tagId = "tag-2")
        )
        credentialDao.addTagCrossRefs(crossRefs)

        val tagRefs = credentialDao.getTagCrossRefsForCredential("cred-1")
        assertEquals(2, tagRefs.size)
    }

    @Test
    fun `removeTagCrossRef removes relationship`() = runTest {
        val entity = createTestCredential("cred-1", "Credential 1")
        credentialDao.insert(entity)
        insertTestTags("tag-1")
        credentialDao.addTagCrossRef(CredentialTagCrossRef(credentialId = "cred-1", tagId = "tag-1"))

        credentialDao.removeTagCrossRef("cred-1", "tag-1")

        val tagRefs = credentialDao.getTagCrossRefsForCredential("cred-1")
        assertTrue(tagRefs.isEmpty())
    }

    @Test
    fun `removeAllTagsFromCredential removes all tags`() = runTest {
        val entity = createTestCredential("cred-1", "Credential 1")
        credentialDao.insert(entity)
        insertTestTags("tag-1", "tag-2", "tag-3")
        credentialDao.addTagCrossRefs(listOf(
            CredentialTagCrossRef(credentialId = "cred-1", tagId = "tag-1"),
            CredentialTagCrossRef(credentialId = "cred-1", tagId = "tag-2"),
            CredentialTagCrossRef(credentialId = "cred-1", tagId = "tag-3")
        ))

        credentialDao.removeAllTagsFromCredential("cred-1")

        val tagRefs = credentialDao.getTagCrossRefsForCredential("cred-1")
        assertTrue(tagRefs.isEmpty())
    }

    // ==================== Transaction Operations ====================

    @Test
    fun `replaceTagsForCredential replaces all tags`() = runTest {
        val entity = createTestCredential("cred-1", "Credential 1")
        credentialDao.insert(entity)
        insertTestTags("tag-1", "tag-2", "tag-3", "tag-4")
        credentialDao.addTagCrossRefs(listOf(
            CredentialTagCrossRef(credentialId = "cred-1", tagId = "tag-1"),
            CredentialTagCrossRef(credentialId = "cred-1", tagId = "tag-2")
        ))

        credentialDao.replaceTagsForCredential("cred-1", listOf("tag-3", "tag-4"))

        val tagRefs = credentialDao.getTagCrossRefsForCredential("cred-1")
        assertEquals(2, tagRefs.size)
        assertTrue(tagRefs.any { it.tagId == "tag-3" })
        assertTrue(tagRefs.any { it.tagId == "tag-4" })
        assertFalse(tagRefs.any { it.tagId == "tag-1" })
        assertFalse(tagRefs.any { it.tagId == "tag-2" })
    }

    @Test
    fun `updateCredentialWithTags updates credential and tags`() = runTest {
        val entity = createTestCredential("cred-1", "Credential 1")
        credentialDao.insert(entity)
        insertTestTags("tag-1", "tag-2")

        val updatedEntity = entity.copy(type = "Updated Type")
        credentialDao.updateCredentialWithTags(updatedEntity, listOf("tag-1", "tag-2"))

        val retrieved = credentialDao.getById("cred-1")
        assertNotNull(retrieved)
        assertEquals("Updated Type", retrieved?.type)

        val tagRefs = credentialDao.getTagCrossRefsForCredential("cred-1")
        assertEquals(2, tagRefs.size)
    }

    // ==================== Health Analysis Operations ====================

    @Test
    fun `getLoginsForHealthAnalysis returns only login credentials`() = runTest {
        val entities = listOf(
            createTestCredential("cred-1", "Login"),
            createTestCredential("cred-2", "SecureNote"),
            createTestCredential("cred-3", "Login")
        )
        credentialDao.insertAll(entities)

        val logins = credentialDao.getLoginsForHealthAnalysis()

        assertEquals(2, logins.size)
        assertTrue(logins.all { it.type == "Login" })
    }

    // ==================== Helper Functions ====================

    private fun createTestCredential(
        id: String,
        type: String,
        folderId: String? = null,
        isFavorite: Boolean = false
    ): CredentialRecordEntity {
        val now = Clock.System.now().toEpochMilliseconds()
        return CredentialRecordEntity(
            id = id,
            type = type,
            titleHash = ByteArray(32) { 0 },
            summaryPayload = ByteArray(64) { 0 },
            summaryNonce = ByteArray(24) { 0 },
            secretPayload = ByteArray(128) { 0 },
            secretNonce = ByteArray(24) { 0 },
            folderId = folderId,
            isFavorite = isFavorite,
            createdAt = now,
            updatedAt = now,
            lastUsedAt = null
        )
    }

    private suspend fun insertTestTags(vararg ids: String) {
        database.tagDao().insertAll(
            ids.mapIndexed { index, id ->
                TagRecordEntity(
                    id = id,
                    nameHash = ByteArray(32) { index.toByte() },
                    encryptedPayload = ByteArray(64) { index.toByte() },
                    payloadNonce = ByteArray(24) { index.toByte() },
                    color = null,
                    createdAt = index.toLong(),
                )
            },
        )
    }
}
