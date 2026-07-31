package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.passvault.core.database.entity.CorruptionLogEntity
import com.passvault.core.database.entity.MigrationStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for migration state records.
 * Tracks database migration history and status.
 */
@Dao
interface MigrationStateDao {

    // ==================== Insert Operations ====================

    /**
     * Insert a migration state record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MigrationStateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MigrationStateEntity>): List<Long>

    // ==================== Update Operations ====================

    @Update
    suspend fun update(entity: MigrationStateEntity)

    @Query("UPDATE migration_state SET completed_at = :timestamp WHERE id = :id")
    suspend fun markCompleted(id: Long, timestamp: Long)

    @Query("UPDATE migration_state SET is_rolled_back = 1 WHERE id = :id")
    suspend fun markRolledBack(id: Long)

    // ==================== Delete Operations ====================

    @Delete
    suspend fun delete(entity: MigrationStateEntity)

    @Query("DELETE FROM migration_state WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM migration_state")
    suspend fun deleteAll()

    // ==================== Query Operations ====================

    @Query("SELECT * FROM migration_state WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MigrationStateEntity?

    @Query("SELECT * FROM migration_state ORDER BY started_at DESC")
    suspend fun getAll(): List<MigrationStateEntity>

    @Query("SELECT * FROM migration_state ORDER BY started_at DESC")
    fun observeAll(): Flow<List<MigrationStateEntity>>

    // ==================== Get by Version ====================

    @Query("SELECT * FROM migration_state WHERE from_version = :fromVersion AND to_version = :toVersion LIMIT 1")
    suspend fun getByVersions(fromVersion: Int, toVersion: Int): MigrationStateEntity?

    @Query("SELECT * FROM migration_state WHERE from_version = :fromVersion AND to_version = :toVersion ORDER BY started_at DESC")
    suspend fun getAllByVersions(fromVersion: Int, toVersion: Int): List<MigrationStateEntity>

    @Query("SELECT * FROM migration_state WHERE to_version = :version ORDER BY started_at DESC LIMIT 1")
    suspend fun getByTargetVersion(version: Int): MigrationStateEntity?

    // ==================== Check Completion ====================

    @Query("SELECT EXISTS(SELECT 1 FROM migration_state WHERE from_version = :fromVersion AND to_version = :toVersion AND completed_at IS NOT NULL)")
    suspend fun isCompleted(fromVersion: Int, toVersion: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM migration_state WHERE from_version = :fromVersion AND to_version = :toVersion AND completed_at IS NULL)")
    suspend fun isInProgress(fromVersion: Int, toVersion: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM migration_state WHERE from_version = :fromVersion AND to_version = :toVersion AND is_rolled_back = 1)")
    suspend fun isRolledBack(fromVersion: Int, toVersion: Int): Boolean

    // ==================== Latest Migration ====================

    @Query("SELECT * FROM migration_state WHERE completed_at IS NOT NULL ORDER BY completed_at DESC LIMIT 1")
    suspend fun getLatestCompleted(): MigrationStateEntity?

    @Query("SELECT * FROM migration_state ORDER BY started_at DESC LIMIT 1")
    suspend fun getLatest(): MigrationStateEntity?

    // ==================== Count Operations ====================

    @Query("SELECT COUNT(*) FROM migration_state")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM migration_state WHERE completed_at IS NOT NULL")
    suspend fun getCompletedCount(): Int

    @Query("SELECT COUNT(*) FROM migration_state WHERE completed_at IS NULL")
    suspend fun getPendingCount(): Int

    // ==================== Statistics ====================

    @Query("""
        SELECT AVG(duration_ms) FROM migration_state 
        WHERE completed_at IS NOT NULL AND duration_ms IS NOT NULL
    """)
    suspend fun getAverageDuration(): Long?

    @Query("""
        SELECT MAX(duration_ms) FROM migration_state 
        WHERE completed_at IS NOT NULL
    """)
    suspend fun getMaxDuration(): Long?

    @Query("""
        SELECT * FROM migration_state 
        WHERE duration_ms IS NOT NULL 
        ORDER BY duration_ms DESC 
        LIMIT :limit
    """)
    suspend fun getSlowestMigrations(limit: Int): List<MigrationStateEntity>

    // ==================== Transaction Operations ====================

    @Transaction
    suspend fun recordMigration(
        fromVersion: Int,
        toVersion: Int,
        startTime: Long,
        endTime: Long,
    ): Long {
        val entity = MigrationStateEntity(
            fromVersion = fromVersion,
            toVersion = toVersion,
            startedAt = startTime,
            completedAt = endTime,
            durationMs = endTime - startTime,
        )
        return insert(entity)
    }

    @Transaction
    suspend fun recordMigrationFailure(
        fromVersion: Int,
        toVersion: Int,
        startTime: Long,
        errorMessage: String,
    ): Long {
        val entity = MigrationStateEntity(
            fromVersion = fromVersion,
            toVersion = toVersion,
            startedAt = startTime,
            completedAt = null,
            durationMs = null,
            errorMessage = errorMessage,
            isSuccessful = false,
        )
        return insert(entity)
    }

    // ==================== Corruption Log Operations ====================

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorruptionLog(entity: CorruptionLogEntity): Long

    @Query("SELECT * FROM corruption_logs ORDER BY timestamp DESC")
    suspend fun getAllCorruptionLogs(): List<CorruptionLogEntity>

    @Query("SELECT * FROM corruption_logs WHERE recovery_successful = 0 ORDER BY timestamp DESC LIMIT 10")
    suspend fun getFailedRecoveryLogs(): List<CorruptionLogEntity>

    @Query("UPDATE corruption_logs SET recovery_attempted = 1, recovery_successful = :successful WHERE id = :id")
    suspend fun updateRecoveryStatus(id: Long, successful: Boolean)

    @Query("DELETE FROM corruption_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOldCorruptionLogs(cutoffTime: Long)

    @Query("SELECT COUNT(*) FROM corruption_logs")
    suspend fun getCorruptionLogCount(): Int
}
