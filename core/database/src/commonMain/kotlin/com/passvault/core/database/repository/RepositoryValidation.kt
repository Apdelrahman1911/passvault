package com.passvault.core.database.repository

import com.passvault.core.crypto.PaddedPayload
import com.passvault.core.domain.model.codePointLength
import com.passvault.core.domain.model.hasWellFormedUnicode
import com.passvault.core.domain.model.hasOnlySafeSingleLineCodePoints

internal const val MAX_RECORD_IDENTIFIER_LENGTH = 256
internal const val MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES = 32 * 1024 * 1024
internal val MAX_CREDENTIAL_PLAINTEXT_BYTES =
    PaddedPayload.maximumPlaintextSize(MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES - ENCRYPTED_ENVELOPE_OVERHEAD_BYTES)
internal const val MAX_FOLDER_ENCRYPTED_PAYLOAD_BYTES = 64 * 1024
internal const val MAX_TAG_ENCRYPTED_PAYLOAD_BYTES = 64 * 1024
internal const val MAX_ATTACHMENT_FILENAME_ENCRYPTED_PAYLOAD_BYTES = MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES
internal const val MAX_PASSWORD_HISTORY_ENCRYPTED_PAYLOAD_BYTES = MAX_CREDENTIAL_ENCRYPTED_PAYLOAD_BYTES
private const val ENCRYPTED_ENVELOPE_OVERHEAD_BYTES = 4 + 16

internal fun ByteArray.decodeUtf8Strict(): String =
    decodeToString(throwOnInvalidSequence = true)

internal fun String.requireRecordIdentifier(fieldName: String) {
    require(
        isNotBlank() &&
            hasAtMostCodePoints(MAX_RECORD_IDENTIFIER_LENGTH) &&
            hasOnlySafeTextCodePoints() &&
            none { it == '/' || it == '\\' },
    ) { "$fieldName is invalid" }
}

internal fun String?.requireBoundedMetadata(
    fieldName: String,
    maxLength: Int,
) {
    if (this == null) return
    require(hasAtMostCodePoints(maxLength) && hasOnlySafeTextCodePoints()) {
        "$fieldName is invalid"
    }
}

internal fun String.hasAtMostCodePoints(maxLength: Int): Boolean =
    hasWellFormedUnicode() && codePointLength() <= maxLength

/**
 * Rejects controls, malformed UTF-16, Unicode format controls, and explicit
 * line/paragraph separators in metadata rendered as a single-line label.
 *
 * Iterating complete code points is important here: several format controls
 * live outside the BMP and are represented by otherwise innocuous-looking
 * surrogate pairs on Kotlin/JVM and Kotlin/Native.
 */
internal fun String.hasOnlySafeTextCodePoints(): Boolean {
    return hasOnlySafeSingleLineCodePoints()
}
