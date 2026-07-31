package com.passvault.desktop.security

import com.passvault.core.security.ClipboardService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.cancel
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.util.concurrent.atomic.AtomicReference

/**
 * Ownership-aware AWT clipboard service.
 *
 * Timers never blindly replace the user's later clipboard contents.  The
 * service retains only an opaque digest and the transferable instance, not the
 * copied secret.
 */
class DesktopClipboardService(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : ClipboardService {

    private val clipboard: Clipboard by lazy { Toolkit.getDefaultToolkit().systemClipboard }
    private val ownedTransferable = AtomicReference<Transferable?>(null)
    private val clipboardMutex = Mutex()
    private var clearJob: Job? = null
    private var defaultTimeoutMs = DEFAULT_TIMEOUT_MS

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            runBlocking { clear() }
        })
    }

    override suspend fun copySensitive(text: String, timeoutMs: Long): Job {
        return clipboardMutex.withLock {
            val timeout = timeoutMs.coerceIn(MIN_TIMEOUT_MS, MAX_TIMEOUT_MS)
            clearJob?.cancel()
            val selection = StringSelection(text)
            setClipboard(selection)
            ownedTransferable.set(selection)

            val job = scope.launch {
                delay(timeout)
                clipboardMutex.withLock { clearIfOwned(selection) }
            }
            clearJob = job
            job
        }
    }

    override suspend fun copy(text: String) {
        clipboardMutex.withLock {
            clearJob?.cancel()
            clearJob = null
            ownedTransferable.set(null)
            setClipboard(StringSelection(text))
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
            withContext(Dispatchers.Default) {
                try {
                    val current = clipboard.getContents(null)
                    current === ownedTransferable.get()
                } catch (_: Exception) {
                    false
                }
            }
        }
    }

    suspend fun copySensitiveWithId(
        text: String,
        jobId: String,
        timeoutMs: Long = defaultTimeoutMs,
    ): Job = copySensitive(text, timeoutMs)

    fun cancelClearJob(jobId: String) {
        clearJob?.cancel()
        clearJob = null
    }

    fun cancelAllClearJobs() {
        clearJob?.cancel()
        clearJob = null
    }

    suspend fun getClipboardContent(): String? = withContext(Dispatchers.Default) {
        try {
            clipboard.getData(DataFlavor.stringFlavor) as? String
        } catch (_: Exception) {
            null
        }
    }

    fun hasContent(): Boolean = runCatching { clipboard.getContents(null) != null }.getOrDefault(false)

    fun setDefaultTimeout(timeoutMs: Long) {
        defaultTimeoutMs = timeoutMs.coerceIn(MIN_TIMEOUT_MS, MAX_TIMEOUT_MS)
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun clearIfMatches(text: String): Boolean {
        return clipboardMutex.withLock {
            val owned = ownedTransferable.get() ?: return@withLock false
            val current = withContext(Dispatchers.Default) {
                runCatching { clipboard.getContents(null) }.getOrNull()
            }
            if (current !== owned) return@withLock false
            clearIfOwned(owned)
            true
        }
    }

    suspend fun clearNow(): Boolean = clipboardMutex.withLock {
        val owned = ownedTransferable.get() ?: return@withLock true
        clearIfOwned(owned)
        ownedTransferable.get() == null
    }

    fun hasClearJob(jobId: String): Boolean = clearJob?.isActive == true

    fun getPendingClearJobCount(): Int = if (clearJob?.isActive == true) 1 else 0

    suspend fun copyMultipleSensitive(
        items: Map<String, String>,
        timeoutMs: Long = defaultTimeoutMs,
    ): Map<String, Job> = items.mapValues { copySensitive(it.value, timeoutMs) }

    suspend fun onWindowFocusLost() {
        if (containsSensitive()) clear()
    }

    fun destroy() {
        runBlocking {
            clipboardMutex.withLock {
                ownedTransferable.get()?.let { clearIfOwned(it) }
                clearJob?.cancel()
                clearJob = null
            }
        }
        scope.cancel()
    }

    private suspend fun clearIfOwned(expected: Transferable): Boolean =
        withContext(Dispatchers.Default) {
            try {
                val current = clipboard.getContents(null)
                if (current !== expected) {
                    ownedTransferable.compareAndSet(expected, null)
                    return@withContext false
                }
                clipboard.setContents(StringSelection(""), null)
                ownedTransferable.compareAndSet(expected, null)
                clearJob?.cancel()
                clearJob = null
                true
            } catch (_: Exception) {
                false
            }
        }

    private suspend fun setClipboard(selection: Transferable) {
        withContext(Dispatchers.Default) {
            clipboard.setContents(selection, null)
        }
    }

    companion object {
        const val DEFAULT_TIMEOUT_MS = 30_000L
        const val MAX_TIMEOUT_MS = 300_000L
        const val MIN_TIMEOUT_MS = 5_000L
    }
}
