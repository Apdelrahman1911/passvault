package com.passvault.feature.credential

import com.passvault.core.domain.model.AttachmentMetadata
import com.passvault.core.domain.repository.AttachmentContentSink
import com.passvault.core.domain.repository.AttachmentContentSource

/** Native picker/viewer boundary for plaintext attachment streams. */
interface AttachmentFileStore {
    suspend fun selectForImport(): Result<AttachmentContentSource>

    suspend fun createOutput(
        attachment: AttachmentMetadata,
        action: AttachmentOutputAction,
    ): Result<PreparedAttachmentOutput>
}

/**
 * Plaintext output staged by a platform adapter.
 *
 * [sink] is consumed while the repository owns a short-lived vault-key lease.
 * [present] runs only after that lease has been released, so a system viewer or
 * export sheet cannot delay a background lock. [abort] must be idempotent and
 * remove any plaintext that was not successfully handed to the operating
 * system.
 */
interface PreparedAttachmentOutput {
    val sink: AttachmentContentSink

    suspend fun present(): Result<Unit>
    suspend fun abort()
}

enum class AttachmentOutputAction {
    OPEN,
    EXPORT,
}

class AttachmentFileSelectionCancelled : Exception()

object UnavailableAttachmentFileStore : AttachmentFileStore {
    override suspend fun selectForImport(): Result<AttachmentContentSource> =
        Result.failure(IllegalStateException("Attachment file selection is unavailable"))

    override suspend fun createOutput(
        attachment: AttachmentMetadata,
        action: AttachmentOutputAction,
    ): Result<PreparedAttachmentOutput> =
        Result.failure(IllegalStateException("Attachment file output is unavailable"))
}
