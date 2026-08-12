package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.passvault.core.database.entity.PasswordHistoryRecordEntity

/**
 * Minimal encrypted password-history access used by the credential repository.
 * History insertion during credential edits is part of CredentialDao's single
 * transaction; this DAO intentionally exposes no bulk/statistics surface.
 */
@Dao
interface PasswordHistoryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PasswordHistoryRecordEntity): Long

    @Query(
        "SELECT * FROM password_history_records " +
            "WHERE credential_id = :credentialId ORDER BY changed_at DESC, id DESC",
    )
    suspend fun getByCredential(credentialId: String): List<PasswordHistoryRecordEntity>

    @Query("""
        SELECT * FROM password_history_records
        WHERE credential_id = :credentialId
        ORDER BY changed_at DESC, id DESC
        LIMIT 1
    """)
    suspend fun getLatestByCredential(credentialId: String): PasswordHistoryRecordEntity?
}
