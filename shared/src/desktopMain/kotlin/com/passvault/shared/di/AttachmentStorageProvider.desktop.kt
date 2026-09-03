package com.passvault.shared.di

import com.passvault.core.database.attachment.AttachmentBlobStore
import com.passvault.core.database.attachment.LocalAttachmentBlobStore
import com.passvault.desktop.security.createOrHardenPrivateDesktopDirectory
import java.nio.file.Files
import java.nio.file.Path

actual fun createAttachmentBlobStore(context: Any): AttachmentBlobStore {
    return createDesktopAttachmentBlobStore(
        Path.of(System.getProperty("user.home"), ".passvault"),
    )
}

internal fun createDesktopAttachmentBlobStore(dataRoot: Path): AttachmentBlobStore {
    val privateDataRoot = createOrHardenPrivateDesktopDirectory(dataRoot)
    val root = createOrHardenPrivateDesktopDirectory(privateDataRoot.resolve("attachments"))
    ATTACHMENT_STORAGE_CHILDREN.forEach { child ->
        createOrHardenPrivateDesktopDirectory(root.resolve(child))
    }
    return LocalAttachmentBlobStore(
        rootPath = root.toString(),
        availableBytesProvider = { Files.getFileStore(root).usableSpace },
    )
}

private val ATTACHMENT_STORAGE_CHILDREN = listOf("objects", "staging")
