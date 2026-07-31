package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock

/**
 * Data Access Object for password history records.
 * Tracks password changes for recovery purposes.
 */
@Dao
interface PasswordHistoryDao {

    // ==================== Insert Operations ====================

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PasswordHistoryRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: PasswordHistoryRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAll(entities: List<PasswordHistoryRecordEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(entities: List<PasswordHistoryRecordEntity>): List<Long>

    // ==================== Update Operations ====================

    @Update
    suspend fun update(entity: PasswordHistoryRecordEntity)

    // ==================== Delete Operations ====================

    @Delete
    suspend fun delete(entity: PasswordHistoryRecordEntity)

    @Query("DELETE FROM password_history_records WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM password_history_records WHERE credential_id = :credentialId")
    suspend fun deleteByCredential(credentialId: String)

    @Query("DELETE FROM password_history_records WHERE credential_id = :credentialId AND changed_at < :beforeTimestamp")
    suspend fun deleteOldByCredential(credentialId: String, beforeTimestamp: Long)

    @Query("DELETE FROM password_history_records WHERE changed_at < :beforeTimestamp")
    suspend fun deleteAllBefore(beforeTimestamp: Long)

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
    suspend fun keepOnlyRecentByCredential(credentialId: String, keepCount: Int)

    @Query("DELETE FROM password_history_records")
    suspend fun deleteAll()

    // ==================== Query Operations - Single ====================

    @Query("SELECT * FROM password_history_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): PasswordHistoryRecordEntity?

    @Query("SELECT * FROM password_history_records WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<PasswordHistoryRecordEntity?>

    // ==================== Query Operations - By Credential ====================

    @Query("SELECT * FROM password_history_records WHERE credential_id = :credentialId ORDER BY changed_at DESC")
    suspend fun getByCredential(credentialId: String): List<PasswordHistoryRecordEntity>

    @Query("SELECT * FROM password_history_records WHERE credential_id = :credentialId ORDER BY changed_at DESC")
    fun observeByCredential(credentialId: String): Flow<List<PasswordHistoryRecordEntity>>

    @Query("SELECT * FROM password_history_records WHERE credential_id = :credentialId ORDER BY changed_at DESC LIMIT :limit")
    suspend fun getRecentByCredential(credentialId: String, limit: Int): List<PasswordHistoryRecordEntity>

    @Query("SELECT * FROM password_history_records WHERE credential_id = :credentialId ORDER BY changed_at DESC LIMIT :limit")
    fun observeRecentByCredential(credentialId: String, limit: Int): Flow<List<PasswordHistoryRecordEntity>>

    // ==================== Query Operations - All ====================

    @Query("SELECT * FROM password_history_records ORDER BY changed_at DESC")
    suspend fun getAll(): List<PasswordHistoryRecordEntity>

    @Query("SELECT * FROM password_history_records ORDER BY changed_at DESC")
    fun observeAll(): Flow<List<PasswordHistoryRecordEntity>>

    // ==================== Time-based Queries ====================

    @Query("SELECT * FROM password_history_records WHERE changed_at > :afterTimestamp ORDER BY changed_at DESC")
    suspend fun getAfter(afterTimestamp: Long): List<PasswordHistoryRecordEntity>

    @Query("SELECT * FROM password_history_records WHERE changed_at < :beforeTimestamp ORDER BY changed_at DESC")
    suspend fun getBefore(beforeTimestamp: Long): List<PasswordHistoryRecordEntity>

    @Query("SELECT * FROM password_history_records WHERE changed_at BETWEEN :startTimestamp AND :endTimestamp ORDER BY changed_at DESC")
    suspend fun getBetween(startTimestamp: Long, endTimestamp: Long): List<PasswordHistoryRecordEntity>

    // ==================== Count Operations ====================

    @Query("SELECT COUNT(*) FROM password_history_records")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM password_history_records WHERE credential_id = :credentialId")
    suspend fun getCountByCredential(credentialId: String): Int

    @Query("SELECT COUNT(*) FROM password_history_records")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM password_history_records WHERE credential_id = :credentialId")
    fun observeCountByCredential(credentialId: String): Flow<Int>

    // ==================== Exists Check ====================

    @Query("SELECT EXISTS(SELECT 1 FROM password_history_records WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM password_history_records WHERE credential_id = :credentialId)")
    suspend fun hasHistory(credentialId: String): Boolean

    // ==================== Latest Password ====================

    @Query("""
        SELECT * FROM password_history_records 
        WHERE credential_id = :credentialId 
        ORDER BY changed_at DESC 
        LIMIT 1
    """)
    suspend fun getLatestByCredential(credentialId: String): PasswordHistoryRecordEntity?

    @Query("""
        SELECT * FROM password_history_records 
        WHERE credential_id = :credentialId 
        ORDER BY changed_at DESC 
        LIMIT 1
    """)
    fun observeLatestByCredential(credentialId: String): Flow<PasswordHistoryRecordEntity?>

    // ==================== Oldest Records ====================

    @Query("""
        SELECT * FROM password_history_records 
        WHERE credential_id = :credentialId 
        ORDER BY changed_at ASC 
        LIMIT :limit
    """)
    suspend fun getOldestByCredential(credentialId: String, limit: Int): List<PasswordHistoryRecordEntity>

    // ==================== Transaction Operations ====================

    /**
     * Add a new password history entry and maintain only the last N entries.
     */
    @Transaction
    suspend fun addWithLimit(entity: PasswordHistoryRecordEntity, maxEntries: Int) {
        insert(entity)
        keepOnlyRecentByCredential(entity.credentialId, maxEntries)
    }

    /**
     * Migrate all history from one credential to another.
     */
    @Transaction
    suspend fun migrateHistory(fromCredentialId: String, toCredentialId: String) {
        // Update all history records to point to new credential
        updateCredentialId(fromCredentialId, toCredentialId)
    }

    @Query("UPDATE password_history_records SET credential_id = :newCredentialId WHERE credential_id = :oldCredentialId")
    suspend fun updateCredentialId(oldCredentialId: String, newCredentialId: String)

    /**
     * Clean up old entries across all credentials.
     */
    @Transaction
    suspend fun cleanupOldEntries(maxAgeDays: Int, maxEntriesPerCredential: Int) {
        val cutoffTime = Clock.System.now().toEpochMilliseconds() -
            (maxAgeDays * 24 * 60 * 60 * 1000L)

        // Delete entries older than maxAgeDays
        deleteAllBefore(cutoffTime)

        // For each credential with more than maxEntriesPerCredential, trim to maxEntriesPerCredential
        val credentialIds = getCredentialIdsWithExcessHistory(maxEntriesPerCredential)
        credentialIds.forEach { credentialId ->
            keepOnlyRecentByCredential(credentialId, maxEntriesPerCredential)
        }
    }

    @Query("""
        SELECT credential_id FROM password_history_records 
        GROUP BY credential_id 
        HAVING COUNT(*) > :maxEntries
    """)
    suspend fun getCredentialIdsWithExcessHistory(maxEntries: Int): List<String>

    // ==================== Statistics ====================

    @Query("""
        SELECT credential_id, COUNT(*) as count, 
               MIN(changed_at) as oldest_change, 
               MAX(changed_at) as latest_change
        FROM password_history_records
        GROUP BY credential_id
        HAVING count > 1
        ORDER BY count DESC
    """)
    suspend fun getChangeStatistics(): List<PasswordChangeStats>

    @Query("""
        SELECT AVG(count) FROM (
            SELECT COUNT(*) as count 
            FROM password_history_records 
            GROUP BY credential_id
        )
    """)
    suspend fun getAverageHistoryPerCredential(): Double?

    @Query("""
        SELECT COUNT(DISTINCT credential_id) 
        FROM password_history_records 
        WHERE changed_at > :sinceTimestamp
    """)
    suspend fun getCredentialsWithRecentChanges(sinceTimestamp: Long): Int

    // ==================== Encryption Key Derivation Context ====================

    @Query("""
        SELECT credential_id, id, changed_at 
        FROM password_history_records 
        WHERE credential_id IN (:credentialIds)
        ORDER BY credential_id, changed_at DESC
    """)
    suspend fun getHistoryMetadata(credentialIds: List<String>): List<PasswordHistoryMetadata>
}

/**
 * Statistics for password changes per credential.
 */
data class PasswordChangeStats(
    val credential_id: String,
    val count: Int,
    val oldest_change: Long,
    val latest_change: Long
)

/**
 * Minimal metadata for password history entries.
 */
data class PasswordHistoryMetadata(
    val credential_id: String,
    val id: String,
    val changed_at: Long
)
