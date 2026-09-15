package com.passvault.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index

/**
 * Encrypted credential storage entity.
 * Stores credentials as encrypted payloads with indices for querying.
 */
@Entity(
    tableName = "credential_records",
    foreignKeys = [
        ForeignKey(
            entity = FolderRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["folder_id"]),
        Index(value = ["is_favorite"]),
        Index(value = ["type"]),
        Index(value = ["created_at"]),
        Index(value = ["updated_at"]),
        Index(value = ["last_used_at"]),
    ]
)
data class CredentialRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "type")
    val type: String,

    /**
     * Encrypted summary payload containing privacy-sensitive list metadata
     * (title and username/email display hint).
     */
    @ColumnInfo(name = "summary_payload", typeAffinity = ColumnInfo.BLOB)
    val summaryPayload: ByteArray,

    @ColumnInfo(name = "summary_nonce", typeAffinity = ColumnInfo.BLOB)
    val summaryNonce: ByteArray,

    /**
     * Encrypted secret payload containing sensitive data
     * (passwords, keys, recovery codes, full notes).
     */
    @ColumnInfo(name = "secret_payload", typeAffinity = ColumnInfo.BLOB)
    val secretPayload: ByteArray,

    @ColumnInfo(name = "secret_nonce", typeAffinity = ColumnInfo.BLOB)
    val secretNonce: ByteArray,

    @ColumnInfo(name = "folder_id")
    val folderId: String?,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long?,
) {
    override fun equals(other: Any?): Boolean =
        this === other ||
            (other is CredentialRecordEntity &&
                hasSameEncryptedValues(other) &&
                hasSameRecordMetadata(other))

    private fun hasSameEncryptedValues(other: CredentialRecordEntity): Boolean =
        summaryPayload.contentEquals(other.summaryPayload) &&
            summaryNonce.contentEquals(other.summaryNonce) &&
            secretPayload.contentEquals(other.secretPayload) &&
            secretNonce.contentEquals(other.secretNonce)

    private fun hasSameRecordMetadata(other: CredentialRecordEntity): Boolean =
        id == other.id &&
            type == other.type &&
            folderId == other.folderId &&
            isFavorite == other.isFavorite &&
            createdAt == other.createdAt &&
            updatedAt == other.updatedAt &&
            lastUsedAt == other.lastUsedAt

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + summaryPayload.contentHashCode()
        result = 31 * result + summaryNonce.contentHashCode()
        result = 31 * result + secretPayload.contentHashCode()
        result = 31 * result + secretNonce.contentHashCode()
        result = 31 * result + (folderId?.hashCode() ?: 0)
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + (lastUsedAt?.hashCode() ?: 0)
        return result
    }
}

/**
 * Cross-reference table for credential-folder many-to-many relationship.
 */
@Entity(
    tableName = "credential_folder_cross_ref",
    primaryKeys = ["credential_id", "folder_id"],
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = CredentialRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["credential_id"],
            onDelete = androidx.room.ForeignKey.CASCADE
        ),
        androidx.room.ForeignKey(
            entity = FolderRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["folder_id"])
    ]
)
data class CredentialFolderCrossRef(
    @ColumnInfo(name = "credential_id")
    val credentialId: String,

    @ColumnInfo(name = "folder_id")
    val folderId: String
)

/**
 * Cross-reference table for credential-tag many-to-many relationship.
 */
@Entity(
    tableName = "credential_tag_cross_ref",
    primaryKeys = ["credential_id", "tag_id"],
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = CredentialRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["credential_id"],
            onDelete = androidx.room.ForeignKey.CASCADE
        ),
        androidx.room.ForeignKey(
            entity = TagRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tag_id"])
    ]
)
data class CredentialTagCrossRef(
    @ColumnInfo(name = "credential_id")
    val credentialId: String,

    @ColumnInfo(name = "tag_id")
    val tagId: String
)

/**
 * Projection for credential summary data.
 * Used for list views without decrypting full payload.
 */
data class CredentialSummaryProjection(
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "folder_id")
    val folderId: String?,

    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,

    @ColumnInfo(name = "summary_payload", typeAffinity = ColumnInfo.BLOB)
    val summaryPayload: ByteArray,

    @ColumnInfo(name = "summary_nonce", typeAffinity = ColumnInfo.BLOB)
    val summaryNonce: ByteArray,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CredentialSummaryProjection

        if (id != other.id) return false
        if (type != other.type) return false
        if (folderId != other.folderId) return false
        if (isFavorite != other.isFavorite) return false
        if (!summaryPayload.contentEquals(other.summaryPayload)) return false
        if (!summaryNonce.contentEquals(other.summaryNonce)) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (lastUsedAt != other.lastUsedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (folderId?.hashCode() ?: 0)
        result = 31 * result + isFavorite.hashCode()
        result = 31 * result + summaryPayload.contentHashCode()
        result = 31 * result + summaryNonce.contentHashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + (lastUsedAt?.hashCode() ?: 0)
        return result
    }
}
