@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.passvault.shared.platform

import platform.Foundation.NSFileManager
import platform.Foundation.NSFileProtectionComplete
import platform.Foundation.NSFileProtectionKey
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IosAttachmentFileStoreTest {
    @Test
    fun `picker copy is protected before the import path is returned`() {
        withTemporaryFile { fileManager, path ->
            val events = mutableListOf<String>()

            val result = prepareIosAttachmentImportPath(
                path = path,
                protectPath = {
                    events += "protect"
                    protectIosAttachmentPath(fileManager, it)
                },
                deletePath = { events += "delete" },
            )

            assertEquals(path, result.getOrThrow())
            assertEquals(listOf("protect"), events)
            assertEquals(NSFileProtectionComplete, iosAttachmentProtectionAttributes()[NSFileProtectionKey])
        }
    }

    @Test
    fun `picker copy is deleted when immediate protection fails`() {
        withTemporaryFile { fileManager, path ->
            val events = mutableListOf<String>()

            val result = prepareIosAttachmentImportPath(
                path = path,
                protectPath = {
                    events += "protect"
                    error("protection unavailable")
                },
                deletePath = {
                    events += "delete"
                    assertTrue(fileManager.removeItemAtPath(it, error = null))
                },
            )

            assertTrue(result.isFailure)
            assertEquals(listOf("protect", "delete"), events)
            assertFalse(fileManager.fileExistsAtPath(path))
        }
    }

    private fun withTemporaryFile(block: (NSFileManager, String) -> Unit) {
        val fileManager = NSFileManager.defaultManager
        val path = "${NSTemporaryDirectory()}passvault-picker-${NSUUID.UUID().UUIDString}"
        assertTrue(fileManager.createFileAtPath(path, contents = null, attributes = null))
        try {
            block(fileManager, path)
        } finally {
            if (fileManager.fileExistsAtPath(path)) fileManager.removeItemAtPath(path, error = null)
        }
    }
}
