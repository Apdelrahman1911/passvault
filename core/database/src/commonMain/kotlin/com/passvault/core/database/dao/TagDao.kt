package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.database.entity.TagWithCountProjection

/**
 * Encrypted tag persistence used by the tag and credential repositories.
 */
@Dao
interface TagDao {
    @Upsert
    suspend fun insertOrUpdate(entity: TagRecordEntity): Long

    @Query("DELETE FROM tag_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM tag_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TagRecordEntity?

    @Query(
        """
        SELECT t.id, t.encrypted_payload, t.payload_nonce, t.color,
               COUNT(ctr.credential_id) AS credential_count
        FROM tag_records t
        LEFT JOIN credential_tag_cross_ref ctr ON t.id = ctr.tag_id
        GROUP BY t.id
        ORDER BY credential_count DESC, t.created_at ASC
        """,
    )
    suspend fun getAllWithCount(): List<TagWithCountProjection>

    @Query(
        """
        SELECT t.id, t.encrypted_payload, t.payload_nonce, t.color,
               COUNT(ctr.credential_id) AS credential_count
        FROM tag_records t
        LEFT JOIN credential_tag_cross_ref ctr ON t.id = ctr.tag_id
        WHERE t.name_hash = :nameHash
        GROUP BY t.id
        """,
    )
    suspend fun searchByNameHash(nameHash: ByteArray): List<TagWithCountProjection>

    @Query("SELECT COUNT(*) FROM credential_tag_cross_ref WHERE tag_id = :tagId")
    suspend fun getCredentialCount(tagId: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM tag_records WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("DELETE FROM credential_tag_cross_ref WHERE tag_id = :tagId")
    suspend fun removeAllCredentialsFromTag(tagId: String)

    @Transaction
    suspend fun deleteTagAndRemoveReferences(tagId: String) {
        removeAllCredentialsFromTag(tagId)
        deleteById(tagId)
    }
}
