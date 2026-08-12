package com.passvault.shared.di

import com.passvault.core.database.attachment.AttachmentBlobStore
import com.passvault.core.database.attachment.LocalAttachmentBlobStore
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.PosixFileAttributeView
import java.nio.file.attribute.PosixFilePermission

actual fun createAttachmentBlobStore(context: Any): AttachmentBlobStore {
    val root = Path.of(System.getProperty("user.home"), ".passvault", "attachments")
    check(!Files.isSymbolicLink(root)) { "PassVault's attachment directory must not be a symbolic link" }
    Files.createDirectories(root)
    check(Files.isDirectory(root, LinkOption.NOFOLLOW_LINKS)) {
        "PassVault's attachment storage path is not a directory"
    }
    Files.getFileAttributeView(root, PosixFileAttributeView::class.java)
        ?.setPermissions(
            setOf(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.OWNER_EXECUTE,
            ),
        )
    return LocalAttachmentBlobStore(root.toString())
}
