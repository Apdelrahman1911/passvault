package com.passvault.core.database.backup

import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.database.entity.CredentialFolderCrossRef
import com.passvault.core.database.entity.CredentialRecordEntity
import com.passvault.core.database.entity.CredentialTagCrossRef
import com.passvault.core.database.entity.FolderRecordEntity
import com.passvault.core.database.entity.PasswordHistoryRecordEntity
import com.passvault.core.database.entity.TagRecordEntity
import com.passvault.core.database.entity.VaultMetadataEntity
import com.passvault.core.database.repository.MAX_ATTACHMENT_FILENAME_ENCRYPTED_PAYLOAD_BYTES
import com.passvault.core.database.repository.MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES
import com.passvault.core.database.repository.MAX_FOLDER_ENCRYPTED_PAYLOAD_BYTES
import com.passvault.core.database.repository.MAX_PASSWORD_HISTORY_ENCRYPTED_PAYLOAD_BYTES
import com.passvault.core.database.repository.MAX_TAG_ENCRYPTED_PAYLOAD_BYTES
import okio.Buffer

/** Counts authenticated before any streamed metadata row. */
internal data class BackupStreamManifest(
    val credentialCount: Int,
    val folderCount: Int,
    val tagCount: Int,
    val credentialFolderReferenceCount: Int,
    val credentialTagReferenceCount: Int,
    val attachmentCount: Int,
    val managedAttachmentCount: Int,
    val passwordHistoryCount: Int,
    val metadataSchemaVersion: Int = CURRENT_BACKUP_METADATA_SCHEMA_VERSION,
)

/** One typed Room value. Only encrypted payload bytes cross this boundary. */
internal sealed interface BackupMetadataValue {
    val recordType: Int
    fun clear()

    data class Metadata(val value: VaultMetadataEntity) : BackupMetadataValue {
        override val recordType = BackupRecordType.METADATA
        override fun clear() = value.clearBinaryFields()
    }

    data class Folder(val value: FolderRecordEntity) : BackupMetadataValue {
        override val recordType = BackupRecordType.FOLDER
        override fun clear() = value.clearBinaryFields()
    }

    data class Tag(val value: TagRecordEntity) : BackupMetadataValue {
        override val recordType = BackupRecordType.TAG
        override fun clear() = value.clearBinaryFields()
    }

    data class Credential(val value: CredentialRecordEntity) : BackupMetadataValue {
        override val recordType = BackupRecordType.CREDENTIAL
        override fun clear() = value.clearBinaryFields()
    }

    data class CredentialFolderReference(
        val value: CredentialFolderCrossRef,
    ) : BackupMetadataValue {
        override val recordType = BackupRecordType.CREDENTIAL_FOLDER_REFERENCE
        override fun clear() = Unit
    }

    data class CredentialTagReference(
        val value: CredentialTagCrossRef,
    ) : BackupMetadataValue {
        override val recordType = BackupRecordType.CREDENTIAL_TAG_REFERENCE
        override fun clear() = Unit
    }

    data class Attachment(val value: AttachmentRecordEntity) : BackupMetadataValue {
        override val recordType = BackupRecordType.ATTACHMENT
        override fun clear() = value.clearBinaryFields()
    }

    data class PasswordHistory(val value: PasswordHistoryRecordEntity) : BackupMetadataValue {
        override val recordType = BackupRecordType.PASSWORD_HISTORY
        override fun clear() = value.clearBinaryFields()
    }
}

/**
 * Compact length-prefixed codec for v2 metadata records.
 *
 * JSON/Base64 is deliberately not used here: it amplified every Room payload
 * into a UTF-16 string plus UTF-8 and Base64 copies. Each encoded value is now
 * bounded independently and can be released before the next row is queried.
 */
