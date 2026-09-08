package com.passvault.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NumericPasswordSequenceTest {
    @Test
    fun `ascending and descending numeric master passwords receive the sequence penalty`() {
        listOf("012345678901", "123456789012", "987654321098").forEach { password ->
            val chars = password.toCharArray()
            val sensitive = SensitiveText.from(password)
            try {
                assertTrue(PasswordStrengthEvaluator.score(password) <= PasswordScore.WEAK)
                assertEquals(PasswordStrengthEvaluator.score(password), PasswordStrengthEvaluator.score(chars))
                assertFalse(MasterPasswordPolicy.accepts(password))
                assertFalse(MasterPasswordPolicy.accepts(sensitive))
                // The admission fix must not lock out vaults created with an older policy.
                assertTrue(MasterPasswordPolicy.acceptsExisting(sensitive))
            } finally {
                chars.fill('\u0000')
                sensitive.clear()
            }
        }
    }

    @Test
    fun `every four-digit table window is detected in both directions`() {
        "0123456789".windowed(4).forEach { window ->
            listOf(window, window.reversed()).forEach { sequence ->
                val password = "62804913$sequence"
                val chars = password.toCharArray()
                try {
                    assertTrue(PasswordStrengthEvaluator.score(password) <= PasswordScore.WEAK)
                    assertEquals(PasswordStrengthEvaluator.score(password), PasswordStrengthEvaluator.score(chars))
                } finally {
                    chars.fill('\u0000')
                }
            }
        }
    }

    @Test
    fun `numeric input is not rejected just for containing digits`() {
        val password = "628049136208"
        val sensitive = SensitiveText.from(password)
        try {
            assertEquals(PasswordScore.FAIR, PasswordStrengthEvaluator.score(password))
            assertTrue(MasterPasswordPolicy.accepts(password))
            assertTrue(MasterPasswordPolicy.accepts(sensitive))
        } finally {
            sensitive.clear()
        }
    }

    @Test
    fun `leetspeak and alphabetic penalties remain independent of numeric detection`() {
        listOf("p@ssw0rdpass!", "abcdefghijkl", "lkjihgfedcba").forEach { password ->
            assertTrue(PasswordStrengthEvaluator.score(password) <= PasswordScore.WEAK)
        }
        assertTrue(MasterPasswordPolicy.accepts("correct horse battery staple"))
        assertTrue(MasterPasswordPolicy.accepts("😀😁😂😃😄😅😆😇😈😉😊😋"))
    }
}
