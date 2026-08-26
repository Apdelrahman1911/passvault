package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.CredentialFolderCrossRef
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.database.entity.VaultMetadataEntity

/**
 * Raw encrypted-record access used only by the backup boundary.
 *
 * Keeping this API in the database module means the backup feature never
 * receives the vault encryption key or decrypted domain objects.
 */
@Dao
@Suppress("TooManyFunctions") // Room requires every step of the atomic replacement on the transaction-owning DAO.
interface VaultBackupDao {
    @Query("SELECT * FROM vault_metadata WHERE id = 1 LIMIT 1")
    suspend fun getVaultMetadata(): VaultMetadataEntity?

    @Query("SELECT * FROM credential_records ORDER BY updated_at DESC")
    suspend fun getCredentials(): List<CredentialRecordEntity>

    @Query("SELECT * FROM folder_records ORDER BY sort_order ASC, created_at ASC")
    suspend fun getFolders(): List<FolderRecordEntity>

    @Query("SELECT * FROM tag_records ORDER BY created_at ASC")
    suspend fun getTags(): List<TagRecordEntity>

    @Query("SELECT * FROM credential_folder_cross_ref")
    suspend fun getCredentialFolderReferences(): List<CredentialFolderCrossRef>

    @Query("SELECT * FROM credential_tag_cross_ref")
    suspend fun getCredentialTagReferences(): List<CredentialTagCrossRef>

    @Query("SELECT * FROM attachment_records ORDER BY created_at ASC")
    suspend fun getAttachments(): List<AttachmentRecordEntity>

    @Query("SELECT storage_path FROM attachment_records")
    suspend fun getAttachmentStoragePaths(): List<String>

    @Query("SELECT * FROM password_history_records ORDER BY changed_at DESC, id DESC")
    suspend fun getPasswordHistory(): List<PasswordHistoryRecordEntity>

    @Query("SELECT COUNT(*) FROM credential_records")
    suspend fun getCredentialCount(): Int

    @Query("SELECT COUNT(*) FROM folder_records")
    suspend fun getFolderCount(): Int

    @Query("SELECT COUNT(*) FROM tag_records")
    suspend fun getTagCount(): Int

    @Query("SELECT COUNT(*) FROM credential_records WHERE folder_id IS NOT NULL")
    suspend fun getCanonicalCredentialFolderReferenceCount(): Int

    @Query("SELECT COUNT(*) FROM credential_tag_cross_ref")
    suspend fun getCredentialTagReferenceCount(): Int

    @Query("SELECT COUNT(*) FROM attachment_records")
    suspend fun getAttachmentCount(): Int

    @Query(
        """
        SELECT COUNT(*) FROM attachment_records
        WHERE storage_state != 'LEGACY' OR content_format_version != 0
        """,
    )
    suspend fun getManagedAttachmentCount(): Int

    @Query("SELECT COUNT(*) FROM password_history_records")
    suspend fun getPasswordHistoryCount(): Int

    @Query("SELECT * FROM credential_records WHERE id > :afterId ORDER BY id LIMIT :limit")
    suspend fun getCredentialPage(afterId: String, limit: Int): List<CredentialRecordEntity>

    @Query("SELECT * FROM folder_records WHERE id > :afterId ORDER BY id LIMIT :limit")
    suspend fun getFolderPage(afterId: String, limit: Int): List<FolderRecordEntity>

    @Query("SELECT * FROM tag_records WHERE id > :afterId ORDER BY id LIMIT :limit")
    suspend fun getTagPage(afterId: String, limit: Int): List<TagRecordEntity>

    @Query(
        """
        SELECT id AS credential_id, folder_id AS folder_id
        FROM credential_records
        WHERE folder_id IS NOT NULL AND id > :afterCredentialId
        ORDER BY id
        LIMIT :limit
        """,
    )
    suspend fun getCanonicalCredentialFolderReferencePage(
        afterCredentialId: String,
        limit: Int,
    ): List<CredentialFolderCrossRef>

    @Query(
        """
        SELECT * FROM credential_tag_cross_ref
        WHERE credential_id > :afterCredentialId
           OR (credential_id = :afterCredentialId AND tag_id > :afterTagId)
        ORDER BY credential_id, tag_id
        LIMIT :limit
        """,
    )
    suspend fun getCredentialTagReferencePage(
        afterCredentialId: String,
        afterTagId: String,
        limit: Int,
    ): List<CredentialTagCrossRef>

    @Query("SELECT * FROM attachment_records WHERE id > :afterId ORDER BY id LIMIT :limit")
    suspend fun getAttachmentPage(afterId: String, limit: Int): List<AttachmentRecordEntity>

