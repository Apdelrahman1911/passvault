package com.passvault.core.database.backup

/** Lightweight result retained after streamed row validation. */
internal data class ValidatedBackupStream(
    val manifest: BackupStreamManifest,
    val managedAttachmentIds: List<String>,
)

/** Stateful referential validator that never retains encrypted row payloads. */
internal interface BackupStreamValidator {
    val manifest: BackupStreamManifest

    fun accept(value: BackupMetadataValue)

    fun finish(): ValidatedBackupStream
}
