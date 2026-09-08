package com.passvault.android.security

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidClipboardServiceTest {
    @Test
    fun unavailableExpiryRetainsOwnershipUntilForegroundCanClearTheSameClip() = runBlocking {
        withClipboard { service, clipboard, expiry ->
            service.copySensitive("synthetic secret")
            clipboard.readable = false
            expiry.complete(Unit)
            assertTrue(service.containsSensitive())
            assertEquals(0, clipboard.clearCalls)

            service.onForeground()
            assertEquals(0, clipboard.clearCalls)
            clipboard.readable = true
            service.onForeground()

            assertEquals(1, clipboard.clearCalls)
            assertFalse(service.containsSensitive())
            service.onForeground()
            assertEquals(1, clipboard.clearCalls)
        }
    }

    @Test
    fun unavailableOwnershipQueryDoesNotCancelTheRemainingExpiryTimer() = runBlocking {
        withClipboard { service, clipboard, expiry ->
            service.copySensitive("synthetic secret")
            clipboard.readable = false
            assertTrue(service.containsSensitive())
            clipboard.readable = true
            expiry.complete(Unit)

            assertEquals(1, clipboard.clearCalls)
            assertFalse(service.containsSensitive())
        }
    }

    @Test
    fun foregroundRetryPreservesAReplacementEvenWhenItsTextIsIdentical() = runBlocking {
        withClipboard { service, clipboard, expiry ->
            service.copySensitive("synthetic secret")
            clipboard.readable = false
            service.clear()
            expiry.complete(Unit)
            clipboard.token = "another-owner"
            clipboard.readable = true
            service.onForeground()

            assertEquals(0, clipboard.clearCalls)
            assertEquals("synthetic secret", clipboard.text)
            assertFalse(service.containsSensitive())
        }
    }

    @Test
    fun readableUnlabelledReplacementDiscardsOnlyOurCleanupOwnership() = runBlocking {
        withClipboard { service, clipboard, expiry ->
            service.copySensitive("synthetic secret")
            clipboard.token = null
            assertFalse(service.containsSensitive())
            expiry.complete(Unit)
            service.onForeground()

            assertEquals(0, clipboard.clearCalls)
        }
    }

    @Test
    fun providerClearFailureKeepsAForegroundRetryWithoutBlockingTheCaller() = runBlocking {
        withClipboard { service, clipboard, _ ->
            service.copySensitive("synthetic secret")
            clipboard.failClear = true
            service.clear()
            assertTrue(service.containsSensitive())
            clipboard.failClear = false
            service.onForeground()

            assertEquals(2, clipboard.clearCalls)
            assertFalse(service.containsSensitive())
        }
    }

    @Test
    fun foregroundBeforeExpiryLeavesAnUnexpiredClipAlone() = runBlocking {
        withClipboard { service, clipboard, _ ->
            service.copySensitive("synthetic secret")
            clipboard.readable = false
            assertTrue(service.containsSensitive())
            clipboard.readable = true
            service.onForeground()

            assertEquals(0, clipboard.clearCalls)
            assertTrue(service.containsSensitive())
        }
    }

    private suspend fun withClipboard(
        block: suspend (AndroidClipboardService, FakeClipboard, CompletableDeferred<Unit>) -> Unit,
    ) {
        val clipboard = FakeClipboard()
        val expiry = CompletableDeferred<Unit>()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        val service = AndroidClipboardService(clipboard, scope, awaitTimeout = { expiry.await() })
        try {
            block(service, clipboard, expiry)
        } finally {
            scope.cancel()
        }
    }
}

private class FakeClipboard : AndroidClipboardAccess {
    var readable = true
    var failClear = false
    var token: String? = null
    var text = ""
    var clearCalls = 0

    override fun copySensitive(token: String, text: String) {
        this.token = token
        this.text = text
    }

    override fun copy(text: String) {
        token = "PassVault"
        this.text = text
    }

    override fun readContents(): AndroidClipboardContents =
        if (readable) AndroidClipboardContents.Readable(token) else AndroidClipboardContents.Unavailable

    override fun clear() {
        clearCalls += 1
        check(!failClear) { "Synthetic provider failure" }
        token = null
        text = ""
    }
}
