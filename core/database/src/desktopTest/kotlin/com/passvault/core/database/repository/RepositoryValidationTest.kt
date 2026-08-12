package com.passvault.core.database.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.text.CharacterCodingException

class RepositoryUtf8ValidationTest {
    @Test
    fun `strict decoder preserves valid UTF-8`() {
        assertEquals("PassVault 🔐", "PassVault 🔐".encodeToByteArray().decodeUtf8Strict())
    }

    @Test
    fun `strict decoder rejects malformed UTF-8`() {
        assertFailsWith<CharacterCodingException> {
            byteArrayOf(0xC3.toByte(), 0x28).decodeUtf8Strict()
        }
    }
}

class RepositoryValidationTest {

    @Test
    fun `code point bounds count supplementary characters once and reject malformed Unicode`() {
        assertTrue("🔐".repeat(256).hasAtMostCodePoints(256))
        assertFalse("🔐".repeat(257).hasAtMostCodePoints(256))
        assertFalse("valid\uD800".hasAtMostCodePoints(256))
    }

    @Test
    fun `single line metadata accepts ordinary Unicode and supplementary characters`() {
        assertTrue("résumé-🔐.txt".hasOnlySafeTextCodePoints())
    }

    @Test
    fun `single line metadata rejects controls separators and Unicode formatting`() {
        listOf(
            "control\u0000character",
            "line\u2028separator",
            "paragraph\u2029separator",
            "right-to-left\u202Eoverride",
            "word\u2060joiner",
            "supplementary\uDB40\uDC01format",
            "unpaired-high\uD800surrogate",
            "unpaired-low\uDC00surrogate",
        ).forEach { unsafe ->
            assertFalse(unsafe.hasOnlySafeTextCodePoints(), unsafe)
        }
    }
}
