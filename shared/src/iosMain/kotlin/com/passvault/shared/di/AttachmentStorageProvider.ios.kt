@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.passvault.shared.di

import com.passvault.core.database.attachment.AttachmentBlobStore
import com.passvault.core.database.attachment.LocalAttachmentBlobStore
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileProtectionComplete
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSURLIsExcludedFromBackupKey
import platform.Foundation.NSUserDomainMask

actual fun createAttachmentBlobStore(context: Any): AttachmentBlobStore {
    val fileManager = NSFileManager.defaultManager
    val applicationSupport = fileManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )?.path ?: error("The iOS Application Support directory is unavailable")
    val root = "$applicationSupport/PassVault/attachments"
    val attributes = mapOf<Any?, Any?>(NSFileProtectionKey to NSFileProtectionComplete)
    check(
        fileManager.createDirectoryAtPath(
            path = root,
            withIntermediateDirectories = true,
            attributes = attributes,
            error = null,
        ),
    ) { "The iOS attachment directory could not be created" }
    check(fileManager.setAttributes(attributes, ofItemAtPath = root, error = null)) {
        "The iOS attachment directory could not be protected"
    }
    val url = NSURL.fileURLWithPath(root)
    check(url.setResourceValue(true, forKey = NSURLIsExcludedFromBackupKey, error = null)) {
        "The iOS attachment directory could not be excluded from device backups"
    }
    return LocalAttachmentBlobStore(
        rootPath = root,
        availableBytesProvider = {
            val attributes = fileManager.attributesOfFileSystemForPath(root, error = null)
            (attributes?.get(NSFileSystemFreeSize) as? NSNumber)?.longLongValue
        },
    )
}
