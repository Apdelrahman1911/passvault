@file:Suppress("MatchingDeclarationName")

package com.passvault.core.database.attachment

import com.passvault.core.database.entity.AttachmentRecordEntity
import com.passvault.core.domain.repository.AttachmentCorruptedException
import com.passvault.core.domain.repository.AttachmentPolicy

/** Stable states that may be exposed outside the attachment transaction boundary. */
internal enum class AttachmentStorageKind {
    LEGACY,
    MANAGED,
}

/**
 * Validates the redundant state/format invariant before either value controls
 * reads or destructive filesystem work. A single-column state downgrade must
 * fail closed instead of making an intact current-format object look legacy.
 */
internal fun AttachmentRecordEntity.requireStableStorageKind(): AttachmentStorageKind = when {
    storageState == AttachmentRecordEntity.STORAGE_STATE_LEGACY && contentFormatVersion == 0 ->
        AttachmentStorageKind.LEGACY
    storageState == AttachmentRecordEntity.STORAGE_STATE_READY &&
        contentFormatVersion == AttachmentPolicy.CONTENT_FORMAT_VERSION -> AttachmentStorageKind.MANAGED
    else -> throw AttachmentCorruptedException()
}

internal fun AttachmentRecordEntity.referencesManagedObject(): Boolean =
    contentFormatVersion == AttachmentPolicy.CONTENT_FORMAT_VERSION
