package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passvault.core.database.entity.VaultMetadataEntity

/**
 * Data Access Object for vault metadata.
 * Manages the singleton vault configuration record.
 */
@Dao
interface VaultMetadataDao {

    // ==================== Insert Operations ====================

    /**
     * Insert vault metadata.
     * Should only be called once during vault creation.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: VaultMetadataEntity): Long

    // ==================== Update Operations ====================

    @Update
    suspend fun update(entity: VaultMetadataEntity)

    @Query("UPDATE vault_metadata SET last_accessed_at = :timestamp WHERE id = 1")
    suspend fun updateLastAccessed(timestamp: Long)

    // ==================== Query Operations ====================

    @Query("SELECT * FROM vault_metadata WHERE id = 1 LIMIT 1")
    suspend fun get(): VaultMetadataEntity?

    @Query("SELECT vault_format_version FROM vault_metadata WHERE id = 1")
    suspend fun getVaultFormatVersion(): Int?

    // ==================== Exists Check ====================

    @Query("SELECT EXISTS(SELECT 1 FROM vault_metadata WHERE id = 1)")
    suspend fun exists(): Boolean

    /**
     * Update the vault metadata with new encryption parameters.
     * Used when changing master password.
     */
    @Query("""
        UPDATE vault_metadata
        SET argon2_salt = :salt,
            argon2_ops_limit = :opsLimit,
            argon2_mem_limit = :memLimit,
            argon2_parallelism = :parallelism,
            wrapped_vek = :wrappedVek,
            vek_nonce = :vekNonce,
            encrypted_verification_record = :verificationRecord,
            verification_nonce = :verificationNonce,
            last_accessed_at = :lastAccessedAt
        WHERE id = 1
    """)
    suspend fun updateEncryptionParameters(
        salt: ByteArray,
        opsLimit: Int,
        memLimit: Int,
        parallelism: Int,
        wrappedVek: ByteArray,
        vekNonce: ByteArray,
        verificationRecord: ByteArray,
        verificationNonce: ByteArray,
        lastAccessedAt: Long,
    )

}