    @Query(
        """
        SELECT * FROM attachment_records
        WHERE (storage_state != 'LEGACY' OR content_format_version != 0)
          AND id > :afterId
        ORDER BY id
        LIMIT :limit
        """,
    )
    suspend fun getManagedAttachmentPage(afterId: String, limit: Int): List<AttachmentRecordEntity>

    @Query(
        """
        SELECT * FROM password_history_records
        WHERE credential_id > :afterCredentialId
           OR (credential_id = :afterCredentialId AND id > :afterHistoryId)
        ORDER BY credential_id, id
        LIMIT :limit
        """,
    )
    suspend fun getPasswordHistoryPage(
        afterCredentialId: String,
        afterHistoryId: String,
        limit: Int,
    ): List<PasswordHistoryRecordEntity>

    @Transaction
    suspend fun readSnapshot(): VaultBackupEntities {
        return VaultBackupEntities(
            metadata = getVaultMetadata()
                ?: error("Vault metadata is missing"),
            credentials = getCredentials(),
            folders = getFolders(),
            tags = getTags(),
            credentialFolderReferences = getCredentialFolderReferences(),
            credentialTagReferences = getCredentialTagReferences(),
            attachments = getAttachments(),
            passwordHistory = getPasswordHistory(),
        )
    }

    @Query("DELETE FROM credential_folder_cross_ref")
    suspend fun deleteCredentialFolderReferences()

    @Query("DELETE FROM credential_tag_cross_ref")
    suspend fun deleteCredentialTagReferences()

    @Query("DELETE FROM password_history_records")
    suspend fun deletePasswordHistory()

    @Query("DELETE FROM attachment_records")
    suspend fun deleteAttachments()

    @Query("DELETE FROM credential_records")
    suspend fun deleteCredentials()

    @Query("DELETE FROM folder_records")
    suspend fun deleteFolders()

    @Query("DELETE FROM tag_records")
    suspend fun deleteTags()

    @Query("DELETE FROM vault_metadata")
    suspend fun deleteVaultMetadata()

    @Query("DELETE FROM migration_state")
    suspend fun deleteMigrationState()

    @Query("DELETE FROM current_version_info")
    suspend fun deleteCurrentVersionInfo()

    @Query("DELETE FROM corruption_logs")
    suspend fun deleteCorruptionLogs()

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertVaultMetadata(entity: VaultMetadataEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFolders(entities: List<FolderRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTags(entities: List<TagRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCredentials(entities: List<CredentialRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCredentialFolderReferences(entities: List<CredentialFolderCrossRef>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCredentialTagReferences(entities: List<CredentialTagCrossRef>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAttachments(entities: List<AttachmentRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPasswordHistory(entities: List<PasswordHistoryRecordEntity>)

    @Transaction
    suspend fun replaceVault(snapshot: VaultBackupEntities) {
        // Delete children first even though most relationships cascade. This
        // keeps the operation correct if a future schema tightens FKs.
        deleteCredentialFolderReferences()
        deleteCredentialTagReferences()
        deletePasswordHistory()
        deleteAttachments()
        deleteCredentials()
        deleteFolders()
        deleteTags()
        deleteVaultMetadata()
        deleteMigrationState()
        deleteCurrentVersionInfo()
        deleteCorruptionLogs()

        insertVaultMetadata(snapshot.metadata)
        if (snapshot.folders.isNotEmpty()) insertFolders(snapshot.folders)
        if (snapshot.tags.isNotEmpty()) insertTags(snapshot.tags)
        if (snapshot.credentials.isNotEmpty()) insertCredentials(snapshot.credentials)
        if (snapshot.credentialFolderReferences.isNotEmpty()) {
            insertCredentialFolderReferences(snapshot.credentialFolderReferences)
        }
        if (snapshot.credentialTagReferences.isNotEmpty()) {
            insertCredentialTagReferences(snapshot.credentialTagReferences)
        }
        if (snapshot.attachments.isNotEmpty()) insertAttachments(snapshot.attachments)
        if (snapshot.passwordHistory.isNotEmpty()) insertPasswordHistory(snapshot.passwordHistory)
    }
}

data class VaultBackupEntities(
    val metadata: VaultMetadataEntity,
    val credentials: List<CredentialRecordEntity>,
    val folders: List<FolderRecordEntity>,
    val tags: List<TagRecordEntity>,
    val credentialFolderReferences: List<CredentialFolderCrossRef>,
    val credentialTagReferences: List<CredentialTagCrossRef>,
    val attachments: List<AttachmentRecordEntity>,
    val passwordHistory: List<PasswordHistoryRecordEntity>,
)
