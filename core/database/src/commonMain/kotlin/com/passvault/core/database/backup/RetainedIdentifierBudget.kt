package com.passvault.core.database.backup

/**
 * Bounds the identifier text retained while a streaming backup is validated.
 * The validator still needs exact strings for collision-free referential checks,
 * so it accounts them instead of replacing them with truncated digests.
 */
internal class RetainedIdentifierBudget(
    private val maximumBytes: Long,
) {
    private var retainedBytes = 0L

    init {
        require(maximumBytes >= 0L)
    }

    fun retain(identifier: String) {
        val encodedBytes = identifier.utf8Size()
        require(encodedBytes <= maximumBytes - retainedBytes) {
            "Backup identifiers exceed the validation memory budget"
        }
        retainedBytes += encodedBytes
    }

    fun retain(first: String, second: String?) {
        retain(first)
        second?.let(::retain)
    }
}

/** Worst-case identifier occurrences held by the validator's sets and maps. */
internal fun BackupStreamManifest.retainedIdentifierCount(): Long =
    credentialCount.toLong() +
        credentialFolderReferenceCount.toLong() +
        folderCount.toLong() * IDENTIFIERS_PER_FOLDER +
        tagCount.toLong() +
        credentialFolderReferenceCount.toLong() * IDENTIFIERS_PER_RELATIONSHIP +
        credentialTagReferenceCount.toLong() * IDENTIFIERS_PER_RELATIONSHIP +
        attachmentCount.toLong() * IDENTIFIERS_PER_ATTACHMENT +
        passwordHistoryCount.toLong() * IDENTIFIERS_PER_PASSWORD_HISTORY

internal fun BackupStreamManifest.requireRetentionBound() {
    require(retainedIdentifierCount() <= BackupLimits.MAX_RETAINED_IDENTIFIER_COUNT) {
        "Backup relationships exceed the validation memory budget"
    }
}

private fun String.utf8Size(): Long {
    var bytes = 0L
    var index = 0
    while (index < length) {
        val current = this[index]
        when {
            current.code <= MAX_ONE_BYTE_CODE_POINT -> bytes += 1L
            current.code <= MAX_TWO_BYTE_CODE_POINT -> bytes += 2L
            current.isHighSurrogate() -> {
                require(index + 1 < length && this[index + 1].isLowSurrogate())
                bytes += 4L
                index++
            }
            else -> {
                require(!current.isLowSurrogate())
                bytes += 3L
            }
        }
        index++
    }
    return bytes
}

private const val IDENTIFIERS_PER_FOLDER = 2L
private const val IDENTIFIERS_PER_RELATIONSHIP = 2L
private const val IDENTIFIERS_PER_ATTACHMENT = 2L
private const val IDENTIFIERS_PER_PASSWORD_HISTORY = 2L
private const val MAX_ONE_BYTE_CODE_POINT = 0x7F
private const val MAX_TWO_BYTE_CODE_POINT = 0x7FF
