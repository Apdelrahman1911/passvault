package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.passvault.core.database.entity.CredentialFolderCrossRef
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialSummaryProjection
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for credential records.
 * Provides CRUD operations and search functionality.
 */
@Dao
interface CredentialDao {

    // ==================== Insert Operations ====================

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: CredentialRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: CredentialRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(entities: List<CredentialRecordEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<CredentialRecordEntity>): List<Long>

    // ==================== Update Operations ====================

    @Update
    suspend fun update(entity: CredentialRecordEntity)

    @Update
    suspend fun updateAll(entities: List<CredentialRecordEntity>)

    @Query("UPDATE credential_records SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE credential_records SET folder_id = :folderId WHERE id = :id")
    suspend fun updateFolder(id: String, folderId: String?)

    @Transaction
    suspend fun updateFolderAndCrossReference(id: String, folderId: String?) {
        updateFolder(id, folderId)
        replaceFolderForCredential(id, folderId)
    }

    @Query("UPDATE credential_records SET last_used_at = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: String, timestamp: Long)

    // ==================== Delete Operations ====================

    @Delete
    suspend fun delete(entity: CredentialRecordEntity)

    @Query("DELETE FROM credential_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    suspend fun deleteCredentialAndRefreshCount(id: String) {
        deleteById(id)
        refreshVaultEntryCount()
    }

    @Query("DELETE FROM credential_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM credential_records")
    suspend fun deleteAll()

    // ==================== Query Operations - Single ====================

    @Query("SELECT * FROM credential_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CredentialRecordEntity?

    @Query("SELECT * FROM credential_records WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<CredentialRecordEntity?>

    // ==================== Query Operations - Summary Projection ====================

    @Query("""
        SELECT id, type, folder_id, is_favorite, 
               summary_payload, summary_nonce, 
               created_at, updated_at, last_used_at 
        FROM credential_records 
        ORDER BY updated_at DESC
    """)
    suspend fun getAllSummaries(): List<CredentialSummaryProjection>

    @Query("""
        SELECT id, type, folder_id, is_favorite, 
               summary_payload, summary_nonce, 
               created_at, updated_at, last_used_at 
        FROM credential_records 
        ORDER BY updated_at DESC
    """)
    fun observeAllSummaries(): Flow<List<CredentialSummaryProjection>>

    @Query("""
        SELECT id, type, folder_id, is_favorite, 
               summary_payload, summary_nonce, 
               created_at, updated_at, last_used_at 
        FROM credential_records 
        WHERE folder_id = :folderId 
        ORDER BY updated_at DESC
    """)
    suspend fun getSummariesByFolder(folderId: String): List<CredentialSummaryProjection>

    @Query("""
        SELECT id, type, folder_id, is_favorite, 
               summary_payload, summary_nonce, 
               created_at, updated_at, last_used_at 
        FROM credential_records 
        WHERE folder_id = :folderId 
        ORDER BY updated_at DESC
    """)
    fun observeSummariesByFolder(folderId: String): Flow<List<CredentialSummaryProjection>>

    @Query("""
        SELECT c.id, c.type, c.folder_id, c.is_favorite, 
               c.summary_payload, c.summary_nonce, 
               c.created_at, c.updated_at, c.last_used_at 
        FROM credential_records c
        INNER JOIN credential_tag_cross_ref ctr ON c.id = ctr.credential_id
        WHERE ctr.tag_id = :tagId
        ORDER BY c.updated_at DESC
    """)
    suspend fun getSummariesByTag(tagId: String): List<CredentialSummaryProjection>

    @Query("""
        SELECT c.id, c.type, c.folder_id, c.is_favorite, 
               c.summary_payload, c.summary_nonce, 
               c.created_at, c.updated_at, c.last_used_at 
        FROM credential_records c
        INNER JOIN credential_tag_cross_ref ctr ON c.id = ctr.credential_id
        WHERE ctr.tag_id = :tagId
        ORDER BY c.updated_at DESC
    """)
    fun observeSummariesByTag(tagId: String): Flow<List<CredentialSummaryProjection>>

    @Query("""
        SELECT id, type, folder_id, is_favorite, 
               summary_payload, summary_nonce, 
               created_at, updated_at, last_used_at 
        FROM credential_records 
        WHERE is_favorite = 1
        ORDER BY updated_at DESC
    """)
    suspend fun getFavoriteSummaries(): List<CredentialSummaryProjection>

    @Query("""
        SELECT id, type, folder_id, is_favorite, 
               summary_payload, summary_nonce, 
               created_at, updated_at, last_used_at 
        FROM credential_records 
        WHERE is_favorite = 1
        ORDER BY updated_at DESC
    """)
    fun observeFavoriteSummaries(): Flow<List<CredentialSummaryProjection>>

    // ==================== Search by Title Hash ====================

    /**
     * Search credentials by title hash.
     * The hash parameter is the repository's keyed BLAKE2b blind index.
     */
    @Query("""
        SELECT id, type, folder_id, is_favorite, 
               summary_payload, summary_nonce, 
               created_at, updated_at, last_used_at 
        FROM credential_records 
        WHERE title_hash = :titleHash
        ORDER BY updated_at DESC
    """)
    suspend fun searchByTitleHash(titleHash: ByteArray): List<CredentialSummaryProjection>

    @Query("""
        SELECT id, type, folder_id, is_favorite, 
               summary_payload, summary_nonce, 
               created_at, updated_at, last_used_at 
        FROM credential_records 
        WHERE title_hash = :titleHash
        ORDER BY updated_at DESC
    """)
    fun observeByTitleHash(titleHash: ByteArray): Flow<List<CredentialSummaryProjection>>

    // ==================== Full Records ====================

    @Query("SELECT * FROM credential_records ORDER BY updated_at DESC")
    suspend fun getAll(): List<CredentialRecordEntity>

    @Query("SELECT * FROM credential_records ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<CredentialRecordEntity>>

    @Query("SELECT * FROM credential_records WHERE folder_id = :folderId ORDER BY updated_at DESC")
    suspend fun getByFolder(folderId: String): List<CredentialRecordEntity>

    @Query("SELECT * FROM credential_records WHERE folder_id = :folderId ORDER BY updated_at DESC")
    fun observeByFolder(folderId: String): Flow<List<CredentialRecordEntity>>

    // ==================== Cross-Reference Operations ====================

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

    @Query("DELETE FROM credential_folder_cross_ref WHERE credential_id = :credentialId")
    suspend fun removeAllFoldersFromCredential(credentialId: String)

    // ==================== Count Operations ====================

    @Query("SELECT COUNT(*) FROM credential_records")
    suspend fun getCount(): Int

    @Query("""
        UPDATE vault_metadata
        SET entry_count = (SELECT COUNT(*) FROM credential_records)
        WHERE id = 1
    """)
    suspend fun refreshVaultEntryCount()

    @Query("SELECT COUNT(*) FROM credential_records WHERE folder_id = :folderId")
    suspend fun getCountByFolder(folderId: String): Int

    @Query("SELECT COUNT(*) FROM credential_records WHERE is_favorite = 1")
    suspend fun getFavoriteCount(): Int

    @Query("SELECT COUNT(*) FROM credential_records")
    fun observeCount(): Flow<Int>

    // ==================== Exists Check ====================

    @Query("SELECT EXISTS(SELECT 1 FROM credential_records WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    // ==================== Transaction Operations ====================

    @Transaction
    @Query("""
        SELECT c.id, c.type, c.folder_id, c.is_favorite, 
               c.summary_payload, c.summary_nonce, 
               c.created_at, c.updated_at, c.last_used_at 
        FROM credential_records c
        INNER JOIN credential_tag_cross_ref ctr ON c.id = ctr.credential_id
        WHERE ctr.tag_id IN (:tagIds)
        GROUP BY c.id
        HAVING COUNT(DISTINCT ctr.tag_id) = :tagCount
        ORDER BY c.updated_at DESC
    """)
    suspend fun getSummariesByAllTags(tagIds: List<String>, tagCount: Int): List<CredentialSummaryProjection>

    @Transaction
    @Query("""
        SELECT c.id, c.type, c.folder_id, c.is_favorite, 
               c.summary_payload, c.summary_nonce, 
               c.created_at, c.updated_at, c.last_used_at 
        FROM credential_records c
        INNER JOIN credential_tag_cross_ref ctr ON c.id = ctr.credential_id
        WHERE ctr.tag_id IN (:tagIds)
        GROUP BY c.id
        HAVING COUNT(DISTINCT ctr.tag_id) = :tagCount
        ORDER BY c.updated_at DESC
    """)
    fun observeSummariesByAllTags(tagIds: List<String>, tagCount: Int): Flow<List<CredentialSummaryProjection>>

    @Transaction
    suspend fun replaceTagsForCredential(credentialId: String, tagIds: List<String>) {
        removeAllTagsFromCredential(credentialId)
        val crossRefs = tagIds.map { tagId ->
            CredentialTagCrossRef(credentialId = credentialId, tagId = tagId)
        }
        addTagCrossRefs(crossRefs)
    }

    @Transaction
    suspend fun replaceFolderForCredential(credentialId: String, folderId: String?) {
        removeFolderCrossRef(credentialId)
        if (folderId != null) {
            addFolderCrossRef(
                CredentialFolderCrossRef(
                    credentialId = credentialId,
                    folderId = folderId,
                ),
            )
        }
    }

    @Transaction
    suspend fun updateCredentialWithTags(
        entity: CredentialRecordEntity,
        tagIds: List<String>
    ) {
        insertOrUpdate(entity)
        replaceTagsForCredential(entity.id, tagIds)
        replaceFolderForCredential(entity.id, entity.folderId)
    }

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPasswordHistory(entity: PasswordHistoryRecordEntity): Long

    @Query("""
        DELETE FROM password_history_records
        WHERE credential_id = :credentialId
        AND id NOT IN (
            SELECT id FROM password_history_records
            WHERE credential_id = :credentialId
            ORDER BY changed_at DESC
            LIMIT :keepCount
        )
    """)
    suspend fun trimPasswordHistory(credentialId: String, keepCount: Int)

    @Transaction
    suspend fun updateCredentialWithTagsAndHistory(
        entity: CredentialRecordEntity,
        tagIds: List<String>,
        history: PasswordHistoryRecordEntity?,
    ) {
        insertOrUpdate(entity)
        replaceTagsForCredential(entity.id, tagIds)
        replaceFolderForCredential(entity.id, entity.folderId)
        if (history != null) {
            insertPasswordHistory(history)
            trimPasswordHistory(entity.id, 10)
        }
        refreshVaultEntryCount()
    }

    // ==================== Health Analysis ====================

    @Query("SELECT * FROM credential_records WHERE type = 'Login' ORDER BY updated_at ASC")
    suspend fun getLoginsForHealthAnalysis(): List<CredentialRecordEntity>

    @Query("""
        SELECT c.* FROM credential_records c
        INNER JOIN credential_tag_cross_ref ctr ON c.id = ctr.credential_id
        WHERE ctr.tag_id = :tagId AND c.is_favorite = :isFavorite
    """)
    suspend fun getByTagAndFavorite(tagId: String, isFavorite: Boolean): List<CredentialRecordEntity>

    @Query("""
        SELECT c.* FROM credential_records c
        INNER JOIN credential_folder_cross_ref cfr ON c.id = cfr.credential_id
        WHERE cfr.folder_id = :folderId
    """)
    suspend fun getByFolderCrossRef(folderId: String): List<CredentialRecordEntity>
}
