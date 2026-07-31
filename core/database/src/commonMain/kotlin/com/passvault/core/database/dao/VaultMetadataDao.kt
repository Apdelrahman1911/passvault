package com.passvault.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.passvault.core.database.entity.VaultMetadataEntity
import kotlinx.coroutines.flow.Flow

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: VaultMetadataEntity): Long

    // ==================== Update Operations ====================

    @Update
    suspend fun update(entity: VaultMetadataEntity)

    @Query("UPDATE vault_metadata SET last_accessed_at = :timestamp WHERE id = 1")
    suspend fun updateLastAccessed(timestamp: Long)

    @Query("UPDATE vault_metadata SET entry_count = :count WHERE id = 1")
    suspend fun updateEntryCount(count: Int)

    @Query("UPDATE vault_metadata SET entry_count = entry_count + :delta WHERE id = 1")
    suspend fun incrementEntryCount(delta: Int = 1)

    @Query("UPDATE vault_metadata SET entry_count = MAX(0, entry_count - :delta) WHERE id = 1")
    suspend fun decrementEntryCount(delta: Int = 1)

    @Query("UPDATE vault_metadata SET wrapped_vek = :wrappedVek, vek_nonce = :vekNonce WHERE id = 1")
    suspend fun updateWrappedVek(wrappedVek: ByteArray, vekNonce: ByteArray)

    @Query("UPDATE vault_metadata SET encrypted_verification_record = :record, verification_nonce = :nonce WHERE id = 1")
    suspend fun updateVerificationRecord(record: ByteArray, nonce: ByteArray)

    // ==================== Delete Operations ====================

    @Delete
    suspend fun delete(entity: VaultMetadataEntity)

    @Query("DELETE FROM vault_metadata WHERE id = 1")
    suspend fun deleteAll()

    // ==================== Query Operations ====================

    @Query("SELECT * FROM vault_metadata WHERE id = 1 LIMIT 1")
    suspend fun get(): VaultMetadataEntity?

    @Query("SELECT * FROM vault_metadata WHERE id = 1 LIMIT 1")
    fun observe(): Flow<VaultMetadataEntity?>

    // ==================== Field Accessors ====================

    @Query("SELECT vault_id FROM vault_metadata WHERE id = 1")
    suspend fun getVaultId(): String?

    @Query("SELECT vault_format_version FROM vault_metadata WHERE id = 1")
    suspend fun getVaultFormatVersion(): Int?

    @Query("SELECT crypto_format_version FROM vault_metadata WHERE id = 1")
    suspend fun getCryptoFormatVersion(): Int?

    @Query("SELECT argon2_algorithm_id FROM vault_metadata WHERE id = 1")
    suspend fun getArgon2AlgorithmId(): String?

    @Query("SELECT argon2_salt FROM vault_metadata WHERE id = 1")
    suspend fun getArgon2Salt(): ByteArray?

    @Query("SELECT argon2_ops_limit FROM vault_metadata WHERE id = 1")
    suspend fun getArgon2OpsLimit(): Int?

    @Query("SELECT argon2_mem_limit FROM vault_metadata WHERE id = 1")
    suspend fun getArgon2MemLimit(): Int?

    @Query("SELECT argon2_parallelism FROM vault_metadata WHERE id = 1")
    suspend fun getArgon2Parallelism(): Int?

    @Query("SELECT wrapped_vek FROM vault_metadata WHERE id = 1")
    suspend fun getWrappedVek(): ByteArray?

    @Query("SELECT vek_nonce FROM vault_metadata WHERE id = 1")
    suspend fun getVekNonce(): ByteArray?

    @Query("SELECT encrypted_verification_record FROM vault_metadata WHERE id = 1")
    suspend fun getEncryptedVerificationRecord(): ByteArray?

    @Query("SELECT verification_nonce FROM vault_metadata WHERE id = 1")
    suspend fun getVerificationNonce(): ByteArray?

    @Query("SELECT created_at FROM vault_metadata WHERE id = 1")
    suspend fun getCreatedAt(): Long?

    @Query("SELECT last_accessed_at FROM vault_metadata WHERE id = 1")
    suspend fun getLastAccessedAt(): Long?

    @Query("SELECT entry_count FROM vault_metadata WHERE id = 1")
    suspend fun getEntryCount(): Int?

    // ==================== Observable Field Accessors ====================

    @Query("SELECT entry_count FROM vault_metadata WHERE id = 1")
    fun observeEntryCount(): Flow<Int?>

    @Query("SELECT last_accessed_at FROM vault_metadata WHERE id = 1")
    fun observeLastAccessedAt(): Flow<Long?>

    // ==================== Exists Check ====================

    @Query("SELECT EXISTS(SELECT 1 FROM vault_metadata WHERE id = 1)")
    suspend fun exists(): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM vault_metadata WHERE id = 1)")
    fun observeExists(): Flow<Boolean>

    // ==================== Statistics ====================

    /**
     * Get all metadata fields as a single entity.
     * This is the preferred way to read vault configuration.
     */
    @Query("SELECT * FROM vault_metadata LIMIT 1")
    suspend fun getMetadata(): VaultMetadataEntity?

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
            verification_nonce = :verificationNonce
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
        verificationNonce: ByteArray
    )

    /**
     * Get the vault format and crypto versions.
     */
    @Query("SELECT vault_format_version, crypto_format_version FROM vault_metadata WHERE id = 1")
    suspend fun getVersions(): VaultVersions?

    /**
     * Get the Argon2 parameters for key derivation.
     */
    @Query("""
        SELECT argon2_algorithm_id, argon2_salt, argon2_ops_limit, argon2_mem_limit, argon2_parallelism
        FROM vault_metadata
        WHERE id = 1
    """)
    suspend fun getArgon2Params(): Argon2Params?

    /**
     * Check if the vault needs format upgrade.
     */
    @Query("SELECT vault_format_version FROM vault_metadata WHERE id = 1")
    suspend fun needsFormatUpgrade(currentVersion: Int): Boolean {
        val version = getVaultFormatVersion()
        return version != null && version < currentVersion
    }
}

/**
 * Vault version information.
 */
data class VaultVersions(
    val vault_format_version: Int,
    val crypto_format_version: Int
)

/**
 * Argon2 parameters from the vault metadata.
 */
data class Argon2Params(
    val argon2_algorithm_id: String,
    val argon2_salt: ByteArray,
    val argon2_ops_limit: Int,
    val argon2_mem_limit: Int,
    val argon2_parallelism: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Argon2Params

        if (argon2_algorithm_id != other.argon2_algorithm_id) return false
        if (!argon2_salt.contentEquals(other.argon2_salt)) return false
        if (argon2_ops_limit != other.argon2_ops_limit) return false
        if (argon2_mem_limit != other.argon2_mem_limit) return false
        if (argon2_parallelism != other.argon2_parallelism) return false

        return true
    }

    override fun hashCode(): Int {
        var result = argon2_algorithm_id.hashCode()
        result = 31 * result + argon2_salt.contentHashCode()
        result = 31 * result + argon2_ops_limit
        result = 31 * result + argon2_mem_limit
        result = 31 * result + argon2_parallelism
        return result
    }
}
