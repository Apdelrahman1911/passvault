package com.passvault.desktop

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DesktopInstanceLockTest {

    @Test
    fun `only one process lease can own a vault directory at a time`() {
        val temporaryRoot = Files.createTempDirectory("passvault-instance-lock-test-")
        val dataDirectory = temporaryRoot.resolve("data")
        try {
            assertFalse(Files.exists(dataDirectory))
            val first = assertNotNull(DesktopInstanceLock.acquire(dataDirectory))
            try {
                assertTrue(Files.isDirectory(dataDirectory))
                assertNull(DesktopInstanceLock.acquire(dataDirectory))
            } finally {
                first.close()
            }

            DesktopInstanceLock.acquire(dataDirectory)?.close()
                ?: error("A released Desktop instance lock must be acquirable immediately")
        } finally {
            temporaryRoot.toFile().deleteRecursively()
        }
    }
}
