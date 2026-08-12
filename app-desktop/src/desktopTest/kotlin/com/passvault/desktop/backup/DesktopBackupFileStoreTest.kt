package com.passvault.desktop.backup

import kotlin.test.Test
import kotlin.test.assertEquals

class DesktopBackupFileStoreTest {
    @Test
    fun `safe file names preserve normal names`() {
        assertEquals(
            "PassVault-2026-08-10.pvault",
            safeDesktopBackupFileName("PassVault-2026-08-10.pvault", "backup"),
        )
    }

    @Test
    fun `unsafe file names use a neutral fallback`() {
        val unsafeNames = listOf(
            "../spoof.pvault",
            "folder\\spoof.pvault",
            "report\u202Eexe.pvault",
            "line\nbreak.pvault",
            ".",
            " ",
        )

        unsafeNames.forEach { name ->
            assertEquals("backup", safeDesktopBackupFileName(name, "backup"))
        }
    }

    @Test
    fun `overlong file names use a neutral fallback`() {
        assertEquals(
            "backup",
            safeDesktopBackupFileName("a".repeat(161), "backup"),
        )
    }
}