internal object BackupEntityBinaryCodec {
    fun encodeManifest(value: BackupStreamManifest): ByteArray = Buffer()
        .writeInt(value.metadataSchemaVersion.also(::requireSupportedMetadataSchema))
        .writeCount(value.credentialCount)
        .writeCount(value.folderCount)
        .writeCount(value.tagCount)
        .writeCount(value.credentialFolderReferenceCount)
        .writeCount(value.credentialTagReferenceCount)
        .writeCount(value.attachmentCount)
        .writeCount(value.managedAttachmentCount)
        .writeCount(value.passwordHistoryCount)
        .readByteArray()

    fun decodeManifest(bytes: ByteArray): BackupStreamManifest = Buffer().write(bytes).let { source ->
        val metadataSchemaVersion = source.readInt().also(::requireSupportedMetadataSchema)
        BackupStreamManifest(
            credentialCount = source.readCount(),
            folderCount = source.readCount(),
            tagCount = source.readCount(),
            credentialFolderReferenceCount = source.readCount(),
            credentialTagReferenceCount = source.readCount(),
            attachmentCount = source.readCount(),
            managedAttachmentCount = source.readCount(),
            passwordHistoryCount = source.readCount(),
            metadataSchemaVersion = metadataSchemaVersion,
        ).also { require(source.exhausted()) }
    }

    fun encode(
        value: BackupMetadataValue,
        metadataSchemaVersion: Int = CURRENT_BACKUP_METADATA_SCHEMA_VERSION,
    ): ByteArray {
        require(metadataSchemaVersion == CURRENT_BACKUP_METADATA_SCHEMA_VERSION)
        val sink = Buffer()
        when (value) {
            is BackupMetadataValue.Metadata -> sink.writeMetadata(value.value)
            is BackupMetadataValue.Folder -> sink.writeFolder(value.value)
            is BackupMetadataValue.Tag -> sink.writeTag(value.value)
            is BackupMetadataValue.Credential -> sink.writeCredential(value.value)
            is BackupMetadataValue.CredentialFolderReference -> sink.writeFolderReference(value.value)
            is BackupMetadataValue.CredentialTagReference -> sink.writeTagReference(value.value)
            is BackupMetadataValue.Attachment -> sink.writeAttachment(value.value)
            is BackupMetadataValue.PasswordHistory -> sink.writePasswordHistory(value.value)
        }
        return sink.readByteArray().also {
            require(it.size <= maximumPlaintextBytes(value.recordType))
        }
    }

    fun decode(
        recordType: Int,
        bytes: ByteArray,
        metadataSchemaVersion: Int = CURRENT_BACKUP_METADATA_SCHEMA_VERSION,
    ): BackupMetadataValue {
        requireSupportedMetadataSchema(metadataSchemaVersion)
        require(bytes.size <= maximumPlaintextBytes(recordType))
        val source = Buffer().write(bytes)
        val value = when (recordType) {
            BackupRecordType.METADATA -> BackupMetadataValue.Metadata(source.readMetadata())
            BackupRecordType.FOLDER -> BackupMetadataValue.Folder(source.readFolder())
            BackupRecordType.TAG -> BackupMetadataValue.Tag(source.readTag())
            BackupRecordType.CREDENTIAL -> BackupMetadataValue.Credential(
                source.readCredential(metadataSchemaVersion),
            )
            BackupRecordType.CREDENTIAL_FOLDER_REFERENCE ->
                BackupMetadataValue.CredentialFolderReference(source.readFolderReference())
            BackupRecordType.CREDENTIAL_TAG_REFERENCE ->
                BackupMetadataValue.CredentialTagReference(source.readTagReference())
            BackupRecordType.ATTACHMENT -> BackupMetadataValue.Attachment(source.readAttachment())
            BackupRecordType.PASSWORD_HISTORY ->
                BackupMetadataValue.PasswordHistory(source.readPasswordHistory())
            else -> error("Not a metadata record type")
        }
        require(source.exhausted())
        return value
    }

