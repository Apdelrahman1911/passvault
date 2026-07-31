package com.passvault.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.ForeignKey

/**
 * Reserved attachment metadata from the version-1 schema.
 *
 * The current application does not implement attachment-file storage. These
 * rows are retained only so an existing database or backup can preserve its
 * encrypted metadata without pretending that the referenced file is managed.
 */
@Entity(
    tableName = "attachment_records",
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
        Index(value = ["created_at"]),
    ]
)
data class AttachmentRecordEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "credential_id")
    val credentialId: String,

    /**
     * Encrypted filename.
     * Original filename is encrypted to protect privacy.
     */
    @ColumnInfo(name = "encrypted_filename", typeAffinity = ColumnInfo.BLOB)
    val encryptedFilename: ByteArray,

    @ColumnInfo(name = "filename_nonce", typeAffinity = ColumnInfo.BLOB)
    val filenameNonce: ByteArray,

    /**
     * MIME type (e.g., "application/pdf", "image/png").
     * Not encrypted as it's needed for file handling.
     */
    @ColumnInfo(name = "mime_type")
    val mimeType: String,

    /**
     * File size in bytes.
     */
    @ColumnInfo(name = "size_bytes")
    val sizeBytes: Long,

    /**
     * Legacy relative storage reference. The current application never opens
     * or creates a file from this value.
     */
    @ColumnInfo(name = "storage_path")
    val storagePath: String,

    /**
     * Encryption key ID reference.
     * Used to derive the per-file encryption key.
     */
    @ColumnInfo(name = "key_derivation_context")
    val keyDerivationContext: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AttachmentRecordEntity

        if (id != other.id) return false
        if (credentialId != other.credentialId) return false
        if (!encryptedFilename.contentEquals(other.encryptedFilename)) return false
        if (!filenameNonce.contentEquals(other.filenameNonce)) return false
        if (mimeType != other.mimeType) return false
        if (sizeBytes != other.sizeBytes) return false
        if (storagePath != other.storagePath) return false
        if (keyDerivationContext != other.keyDerivationContext) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + credentialId.hashCode()
        result = 31 * result + encryptedFilename.contentHashCode()
        result = 31 * result + filenameNonce.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + sizeBytes.hashCode()
        result = 31 * result + storagePath.hashCode()
        result = 31 * result + keyDerivationContext.hashCode()
        result = 31 * result + createdAt.hashCode()
        return result
    }
}
