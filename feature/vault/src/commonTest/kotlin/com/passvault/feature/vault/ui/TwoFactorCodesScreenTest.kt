package com.passvault.feature.vault.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class TwoFactorCodesScreenTest {
    @Test
    fun `verification codes are grouped without changing digits`() {
        assertEquals("123 456", formatTotpCode("123456"))
        assertEquals("1234 5678", formatTotpCode("12345678"))
        assertEquals("12345", formatTotpCode("12345"))
    }
}