    fun maximumPlaintextBytes(recordType: Int): Int = when (recordType) {
        BackupRecordType.MANIFEST -> MANIFEST_MAX_BYTES
        BackupRecordType.METADATA -> METADATA_MAX_BYTES
        BackupRecordType.FOLDER,
        BackupRecordType.TAG,
        -> SMALL_ENTITY_MAX_BYTES
        BackupRecordType.CREDENTIAL -> CREDENTIAL_MAX_BYTES
        BackupRecordType.CREDENTIAL_FOLDER_REFERENCE,
        BackupRecordType.CREDENTIAL_TAG_REFERENCE,
        -> REFERENCE_MAX_BYTES
        BackupRecordType.ATTACHMENT,
        BackupRecordType.PASSWORD_HISTORY,
        -> LARGE_ENTITY_MAX_BYTES
        BackupRecordType.METADATA_END -> 0
        else -> error("Not a metadata record type")
    }

    private fun Buffer.writeMetadata(value: VaultMetadataEntity) {
        writeInt(value.id)
        writeInt(value.vaultFormatVersion)
        writeInt(value.cryptoFormatVersion)
        writeString(value.vaultId, MAX_IDENTIFIER_UTF8_BYTES)
        writeString(value.argon2AlgorithmId, MAX_SHORT_TEXT_UTF8_BYTES)
        writeBytes(value.argon2Salt, MAX_SALT_BYTES)
        writeInt(value.argon2OpsLimit)
        writeInt(value.argon2MemLimit)
        writeInt(value.argon2Parallelism)
        writeBytes(value.wrappedVek, MAX_FIXED_SECRET_BYTES)
        writeBytes(value.vekNonce, MAX_NONCE_BYTES)
        writeBytes(value.encryptedVerificationRecord, MAX_FIXED_SECRET_BYTES)
        writeBytes(value.verificationNonce, MAX_NONCE_BYTES)
        writeLong(value.createdAt)
        writeNullableLong(value.lastAccessedAt)
        writeInt(value.entryCount)
    }

    private fun Buffer.readMetadata() = VaultMetadataEntity(
        id = readInt(),
        vaultFormatVersion = readInt(),
        cryptoFormatVersion = readInt(),
        vaultId = readString(MAX_IDENTIFIER_UTF8_BYTES),
        argon2AlgorithmId = readString(MAX_SHORT_TEXT_UTF8_BYTES),
        argon2Salt = readBytes(MAX_SALT_BYTES),
        argon2OpsLimit = readInt(),
        argon2MemLimit = readInt(),
        argon2Parallelism = readInt(),
        wrappedVek = readBytes(MAX_FIXED_SECRET_BYTES),
        vekNonce = readBytes(MAX_NONCE_BYTES),
        encryptedVerificationRecord = readBytes(MAX_FIXED_SECRET_BYTES),
        verificationNonce = readBytes(MAX_NONCE_BYTES),
        createdAt = readLong(),
        lastAccessedAt = readNullableLong(),
        entryCount = readInt(),
    )

    private fun Buffer.writeFolder(value: FolderRecordEntity) {
        writeString(value.id, MAX_IDENTIFIER_UTF8_BYTES)
        writeNullableString(value.parentId, MAX_IDENTIFIER_UTF8_BYTES)
        writeBytes(value.nameHash, BLIND_INDEX_BYTES)
        writeBytes(value.encryptedPayload, MAX_FOLDER_ENCRYPTED_PAYLOAD_BYTES)
        writeBytes(value.payloadNonce, MAX_NONCE_BYTES)
        writeNullableString(value.icon, MAX_SHORT_TEXT_UTF8_BYTES)
        writeInt(value.sortOrder)
        writeLong(value.createdAt)
        writeLong(value.updatedAt)
    }

    private fun Buffer.readFolder() = FolderRecordEntity(
        id = readString(MAX_IDENTIFIER_UTF8_BYTES),
        parentId = readNullableString(MAX_IDENTIFIER_UTF8_BYTES),
        nameHash = readBytes(BLIND_INDEX_BYTES),
        encryptedPayload = readBytes(MAX_FOLDER_ENCRYPTED_PAYLOAD_BYTES),
        payloadNonce = readBytes(MAX_NONCE_BYTES),
        icon = readNullableString(MAX_SHORT_TEXT_UTF8_BYTES),
        sortOrder = readInt(),
        createdAt = readLong(),
        updatedAt = readLong(),
    )

