package com.passvault.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.ForeignKey

/**
 * Password history entry for a credential.
 * Maintains history of password changes for recovery.
 */
@Entity(
    tableName = "password_history_records",
    foreignKeys = [
        ForeignKey(
            entity = CredentialRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["credential_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["credential_id"]),
        Index(value = ["changed_at"]),
    ]
)
data class PasswordHistoryRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "credential_id")
    val credentialId: String,

    /**
     * Encrypted password.
     * Previous password value, encrypted.
     */
    @ColumnInfo(name = "encrypted_password", typeAffinity = ColumnInfo.BLOB)
    val encryptedPassword: ByteArray,

    @ColumnInfo(name = "password_nonce", typeAffinity = ColumnInfo.BLOB)
    val passwordNonce: ByteArray,

    /**
     * Timestamp when this password was in use.
     */
    @ColumnInfo(name = "changed_at")
    val changedAt: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PasswordHistoryRecordEntity

        if (id != other.id) return false
        if (credentialId != other.credentialId) return false
        if (!encryptedPassword.contentEquals(other.encryptedPassword)) return false
        if (!passwordNonce.contentEquals(other.passwordNonce)) return false
        if (changedAt != other.changedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + credentialId.hashCode()
        result = 31 * result + encryptedPassword.contentHashCode()
        result = 31 * result + passwordNonce.contentHashCode()
        result = 31 * result + changedAt.hashCode()
        return result
    }
}
