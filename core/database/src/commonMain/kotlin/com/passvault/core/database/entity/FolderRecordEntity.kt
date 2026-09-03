package com.passvault.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

/**
 * Folder record entity with encrypted payload.
 * Folders organize credentials hierarchically.
 */
@Entity(
    tableName = "folder_records",
    indices = [
        Index(value = ["parent_id"]),
        Index(value = ["name_hash"]),
        Index(value = ["sort_order"]),
        Index(value = ["created_at"]),
    ]
)
data class FolderRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "parent_id")
    val parentId: String?,

    /**
     * Keyed BLAKE2b blind index of the normalized folder name.
     */
    @ColumnInfo(name = "name_hash", typeAffinity = ColumnInfo.BLOB)
    val nameHash: ByteArray,

    /**
     * Encrypted payload containing the name and schema-retained optional
     * description/color fields. The icon is stored separately below.
     */
    @ColumnInfo(name = "encrypted_payload", typeAffinity = ColumnInfo.BLOB)
    val encryptedPayload: ByteArray,

    @ColumnInfo(name = "payload_nonce", typeAffinity = ColumnInfo.BLOB)
    val payloadNonce: ByteArray,

    /** Reviewed plaintext display metadata retained for schema and backup compatibility. */
    @ColumnInfo(name = "icon")
    val icon: String?,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FolderRecordEntity

        if (id != other.id) return false
        if (parentId != other.parentId) return false
        if (!nameHash.contentEquals(other.nameHash)) return false
        if (!encryptedPayload.contentEquals(other.encryptedPayload)) return false
        if (!payloadNonce.contentEquals(other.payloadNonce)) return false
        if (icon != other.icon) return false
        if (sortOrder != other.sortOrder) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (parentId?.hashCode() ?: 0)
        result = 31 * result + nameHash.contentHashCode()
        result = 31 * result + encryptedPayload.contentHashCode()
        result = 31 * result + payloadNonce.contentHashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + sortOrder
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}

/**
 * Projection for folder list data.
 */
data class FolderSummaryProjection(
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "parent_id")
    val parentId: String?,

    @ColumnInfo(name = "encrypted_payload", typeAffinity = ColumnInfo.BLOB)
    val encryptedPayload: ByteArray,

    @ColumnInfo(name = "payload_nonce", typeAffinity = ColumnInfo.BLOB)
    val payloadNonce: ByteArray,

    @ColumnInfo(name = "icon")
    val icon: String?,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as FolderSummaryProjection

        if (id != other.id) return false
        if (parentId != other.parentId) return false
        if (!encryptedPayload.contentEquals(other.encryptedPayload)) return false
        if (!payloadNonce.contentEquals(other.payloadNonce)) return false
        if (icon != other.icon) return false
        if (sortOrder != other.sortOrder) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (parentId?.hashCode() ?: 0)
        result = 31 * result + encryptedPayload.contentHashCode()
        result = 31 * result + payloadNonce.contentHashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + sortOrder
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}
