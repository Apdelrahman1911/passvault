package com.passvault.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

/**
 * Tracks database migration state and version history.
 * Used for recovery and audit purposes.
 */
@Entity(
    tableName = "migration_state",
    indices = [
        Index(value = ["started_at"]),
    ]
)
data class MigrationStateEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "from_version")
    val fromVersion: Int,

    @ColumnInfo(name = "to_version")
    val toVersion: Int,

    /**
     * Migration name or description.
     */
    @ColumnInfo(name = "migration_name")
    val migrationName: String = "Database migration",

    /**
     * Migration checksum for integrity verification.
     */
    @ColumnInfo(name = "checksum", typeAffinity = ColumnInfo.BLOB)
    val checksum: ByteArray? = null,

    /**
     * Whether migration was successful.
     */
    @ColumnInfo(name = "is_successful")
    val isSuccessful: Boolean = true,

    /**
     * Error message if migration failed.
     */
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,

    /**
     * Timestamp when migration was started.
     */
    @ColumnInfo(name = "started_at")
    val startedAt: Long,

    /**
     * Timestamp when migration was completed.
     */
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    /**
     * Whether migration was rolled back.
     */
    @ColumnInfo(name = "is_rolled_back")
    val isRolledBack: Boolean = false,

    /**
     * Duration of migration in milliseconds.
     */
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as MigrationStateEntity

        if (id != other.id) return false
        if (fromVersion != other.fromVersion) return false
        if (toVersion != other.toVersion) return false
        if (migrationName != other.migrationName) return false
        if (checksum != null) {
            if (other.checksum == null) return false
            if (!checksum.contentEquals(other.checksum)) return false
        } else if (other.checksum != null) return false
        if (isSuccessful != other.isSuccessful) return false
        if (errorMessage != other.errorMessage) return false
        if (startedAt != other.startedAt) return false
        if (completedAt != other.completedAt) return false
        if (isRolledBack != other.isRolledBack) return false
        if (durationMs != other.durationMs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + fromVersion
        result = 31 * result + toVersion
        result = 31 * result + migrationName.hashCode()
        result = 31 * result + (checksum?.contentHashCode() ?: 0)
        result = 31 * result + isSuccessful.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        result = 31 * result + startedAt.hashCode()
        result = 31 * result + (completedAt?.hashCode() ?: 0)
        result = 31 * result + isRolledBack.hashCode()
        result = 31 * result + (durationMs?.hashCode() ?: 0)
        return result
    }
}

/**
 * Current database version info singleton.
 * Stores current state separate from migration history.
 */
@Entity(
    tableName = "current_version_info",
)
data class CurrentVersionInfoEntity(
    @PrimaryKey
    @ColumnInfo(name = "singleton_key")
    val singletonKey: Int = 1,

    @ColumnInfo(name = "current_version")
    val currentVersion: Int,

    @ColumnInfo(name = "last_migration_id")
    val lastMigrationId: Int?,

    @ColumnInfo(name = "last_checked_at")
    val lastCheckedAt: Long,

    @ColumnInfo(name = "is_healthy")
    val isHealthy: Boolean = true,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CurrentVersionInfoEntity

        if (singletonKey != other.singletonKey) return false
        if (currentVersion != other.currentVersion) return false
        if (lastMigrationId != other.lastMigrationId) return false
        if (lastCheckedAt != other.lastCheckedAt) return false
        if (isHealthy != other.isHealthy) return false

        return true
    }

    override fun hashCode(): Int {
        var result = singletonKey
        result = 31 * result + currentVersion
        result = 31 * result + (lastMigrationId ?: 0)
        result = 31 * result + lastCheckedAt.hashCode()
        result = 31 * result + isHealthy.hashCode()
        return result
    }
}

/**
 * Corruption log entity for tracking database corruption events.
 * Used for debugging and audit purposes.
 */
@Entity(
    tableName = "corruption_logs",
    indices = [
        Index(value = ["timestamp"]),
    ]
)
data class CorruptionLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: kotlin.time.Instant,

    @ColumnInfo(name = "error_message")
    val errorMessage: String,

    @ColumnInfo(name = "error_type")
    val errorType: String,

    @ColumnInfo(name = "stack_trace")
    val stackTrace: String,

    @ColumnInfo(name = "recovery_attempted")
    val recoveryAttempted: Boolean = false,

    @ColumnInfo(name = "recovery_successful")
    val recoverySuccessful: Boolean? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CorruptionLogEntity

        if (id != other.id) return false
        if (timestamp != other.timestamp) return false
        if (errorMessage != other.errorMessage) return false
        if (errorType != other.errorType) return false
        if (stackTrace != other.stackTrace) return false
        if (recoveryAttempted != other.recoveryAttempted) return false
        if (recoverySuccessful != other.recoverySuccessful) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + errorMessage.hashCode()
        result = 31 * result + errorType.hashCode()
        result = 31 * result + stackTrace.hashCode()
        result = 31 * result + recoveryAttempted.hashCode()
        result = 31 * result + (recoverySuccessful?.hashCode() ?: 0)
        return result
    }
}
