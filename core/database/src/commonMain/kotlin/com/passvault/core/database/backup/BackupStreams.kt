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
    const val LEGACY_MAX_BACKUP_BYTES = 128L * 1024L * 1024L
    /** Largest independently materialized metadata row (one maximum-size credential). */
    const val MAX_ENTITY_RECORD_BYTES = 65 * 1024 * 1024
    const val RECORD_PLAINTEXT_BYTES = 256 * 1024
    const val MAX_BACKUP_BYTES = 16L * 1024L * 1024L * 1024L
}
