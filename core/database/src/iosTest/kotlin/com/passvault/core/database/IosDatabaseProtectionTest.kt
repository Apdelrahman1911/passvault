@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.passvault.core.database

import platform.Foundation.NSFileProtectionComplete
import platform.Foundation.NSFileProtectionKey
import kotlin.test.Test
import kotlin.test.assertEquals

class IosDatabaseProtectionTest {
    @Test
    fun `database and every sqlite sidecar retain complete protection`() {
        assertEquals(
            NSFileProtectionComplete,
            iosDatabaseProtectionAttributes()[NSFileProtectionKey],
        )
        assertEquals(listOf("-wal", "-shm", "-journal", ""), IOS_DATABASE_SUFFIXES)
    }
}
