package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.FolderSummaryProjection
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for folder records.
 * Provides CRUD operations with hierarchical support.
 */
@Dao
interface FolderDao {

    // ==================== Insert Operations ====================

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: FolderRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: FolderRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<FolderRecordEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(entities: List<FolderRecordEntity>): List<Long>

    // ==================== Update Operations ====================

    @Update
    suspend fun update(entity: FolderRecordEntity)

    @Update
    suspend fun updateAll(entities: List<FolderRecordEntity>)

    @Query("UPDATE folder_records SET parent_id = :parentId WHERE id = :id")
    suspend fun updateParent(id: String, parentId: String?)

    @Query("UPDATE folder_records SET sort_order = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)

    /**
     * Update sort order for multiple folders.
     * Should be called within a transaction.
     */
    @Query("UPDATE folder_records SET sort_order = :sortOrder WHERE id = :id")
    suspend fun updateSortOrderInternal(id: String, sortOrder: Int)

    // ==================== Delete Operations ====================

    @Delete
    suspend fun delete(entity: FolderRecordEntity)

    @Query("DELETE FROM folder_records WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Reparent credentials and remove a folder as one database transaction.
     * The credential table keeps the canonical folder relationship.
     */
    @Transaction
    suspend fun deleteAndMoveCredentialsToRoot(id: String) {
        moveCredentialsToRoot(id)
        removeCredentialFolderReferences(id)
        deleteById(id)
    }

    @Query("UPDATE credential_records SET folder_id = NULL WHERE folder_id = :folderId")
    suspend fun moveCredentialsToRoot(folderId: String)

    @Query("DELETE FROM credential_folder_cross_ref WHERE folder_id = :folderId")
    suspend fun removeCredentialFolderReferences(folderId: String)

    @Query("DELETE FROM folder_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM folder_records")
    suspend fun deleteAll()

    // ==================== Query Operations - Single ====================

    @Query("SELECT * FROM folder_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FolderRecordEntity?

    @Query("SELECT * FROM folder_records WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<FolderRecordEntity?>

    // ==================== Query Operations - Summary Projection ====================

    @Query("""
        SELECT id, parent_id, encrypted_payload, payload_nonce, icon, sort_order, created_at, updated_at 
        FROM folder_records 
        ORDER BY sort_order ASC, created_at ASC
    """)
    suspend fun getAllSummaries(): List<FolderSummaryProjection>

    @Query("""
        SELECT id, parent_id, encrypted_payload, payload_nonce, icon, sort_order, created_at, updated_at 
        FROM folder_records 
        ORDER BY sort_order ASC, created_at ASC
    """)
    fun observeAllSummaries(): Flow<List<FolderSummaryProjection>>

    @Query("""
        SELECT id, parent_id, encrypted_payload, payload_nonce, icon, sort_order, created_at, updated_at 
        FROM folder_records 
        WHERE parent_id = :parentId OR (parent_id IS NULL AND :parentId IS NULL)
        ORDER BY sort_order ASC, created_at ASC
    """)
    suspend fun getSummariesByParent(parentId: String?): List<FolderSummaryProjection>

    @Query("""
        SELECT id, parent_id, encrypted_payload, payload_nonce, icon, sort_order, created_at, updated_at 
        FROM folder_records 
        WHERE parent_id = :parentId OR (parent_id IS NULL AND :parentId IS NULL)
        ORDER BY sort_order ASC, created_at ASC
    """)
    fun observeSummariesByParent(parentId: String?): Flow<List<FolderSummaryProjection>>

    @Query("""
        SELECT id, parent_id, encrypted_payload, payload_nonce, icon, sort_order, created_at, updated_at 
        FROM folder_records 
        WHERE parent_id IS NULL
        ORDER BY sort_order ASC, created_at ASC
    """)
    suspend fun getRootSummaries(): List<FolderSummaryProjection>

    @Query("""
        SELECT id, parent_id, encrypted_payload, payload_nonce, icon, sort_order, created_at, updated_at 
        FROM folder_records 
        WHERE parent_id IS NULL
        ORDER BY sort_order ASC, created_at ASC
    """)
    fun observeRootSummaries(): Flow<List<FolderSummaryProjection>>

    // ==================== Full Records ====================

    @Query("SELECT * FROM folder_records ORDER BY sort_order ASC, created_at ASC")
    suspend fun getAll(): List<FolderRecordEntity>

    @Query("SELECT * FROM folder_records ORDER BY sort_order ASC, created_at ASC")
    fun observeAll(): Flow<List<FolderRecordEntity>>

    @Query("SELECT * FROM folder_records WHERE parent_id = :parentId OR (parent_id IS NULL AND :parentId IS NULL) ORDER BY sort_order ASC, created_at ASC")
    suspend fun getByParent(parentId: String?): List<FolderRecordEntity>

    @Query("SELECT * FROM folder_records WHERE parent_id = :parentId OR (parent_id IS NULL AND :parentId IS NULL) ORDER BY sort_order ASC, created_at ASC")
    fun observeByParent(parentId: String?): Flow<List<FolderRecordEntity>>

    @Query("SELECT * FROM folder_records WHERE parent_id IS NULL ORDER BY sort_order ASC, created_at ASC")
    suspend fun getRoot(): List<FolderRecordEntity>

    @Query("SELECT * FROM folder_records WHERE parent_id IS NULL ORDER BY sort_order ASC, created_at ASC")
    fun observeRoot(): Flow<List<FolderRecordEntity>>

    // ==================== Search ====================

    /**
     * Search folders by name hash.
     * The hash parameter is the repository's keyed BLAKE2b blind index.
     */
    @Query("""
        SELECT id, parent_id, encrypted_payload, payload_nonce, icon, sort_order, created_at, updated_at 
        FROM folder_records 
        WHERE name_hash = :nameHash
        ORDER BY sort_order ASC
    """)
    suspend fun searchByNameHash(nameHash: ByteArray): List<FolderSummaryProjection>

    @Query("""
        SELECT id, parent_id, encrypted_payload, payload_nonce, icon, sort_order, created_at, updated_at 
        FROM folder_records 
        WHERE name_hash = :nameHash
        ORDER BY sort_order ASC
    """)
    fun observeByNameHash(nameHash: ByteArray): Flow<List<FolderSummaryProjection>>

    // ==================== Count Operations ====================

    @Query("SELECT COUNT(*) FROM folder_records")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM folder_records WHERE parent_id = :parentId OR (parent_id IS NULL AND :parentId IS NULL)")
    suspend fun getCountByParent(parentId: String?): Int

    @Query("SELECT COUNT(*) FROM folder_records WHERE parent_id IS NULL")
    suspend fun getRootCount(): Int

    @Query("SELECT COUNT(*) FROM folder_records")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM folder_records WHERE parent_id = :parentId")
    suspend fun getChildCount(parentId: String): Int

    // ==================== Exists Check ====================

    @Query("SELECT EXISTS(SELECT 1 FROM folder_records WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM folder_records WHERE parent_id = :parentId)")
    suspend fun hasChildren(parentId: String): Boolean

    // ==================== Reorder Operations ====================

    @Transaction
    suspend fun reorderFolders(folderIds: List<String>) {
        folderIds.forEachIndexed { index, id ->
            updateSortOrderInternal(id, index)
        }
    }

    @Query("""
        SELECT id, parent_id, encrypted_payload, payload_nonce, icon, sort_order, created_at, updated_at 
        FROM folder_records 
        WHERE parent_id IS NULL
        ORDER BY sort_order ASC
    """)
    suspend fun getRootFoldersOrdered(): List<FolderSummaryProjection>

    // ==================== Get Max Sort Order ====================

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM folder_records WHERE parent_id = :parentId")
    suspend fun getMaxSortOrder(parentId: String?): Int

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM folder_records WHERE parent_id IS NULL")
    suspend fun getRootMaxSortOrder(): Int

    // ==================== Recursive Operations ====================

    /**
     * Get all descendant folder IDs recursively.
     * Note: Room doesn't support recursive CTEs directly,
     * so this returns direct children only.
     * Full recursive fetching must be done at repository level.
     */
    @Query("SELECT id FROM folder_records WHERE parent_id = :parentId")
    suspend fun getDirectChildrenIds(parentId: String): List<String>

    /**
     * Get all folder IDs in a subtree.
     * Returns direct children only; recursive expansion at repository level.
     */
    @Query("SELECT id FROM folder_records WHERE parent_id = :parentId")
    fun observeDirectChildrenIds(parentId: String): Flow<List<String>>

    @Query("SELECT * FROM folder_records WHERE parent_id IN (:parentIds)")
    suspend fun getByParentIds(parentIds: List<String>): List<FolderRecordEntity>
}
