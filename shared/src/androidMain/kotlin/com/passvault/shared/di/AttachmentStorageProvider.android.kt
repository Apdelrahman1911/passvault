package com.passvault.shared.di

import android.content.Context
import android.os.StatFs
import android.system.ErrnoException
import android.system.OsConstants
import com.passvault.core.database.attachment.AttachmentBlobStore
import com.passvault.core.database.attachment.LocalAttachmentBlobStore
import java.io.File

actual fun createAttachmentBlobStore(context: Any): AttachmentBlobStore {
    val appContext = (context as Context).applicationContext
    val root = File(appContext.noBackupFilesDir, "attachments")
    return LocalAttachmentBlobStore(
        rootPath = root.absolutePath,
        availableBytesProvider = { StatFs(root.absolutePath).availableBytes },
        isInsufficientStorageFailure = { error ->
            error.hasErrno(OsConstants.ENOSPC) || error.hasErrno(OsConstants.EDQUOT)
        },
    )
}

private fun Throwable.hasErrno(expected: Int): Boolean {
    var current: Throwable? = this
    var depth = 0
    while (current != null && depth < MAX_CAUSE_DEPTH) {
        if ((current as? ErrnoException)?.errno == expected) return true
        current = current.cause.takeUnless { cause -> cause === current }
        depth++
    }
    return false
}

private const val MAX_CAUSE_DEPTH = 16
