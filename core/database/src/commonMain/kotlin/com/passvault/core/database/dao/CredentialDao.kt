package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.passvault.core.database.entity.CredentialFolderCrossRef
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialSummaryProjection
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.entity.PasswordHistoryRecordEntity

/**
 * Room DAO for the repository's encrypted credential transactions.
 * Query groups are split by responsibility so the supported persistence API
 * remains explicit without one unreviewable grab-bag interface.
 */
@Dao
interface CredentialDao :
    CredentialReadQueries,
    CredentialMutationQueries,
    CredentialRelationQueries,
    CredentialHistoryQueries {

    @Transaction
    suspend fun updateFolderAndCrossReference(id: String, folderId: String?) {
        updateFolder(id, folderId)
        replaceFolderForCredential(id, folderId)
    }

    @Transaction
    suspend fun deleteCredentialAndRefreshCount(id: String) {
        deleteById(id)
        refreshVaultEntryCount()
    }

    @Transaction
    suspend fun replaceTagsForCredential(credentialId: String, tagIds: List<String>) {
        removeAllTagsFromCredential(credentialId)
        addTagCrossRefs(
            tagIds.map { tagId ->
                CredentialTagCrossRef(credentialId = credentialId, tagId = tagId)
            },
        )
    }

    @Transaction
    suspend fun replaceFolderForCredential(credentialId: String, folderId: String?) {
        removeFolderCrossRef(credentialId)
        folderId?.let { id ->
            addFolderCrossRef(
                CredentialFolderCrossRef(
                    credentialId = credentialId,
                    folderId = id,
                ),
            )
        }
    }

    @Transaction
    suspend fun updateCredentialWithTagsAndHistory(
        entity: CredentialRecordEntity,
        tagIds: List<String>,
        history: PasswordHistoryRecordEntity?,
        requiredVaultFormatVersion: Int? = null,
    ) {
        insertOrUpdate(entity)
        replaceTagsForCredential(entity.id, tagIds)
        replaceFolderForCredential(entity.id, entity.folderId)
        history?.let { record ->
            insertPasswordHistory(record)
            trimPasswordHistory(entity.id, PASSWORD_HISTORY_LIMIT)
        }
        refreshVaultEntryCount()
        requiredVaultFormatVersion?.let { version -> markVaultFormatVersion(version) }
    }

    private companion object {
        const val PASSWORD_HISTORY_LIMIT = 10
    }
}

interface CredentialReadQueries {
    @Query("SELECT * FROM credential_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CredentialRecordEntity?

    @Query(
        """
        SELECT id, type, folder_id, is_favorite,
               summary_payload, summary_nonce,
               created_at, updated_at, last_used_at
        FROM credential_records
        ORDER BY updated_at DESC
        """,
    )
    suspend fun getAllSummaries(): List<CredentialSummaryProjection>

    @Query("SELECT EXISTS(SELECT 1 FROM credential_records WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT * FROM credential_records WHERE type = 'Login' ORDER BY updated_at ASC")
    suspend fun getLoginsForHealthAnalysis(): List<CredentialRecordEntity>

    @Query("SELECT * FROM credential_records WHERE type = 'Login' ORDER BY id ASC")
    suspend fun getLoginsForTotpDisplay(): List<CredentialRecordEntity>

    @Query(
        """
        SELECT c.* FROM credential_records c
        INNER JOIN credential_folder_cross_ref cfr ON c.id = cfr.credential_id
        WHERE cfr.folder_id = :folderId
        """,
    )
    suspend fun getByFolderCrossRef(folderId: String): List<CredentialRecordEntity>
}

interface CredentialMutationQueries {
    @Upsert
    suspend fun insertOrUpdate(entity: CredentialRecordEntity): Long

    @Update
    suspend fun update(entity: CredentialRecordEntity)

    @Query("UPDATE credential_records SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE credential_records SET folder_id = :folderId WHERE id = :id")
    suspend fun updateFolder(id: String, folderId: String?)

    @Query("UPDATE credential_records SET last_used_at = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: String, timestamp: Long)

    @Query(
        """
        UPDATE credential_records
        SET summary_payload = :summaryPayload, summary_nonce = :summaryNonce
        WHERE id = :id
        """,
    )
    suspend fun updateEncryptedSummary(
        id: String,
        summaryPayload: ByteArray,
        summaryNonce: ByteArray,
    )

    @Query("DELETE FROM credential_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query(
        """
        UPDATE vault_metadata
        SET entry_count = (SELECT COUNT(*) FROM credential_records)
        WHERE id = 1
        """,
    )
    suspend fun refreshVaultEntryCount()

    @Query(
        """
        UPDATE vault_metadata
        SET vault_format_version = CASE
            WHEN vault_format_version < :version THEN :version
            ELSE vault_format_version
        END
        WHERE id = 1
        """,
    )
    suspend fun markVaultFormatVersion(version: Int)
}

interface CredentialRelationQueries {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagCrossRef(crossRef: CredentialTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagCrossRefs(crossRefs: List<CredentialTagCrossRef>)

    @Query("DELETE FROM credential_tag_cross_ref WHERE credential_id = :credentialId AND tag_id = :tagId")
    suspend fun removeTagCrossRef(credentialId: String, tagId: String)

    @Query("DELETE FROM credential_tag_cross_ref WHERE credential_id = :credentialId")
    suspend fun removeAllTagsFromCredential(credentialId: String)

    @Query("SELECT * FROM credential_tag_cross_ref WHERE credential_id = :credentialId")
    suspend fun getTagCrossRefsForCredential(credentialId: String): List<CredentialTagCrossRef>

    @Query("SELECT * FROM credential_tag_cross_ref WHERE credential_id IN (:credentialIds)")
    suspend fun getTagCrossRefsForCredentials(credentialIds: List<String>): List<CredentialTagCrossRef>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFolderCrossRef(crossRef: CredentialFolderCrossRef)

    @Query("DELETE FROM credential_folder_cross_ref WHERE credential_id = :credentialId")
    suspend fun removeFolderCrossRef(credentialId: String)
}

interface CredentialHistoryQueries {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPasswordHistory(entity: PasswordHistoryRecordEntity): Long

    @Query(
        """
        DELETE FROM password_history_records
        WHERE credential_id = :credentialId
        AND id NOT IN (
            SELECT id FROM password_history_records
            WHERE credential_id = :credentialId
            ORDER BY changed_at DESC, id DESC
            LIMIT :keepCount
        )
        """,
    )
    suspend fun trimPasswordHistory(credentialId: String, keepCount: Int)
}
