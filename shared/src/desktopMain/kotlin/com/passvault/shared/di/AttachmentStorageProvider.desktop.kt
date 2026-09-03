package com.passvault.shared.di

import com.passvault.core.database.attachment.AttachmentBlobStore
import com.passvault.core.database.attachment.LocalAttachmentBlobStore
import com.passvault.desktop.security.createOrHardenPrivateDesktopDirectory
import java.nio.file.Files
import java.nio.file.Path

actual fun createAttachmentBlobStore(context: Any): AttachmentBlobStore {
    val dataRoot = createOrHardenPrivateDesktopDirectory(
        Path.of(System.getProperty("user.home"), ".passvault"),
    )
    val root = createOrHardenPrivateDesktopDirectory(dataRoot.resolve("attachments"))
    return LocalAttachmentBlobStore(
        rootPath = root.toString(),
        availableBytesProvider = { Files.getFileStore(root).usableSpace },
    )
}
