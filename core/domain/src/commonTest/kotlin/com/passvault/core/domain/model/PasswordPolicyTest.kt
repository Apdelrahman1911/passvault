package com.passvault.core.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PasswordPolicyTest {
    @Test
    fun `new password policies count supplementary characters once`() {
        val minimum = "🔐".repeat(MasterPasswordPolicy.MIN_LENGTH)
        val tooShort = "🔐".repeat(MasterPasswordPolicy.MIN_LENGTH - 1)
        val tooLong = "🔐".repeat(MasterPasswordPolicy.MAX_LENGTH + 1)

        assertTrue(MasterPasswordPolicy.accepts(minimum))
        assertTrue(BackupPasswordPolicy.acceptsNew(minimum))
        assertFalse(MasterPasswordPolicy.accepts(tooShort))
        assertFalse(BackupPasswordPolicy.acceptsNew(tooShort))
        assertFalse(MasterPasswordPolicy.accepts(tooLong))
        assertFalse(BackupPasswordPolicy.acceptsNew(tooLong))
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
