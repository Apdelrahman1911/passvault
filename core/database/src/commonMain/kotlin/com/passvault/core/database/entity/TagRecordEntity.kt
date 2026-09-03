package com.passvault.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

/**
 * Tag record entity with encrypted payload.
 * Tags allow flexible categorization of credentials.
 */
@Entity(
    tableName = "tag_records",
    indices = [
        Index(value = ["name_hash"]),
        Index(value = ["color"]),
        Index(value = ["created_at"]),
    ]
)
data class TagRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    /**
     * Keyed BLAKE2b blind index of the normalized tag name.
     */
    @ColumnInfo(name = "name_hash", typeAffinity = ColumnInfo.BLOB)
    val nameHash: ByteArray,

    /**
     * Encrypted payload containing the name and schema-retained optional
     * description field. The display color is stored separately below.
     */
    @ColumnInfo(name = "encrypted_payload", typeAffinity = ColumnInfo.BLOB)
    val encryptedPayload: ByteArray,

    @ColumnInfo(name = "payload_nonce", typeAffinity = ColumnInfo.BLOB)
    val payloadNonce: ByteArray,

    /** Reviewed plaintext display metadata retained for schema and backup compatibility. */
    @ColumnInfo(name = "color")
    val color: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as TagRecordEntity

        if (id != other.id) return false
        if (!nameHash.contentEquals(other.nameHash)) return false
        if (!encryptedPayload.contentEquals(other.encryptedPayload)) return false
        if (!payloadNonce.contentEquals(other.payloadNonce)) return false
        if (color != other.color) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + nameHash.contentHashCode()
        result = 31 * result + encryptedPayload.contentHashCode()
        result = 31 * result + payloadNonce.contentHashCode()
        result = 31 * result + (color?.hashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        return result
    }
}

/**
 * Projection for tag list data with credential count.
 */
data class TagWithCountProjection(
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "encrypted_payload", typeAffinity = ColumnInfo.BLOB)
    val encryptedPayload: ByteArray,

    @ColumnInfo(name = "payload_nonce", typeAffinity = ColumnInfo.BLOB)
    val payloadNonce: ByteArray,

    @ColumnInfo(name = "color")
    val color: String?,

    @ColumnInfo(name = "credential_count")
    val credentialCount: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as TagWithCountProjection

        if (id != other.id) return false
        if (!encryptedPayload.contentEquals(other.encryptedPayload)) return false
        if (!payloadNonce.contentEquals(other.payloadNonce)) return false
        if (color != other.color) return false
        if (credentialCount != other.credentialCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + encryptedPayload.contentHashCode()
        result = 31 * result + payloadNonce.contentHashCode()
        result = 31 * result + (color?.hashCode() ?: 0)
        result = 31 * result + credentialCount
        return result
    }
}
