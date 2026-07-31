package com.passvault.core.testing.fakes

import com.passvault.core.domain.model.*
import com.passvault.core.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock

/**
 * Fake vault repository for testing.
 * Simulates vault operations in memory.
 */
class FakeVaultRepository : VaultRepository {
    
    private var vaultExists = false
    private var currentVaultId: VaultId? = null
    private var sessionState = MutableStateFlow<VaultSessionState>(VaultSessionState.Uninitialized)
    private val vaultMetadata = mutableMapOf<VaultId, VaultMetadata>()
    
    private var shouldFailNext = false
    private var failWith: Throwable? = null
    private var unlockDelayMs: Long = 0
    
    /**
     * Pre-configure a vault to exist.
     */
    fun setupExistingVault(
        vaultId: VaultId = VaultId("test-vault-123"),
        metadata: VaultMetadata = VaultMetadata(
            id = vaultId,
            formatVersion = 1,
            createdAt = Clock.System.now(),
            lastAccessedAt = null,
            entryCount = 0,
        ),
    ) {
        vaultExists = true
        currentVaultId = vaultId
        vaultMetadata[vaultId] = metadata
    }
    
    /**
     * Configure the next operation to fail.
     */
    fun setShouldFail(error: Throwable? = RuntimeException("Fake vault error")) {
        shouldFailNext = true
        failWith = error
    }
    
    /**
     * Set delay for unlock operation (to test loading states).
     */
    fun setUnlockDelay(delayMs: Long) {
        unlockDelayMs = delayMs
    }
    
    /**
     * Reset to initial state.
     */
    fun reset() {
        vaultExists = false
        currentVaultId = null
        sessionState.value = VaultSessionState.Uninitialized
        vaultMetadata.clear()
        shouldFailNext = false
        failWith = null
        unlockDelayMs = 0
    }
    
    override suspend fun exists(): Result<Boolean> {
        return checkFailure { Result.success(vaultExists) }
    }
    
    override suspend fun create(masterPassword: SensitiveText): Result<VaultId> {
        return checkFailure {
            val vaultId = VaultId("vault-${Clock.System.now().toEpochMilliseconds()}")
            currentVaultId = vaultId
            vaultExists = true
            vaultMetadata[vaultId] = VaultMetadata(
                id = vaultId,
                formatVersion = 1,
                createdAt = Clock.System.now(),
                lastAccessedAt = null,
                entryCount = 0,
            )
            sessionState.value = VaultSessionState.Locked
            Result.success(vaultId)
        }
    }
    
    override suspend fun unlock(masterPassword: SensitiveText): Result<SessionId> {
        return checkFailure {
            sessionState.value = VaultSessionState.Unlocking
            
            if (unlockDelayMs > 0) {
                kotlinx.coroutines.delay(unlockDelayMs)
            }
            
            val sessionId = SessionId("session-${Clock.System.now().toEpochMilliseconds()}")
            sessionState.value = VaultSessionState.Unlocked(sessionId)
            Result.success(sessionId)
        }
    }
    
    override suspend fun lock(): Result<Unit> {
        return checkFailure {
            sessionState.value = VaultSessionState.Locked
            Result.success(Unit)
        }
    }
    
    override suspend fun changeMasterPassword(
        currentPassword: SensitiveText,
        newPassword: SensitiveText,
    ): Result<Unit> {
        return checkFailure {
            // Simulate password change
            Result.success(Unit)
        }
    }
    
    override suspend fun getMetadata(): Result<VaultMetadata> {
        return checkFailure {
            currentVaultId?.let { vaultMetadata[it] }?.let { Result.success(it) }
                ?: Result.failure(IllegalStateException("No vault exists"))
        }
    }
    
    override fun getSessionState(): Flow<VaultSessionState> {
        return sessionState.asStateFlow()
    }
    
    /**
     * Simulate session state change.
     */
    fun setSessionState(state: VaultSessionState) {
        sessionState.value = state
    }
    
    /**
     * Get current session state value.
     */
    fun getCurrentSessionState(): VaultSessionState = sessionState.value
    
    private suspend fun <T> checkFailure(block: suspend () -> Result<T>): Result<T> {
        return if (shouldFailNext) {
            shouldFailNext = false
            Result.failure(failWith ?: RuntimeException("Operation failed"))
        } else {
            block()
        }
    }
}

/**
 * Fake folder repository for testing.
 */
class FakeFolderRepository : FolderRepository {
    
    private val folders = mutableMapOf<FolderId, Folder>()
    private var shouldFailNext = false
    
    fun setupFolders(vararg foldersToAdd: Folder) {
        foldersToAdd.forEach { folders[it.id] = it }
    }
    
    fun setShouldFail(shouldFail: Boolean) {
        shouldFailNext = shouldFail
    }
    
    override suspend fun getAll(): Result<List<Folder>> {
        return if (shouldFailNext) {
            shouldFailNext = false
            Result.failure(RuntimeException("Failed to get folders"))
        } else {
            Result.success(folders.values.toList().sortedBy { it.sortOrder })
        }
    }
    
    override suspend fun getById(id: FolderId): Result<Folder?> {
        return Result.success(folders[id])
    }
    
    override suspend fun save(folder: Folder): Result<FolderId> {
        folders[folder.id] = folder
        return Result.success(folder.id)
    }
    
    override suspend fun delete(id: FolderId): Result<Unit> {
        folders.remove(id)
        return Result.success(Unit)
    }
    
    override suspend fun reorder(id: FolderId, newOrder: Int): Result<Unit> {
        folders[id]?.let { folder ->
            folders[id] = folder.copy(sortOrder = newOrder)
        }
        return Result.success(Unit)
    }
}

/**
 * Fake tag repository for testing.
 */
class FakeTagRepository : TagRepository {
    
    private val tags = mutableMapOf<TagId, Tag>()
    private var shouldFailNext = false
    
    fun setupTags(vararg tagsToAdd: Tag) {
        tagsToAdd.forEach { tags[it.id] = it }
    }
    
    fun setShouldFail(shouldFail: Boolean) {
        shouldFailNext = shouldFail
    }
    
    override suspend fun getAll(): Result<List<Tag>> {
        return if (shouldFailNext) {
            shouldFailNext = false
            Result.failure(RuntimeException("Failed to get tags"))
        } else {
            Result.success(tags.values.toList())
        }
    }
    
    override suspend fun getById(id: TagId): Result<Tag?> {
        return Result.success(tags[id])
    }
    
    override suspend fun save(tag: Tag): Result<TagId> {
        tags[tag.id] = tag
        return Result.success(tag.id)
    }
    
    override suspend fun delete(id: TagId): Result<Unit> {
        tags.remove(id)
        return Result.success(Unit)
    }
}
