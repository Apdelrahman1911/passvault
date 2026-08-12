package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.FolderSummaryProjection

/**
 * Encrypted folder persistence used by [com.passvault.core.database.repository.FolderRepositoryImpl].
 */
@Dao
interface FolderDao {
    @Upsert
    suspend fun insertOrUpdate(entity: FolderRecordEntity): Long

    @Query("UPDATE folder_records SET sort_order = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)

    @Transaction
    suspend fun deleteAndMoveCredentialsToRoot(id: String) {
        val folder = getById(id) ?: return
        reparentChildFolders(id, folder.parentId)
        moveCredentialsToRoot(id)
        removeCredentialFolderReferences(id)
        deleteById(id)
    }

    @Query("UPDATE folder_records SET parent_id = :newParentId WHERE parent_id = :deletedId")
    suspend fun reparentChildFolders(deletedId: String, newParentId: String?)

    @Query("UPDATE credential_records SET folder_id = NULL WHERE folder_id = :folderId")
    suspend fun moveCredentialsToRoot(folderId: String)

    @Query("DELETE FROM credential_folder_cross_ref WHERE folder_id = :folderId")
    suspend fun removeCredentialFolderReferences(folderId: String)

    @Query("DELETE FROM folder_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM folder_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): FolderRecordEntity?

    @Query(
        """
        SELECT id, parent_id, encrypted_payload, payload_nonce, icon, sort_order, created_at, updated_at
        FROM folder_records
        ORDER BY sort_order ASC, created_at ASC
        """,
    )
    suspend fun getAllSummaries(): List<FolderSummaryProjection>

    @Query(
        """
        SELECT id, parent_id, encrypted_payload, payload_nonce, icon, sort_order, created_at, updated_at
        FROM folder_records
        WHERE name_hash = :nameHash
        ORDER BY sort_order ASC
        """,
    )
    suspend fun searchByNameHash(nameHash: ByteArray): List<FolderSummaryProjection>

    @Query("SELECT EXISTS(SELECT 1 FROM folder_records WHERE id = :id)")
    suspend fun exists(id: String): Boolean
}
