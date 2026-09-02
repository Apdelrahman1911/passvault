package com.passvault.core.database.backup

/** One-shot bounded input for a selected encrypted backup file. */
interface BackupContentSource {
    val declaredSizeBytes: Long?
    suspend fun read(buffer: ByteArray): Int

    /**
     * Reopens the same selected object at byte zero.
     *
     * Streaming restore authenticates the complete object before beginning an
     * atomic Room replacement, then replays its metadata. The two passes are
     * cryptographically transcript-bound; implementations therefore need only
     * reopen the same platform selection, not buffer it in memory.
     */
    suspend fun rewind()

    suspend fun close()
}

/** Atomic or best-effort transactional output for an encrypted backup file. */
interface BackupContentSink {
    suspend fun write(buffer: ByteArray, byteCount: Int)
    suspend fun commit()
    suspend fun abort()
}

object BackupLimits {
    const val FORMAT_VERSION = 2
    const val MAX_ENTITY_COUNT = 1_000_000
    /**
     * Maximum number of identifier occurrences that the streaming validator
     * may need to retain across its sets and maps. This is intentionally lower
     * than applying [MAX_ENTITY_COUNT] independently to every record type.
     */
    const val MAX_RETAINED_IDENTIFIER_COUNT = 1_000_000L

    /** Maximum aggregate UTF-8 bytes accepted for retained validator identifiers. */
    const val MAX_RETAINED_IDENTIFIER_BYTES = 64L * 1024L * 1024L
    /**
     * Maximum outer content records accepted for one encrypted attachment object.
     *
     * Older writers inherited short-read boundaries from buffered filesystem sources, so this
     * deliberately permits substantially more records than ideal 256 KiB chunking while still
     * reducing a one-byte-record attack from roughly 100 million AEAD operations to 65,536.
     */
    const val MAX_ATTACHMENT_CONTENT_RECORDS = 65_536L
    /** Maximum plaintext accepted by the legacy v1 snapshot decoder. */
    const val LEGACY_MAX_SNAPSHOT_BYTES = 64 * 1024 * 1024
    /**
     * A shipped v1 snapshot plus the versioned AEAD prefix and both the canonical and
     * legacy duplicated authentication-tag representations.
     */
    const val LEGACY_MAX_CIPHERTEXT_BYTES = LEGACY_MAX_SNAPSHOT_BYTES + 4 + (2 * 16)
    /** Base64 ciphertext plus a bounded allowance for the fixed v1 JSON header. */
    const val LEGACY_MAX_BACKUP_BYTES =
        (((LEGACY_MAX_CIPHERTEXT_BYTES.toLong() + 2L) / 3L) * 4L) + 4_096L
    const val RECORD_PLAINTEXT_BYTES = 256 * 1024
    const val ATTACHMENT_CONTROL_PLAINTEXT_BYTES = 1024
    const val FINAL_RECORD_PLAINTEXT_BYTES = 64
    const val MAX_BACKUP_BYTES = 16L * 1024L * 1024L * 1024L
}
