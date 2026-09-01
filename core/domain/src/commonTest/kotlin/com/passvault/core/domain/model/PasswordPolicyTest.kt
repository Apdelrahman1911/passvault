package com.passvault.core.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordPolicyTest {
    @Test
    fun `new password policies count supplementary characters once`() {
        val minimum = "😀😁😂😃😄😅😆😇😈😉😊😋"
        val tooShort = minimum.takeCodePoints(MasterPasswordPolicy.MIN_LENGTH - 1)
        val tooLong = "🔐".repeat(MasterPasswordPolicy.MAX_LENGTH + 1)

        assertTrue(MasterPasswordPolicy.acceptsLength(minimum.codePointLength()))
        assertTrue(MasterPasswordPolicy.accepts(minimum))
        assertTrue(BackupPasswordPolicy.acceptsNew(minimum))
        assertFalse(MasterPasswordPolicy.acceptsLength(tooShort.codePointLength()))
        assertFalse(MasterPasswordPolicy.accepts(tooShort))
        assertFalse(BackupPasswordPolicy.acceptsNew(tooShort))
        assertFalse(MasterPasswordPolicy.acceptsLength(tooLong.codePointLength()))
        assertFalse(MasterPasswordPolicy.accepts(tooLong))
        assertFalse(BackupPasswordPolicy.acceptsNew(tooLong))
    }

    @Test
    fun `new master password policy rejects predictable passwords at valid lengths`() {
        val predictablePasswords = listOf(
            "passwordpass",
            "Summer2024!!",
            "Qwerty123456!",
            "abcabcabcabc",
        )

        predictablePasswords.forEach { password ->
            val sensitive = SensitiveText.from(password)
            try {
                assertTrue(MasterPasswordPolicy.acceptsLength(password.codePointLength()))
                assertFalse(MasterPasswordPolicy.accepts(password))
                assertFalse(MasterPasswordPolicy.accepts(sensitive))
            } finally {
                sensitive.clear()
            }
        }
        assertTrue(MasterPasswordPolicy.accepts("correct horse battery staple"))
    }

    @Test
    fun `existing weak master passwords remain eligible for unlock`() {
        val legacyPassword = SensitiveText.from("passwordpass")
        try {
            assertFalse(MasterPasswordPolicy.accepts(legacyPassword))
            assertTrue(MasterPasswordPolicy.acceptsExisting(legacyPassword))
        } finally {
            legacyPassword.clear()
        }
    }

    @Test
    fun `existing backup policy accepts one supplementary character`() {
        assertTrue(BackupPasswordPolicy.acceptsExisting("🔐"))
        assertFalse(BackupPasswordPolicy.acceptsExisting(""))
    }

    @Test
    fun `password policies reject malformed Unicode regardless of length`() {
        val malformed = "valid-password\uD800"
        val sensitive = SensitiveText.from(malformed)
        try {
            assertFalse(MasterPasswordPolicy.accepts(malformed))
            assertFalse(MasterPasswordPolicy.accepts(sensitive))
            assertFalse(MasterPasswordPolicy.acceptsExisting(sensitive))
            assertFalse(BackupPasswordPolicy.acceptsNew(malformed))
            assertFalse(BackupPasswordPolicy.acceptsNew(sensitive))
            assertFalse(BackupPasswordPolicy.acceptsExisting(malformed))
            assertFalse(BackupPasswordPolicy.acceptsExisting(sensitive))
        } finally {
            sensitive.clear()
        }
    }
}
