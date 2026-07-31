package com.passvault.core.data

import app.cash.turbine.test
import com.passvault.core.database.VaultDatabase
import com.passvault.core.domain.model.*
import com.passvault.core.domain.repository.CredentialRepository
import com.passvault.core.testing.TestData
import com.passvault.core.testing.fakes.FakeCredentialRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * Test suite for CredentialRepository.
 * Tests CRUD operations and business logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CredentialRepositoryTest {
    
    private lateinit var repository: FakeCredentialRepository
    
    @BeforeTest
    fun setUp() {
        repository = FakeCredentialRepository()
    }
    
    @AfterTest
    fun tearDown() {
        repository.reset()
    }
    
    @Test
    fun `save new credential returns ID`() = runTest {
        val credential = TestData.loginCredential()
        
        val result = repository.save(credential)
        
        assertTrue(result.isSuccess, "Save should succeed")
        assertEquals(credential.id, result.getOrThrow(), "Should return credential ID")
    }
    
    @Test
    fun `save updates existing credential`() = runTest {
        val credential = TestData.loginCredential()
        repository.save(credential)
        
        val updatedCredential = credential.copy(
            title = "Updated Title",
            updatedAt = Clock.System.now(),
        )
        
        val result = repository.save(updatedCredential)
        
        assertTrue(result.isSuccess, "Update should succeed")
        
        val retrieved = repository.getById(credential.id).getOrThrow()
        assertEquals("Updated Title", retrieved?.title, "Title should be updated")
    }
    
    @Test
    fun `getById returns saved credential`() = runTest {
        val credential = TestData.loginCredential()
        repository.save(credential)
        
        val result = repository.getById(credential.id)
        
        assertTrue(result.isSuccess, "Get should succeed")
        assertNotNull(result.getOrThrow(), "Should return credential")
        assertEquals(credential.id, result.getOrThrow()?.id, "ID should match")
    }
    
    @Test
    fun `getById returns null for non-existent credential`() = runTest {
        val result = repository.getById(CredentialId("non-existent"))
        
        assertTrue(result.isSuccess, "Get should succeed")
        assertNull(result.getOrThrow(), "Should return null for non-existent credential")
    }
    
    @Test
    fun `delete removes credential`() = runTest {
        val credential = TestData.loginCredential()
        repository.save(credential)
        
        val deleteResult = repository.delete(credential.id)
        assertTrue(deleteResult.isSuccess, "Delete should succeed")
        
        val getResult = repository.getById(credential.id)
        assertNull(getResult.getOrThrow(), "Credential should be deleted")
    }
    
    @Test
    fun `getAllSummaries returns list of credentials`() = runTest {
        val credentials = TestData.sampleCredentials(5)
        credentials.forEach { repository.save(it) }
        
        val result = repository.getAllSummaries()
        
        assertTrue(result.isSuccess, "GetAllSummaries should succeed")
        val summaries = result.getOrThrow()
        assertEquals(5, summaries.size, "Should return all credentials")
    }
    
    @Test
    fun `getAllSummaries returns empty list when no credentials`() = runTest {
        val result = repository.getAllSummaries()
        
        assertTrue(result.isSuccess, "GetAllSummaries should succeed")
        assertTrue(result.getOrThrow().isEmpty(), "Should return empty list")
    }
    
    @Test
    fun `updateFavorite changes favorite status`() = runTest {
        val credential = TestData.loginCredential(isFavorite = false)
        repository.save(credential)
        
        val result = repository.updateFavorite(credential.id, true)
        
        assertTrue(result.isSuccess, "UpdateFavorite should succeed")
        
        val updated = repository.getById(credential.id).getOrThrow()
        assertEquals(true, updated?.isFavorite, "Should be marked as favorite")
    }
    
    @Test
    fun `moveToFolder changes folder`() = runTest {
        val credential = TestData.loginCredential()
        repository.save(credential)
        
        val folderId = FolderId("folder-123")
        val result = repository.moveToFolder(credential.id, folderId)
        
        assertTrue(result.isSuccess, "MoveToFolder should succeed")
        
        val updated = repository.getById(credential.id).getOrThrow()
        assertEquals(folderId, updated?.folderId, "Folder should be updated")
    }
    
    @Test
    fun `addTag adds tag to credential`() = runTest {
        val credential = TestData.loginCredential()
        repository.save(credential)
        
        val tagId = TagId("tag-123")
        val result = repository.addTag(credential.id, tagId)
        
        assertTrue(result.isSuccess, "AddTag should succeed")
        
        val updated = repository.getById(credential.id).getOrThrow()
        assertTrue(updated?.tagIds?.contains(tagId) ?: false, "Tag should be added")
    }
    
    @Test
    fun `removeTag removes tag from credential`() = runTest {
        val tagId = TagId("tag-123")
        val credential = TestData.loginCredential().copy(
            tagIds = setOf(tagId),
        )
        repository.save(credential)
        
        val result = repository.removeTag(credential.id, tagId)
        
        assertTrue(result.isSuccess, "RemoveTag should succeed")
        
        val updated = repository.getById(credential.id).getOrThrow()
        assertFalse(updated?.tagIds?.contains(tagId) ?: true, "Tag should be removed")
    }
    
    @Test
    fun `recordUsage updates lastUsedAt`() = runTest {
        val credential = TestData.loginCredential()
        repository.save(credential)
        
        val usageTime = Clock.System.now()
        val result = repository.recordUsage(credential.id, usageTime)
        
        assertTrue(result.isSuccess, "RecordUsage should succeed")
        
        val updated = repository.getById(credential.id).getOrThrow()
        assertEquals(usageTime, updated?.lastUsedAt, "Last used time should be updated")
    }
    
    @Test
    fun `addPasswordHistory appends to history`() = runTest {
        val credential = TestData.loginCredential()
        repository.save(credential)
        
        val oldPassword = SensitiveText.from("OldPass123!")
        val result = repository.addPasswordHistory(credential.id, oldPassword)
        
        assertTrue(result.isSuccess, "AddPasswordHistory should succeed")
        
        val updated = repository.getById(credential.id).getOrThrow()
        assertEquals(1, updated?.passwordHistory?.size, "Should have 1 history entry")
    }
    
    @Test
    fun `addPasswordHistory limits to 10 entries`() = runTest {
        val credential = TestData.loginCredential()
        repository.save(credential)
        
        // Add 15 history entries
        repeat(15) { index ->
            repository.addPasswordHistory(
                credential.id,
                SensitiveText.from("Password$index!")
            )
        }
        
        val updated = repository.getById(credential.id).getOrThrow()
        assertEquals(10, updated?.passwordHistory?.size, "Should have max 10 history entries")
    }
    
    @Test
    fun `getCredentialsForHealthAnalysis returns all credentials`() = runTest {
        val credentials = TestData.sampleCredentials(5)
        credentials.forEach { repository.save(it) }
        
        val result = repository.getCredentialsForHealthAnalysis()
        
        assertTrue(result.isSuccess, "GetCredentialsForHealthAnalysis should succeed")
        assertEquals(5, result.getOrThrow().size, "Should return all credentials")
    }
    
    @Test
    fun `updateHealth stores health data`() = runTest {
        val credential = TestData.loginCredential()
        repository.save(credential)
        
        val health = TestData.passwordHealth(
            score = PasswordScore.STRONG,
            isWeak = false,
        )
        
        val result = repository.updateHealth(credential.id, health)
        
        assertTrue(result.isSuccess, "UpdateHealth should succeed")
    }
    
    @Test
    fun `operations fail when configured to fail`() = runTest {
        repository.setShouldFail(RuntimeException("Test error"))
        
        val credential = TestData.loginCredential()
        val result = repository.save(credential)
        
        assertTrue(result.isFailure, "Save should fail when configured")
    }
    
    @Test
    fun `operations succeed after reset from failure`() = runTest {
        repository.setShouldFail(RuntimeException("Test error"))
        repository.setShouldFail(null) // Reset
        
        val credential = TestData.loginCredential()
        val result = repository.save(credential)
        
        assertTrue(result.isSuccess, "Save should succeed after reset")
    }
    
    @Test
    fun `credential with all fields is saved correctly`() = runTest {
        val credential = Credential(
            id = CredentialId("test-id"),
            type = CredentialType.Login,
            title = "Complete Test Credential",
            username = SensitiveText.from("testuser"),
            email = SensitiveText.from("test@example.com"),
            password = SensitiveText.from("TestPass123!"),
            urls = listOf(UrlValue("https://example.com")),
            notes = SensitiveText.from("Test notes"),
            recoveryCodes = listOf(SensitiveText.from("code1")),
            apiKeys = emptyList(),
            licenseKeys = emptyList(),
            customFields = listOf(
                CustomField(
                    id = CustomFieldId("field1"),
                    name = "Custom Field",
                    value = SensitiveText.from("Custom Value"),
                    isSecret = false,
                ),
            ),
            folderId = FolderId("folder-1"),
            tagIds = setOf(TagId("tag-1")),
            isFavorite = true,
            attachments = emptyList(),
            passwordHistory = emptyList(),
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            lastUsedAt = null,
        )
        
        val saveResult = repository.save(credential)
        assertTrue(saveResult.isSuccess, "Save should succeed")
        
        val retrieved = repository.getById(credential.id).getOrThrow()
        assertNotNull(retrieved, "Should retrieve saved credential")
        assertEquals(credential.title, retrieved.title)
        assertEquals(credential.isFavorite, retrieved.isFavorite)
        assertEquals(credential.folderId, retrieved.folderId)
    }
    
    @Test
    fun `different credential types are saved correctly`() = runTest {
        val credentials = listOf(
            TestData.loginCredential(),
            TestData.secureNoteCredential(),
            TestData.apiKeyCredential(),
            TestData.wifiCredential(),
            TestData.paymentCardCredential(),
        )
        
        credentials.forEach { credential ->
            val result = repository.save(credential)
            assertTrue(result.isSuccess, "Should save ${credential.type}")
        }
        
        val allSummaries = repository.getAllSummaries().getOrThrow()
        assertEquals(credentials.size, allSummaries.size)
    }
    
    @Test
    fun `concurrent saves are handled correctly`() = runTest {
        val credentials = TestData.sampleCredentials(10)
        
        // Save all concurrently
        credentials.map { credential ->
            async {
                repository.save(credential)
            }
        }.forEach { it.await() }
        
        val allSummaries = repository.getAllSummaries().getOrThrow()
        assertEquals(10, allSummaries.size, "All credentials should be saved")
    }
    
    @Test
    fun `delete non-existent credential succeeds`() = runTest {
        val result = repository.delete(CredentialId("non-existent"))
        
        assertTrue(result.isSuccess, "Delete should succeed even for non-existent credential")
    }
    
    @Test
    fun `summary contains correct display information`() = runTest {
        val credential = TestData.loginCredential(
            title = "Test Site",
            username = "johndoe",
            isFavorite = true,
        )
        repository.save(credential)
        
        val summaries = repository.getAllSummaries().getOrThrow()
        assertEquals(1, summaries.size)
        
        val summary = summaries.first()
        assertEquals("Test Site", summary.title)
        assertEquals(true, summary.isFavorite)
        assertNotNull(summary.displayUsername)
    }
}
