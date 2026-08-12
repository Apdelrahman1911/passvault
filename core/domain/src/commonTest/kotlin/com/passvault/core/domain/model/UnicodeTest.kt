package com.passvault.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UnicodeTest {
    @Test
    fun `counts basic and non-latin text`() {
        assertEquals(0, "".codePointLength())
        assertEquals(3, "abc".codePointLength())
        assertEquals(3, "الع".codePointLength())
        assertEquals(3, "日本語".codePointLength())
    }

    @Test
    fun `counts surrogate-pair emoji as one code point`() {
        assertEquals(3, "🔐🔑🔒".codePointLength())
        assertEquals(3, "a🔐b".codePointLength())
    }

    @Test
    fun `counts unpaired surrogates defensively`() {
        assertEquals(1, "\uD83D".codePointLength())
        assertEquals(1, "\uDD10".codePointLength())
        assertEquals(2, "\uD83Da".codePointLength())
    }

    @Test
    fun `takes complete code points without splitting emoji`() {
        assertEquals("", "a🔐b".takeCodePoints(0))
        assertEquals("a", "a🔐b".takeCodePoints(1))
        assertEquals("a🔐", "a🔐b".takeCodePoints(2))
        assertEquals("a🔐b", "a🔐b".takeCodePoints(3))
        assertEquals("a🔐b", "a🔐b".takeCodePoints(4))
    }

    @Test
    fun `taking code points rejects a negative bound`() {
        assertFailsWith<IllegalArgumentException> {
            "secret".takeCodePoints(-1)
        }
    }

    @Test
    fun `well formed Unicode validation rejects every unpaired surrogate shape`() {
        assertTrue("PassVault 🔐".hasWellFormedUnicode())
        assertFalse("\uD83D".hasWellFormedUnicode())
        assertFalse("\uDD10".hasWellFormedUnicode())
        assertFalse("\uD83Da".hasWellFormedUnicode())
        assertFalse("\uD83D\uD83D\uDD10".hasWellFormedUnicode())
    }

    @Test
    fun `single-line validation accepts ordinary international text`() {
        assertTrue("PassVault العربية 日本語 🔐".hasOnlySafeSingleLineCodePoints())
    }

    @Test
    fun `single-line validation rejects controls malformed UTF-16 and format controls`() {
        val unsafeValues = listOf(
            "line\nbreak",
            "invoice\u202Efdp.exe",
            "hidden\u200Bseparator",
            "\uD83D",
            "tag\uDB40\uDC01",
        )

        unsafeValues.forEach { value ->
            assertFalse(value.hasOnlySafeSingleLineCodePoints())
        }
    }
}
