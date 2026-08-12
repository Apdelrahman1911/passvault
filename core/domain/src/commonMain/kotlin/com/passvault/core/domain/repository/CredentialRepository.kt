package com.passvault.core.domain.repository

import com.passvault.core.domain.model.Credential
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialSummary
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.Folder
import com.passvault.core.domain.model.FolderId
import com.passvault.core.domain.model.PasswordHealth
import com.passvault.core.domain.model.SensitiveText
import com.passvault.core.domain.model.SessionId
import com.passvault.core.domain.model.Tag
import com.passvault.core.domain.model.TagId
import com.passvault.core.domain.model.VaultId
import com.passvault.core.domain.model.VaultMetadata
import com.passvault.core.domain.model.VaultSessionState
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
     * Get all credentials that need health analysis.
     */
    suspend fun getCredentialsForHealthAnalysis(): Result<List<CredentialHealthInput>>

    /**
     * Update credential health.
     */
    suspend fun updateHealth(id: CredentialId, health: PasswordHealth): Result<Unit>
}

/**
 * Minimum decrypted view required by the local password-health scan.
 * Unrelated notes, TOTP seeds, custom secrets, recovery codes, API keys,
 * attachments, and historical password values never cross this boundary.
 */
data class CredentialHealthInput(
    val id: CredentialId,
    val type: CredentialType,
    val title: String,
    val username: SensitiveText?,
    val email: SensitiveText?,
    val password: SensitiveText?,
    val isFavorite: Boolean,
    val folderId: FolderId?,
    val tagIds: Set<TagId>,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastUsedAt: Instant?,
    val passwordHealth: PasswordHealth,
    val passwordChangedAt: Instant?,
)

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
     * Lock vault and revoke the in-memory session.
     *
     * Every non-cancelled return, including a failure result caused by
     * best-effort cleanup, must leave the repository in a terminal
     * [VaultSessionState.Locked] state with no usable vault key. The result
     * reports cleanup health; it is not authorization to retain the session.
     */
    suspend fun lock(reason: LockReason = LockReason.Manual): Result<Unit>

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
    data object MemoryPressure : LockReason
    data object Restore : LockReason
}
