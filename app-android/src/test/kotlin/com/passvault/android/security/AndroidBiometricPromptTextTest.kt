package com.passvault.android.security

import com.passvault.shared.platform.NativeBiometricPromptStrings
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidBiometricPromptTextTest {
    @Test
    fun enrollmentAndUnlockUseTheSelectedAppTextRatherThanContextResources() {
        listOf("ar", "en", "ar").forEach { appLanguage ->
            val strings = NativeBiometricPromptStrings.forLanguageTag(appLanguage)
            val enrollment = androidBiometricPromptText(enrolling = true, strings = strings)
            val unlock = androidBiometricPromptText(enrolling = false, strings = strings)
            assertEquals(strings.enrollmentReason, enrollment.reason)
            assertEquals(strings.unlockReason, unlock.reason)
            assertEquals(strings.cancel, enrollment.cancel)
            assertEquals(strings.cancel, unlock.cancel)
        }
    }
}
