package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.database.entity.TagWithCountProjection
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for tag records.
 * Provides CRUD operations and credential counting.
 */
@Dao
interface TagDao {

    // ==================== Insert Operations ====================

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: TagRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: TagRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<TagRecordEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(entities: List<TagRecordEntity>): List<Long>

    // ==================== Update Operations ====================

    @Update
    suspend fun update(entity: TagRecordEntity)

    @Update
    suspend fun updateAll(entities: List<TagRecordEntity>)

    @Query("UPDATE tag_records SET color = :color WHERE id = :id")
    suspend fun updateColor(id: String, color: String?)

    // ==================== Delete Operations ====================

    @Delete
    suspend fun delete(entity: TagRecordEntity)

    @Query("DELETE FROM tag_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM tag_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM tag_records")
    suspend fun deleteAll()

    // ==================== Query Operations - Single ====================

    @Query("SELECT * FROM tag_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TagRecordEntity?

    @Query("SELECT * FROM tag_records WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<TagRecordEntity?>

    // ==================== Query Operations - With Count ====================

    @Query("""
        SELECT t.id, t.encrypted_payload, t.payload_nonce, t.color,
               COUNT(ctr.credential_id) as credential_count
        FROM tag_records t
        LEFT JOIN credential_tag_cross_ref ctr ON t.id = ctr.tag_id
        GROUP BY t.id
        ORDER BY credential_count DESC, t.created_at ASC
    """)
    suspend fun getAllWithCount(): List<TagWithCountProjection>

    @Query("""
        SELECT t.id, t.encrypted_payload, t.payload_nonce, t.color,
               COUNT(ctr.credential_id) as credential_count
        FROM tag_records t
        LEFT JOIN credential_tag_cross_ref ctr ON t.id = ctr.tag_id
        GROUP BY t.id
        ORDER BY credential_count DESC, t.created_at ASC
    """)
    fun observeAllWithCount(): Flow<List<TagWithCountProjection>>

    @Query("""
        SELECT t.id, t.encrypted_payload, t.payload_nonce, t.color,
               COUNT(ctr.credential_id) as credential_count
        FROM tag_records t
        LEFT JOIN credential_tag_cross_ref ctr ON t.id = ctr.tag_id
        WHERE t.color = :color
        GROUP BY t.id
        ORDER BY credential_count DESC, t.created_at ASC
    """)
    suspend fun getByColorWithCount(color: String): List<TagWithCountProjection>

    @Query("""
        SELECT t.id, t.encrypted_payload, t.payload_nonce, t.color,
               COUNT(ctr.credential_id) as credential_count
        FROM tag_records t
        LEFT JOIN credential_tag_cross_ref ctr ON t.id = ctr.tag_id
        WHERE t.color = :color
        GROUP BY t.id
        ORDER BY credential_count DESC, t.created_at ASC
    """)
    fun observeByColorWithCount(color: String): Flow<List<TagWithCountProjection>>

    // ==================== Full Records ====================

    @Query("SELECT * FROM tag_records ORDER BY created_at ASC")
    suspend fun getAll(): List<TagRecordEntity>

    @Query("SELECT * FROM tag_records ORDER BY created_at ASC")
    fun observeAll(): Flow<List<TagRecordEntity>>

    @Query("SELECT * FROM tag_records WHERE color = :color ORDER BY created_at ASC")
    suspend fun getByColor(color: String): List<TagRecordEntity>

    @Query("SELECT * FROM tag_records WHERE color = :color ORDER BY created_at ASC")
    fun observeByColor(color: String): Flow<List<TagRecordEntity>>

    // ==================== Search ====================

    /**
     * Search tags by name hash.
     * The hash parameter is the repository's keyed BLAKE2b blind index.
     */
    @Query("""
        SELECT t.id, t.encrypted_payload, t.payload_nonce, t.color,
               COUNT(ctr.credential_id) as credential_count
        FROM tag_records t
        LEFT JOIN credential_tag_cross_ref ctr ON t.id = ctr.tag_id
        WHERE t.name_hash = :nameHash
        GROUP BY t.id
    """)
    suspend fun searchByNameHash(nameHash: ByteArray): List<TagWithCountProjection>

    // ==================== Count Operations ====================

    @Query("SELECT COUNT(*) FROM tag_records")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM tag_records")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tag_records WHERE color = :color")
    suspend fun getCountByColor(color: String): Int

    // ==================== Credential Count Operations ====================

    @Query("""
        SELECT COUNT(*) 
        FROM credential_tag_cross_ref 
        WHERE tag_id = :tagId
    """)
    suspend fun getCredentialCount(tagId: String): Int

    @Query("""
        SELECT credential_id 
        FROM credential_tag_cross_ref 
        WHERE tag_id = :tagId
    """)
    suspend fun getCredentialIds(tagId: String): List<String>

    @Query("""
        SELECT credential_id 
        FROM credential_tag_cross_ref 
        WHERE tag_id IN (:tagIds)
    """)
    suspend fun getCredentialIdsForTags(tagIds: List<String>): List<String>

    @Query("""
        SELECT COUNT(*) 
        FROM credential_tag_cross_ref 
        WHERE tag_id = :tagId
    """)
    fun observeCredentialCount(tagId: String): Flow<Int>

    // ==================== Exists Check ====================

    @Query("SELECT EXISTS(SELECT 1 FROM tag_records WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM credential_tag_cross_ref WHERE tag_id = :tagId)")
    suspend fun hasCredentials(tagId: String): Boolean

    // ==================== Cross-Reference Operations ====================

    @Query("DELETE FROM credential_tag_cross_ref WHERE tag_id = :tagId")
    suspend fun removeAllCredentialsFromTag(tagId: String)

    @Query("DELETE FROM credential_tag_cross_ref WHERE credential_id = :credentialId")
    suspend fun removeAllTagsFromCredential(credentialId: String)

    @Query("SELECT tag_id FROM credential_tag_cross_ref WHERE credential_id = :credentialId")
    suspend fun getTagIdsForCredential(credentialId: String): List<String>

    @Query("""
        SELECT DISTINCT tag_id 
        FROM credential_tag_cross_ref 
        WHERE credential_id IN (:credentialIds)
    """)
    suspend fun getTagIdsForCredentials(credentialIds: List<String>): List<String>

    // ==================== Batch Operations ====================

    @Transaction
    suspend fun deleteTagAndRemoveReferences(tagId: String) {
        removeAllCredentialsFromTag(tagId)
        deleteById(tagId)
    }

    @Transaction
    suspend fun deleteTagsAndRemoveReferences(tagIds: List<String>) {
        tagIds.forEach { tagId ->
            removeAllCredentialsFromTag(tagId)
        }
        deleteByIds(tagIds)
    }

    // ==================== Get Tags for Credential ====================

    @Query("""
        SELECT t.* FROM tag_records t
        INNER JOIN credential_tag_cross_ref ctr ON t.id = ctr.tag_id
        WHERE ctr.credential_id = :credentialId
        ORDER BY t.created_at ASC
    """)
    suspend fun getTagsForCredential(credentialId: String): List<TagRecordEntity>

    @Query("""
        SELECT t.* FROM tag_records t
        INNER JOIN credential_tag_cross_ref ctr ON t.id = ctr.tag_id
        WHERE ctr.credential_id = :credentialId
        ORDER BY t.created_at ASC
    """)
    fun observeTagsForCredential(credentialId: String): Flow<List<TagRecordEntity>>

    // ==================== Get Most Used Tags ====================

    @Query("""
        SELECT t.id, t.encrypted_payload, t.payload_nonce, t.color,
               COUNT(ctr.credential_id) as credential_count
        FROM tag_records t
        INNER JOIN credential_tag_cross_ref ctr ON t.id = ctr.tag_id
        GROUP BY t.id
        ORDER BY credential_count DESC
        LIMIT :limit
    """)
    suspend fun getMostUsedTags(limit: Int): List<TagWithCountProjection>

    // ==================== Unused Tags ====================

    @Query("""
        SELECT t.* FROM tag_records t
        LEFT JOIN credential_tag_cross_ref ctr ON t.id = ctr.tag_id
        WHERE ctr.credential_id IS NULL
        ORDER BY t.created_at ASC
    """)
    suspend fun getUnusedTags(): List<TagRecordEntity>

    @Query("DELETE FROM tag_records WHERE id NOT IN (SELECT DISTINCT tag_id FROM credential_tag_cross_ref)")
    suspend fun deleteUnusedTags()
}
