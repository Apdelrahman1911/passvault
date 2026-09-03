package com.passvault.shared.platform

import kotlin.test.Test
import kotlin.test.assertFalse

class ClipboardLockPolicyTest {
    @Test
    fun `Android clears sensitive clipboard on background lock`() {
        assertFalse(preservesSensitiveClipboardOnBackgroundLock())
    }
}
