package com.passvault.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "vault_metadata")
data class VaultMetadataEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1, // Singleton row
    
    @ColumnInfo(name = "vault_format_version")
    val vaultFormatVersion: Int,
    
    @ColumnInfo(name = "crypto_format_version")
    val cryptoFormatVersion: Int,
    
    @ColumnInfo(name = "vault_id")
    val vaultId: String,
    
    @ColumnInfo(name = "argon2_algorithm_id")
    val argon2AlgorithmId: String,
    
    @ColumnInfo(name = "argon2_salt", typeAffinity = ColumnInfo.BLOB)
    val argon2Salt: ByteArray,
    
    @ColumnInfo(name = "argon2_ops_limit")
    val argon2OpsLimit: Int,
    
    @ColumnInfo(name = "argon2_mem_limit")
    val argon2MemLimit: Int,
    
    @ColumnInfo(name = "argon2_parallelism")
    val argon2Parallelism: Int,
    
    @ColumnInfo(name = "wrapped_vek", typeAffinity = ColumnInfo.BLOB)
    val wrappedVek: ByteArray,
    
    @ColumnInfo(name = "vek_nonce", typeAffinity = ColumnInfo.BLOB)
    val vekNonce: ByteArray,
    
    @ColumnInfo(name = "encrypted_verification_record", typeAffinity = ColumnInfo.BLOB)
    val encryptedVerificationRecord: ByteArray,
    
    @ColumnInfo(name = "verification_nonce", typeAffinity = ColumnInfo.BLOB)
    val verificationNonce: ByteArray,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    
    @ColumnInfo(name = "last_accessed_at")
    val lastAccessedAt: Long?,
    
    @ColumnInfo(name = "entry_count")
    val entryCount: Int = 0,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as VaultMetadataEntity

        if (id != other.id) return false
        if (vaultFormatVersion != other.vaultFormatVersion) return false
        if (cryptoFormatVersion != other.cryptoFormatVersion) return false
        if (vaultId != other.vaultId) return false
        if (argon2AlgorithmId != other.argon2AlgorithmId) return false
        if (!argon2Salt.contentEquals(other.argon2Salt)) return false
        if (argon2OpsLimit != other.argon2OpsLimit) return false
        if (argon2MemLimit != other.argon2MemLimit) return false
        if (argon2Parallelism != other.argon2Parallelism) return false
        if (!wrappedVek.contentEquals(other.wrappedVek)) return false
        if (!vekNonce.contentEquals(other.vekNonce)) return false
        if (!encryptedVerificationRecord.contentEquals(other.encryptedVerificationRecord)) return false
        if (!verificationNonce.contentEquals(other.verificationNonce)) return false
        if (createdAt != other.createdAt) return false
        if (lastAccessedAt != other.lastAccessedAt) return false
        if (entryCount != other.entryCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + vaultFormatVersion
        result = 31 * result + cryptoFormatVersion
        result = 31 * result + vaultId.hashCode()
        result = 31 * result + argon2AlgorithmId.hashCode()
        result = 31 * result + argon2Salt.contentHashCode()
        result = 31 * result + argon2OpsLimit
        result = 31 * result + argon2MemLimit
        result = 31 * result + argon2Parallelism
        result = 31 * result + wrappedVek.contentHashCode()
        result = 31 * result + vekNonce.contentHashCode()
        result = 31 * result + encryptedVerificationRecord.contentHashCode()
        result = 31 * result + verificationNonce.contentHashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (lastAccessedAt?.hashCode() ?: 0)
        result = 31 * result + entryCount
        return result
    }
}
