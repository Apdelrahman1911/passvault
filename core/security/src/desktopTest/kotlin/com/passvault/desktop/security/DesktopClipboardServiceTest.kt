package com.passvault.desktop.security

import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.ClipboardOwner
import java.awt.datatransfer.Transferable
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DesktopClipboardServiceTest {

    @Test
    fun `cancellation after native write still installs sensitive ownership`() = runBlocking {
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        lateinit var copyJob: Job
        val clipboard = ControlledClipboard {
            copyJob.cancel()
        }
        val service = testService(serviceScope, clipboard)

        try {
            copyJob = launch {
                service.copySensitive("secret", DesktopClipboardService.MIN_TIMEOUT_MS)
            }
            copyJob.join()

            assertTrue(copyJob.isCancelled)
            assertTrue(service.containsSensitive())
            service.clear()
            assertFalse(service.containsSensitive())
        } finally {
            serviceScope.cancel()
        }
    }

    @Test
    fun `failed replacement preserves prior sensitive ownership`() = runBlocking {
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val clipboard = ControlledClipboard()
        val service = testService(serviceScope, clipboard)

        try {
            service.copySensitive("first", DesktopClipboardService.MIN_TIMEOUT_MS)
            clipboard.failNextWrites(8)

            assertFailsWith<IllegalStateException> {
                service.copySensitive("second", DesktopClipboardService.MIN_TIMEOUT_MS)
            }
            assertTrue(service.containsSensitive())
            service.clear()
            assertFalse(service.containsSensitive())
        } finally {
            serviceScope.cancel()
        }
    }

    private fun testService(
        scope: CoroutineScope,
        clipboard: Clipboard,
    ): DesktopClipboardService = DesktopClipboardService(
        scope = scope,
        clipboardProvider = { clipboard },
        registerShutdownHook = false,
    )

    private class ControlledClipboard(
        private val afterFirstSuccessfulWrite: (() -> Unit)? = null,
    ) : Clipboard("PassVault test clipboard") {
        private val callbackPending = AtomicBoolean(afterFirstSuccessfulWrite != null)
        private val failingWrites = AtomicInteger(0)

        fun failNextWrites(count: Int) {
            failingWrites.set(count)
        }

        override fun setContents(contents: Transferable?, owner: ClipboardOwner?) {
            if (failingWrites.getAndUpdate { remaining -> (remaining - 1).coerceAtLeast(0) } > 0) {
                throw IllegalStateException("Clipboard is busy")
            }
            super.setContents(contents, owner)
            if (callbackPending.compareAndSet(true, false)) afterFirstSuccessfulWrite?.invoke()
        }
    }
}
