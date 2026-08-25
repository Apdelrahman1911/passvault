package com.passvault.desktop.security

import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.ClipboardOwner
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.SystemFlavorMap
import java.awt.datatransfer.Transferable
import java.nio.file.Files
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
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
    fun `windows system clipboard receives retention formats with zero DWORD values`() = runBlocking {
        if (!System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) return@runBlocking
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val service = systemClipboardService(serviceScope)
        val script = Files.createTempFile("passvault-clipboard-hints-", ".ps1")
        try {
            service.copySensitive("passvault-native-hint-smoke", DesktopClipboardService.MAX_TIMEOUT_MS)
            Files.writeString(script, WINDOWS_CLIPBOARD_ASSERTION)
            val process = ProcessBuilder(
                "powershell.exe",
                "-NoProfile",
                "-NonInteractive",
                "-ExecutionPolicy",
                "Bypass",
                "-File",
                script.toString(),
            ).redirectErrorStream(true).start()
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "PowerShell clipboard check timed out")
            val output = process.inputStream.bufferedReader().readText()
            assertEquals(0, process.exitValue(), output)
            WINDOWS_NATIVE_FORMATS.forEach { assertTrue(output.contains(it), output) }
        } finally {
            runCatching { service.clear() }
            serviceScope.cancel()
            Files.deleteIfExists(script)
        }
    }

    @Test
    fun `macos system clipboard receives concealed and transient types`() = runBlocking {
        if (!System.getProperty("os.name").startsWith("Mac", ignoreCase = true)) return@runBlocking
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val service = systemClipboardService(serviceScope)
        try {
            service.copySensitive("passvault-native-hint-smoke", DesktopClipboardService.MAX_TIMEOUT_MS)
            val process = ProcessBuilder(
                "osascript",
                "-l",
                "JavaScript",
                "-e",
                MACOS_CLIPBOARD_TYPES_SCRIPT,
            ).redirectErrorStream(true).start()
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "macOS pasteboard check timed out")
            val output = process.inputStream.bufferedReader().readText()
            assertEquals(0, process.exitValue(), output)
            assertTrue(output.contains("org.nspasteboard.ConcealedType"), output)
            assertTrue(output.contains("org.nspasteboard.TransientType"), output)
        } finally {
            runCatching { service.clear() }
            serviceScope.cancel()
        }
    }

    @Test
    fun `windows sensitive copy publishes all native retention opt outs only for secrets`() = runBlocking {
        val registrations = mutableMapOf<String, DataFlavor>()
        val hints = nativeClipboardHints("Windows 11") { flavor, native ->
            registrations[native] = flavor
        }
        val clipboard = ControlledClipboard()
        val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val service = testService(serviceScope, clipboard, hints)

        try {
            assertEquals(
                setOf(
                    "ExcludeClipboardContentFromMonitorProcessing",
                    "CanIncludeInClipboardHistory",
                    "CanUploadToCloudClipboard",
                ),
                registrations.keys,
            )

            service.copySensitive("secret", DesktopClipboardService.MIN_TIMEOUT_MS)
            val sensitive = clipboard.getContents(null)
            hints.forEach { hint ->
                assertTrue(sensitive.isDataFlavorSupported(hint.flavor))
                assertContentEquals(ByteArray(Int.SIZE_BYTES), sensitive.getTransferData(hint.flavor) as ByteArray)
            }

            service.copy("ordinary")
            val ordinary = clipboard.getContents(null)
            hints.forEach { hint -> assertFalse(ordinary.isDataFlavorSupported(hint.flavor)) }
        } finally {
            serviceScope.cancel()
        }
    }

    @Test
    fun `macos sensitive hints use exact pasteboard type mappings`() {
        val registrations = mutableMapOf<DataFlavor, String>()
        val hints = nativeClipboardHints("Mac OS X") { flavor, native ->
            registrations[flavor] = native
        }

        assertEquals(
            setOf("org.nspasteboard.ConcealedType", "org.nspasteboard.TransientType"),
            hints.mapTo(mutableSetOf(), NativeClipboardHint::nativeFormat),
        )
        assertEquals(hints.associate { it.flavor to it.nativeFormat }, registrations)
        hints.forEach { assertContentEquals(byteArrayOf(0), it.payload) }
    }

    @Test
    fun `production flavor registration preserves exact native names`() {
        val hints = nativeClipboardHints("Windows 11")
        val flavorMap = SystemFlavorMap.getDefaultFlavorMap() as SystemFlavorMap

        hints.forEach { hint ->
            assertEquals(listOf(hint.nativeFormat), flavorMap.getNativesForFlavor(hint.flavor))
        }
    }

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
        securityHints: List<NativeClipboardHint> = emptyList(),
    ): DesktopClipboardService = DesktopClipboardService(
        scope = scope,
        clipboardProvider = { clipboard },
        registerShutdownHook = false,
        securityHints = securityHints,
    )

    private fun systemClipboardService(scope: CoroutineScope): DesktopClipboardService =
        DesktopClipboardService(
            scope = scope,
            clipboardProvider = { Toolkit.getDefaultToolkit().systemClipboard },
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

    private companion object {
        val WINDOWS_NATIVE_FORMATS = listOf(
            "ExcludeClipboardContentFromMonitorProcessing",
            "CanIncludeInClipboardHistory",
            "CanUploadToCloudClipboard",
        )

        val WINDOWS_CLIPBOARD_ASSERTION = """
            Add-Type @'
            using System;
            using System.Runtime.InteropServices;
            public static class PassVaultClipboardNative {
                [DllImport("user32.dll", CharSet = CharSet.Unicode, SetLastError = true)]
                public static extern uint RegisterClipboardFormat(string name);
                [DllImport("user32.dll", SetLastError = true)]
                public static extern bool OpenClipboard(IntPtr owner);
                [DllImport("user32.dll")]
                public static extern bool CloseClipboard();
                [DllImport("user32.dll", SetLastError = true)]
                public static extern IntPtr GetClipboardData(uint format);
                [DllImport("kernel32.dll", SetLastError = true)]
                public static extern IntPtr GlobalLock(IntPtr memory);
                [DllImport("kernel32.dll")]
                public static extern bool GlobalUnlock(IntPtr memory);
            }
            '@
            ${'$'}opened = ${'$'}false
            for (${'$'}attempt = 0; ${'$'}attempt -lt 20 -and -not ${'$'}opened; ${'$'}attempt++) {
                ${'$'}opened = [PassVaultClipboardNative]::OpenClipboard([IntPtr]::Zero)
                if (-not ${'$'}opened) { Start-Sleep -Milliseconds 50 }
            }
            if (-not ${'$'}opened) { throw 'Could not open the Windows clipboard' }
            try {
                ${'$'}names = @(
                    'ExcludeClipboardContentFromMonitorProcessing',
                    'CanIncludeInClipboardHistory',
                    'CanUploadToCloudClipboard'
                )
                foreach (${'$'}name in ${'$'}names) {
                    ${'$'}format = [PassVaultClipboardNative]::RegisterClipboardFormat(${'$'}name)
                    ${'$'}handle = [PassVaultClipboardNative]::GetClipboardData(${'$'}format)
                    if (${'$'}handle -eq [IntPtr]::Zero) { throw "Missing clipboard format: ${'$'}name" }
                    if (${'$'}name -ne 'ExcludeClipboardContentFromMonitorProcessing') {
                        ${'$'}pointer = [PassVaultClipboardNative]::GlobalLock(${'$'}handle)
                        if (${'$'}pointer -eq [IntPtr]::Zero) { throw "Could not lock: ${'$'}name" }
                        try {
                            if ([Runtime.InteropServices.Marshal]::ReadInt32(${'$'}pointer) -ne 0) {
                                throw "Clipboard opt-out is not DWORD zero: ${'$'}name"
                            }
                        } finally {
                            [void][PassVaultClipboardNative]::GlobalUnlock(${'$'}handle)
                        }
                    }
                    Write-Output ${'$'}name
                }
            } finally {
                [void][PassVaultClipboardNative]::CloseClipboard()
            }
        """.trimIndent()

        const val MACOS_CLIPBOARD_TYPES_SCRIPT = """
            ObjC.import('AppKit');
            const items = $.NSPasteboard.generalPasteboard.pasteboardItems.js;
            if (!items || items.length === 0) throw new Error('No pasteboard item');
            console.log(JSON.stringify(items[0].types.js.map(value => ObjC.unwrap(value))));
        """
    }
}
