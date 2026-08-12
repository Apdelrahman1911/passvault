package com.passvault.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import androidx.room.Index
import androidx.room.ForeignKey

/** Attachment metadata for encrypted blobs stored outside Room. */
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

    /** App-private relative object path. Never derived from a user filename. */
    @ColumnInfo(name = "storage_path")
    val storagePath: String,

    /** Unique context for the independently derived per-attachment key. */
    @ColumnInfo(name = "key_derivation_context")
    val keyDerivationContext: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    /** Zero identifies metadata-only rows retained from schema versions 1/2. */
    @ColumnInfo(name = "content_format_version", defaultValue = "0")
    val contentFormatVersion: Int = 0,

    /** Two-phase filesystem/database operation state. */
    @ColumnInfo(name = "storage_state", defaultValue = "'LEGACY'")
    val storageState: String = STORAGE_STATE_LEGACY,
) {
    @Suppress("CyclomaticComplexMethod") // Explicit field equality is required for ByteArray content semantics.
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
        if (contentFormatVersion != other.contentFormatVersion) return false
        if (storageState != other.storageState) return false

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
        result = 31 * result + contentFormatVersion
        result = 31 * result + storageState.hashCode()
        return result
    }

    companion object {
        const val STORAGE_STATE_LEGACY = "LEGACY"
        const val STORAGE_STATE_STAGING = "STAGING"
        const val STORAGE_STATE_READY = "READY"
        const val STORAGE_STATE_DELETING = "DELETING"
    }
}
