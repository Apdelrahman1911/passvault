package com.passvault.core.domain.repository

import com.passvault.core.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

interface CredentialRepository {
    /**
     * Get all credential summaries (decrypted).
     */
    suspend fun getAllSummaries(): Result<List<CredentialSummary.Decrypted>>

    /**
     * Get credential by ID (decrypted).
     */
    suspend fun getById(id: CredentialId): Result<Credential?>

    /**
     * Save a credential (creates or updates).
     */
    suspend fun save(credential: Credential): Result<CredentialId>

    /**
     * Delete a credential.
     */
    suspend fun delete(id: CredentialId): Result<Unit>

    /**
     * Update favorite status.
     */
    suspend fun updateFavorite(id: CredentialId, isFavorite: Boolean): Result<Unit>

    /**
     * Move credential to folder.
     */
    suspend fun moveToFolder(id: CredentialId, folderId: FolderId?): Result<Unit>

    /**
     * Add tag to credential.
     */
    suspend fun addTag(id: CredentialId, tagId: TagId): Result<Unit>

    /**
     * Remove tag from credential.
     */
    suspend fun removeTag(id: CredentialId, tagId: TagId): Result<Unit>

    /**
     * Record that credential was used.
     */
    suspend fun recordUsage(id: CredentialId, timestamp: Instant): Result<Unit>

    /**
     * Add password to history.
     */
    suspend fun addPasswordHistory(id: CredentialId, password: SensitiveText): Result<Unit>

    /**
     * Get all credentials that need health analysis.
     */
    suspend fun getCredentialsForHealthAnalysis(): Result<List<Credential>>

    /**
     * Update credential health.
     */
    suspend fun updateHealth(id: CredentialId, health: PasswordHealth): Result<Unit>
}

interface FolderRepository {
    suspend fun getAll(): Result<List<Folder>>
    suspend fun getById(id: FolderId): Result<Folder?>
    suspend fun save(folder: Folder): Result<FolderId>
    suspend fun delete(id: FolderId): Result<Unit>
    suspend fun reorder(id: FolderId, newOrder: Int): Result<Unit>
}

interface TagRepository {
    suspend fun getAll(): Result<List<Tag>>
    suspend fun getById(id: TagId): Result<Tag?>
    suspend fun save(tag: Tag): Result<TagId>
    suspend fun delete(id: TagId): Result<Unit>
}

interface VaultRepository {
    /**
     * Check if vault exists.
     */
    suspend fun exists(): Result<Boolean>

    /**
     * Create new vault.
     */
    suspend fun create(masterPassword: SensitiveText): Result<VaultId>

    /**
     * Unlock vault.
     */
    suspend fun unlock(masterPassword: SensitiveText): Result<SessionId>

    /**
     * Lock vault.
     */
    suspend fun lock(): Result<Unit>

    /**
     * Change master password.
     */
    suspend fun changeMasterPassword(
        currentPassword: SensitiveText,
        newPassword: SensitiveText
    ): Result<Unit>

    /**
     * Get vault metadata.
     */
    suspend fun getMetadata(): Result<VaultMetadata>

    /**
     * Get current session state.
     */
    fun getSessionState(): Flow<VaultSessionState>
}

sealed interface LockReason {
    data object Manual : LockReason
    data object AutoLock : LockReason
    data object Background : LockReason
    data object DesktopFocusLost : LockReason
    data object SystemSuspend : LockReason
}
