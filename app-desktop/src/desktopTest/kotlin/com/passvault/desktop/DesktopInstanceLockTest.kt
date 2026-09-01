package com.passvault.desktop

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DesktopInstanceLockTest {

    @Test
    fun `only one process lease can own a vault directory at a time`() {
        val dataDirectory = Files.createTempDirectory("passvault-instance-lock-test-")
        try {
            val first = assertNotNull(DesktopInstanceLock.acquire(dataDirectory))
            try {
                assertNull(DesktopInstanceLock.acquire(dataDirectory))
            } finally {
                first.close()
            }

            DesktopInstanceLock.acquire(dataDirectory)?.close()
                ?: error("A released Desktop instance lock must be acquirable immediately")
        } finally {
            dataDirectory.toFile().deleteRecursively()
        }
    }
}
