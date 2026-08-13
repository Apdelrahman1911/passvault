package com.passvault.shared.platform

import com.passvault.core.domain.repository.LanguagePreference
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppLanguageLayoutPolicyTest {
    @Test
    fun `Arabic always uses RTL layout`() {
        assertTrue(LanguagePreference.ARABIC.usesRtlLayout(systemUsesRtl = false))
        assertTrue(LanguagePreference.ARABIC.usesRtlLayout(systemUsesRtl = true))
    }

    @Test
    fun `English always uses LTR layout`() {
        assertFalse(LanguagePreference.ENGLISH.usesRtlLayout(systemUsesRtl = false))
        assertFalse(LanguagePreference.ENGLISH.usesRtlLayout(systemUsesRtl = true))
    }

    @Test
    fun `System follows the native layout direction`() {
        assertFalse(LanguagePreference.SYSTEM.usesRtlLayout(systemUsesRtl = false))
        assertTrue(LanguagePreference.SYSTEM.usesRtlLayout(systemUsesRtl = true))
    }
}
