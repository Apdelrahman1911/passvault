package com.passvault.shared.platform

import kotlin.test.Test
import kotlin.test.assertEquals

class IosBiometricPromptStringsTest {
    @Test
    fun `both native prompt operations receive the selected English or Arabic text`() {
        listOf("en", "ar").forEach { language ->
            val strings = NativeBiometricPromptStrings.forLanguageTag(language)
            val enrollment = iosBiometricPromptContext(enrolling = true, strings = strings)
            val unlock = iosBiometricPromptContext(enrolling = false, strings = strings)
            try {
                assertEquals(strings.enrollmentReason, enrollment.localizedReason)
                assertEquals(strings.unlockReason, unlock.localizedReason)
                assertEquals(strings.cancel, enrollment.localizedCancelTitle)
                assertEquals(strings.cancel, unlock.localizedCancelTitle)
            } finally {
                enrollment.invalidate()
                unlock.invalidate()
            }
        }
    }
}
