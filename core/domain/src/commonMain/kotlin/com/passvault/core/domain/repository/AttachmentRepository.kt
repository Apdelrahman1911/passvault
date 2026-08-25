package com.passvault.core.domain.repository

import com.passvault.core.domain.model.AttachmentId
import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.hasOnlySafeSingleLineCodePoints

/**
 * One-shot plaintext input selected through a platform document picker.
 * Implementations must return -1 only at EOF and must never return more bytes
 * than the supplied buffer can hold.
 */
interface AttachmentContentSource {
    val displayName: String
    val claimedMimeType: String?
    val declaredSizeBytes: Long?

    suspend fun read(buffer: ByteArray): Int

    /** Closes handles and removes any platform-owned plaintext import copy. */
    suspend fun close()
}

/**
 * Platform-controlled plaintext output. The repository only sees this narrow
 * streaming boundary and never receives an arbitrary destination path.
 */
interface AttachmentContentSink {
    /** The implementation must consume or copy [byteCount] bytes before returning. */
    suspend fun write(buffer: ByteArray, byteCount: Int)
    suspend fun commit()
    suspend fun abort()
}

interface AttachmentRepository {
    suspend fun import(
        credentialId: CredentialId,
        source: AttachmentContentSource,
    ): Result<AttachmentMetadata>

    suspend fun rename(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
        newFileName: String,
    ): Result<AttachmentMetadata>

    suspend fun delete(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
    ): Result<Unit>

    suspend fun copyContentTo(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
        sink: AttachmentContentSink,
    ): Result<Unit>

    suspend fun verify(
        credentialId: CredentialId,
        attachmentId: AttachmentId,
    ): Result<Unit>
}

/** Stable, non-sensitive failure categories that presentation code may safely disclose. */
sealed class AttachmentException : Exception()

class AttachmentFileTooLargeException : AttachmentException()

class AttachmentCountLimitException : AttachmentException()

class AttachmentTotalSizeLimitException : AttachmentException()

class AttachmentInvalidFileNameException : AttachmentException()

class AttachmentLegacyContentUnavailableException : AttachmentException()

class AttachmentCorruptedException : AttachmentException()

object AttachmentPolicy {
    const val CONTENT_FORMAT_VERSION = 1
    const val MAX_ATTACHMENTS_PER_CREDENTIAL = 20
    const val MAX_FILE_SIZE_BYTES = 100L * 1024L * 1024L
    const val MAX_TOTAL_SIZE_PER_CREDENTIAL_BYTES = 512L * 1024L * 1024L
    const val CONTENT_CHUNK_BYTES = 256 * 1024
    const val MAX_FILE_NAME_CODE_POINTS = 255
    const val MAX_MIME_TYPE_CODE_POINTS = 127

    fun validateFileName(value: String): String {
        val fileName = value.trim()
        val isValid = fileName.isNotEmpty() &&
            fileName.any { character -> character != '.' } &&
            !fileName.endsWith('.') &&
            fileName.codePointLength() <= MAX_FILE_NAME_CODE_POINTS &&
            fileName.hasOnlySafeSingleLineCodePoints() &&
            fileName.none { character -> character in WINDOWS_INVALID_FILE_NAME_CHARACTERS } &&
            !fileName.substringBefore('.').uppercase().let(WINDOWS_RESERVED_FILE_STEMS::contains)
        if (!isValid) throw AttachmentInvalidFileNameException()
        return fileName
    }

    /**
     * Older vaults can contain names admitted by earlier releases. Keep those
     * rows readable so one historical name cannot block the whole credential;
     * output paths still use [validateFileName] and reject unsafe values.
     */
    fun validateStoredFileName(value: String): String {
        val isValid = value.isNotEmpty() &&
            value.codePointLength() <= MAX_FILE_NAME_CODE_POINTS &&
            value.hasOnlySafeSingleLineCodePoints() &&
            value.none { character -> character == '/' || character == '\\' || character == ':' }
        if (!isValid) throw AttachmentInvalidFileNameException()
        return value
    }

    /** Key shared by duplicate detection and Windows filename equivalence. */
    fun canonicalFileNameKey(value: String): String =
        value.trimEnd { character -> character.isWhitespace() || character == '.' }.lowercase()

    fun validateFileSize(sizeBytes: Long) {
        if (sizeBytes !in 0..MAX_FILE_SIZE_BYTES) throw AttachmentFileTooLargeException()
    }

    private val WINDOWS_INVALID_FILE_NAME_CHARACTERS = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
    private val WINDOWS_RESERVED_FILE_STEMS = buildSet {
        addAll(listOf("CON", "PRN", "AUX", "NUL"))
        for (number in 1..9) {
            add("COM$number")
            add("LPT$number")
        }
    }
}
