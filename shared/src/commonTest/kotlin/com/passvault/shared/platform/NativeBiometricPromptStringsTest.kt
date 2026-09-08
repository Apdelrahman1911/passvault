package com.passvault.shared.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class NativeBiometricPromptStringsTest {
    @Test
    fun `English and Arabic have complete distinct native text`() {
        val english = NativeBiometricPromptStrings.forLanguageTag("en")
        val arabic = NativeBiometricPromptStrings.forLanguageTag("ar")
        assertEquals("Enable biometric unlock for this vault", english.enrollmentReason)
        assertEquals("Unlock PassVault", english.unlockReason)
        assertEquals("Cancel", english.cancel)
        assertEquals("فعّل الفتح باستخدام المقاييس الحيوية لهذه الخزنة", arabic.enrollmentReason)
        assertEquals("افتح PassVault", arabic.unlockReason)
        assertEquals("إلغاء", arabic.cancel)
        assertNotEquals(english.enrollmentReason, arabic.enrollmentReason)
        assertNotEquals(english.unlockReason, arabic.unlockReason)
        assertNotEquals(english.cancel, arabic.cancel)
        listOf(english, arabic).forEach { strings ->
            listOf(strings.enrollmentReason, strings.unlockReason, strings.cancel).forEach { value ->
                assertTrue(value.isNotBlank())
                assertTrue(value.encodeToByteArray().size <= 1_024)
                assertTrue('\u0000' !in value)
            }
        }
    }

    @Test
    fun `regional tags preserve supported languages and unsupported system languages use the base`() {
        val arabic = NativeBiometricPromptStrings.forLanguageTag("ar")
        val english = NativeBiometricPromptStrings.forLanguageTag("en")
        assertEquals(arabic, NativeBiometricPromptStrings.forLanguageTag("ar-EG"))
        assertEquals(arabic, NativeBiometricPromptStrings.forLanguageTag("AR_SA"))
        assertEquals(english, NativeBiometricPromptStrings.forLanguageTag("en-GB"))
        assertEquals(english, NativeBiometricPromptStrings.forLanguageTag("fr"))
    }

    @Test
    fun `native consumers follow cold start and runtime app language publication`() {
        val original = currentNativeBiometricPromptStrings()
        try {
            publishNativeBiometricPromptLanguage("ar-EG")
            assertEquals("إلغاء", currentNativeBiometricPromptStrings().cancel)
            publishNativeBiometricPromptLanguage("en")
            assertEquals("Cancel", currentNativeBiometricPromptStrings().cancel)
            publishNativeBiometricPromptLanguage("ar")
            assertEquals("افتح PassVault", currentNativeBiometricPromptStrings().unlockReason)
        } finally {
            publishNativeBiometricPromptLanguage(original.languageTag)
        }
    }
}
