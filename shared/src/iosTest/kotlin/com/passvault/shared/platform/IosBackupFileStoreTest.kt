@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.passvault.shared.platform

import kotlinx.coroutines.test.runTest
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileProtectionComplete
import platform.Foundation.NSFileProtectionKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IosBackupFileStoreTest {
    @Test
    fun `export staging protects directory and file before writing`() = runTest {
        val fileManager = NSFileManager.defaultManager
        val protectedPaths = mutableListOf<String>()
        val output = IosBackupFileStore(fileManager) { protectedPaths += it }
            .create("protection-test.pvault")
            .getOrThrow()
        try {
            output.sink.write(byteArrayOf(1, 2, 3), 3)

            assertEquals(
                listOf(output.file.path.substringBeforeLast('/'), output.file.path),
                protectedPaths,
            )
            assertTrue(fileManager.fileExistsAtPath(output.file.path))
            assertEquals(NSFileProtectionComplete, iosBackupProtectionAttributes()[NSFileProtectionKey])
        } finally {
            output.sink.abort()
        }
    }

    @Test
    fun `protection failure prevents export file creation`() = runTest {
        val fileManager = NSFileManager.defaultManager
        val output = IosBackupFileStore(fileManager) { error("protection unavailable") }
            .create("protection-failure.pvault")
            .getOrThrow()
        try {
            assertTrue(output.sink.runCatching { write(byteArrayOf(1), 1) }.isFailure)
            assertFalse(fileManager.fileExistsAtPath(output.file.path))
        } finally {
            output.sink.abort()
        }
    }
}
