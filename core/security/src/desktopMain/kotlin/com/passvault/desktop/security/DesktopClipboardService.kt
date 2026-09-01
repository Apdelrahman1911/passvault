package com.passvault.desktop.security

import com.passvault.core.security.ClipboardService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.SystemFlavorMap
import java.awt.datatransfer.Transferable
import java.util.Locale
import java.util.UUID
import java.util.concurrent.atomic.AtomicReference

/**
 * Ownership-aware AWT clipboard service.
 *
 * Timers never blindly replace the user's later clipboard contents.  The
 * secret carries a JVM-local ownership flavor. Expiry verifies that
 * unforgeable token before clearing, and releases the local reference when the
 * clip changes or is cleared. Sensitive writes also publish platform-native
 * retention opt-out hints. Those hints are advisory for third-party clipboard
 * managers, so timed clearing and the documented clipboard threat boundary
 * remain necessary.
 */
class DesktopClipboardService internal constructor(
    private val scope: CoroutineScope,
    private val clipboardProvider: () -> Clipboard,
    registerShutdownHook: Boolean,
    private val securityHints: List<NativeClipboardHint> = nativeClipboardHints(),
) : ClipboardService {

    constructor(
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
    ) : this(
        scope = scope,
        clipboardProvider = { Toolkit.getDefaultToolkit().systemClipboard },
        registerShutdownHook = true,
    )

    private val clipboard: Clipboard by lazy(clipboardProvider)
    private val ownedTransferable = AtomicReference<OwnedSensitiveSelection?>(null)
    private val clipboardMutex = Mutex()
    private var clearJob: Job? = null

    init {
        if (registerShutdownHook) {
            Runtime.getRuntime().addShutdownHook(Thread {
                clearOwnedAtShutdown()
            })
        }
    }

    override suspend fun copySensitive(text: String, timeoutMs: Long) {
        clipboardMutex.withLock {
            val timeout = timeoutMs.coerceIn(MIN_TIMEOUT_MS, MAX_TIMEOUT_MS)
            val selection = OwnedSensitiveSelection(text, securityHints)
            // withContext has a prompt-cancellation boundary when returning
            // from the dispatcher used by setClipboard(). Once the OS accepts
            // a secret, ownership and its expiry job must therefore be
            // installed before caller cancellation can escape this method.
            withContext(NonCancellable) {
                setClipboard(selection)
                clearJob?.cancel()
                ownedTransferable.set(selection)

                clearJob = scope.launch {
                    delay(timeout)
                    clipboardMutex.withLock { clearIfOwned(selection) }
                }
            }
        }
    }

    override suspend fun copy(text: String) {
        clipboardMutex.withLock {
            setClipboard(StringSelection(text))
            clearJob?.cancel()
            clearJob = null
            ownedTransferable.set(null)
        }
    }

    override suspend fun clear() {
        clipboardMutex.withLock {
            val owned = ownedTransferable.get() ?: return@withLock
            clearIfOwned(owned)
        }
    }

    override suspend fun containsSensitive(): Boolean {
        return clipboardMutex.withLock {
            val owned = ownedTransferable.get() ?: return@withLock false
            try {
                val isOwned = ownsClipboardContent(
                    current = withClipboardRetry { clipboard.getContents(null) },
                    expected = owned,
                )
                if (!isOwned) releaseOwnership(owned)
                isOwned
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (_: Exception) {
                false
            }
        }
    }

    private suspend fun clearIfOwned(expected: OwnedSensitiveSelection): Boolean {
        return try {
            val current = withClipboardRetry { clipboard.getContents(null) }
            if (!ownsClipboardContent(current, expected)) {
                releaseOwnership(expected)
                false
            } else {
                withClipboardRetry { clipboard.setContents(StringSelection(""), null) }
                releaseOwnership(expected)
                true
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (_: Exception) {
            // Windows can keep the clipboard busy beyond the short immediate
            // retry window. Retain ownership and continue retrying instead of
            // silently leaving a secret with no expiry job.
            if (ownedTransferable.get() === expected) {
                clearJob = scope.launch {
                    delay(CLIPBOARD_RETRY_RESCHEDULE_MS)
                    clipboardMutex.withLock {
                        if (ownedTransferable.get() === expected) clearIfOwned(expected)
                    }
                }
            }
            false
        }
    }

    private suspend fun setClipboard(selection: Transferable) {
        withClipboardRetry { clipboard.setContents(selection, null) }
    }

    private fun clearOwnedAtShutdown() {
        val expected = ownedTransferable.get() ?: return
        runCatching {
            if (ownsClipboardContent(clipboard.getContents(null), expected)) {
                clipboard.setContents(StringSelection(""), null)
            }
        }
        ownedTransferable.compareAndSet(expected, null)
    }

    private fun ownsClipboardContent(
        current: Transferable?,
        expected: OwnedSensitiveSelection,
    ): Boolean {
        val markerMatches = current?.isDataFlavorSupported(OWNERSHIP_FLAVOR) == true &&
            runCatching { current.getTransferData(OWNERSHIP_FLAVOR) == expected.token }
                .getOrDefault(false)
        return current === expected || markerMatches
    }

    private fun releaseOwnership(expected: OwnedSensitiveSelection) {
        if (ownedTransferable.compareAndSet(expected, null)) {
            clearJob?.cancel()
            clearJob = null
        }
    }

    private suspend fun <T> withClipboardRetry(operation: () -> T): T =
        withContext(Dispatchers.Default) {
            var lastFailure: IllegalStateException? = null
            repeat(CLIPBOARD_RETRY_ATTEMPTS) { attempt ->
                try {
                    return@withContext operation()
                } catch (error: IllegalStateException) {
                    lastFailure = error
                    if (attempt + 1 < CLIPBOARD_RETRY_ATTEMPTS) delay(CLIPBOARD_RETRY_DELAY_MS)
                }
            }
            throw checkNotNull(lastFailure)
        }

    private class OwnedSensitiveSelection(
        text: String,
        private val securityHints: List<NativeClipboardHint>,
    ) : Transferable {
        val token: String = UUID.randomUUID().toString()
        private val textSelection = StringSelection(text)

        override fun getTransferDataFlavors(): Array<DataFlavor> =
            arrayOf(DataFlavor.stringFlavor, OWNERSHIP_FLAVOR) + securityHints.map { it.flavor }

        override fun isDataFlavorSupported(flavor: DataFlavor): Boolean =
            flavor == OWNERSHIP_FLAVOR ||
                securityHints.any { it.flavor == flavor } ||
                textSelection.isDataFlavorSupported(flavor)

        override fun getTransferData(flavor: DataFlavor): Any = when {
            flavor == OWNERSHIP_FLAVOR -> token
            else -> securityHints.firstOrNull { it.flavor == flavor }?.payload?.copyOf()
                ?: textSelection.getTransferData(flavor)
        }
    }

    companion object {
        const val MAX_TIMEOUT_MS = 300_000L
        const val MIN_TIMEOUT_MS = 5_000L
        private const val CLIPBOARD_RETRY_ATTEMPTS = 8
        private const val CLIPBOARD_RETRY_DELAY_MS = 40L
        private const val CLIPBOARD_RETRY_RESCHEDULE_MS = 1_000L
        private val OWNERSHIP_FLAVOR = DataFlavor(
            "${DataFlavor.javaJVMLocalObjectMimeType};class=java.lang.String",
            "PassVault sensitive clipboard owner",
        )
    }
}

internal data class NativeClipboardHint(
    val nativeFormat: String,
    val flavor: DataFlavor,
    val payload: ByteArray,
)

internal fun nativeClipboardHints(
    osName: String = System.getProperty("os.name").orEmpty(),
    register: (DataFlavor, String) -> Unit = ::registerNativeClipboardFlavor,
): List<NativeClipboardHint> {
    val definitions = when {
        osName.startsWith("Windows", ignoreCase = true) -> WINDOWS_CLIPBOARD_HINTS
        osName.startsWith("Mac", ignoreCase = true) -> MACOS_CLIPBOARD_HINTS
        else -> emptyList()
    }
    return definitions.mapIndexed { index, definition ->
        val flavorName = definition.nativeFormat
            .lowercase(Locale.ROOT)
            .replace(NON_MIME_TOKEN_CHARACTER, "-")
        val flavor = DataFlavor(
            "application/x-passvault-$flavorName-$index;class=\"[B\"",
            "PassVault sensitive clipboard hint: ${definition.nativeFormat}",
        )
        register(flavor, definition.nativeFormat)
        NativeClipboardHint(definition.nativeFormat, flavor, definition.payload.copyOf())
    }
}

private fun registerNativeClipboardFlavor(flavor: DataFlavor, nativeFormat: String) {
    val flavorMap = SystemFlavorMap.getDefaultFlavorMap() as SystemFlavorMap
    // A one-way explicit mapping makes the platform data transferer register the exact
    // Win32 clipboard-format name or macOS pasteboard UTI and write this flavor's raw bytes.
    flavorMap.setNativesForFlavor(flavor, arrayOf(nativeFormat))
}

private data class NativeClipboardHintDefinition(
    val nativeFormat: String,
    val payload: ByteArray,
)

private val WINDOWS_CLIPBOARD_HINTS = listOf(
    NativeClipboardHintDefinition("ExcludeClipboardContentFromMonitorProcessing", ByteArray(Int.SIZE_BYTES)),
    NativeClipboardHintDefinition("CanIncludeInClipboardHistory", ByteArray(Int.SIZE_BYTES)),
    NativeClipboardHintDefinition("CanUploadToCloudClipboard", ByteArray(Int.SIZE_BYTES)),
)
private val MACOS_CLIPBOARD_HINTS = listOf(
    NativeClipboardHintDefinition("org.nspasteboard.ConcealedType", byteArrayOf(0)),
    NativeClipboardHintDefinition("org.nspasteboard.TransientType", byteArrayOf(0)),
)
private val NON_MIME_TOKEN_CHARACTER = Regex("[^a-z0-9.-]")
