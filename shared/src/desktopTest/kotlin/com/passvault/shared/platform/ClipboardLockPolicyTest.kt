package com.passvault.shared.platform

import kotlin.test.Test
import kotlin.test.assertFalse

class ClipboardLockPolicyTest {
    @Test
    fun `Desktop clears sensitive clipboard on background lock`() {
        assertFalse(preservesSensitiveClipboardOnBackgroundLock())
    }
}
