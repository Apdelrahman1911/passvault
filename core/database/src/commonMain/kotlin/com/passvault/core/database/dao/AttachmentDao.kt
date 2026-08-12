package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passvault.core.database.entity.AttachmentRecordEntity

/**
 * Minimal attachment-metadata boundary used by the credential repository.
 * Encrypted backup replacement uses [VaultBackupDao] instead of exposing
 * broad attachment mutation APIs to application code.
 */
@Dao
interface AttachmentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: AttachmentRecordEntity): Long

    @Update
    suspend fun update(entity: AttachmentRecordEntity)

    @Query(
        """
        SELECT * FROM attachment_records
        WHERE credential_id = :credentialId
          AND storage_state IN ('READY', 'LEGACY')
        ORDER BY created_at ASC, id ASC
        """,
    )
    suspend fun getByCredential(credentialId: String): List<AttachmentRecordEntity>

    @Query("SELECT * FROM attachment_records WHERE id = :id AND credential_id = :credentialId LIMIT 1")
    suspend fun getById(id: String, credentialId: String): AttachmentRecordEntity?

    @Query("SELECT * FROM attachment_records WHERE storage_state IN ('STAGING', 'DELETING')")
    suspend fun getPendingOperations(): List<AttachmentRecordEntity>

    @Query(
        """
        SELECT COUNT(*) FROM attachment_records
        WHERE credential_id = :credentialId
          AND storage_state IN ('READY', 'STAGING')
        """,
    )
    suspend fun getManagedCount(credentialId: String): Int

    @Query(
        """
        SELECT COALESCE(SUM(size_bytes), 0) FROM attachment_records
        WHERE credential_id = :credentialId
          AND storage_state IN ('READY', 'STAGING')
        """,
    )
    suspend fun getManagedSizeBytes(credentialId: String): Long

    @Query("UPDATE attachment_records SET storage_state = :state WHERE id = :id")
    suspend fun updateStorageState(id: String, state: String)

    @Query("DELETE FROM attachment_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT storage_path FROM attachment_records WHERE storage_state = 'READY'")
    suspend fun getReadyStoragePaths(): List<String>
}
