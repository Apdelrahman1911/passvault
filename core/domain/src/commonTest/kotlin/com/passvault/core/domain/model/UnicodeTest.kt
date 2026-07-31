package com.passvault.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

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
}
