package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.passvault.core.database.entity.AttachmentRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for attachment records.
 * Manages attachment metadata for credentials.
 */
@Dao
interface AttachmentDao {

    // ==================== Insert Operations ====================

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: AttachmentRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: AttachmentRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<AttachmentRecordEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(entities: List<AttachmentRecordEntity>): List<Long>

    // ==================== Update Operations ====================

    @Update
    suspend fun update(entity: AttachmentRecordEntity)

    @Update
    suspend fun updateAll(entities: List<AttachmentRecordEntity>)

    // ==================== Delete Operations ====================

    @Delete
    suspend fun delete(entity: AttachmentRecordEntity)

    @Query("DELETE FROM attachment_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM attachment_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM attachment_records WHERE credential_id = :credentialId")
    suspend fun deleteByCredential(credentialId: String)

    @Query("DELETE FROM attachment_records")
    suspend fun deleteAll()

    // ==================== Query Operations - Single ====================

    @Query("SELECT * FROM attachment_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AttachmentRecordEntity?

    @Query("SELECT * FROM attachment_records WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<AttachmentRecordEntity?>

    // ==================== Query Operations - By Credential ====================

    @Query("SELECT * FROM attachment_records WHERE credential_id = :credentialId ORDER BY created_at ASC")
    suspend fun getByCredential(credentialId: String): List<AttachmentRecordEntity>

    @Query("SELECT * FROM attachment_records WHERE credential_id = :credentialId ORDER BY created_at ASC")
    fun observeByCredential(credentialId: String): Flow<List<AttachmentRecordEntity>>

    // ==================== Query Operations - All ====================

    @Query("SELECT * FROM attachment_records ORDER BY created_at DESC")
    suspend fun getAll(): List<AttachmentRecordEntity>

    @Query("SELECT * FROM attachment_records ORDER BY created_at DESC")
    fun observeAll(): Flow<List<AttachmentRecordEntity>>

    // ==================== Storage Path Operations ====================

    @Query("SELECT storage_path FROM attachment_records WHERE id = :id")
    suspend fun getStoragePath(id: String): String?

    @Query("SELECT storage_path FROM attachment_records WHERE credential_id = :credentialId")
    suspend fun getStoragePathsByCredential(credentialId: String): List<String>

    @Query("SELECT storage_path FROM attachment_records WHERE credential_id = :credentialId")
    fun observeStoragePathsByCredential(credentialId: String): Flow<List<String>>

    @Query("SELECT storage_path FROM attachment_records")
    suspend fun getAllStoragePaths(): List<String>

    // ==================== Size Operations ====================

    @Query("SELECT size_bytes FROM attachment_records WHERE id = :id")
    suspend fun getSize(id: String): Long?

    @Query("SELECT SUM(size_bytes) FROM attachment_records WHERE credential_id = :credentialId")
    suspend fun getTotalSizeByCredential(credentialId: String): Long?

    @Query("SELECT SUM(size_bytes) FROM attachment_records")
    suspend fun getTotalSize(): Long?

    @Query("SELECT SUM(size_bytes) FROM attachment_records WHERE credential_id = :credentialId")
    fun observeTotalSizeByCredential(credentialId: String): Flow<Long?>

    // ==================== Count Operations ====================

    @Query("SELECT COUNT(*) FROM attachment_records")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM attachment_records WHERE credential_id = :credentialId")
    suspend fun getCountByCredential(credentialId: String): Int

    @Query("SELECT COUNT(*) FROM attachment_records")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM attachment_records WHERE credential_id = :credentialId")
    fun observeCountByCredential(credentialId: String): Flow<Int>

    // ==================== Exists Check ====================

    @Query("SELECT EXISTS(SELECT 1 FROM attachment_records WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM attachment_records WHERE credential_id = :credentialId)")
    suspend fun hasAttachments(credentialId: String): Boolean

    // ==================== MIME Type Operations ====================

    @Query("SELECT DISTINCT mime_type FROM attachment_records WHERE credential_id = :credentialId")
    suspend fun getMimeTypesByCredential(credentialId: String): List<String>

    @Query("SELECT * FROM attachment_records WHERE mime_type = :mimeType ORDER BY created_at DESC")
    suspend fun getByMimeType(mimeType: String): List<AttachmentRecordEntity>

    @Query("SELECT * FROM attachment_records WHERE mime_type LIKE :mimeTypePrefix || '%' ORDER BY created_at DESC")
    suspend fun getByMimeTypePrefix(mimeTypePrefix: String): List<AttachmentRecordEntity>

    // ==================== Transaction Operations ====================

    @Transaction
    @Query("DELETE FROM attachment_records WHERE credential_id = :credentialId")
    suspend fun deleteAllByCredential(credentialId: String): Int

    @Transaction
    suspend fun deleteWithPaths(credentialId: String): List<String> {
        val paths = getStoragePathsByCredential(credentialId)
        deleteAllByCredential(credentialId)
        return paths
    }

    @Transaction
    suspend fun deleteAttachmentsWithPaths(ids: List<String>): List<String> {
        val paths = ids.mapNotNull { getStoragePath(it) }
        deleteByIds(ids)
        return paths
    }

    // ==================== Statistics ====================

    @Query("""
        SELECT mime_type, COUNT(*) as count, SUM(size_bytes) as total_size
        FROM attachment_records
        GROUP BY mime_type
        ORDER BY total_size DESC
    """)
    suspend fun getStatisticsByMimeType(): List<MimeTypeStats>

    @Query("""
        SELECT credential_id, COUNT(*) as count, SUM(size_bytes) as total_size
        FROM attachment_records
        GROUP BY credential_id
        HAVING total_size > :minSize
        ORDER BY total_size DESC
    """)
    suspend fun getLargeAttachments(minSize: Long): List<CredentialAttachmentStats>
}

/**
 * Statistics for attachments grouped by MIME type.
 */
data class MimeTypeStats(
    val mime_type: String,
    val count: Int,
    val total_size: Long
)

/**
 * Statistics for attachments grouped by credential.
 */
data class CredentialAttachmentStats(
    val credential_id: String,
    val count: Int,
    val total_size: Long
)