    private fun Buffer.writeTag(value: TagRecordEntity) {
        writeString(value.id, MAX_IDENTIFIER_UTF8_BYTES)
        writeBytes(value.nameHash, BLIND_INDEX_BYTES)
        writeBytes(value.encryptedPayload, MAX_TAG_ENCRYPTED_PAYLOAD_BYTES)
        writeBytes(value.payloadNonce, MAX_NONCE_BYTES)
        writeNullableString(value.color, MAX_SHORT_TEXT_UTF8_BYTES)
        writeLong(value.createdAt)
    }

    private fun Buffer.readTag() = TagRecordEntity(
        id = readString(MAX_IDENTIFIER_UTF8_BYTES),
        nameHash = readBytes(BLIND_INDEX_BYTES),
        encryptedPayload = readBytes(MAX_TAG_ENCRYPTED_PAYLOAD_BYTES),
        payloadNonce = readBytes(MAX_NONCE_BYTES),
        color = readNullableString(MAX_SHORT_TEXT_UTF8_BYTES),
        createdAt = readLong(),
    )

    private fun Buffer.writeCredential(value: CredentialRecordEntity) {
        writeString(value.id, MAX_IDENTIFIER_UTF8_BYTES)
        writeString(value.type, MAX_CREDENTIAL_TYPE_UTF8_BYTES)
        writeBytes(value.summaryPayload, MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES)
        writeBytes(value.summaryNonce, MAX_NONCE_BYTES)
        writeBytes(value.secretPayload, MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES)
        writeBytes(value.secretNonce, MAX_NONCE_BYTES)
        writeNullableString(value.folderId, MAX_IDENTIFIER_UTF8_BYTES)
        writeBoolean(value.isFavorite)
        writeLong(value.createdAt)
        writeLong(value.updatedAt)
        writeNullableLong(value.lastUsedAt)
    }

    private fun Buffer.readCredential(metadataSchemaVersion: Int): CredentialRecordEntity {
        val id = readString(MAX_IDENTIFIER_UTF8_BYTES)
        val type = readString(MAX_CREDENTIAL_TYPE_UTF8_BYTES)
        val legacyTitleHash = if (metadataSchemaVersion == LEGACY_BACKUP_METADATA_SCHEMA_VERSION) {
            readBytes(BLIND_INDEX_BYTES).also { bytes ->
                if (bytes.size != BLIND_INDEX_BYTES) {
                    bytes.fill(0)
                    error("Invalid legacy title blind index")
                }
            }
        } else {
            null
        }
        return try {
            CredentialRecordEntity(
                id = id,
                type = type,
                summaryPayload = readBytes(MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES),
                summaryNonce = readBytes(MAX_NONCE_BYTES),
                secretPayload = readBytes(MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES),
                secretNonce = readBytes(MAX_NONCE_BYTES),
                folderId = readNullableString(MAX_IDENTIFIER_UTF8_BYTES),
                isFavorite = readBoolean(),
                createdAt = readLong(),
                updatedAt = readLong(),
                lastUsedAt = readNullableLong(),
            )
        } finally {
            legacyTitleHash?.fill(0)
        }
    }

    private fun Buffer.writeFolderReference(value: CredentialFolderCrossRef) {
        writeString(value.credentialId, MAX_IDENTIFIER_UTF8_BYTES)
        writeString(value.folderId, MAX_IDENTIFIER_UTF8_BYTES)
    }

    private fun Buffer.readFolderReference() = CredentialFolderCrossRef(
        credentialId = readString(MAX_IDENTIFIER_UTF8_BYTES),
        folderId = readString(MAX_IDENTIFIER_UTF8_BYTES),
    )

    private fun Buffer.writeTagReference(value: CredentialTagCrossRef) {
        writeString(value.credentialId, MAX_IDENTIFIER_UTF8_BYTES)
        writeString(value.tagId, MAX_IDENTIFIER_UTF8_BYTES)
    }

