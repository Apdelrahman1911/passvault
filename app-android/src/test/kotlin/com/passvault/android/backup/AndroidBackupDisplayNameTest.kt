package com.passvault.android.backup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AndroidBackupDisplayNameTest {

    @Test
    fun normalSingleLineNameIsPreserved() {
        assertEquals(
            "PassVault-2026-08-11.pvault",
            "PassVault-2026-08-11.pvault".validatedDisplayName(maxCodePoints = 160),
        )
    }

    @Test
    fun unsafeOrAmbiguousNamesAreRejected() {
        val unsafeNames = listOf(
            "../spoof.pvault",
            "folder\\spoof.pvault",
            "report\u202Eexe.pvault",
            "line\nbreak.pvault",
            ".",
            "..",
            " ",
            "broken\uD800name.pvault",
        )

        unsafeNames.forEach { name ->
            assertNull(name.validatedDisplayName(maxCodePoints = 160))
        }
    }

    @Test
    fun codePointLimitCountsSupplementaryCharactersOnce() {
        val emoji = "\uD83D\uDD10"

        assertEquals(emoji.repeat(160), emoji.repeat(160).validatedDisplayName(maxCodePoints = 160))
        assertNull(emoji.repeat(161).validatedDisplayName(maxCodePoints = 160))
    }
}
