package com.passvault.shared.platform

import kotlin.test.Test
import kotlin.test.assertTrue

class ClipboardLockPolicyTest {
    @Test
    fun `iOS preserves an expiring sensitive clipboard on background lock`() {
        assertTrue(preservesSensitiveClipboardOnBackgroundLock())
    }
}