    private fun Buffer.readTagReference() = CredentialTagCrossRef(
        credentialId = readString(MAX_IDENTIFIER_UTF8_BYTES),
        tagId = readString(MAX_IDENTIFIER_UTF8_BYTES),
    )

    private fun Buffer.writeAttachment(value: AttachmentRecordEntity) {
        writeString(value.id, MAX_IDENTIFIER_UTF8_BYTES)
        writeString(value.credentialId, MAX_IDENTIFIER_UTF8_BYTES)
        writeBytes(value.encryptedFilename, MAX_ATTACHMENT_FILENAME_ENCRYPTED_PAYLOAD_BYTES)
        writeBytes(value.filenameNonce, MAX_NONCE_BYTES)
        writeString(value.mimeType, MAX_METADATA_TEXT_UTF8_BYTES)
        writeLong(value.sizeBytes)
        writeString(value.storagePath, MAX_METADATA_TEXT_UTF8_BYTES)
        writeString(value.keyDerivationContext, MAX_IDENTIFIER_UTF8_BYTES)
        writeLong(value.createdAt)
        writeInt(value.contentFormatVersion)
        writeString(value.storageState, MAX_SHORT_TEXT_UTF8_BYTES)
    }

    private fun Buffer.readAttachment() = AttachmentRecordEntity(
        id = readString(MAX_IDENTIFIER_UTF8_BYTES),
        credentialId = readString(MAX_IDENTIFIER_UTF8_BYTES),
        encryptedFilename = readBytes(MAX_ATTACHMENT_FILENAME_ENCRYPTED_PAYLOAD_BYTES),
        filenameNonce = readBytes(MAX_NONCE_BYTES),
        mimeType = readString(MAX_METADATA_TEXT_UTF8_BYTES),
        sizeBytes = readLong(),
        storagePath = readString(MAX_METADATA_TEXT_UTF8_BYTES),
        keyDerivationContext = readString(MAX_IDENTIFIER_UTF8_BYTES),
        createdAt = readLong(),
        contentFormatVersion = readInt(),
        storageState = readString(MAX_SHORT_TEXT_UTF8_BYTES),
    )

    private fun Buffer.writePasswordHistory(value: PasswordHistoryRecordEntity) {
        writeString(value.id, MAX_IDENTIFIER_UTF8_BYTES)
        writeString(value.credentialId, MAX_IDENTIFIER_UTF8_BYTES)
        writeBytes(value.encryptedPassword, MAX_PASSWORD_HISTORY_ENCRYPTED_PAYLOAD_BYTES)
        writeBytes(value.passwordNonce, MAX_NONCE_BYTES)
        writeLong(value.changedAt)
    }

    private fun Buffer.readPasswordHistory() = PasswordHistoryRecordEntity(
        id = readString(MAX_IDENTIFIER_UTF8_BYTES),
        credentialId = readString(MAX_IDENTIFIER_UTF8_BYTES),
        encryptedPassword = readBytes(MAX_PASSWORD_HISTORY_ENCRYPTED_PAYLOAD_BYTES),
        passwordNonce = readBytes(MAX_NONCE_BYTES),
        changedAt = readLong(),
    )

    private fun Buffer.writeCount(value: Int): Buffer {
        require(value >= 0)
        return writeInt(value)
    }

    private fun Buffer.readCount(): Int = readInt().also { require(it >= 0) }

    private fun Buffer.writeBoolean(value: Boolean): Buffer = writeByte(if (value) 1 else 0)

    private fun Buffer.readBoolean(): Boolean = when (readByte().toInt()) {
        0 -> false
        1 -> true
        else -> error("Invalid Boolean marker")
    }

    private fun Buffer.writeNullableLong(value: Long?): Buffer = if (value == null) {
        writeByte(0)
    } else {
        writeByte(1).writeLong(value)
    }

    private fun Buffer.readNullableLong(): Long? = when (readByte().toInt()) {
        0 -> null
        1 -> readLong()
        else -> error("Invalid nullable marker")
    }

