package com.passvault.android.security

import com.passvault.core.security.ScreenshotProtection
import kotlin.test.Test
import kotlin.test.assertFalse

class AndroidScreenshotProtectionApiTest {

    @Test
    fun screenshotProtectionHasNoRuntimeDisableOperation() {
        assertFalse(AndroidScreenshotProtection::class.java.methods.any { it.name == "disableProtection" })
        assertFalse(ScreenshotProtection::class.java.methods.any { it.name == "disableProtection" })
    }
}
