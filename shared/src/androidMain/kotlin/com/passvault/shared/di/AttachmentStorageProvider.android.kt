package com.passvault.shared.di

import android.content.Context
import com.passvault.core.database.attachment.AttachmentBlobStore
import com.passvault.core.database.attachment.LocalAttachmentBlobStore
import java.io.File

actual fun createAttachmentBlobStore(context: Any): AttachmentBlobStore {
    val appContext = (context as Context).applicationContext
    return LocalAttachmentBlobStore(File(appContext.noBackupFilesDir, "attachments").absolutePath)
}