    private fun Buffer.writeNullableString(value: String?, maximumBytes: Int): Buffer = if (value == null) {
        writeByte(0)
    } else {
        writeByte(1).writeString(value, maximumBytes)
    }

    private fun Buffer.readNullableString(maximumBytes: Int): String? = when (readByte().toInt()) {
        0 -> null
        1 -> readString(maximumBytes)
        else -> error("Invalid nullable marker")
    }

    private fun Buffer.writeString(value: String, maximumBytes: Int): Buffer {
        val encoded = value.encodeToByteArray(throwOnInvalidSequence = true)
        try {
            require(encoded.size <= maximumBytes)
            return writeInt(encoded.size).write(encoded)
        } finally {
            encoded.fill(0)
        }
    }

    private fun Buffer.readString(maximumBytes: Int): String {
        val encoded = readBytes(maximumBytes)
        return try {
            encoded.decodeToString(throwOnInvalidSequence = true)
        } finally {
            encoded.fill(0)
        }
    }

    private fun Buffer.writeBytes(value: ByteArray, maximumBytes: Int): Buffer {
        require(value.size <= maximumBytes)
        return writeInt(value.size).write(value)
    }

    private fun Buffer.readBytes(maximumBytes: Int): ByteArray {
        val size = readInt()
        require(size in 0..maximumBytes)
        require(size.toLong() <= this.size)
        return readByteArray(size.toLong())
    }

    private fun requireSupportedMetadataSchema(version: Int) {
        require(version in LEGACY_BACKUP_METADATA_SCHEMA_VERSION..CURRENT_BACKUP_METADATA_SCHEMA_VERSION)
    }

    private const val MAX_IDENTIFIER_UTF8_BYTES = 256 * 4
    private const val MAX_CREDENTIAL_TYPE_UTF8_BYTES = MAX_IDENTIFIER_UTF8_BYTES + 16
    private const val MAX_METADATA_TEXT_UTF8_BYTES = 16 * 1024 * 4
    private const val MAX_SHORT_TEXT_UTF8_BYTES = 256
    private const val MAX_SALT_BYTES = 64
    private const val MAX_NONCE_BYTES = 64
    private const val MAX_FIXED_SECRET_BYTES = 68
    private const val BLIND_INDEX_BYTES = 32
    private const val MANIFEST_MAX_BYTES = 64
    private const val METADATA_MAX_BYTES = 4 * 1024
    private const val SMALL_ENTITY_MAX_BYTES = 128 * 1024
    private const val REFERENCE_MAX_BYTES = 8 * 1024
    private const val LARGE_ENTITY_MAX_BYTES = 33 * 1024 * 1024
    private const val CREDENTIAL_MAX_BYTES = 65 * 1024 * 1024
}

private fun VaultMetadataEntity.clearBinaryFields() {
    argon2Salt.fill(0)
    wrappedVek.fill(0)
    vekNonce.fill(0)
    encryptedVerificationRecord.fill(0)
    verificationNonce.fill(0)
}

private fun FolderRecordEntity.clearBinaryFields() {
    nameHash.fill(0)
    encryptedPayload.fill(0)
    payloadNonce.fill(0)
}

private fun TagRecordEntity.clearBinaryFields() {
    nameHash.fill(0)
    encryptedPayload.fill(0)
    payloadNonce.fill(0)
}

private fun CredentialRecordEntity.clearBinaryFields() {
    summaryPayload.fill(0)
    summaryNonce.fill(0)
    secretPayload.fill(0)
    secretNonce.fill(0)
}

internal const val LEGACY_BACKUP_METADATA_SCHEMA_VERSION = 1
internal const val CURRENT_BACKUP_METADATA_SCHEMA_VERSION = 2

private fun AttachmentRecordEntity.clearBinaryFields() {
    encryptedFilename.fill(0)
    filenameNonce.fill(0)
}

private fun PasswordHistoryRecordEntity.clearBinaryFields() {
    encryptedPassword.fill(0)
    passwordNonce.fill(0)
}
