package com.passvault.android.security

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import com.passvault.core.security.ClipboardService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/**
 * Android clipboard boundary for secrets.
 *
 * A random ownership label is attached to each sensitive clip. Expiry only
 * clears a clip whose label still belongs to this service, so a later clip
 * copied by the user is never overwritten—even when it contains the same
 * text.
 */
class AndroidClipboardService internal constructor(
    private val clipboard: AndroidClipboardAccess,
    private val scope: CoroutineScope,
    private val awaitTimeout: suspend (Long) -> Unit = { delay(it) },
) : ClipboardService {
    constructor(
        context: Context,
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
    ) : this(SystemAndroidClipboardAccess(context.applicationContext), scope)

    private val clipboardMutex = Mutex()
    private var ownedToken: String? = null
    private var clearRequested = false
    private var clearJob: Job? = null

    override suspend fun copySensitive(text: String, timeoutMs: Long) {
        clipboardMutex.withLock {
            val token = ownershipToken("secret")
            clipboard.copySensitive(token, text)
            cancelClearLocked()
            ownedToken = token
            clearRequested = false
            clearJob = scope.launch {
                awaitTimeout(timeoutMs.coerceIn(MIN_TIMEOUT_MS, MAX_TIMEOUT_MS))
                clipboardMutex.withLock { clearIfOwnedLocked(token) }
            }
        }
    }

    override suspend fun copy(text: String) {
        clipboardMutex.withLock {
            clipboard.copy(text)
            cancelClearLocked()
            ownedToken = null
            clearRequested = false
        }
    }

    override suspend fun clear() {
        clipboardMutex.withLock {
            ownedToken?.let { clearIfOwnedLocked(it) }
        }
    }

    override suspend fun containsSensitive(): Boolean =
        clipboardMutex.withLock {
            val token = ownedToken ?: return@withLock false
            when (val contents = clipboard.readContents()) {
                AndroidClipboardContents.Unavailable -> true
                is AndroidClipboardContents.Readable -> {
                    val isOwned = contents.token == token
                    if (!isOwned) forgetOwnershipLocked()
                    isOwned
                }
            }
        }

    /** Retry expired/locked clips when Android permits clipboard inspection again. */
    fun onForeground() {
        scope.launch {
            clipboardMutex.withLock {
                if (clearRequested) ownedToken?.let { clearIfOwnedLocked(it) }
            }
        }
    }

    private fun clearIfOwnedLocked(expectedToken: String) {
        if (ownedToken != expectedToken) return
        clearRequested = true
        when (val contents = clipboard.readContents()) {
            // A null/denied read is not proof of replacement or absence. Keep
            // the token and retry intent without overwriting an unknown clip.
            AndroidClipboardContents.Unavailable -> Unit
            is AndroidClipboardContents.Readable -> {
                if (contents.token == expectedToken) {
                    // A provider failure retains ownership for the next usable
                    // foreground boundary; it must not veto vault locking.
                    try {
                        clipboard.clear()
                    } catch (_: Exception) {
                        return
                    }
                }
                forgetOwnershipLocked()
            }
        }
    }

    private fun forgetOwnershipLocked() {
        ownedToken = null
        clearRequested = false
        cancelClearLocked()
    }

    private fun cancelClearLocked() {
        clearJob?.cancel()
        clearJob = null
    }

    private fun ownershipToken(label: String): String =
        "PassVault:$label:${UUID.randomUUID()}"

    private companion object {
        const val MIN_TIMEOUT_MS = 5_000L
        const val MAX_TIMEOUT_MS = 300_000L
    }
}

internal sealed interface AndroidClipboardContents {
    data object Unavailable : AndroidClipboardContents
    data class Readable(val token: String?) : AndroidClipboardContents
}

internal interface AndroidClipboardAccess {
    fun copySensitive(token: String, text: String)
    fun copy(text: String)
    fun readContents(): AndroidClipboardContents
    fun clear()
}

private class SystemAndroidClipboardAccess(private val context: Context) : AndroidClipboardAccess {
    private val manager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun copySensitive(token: String, text: String) {
        val clip = ClipData.newPlainText(token, text).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                description.extras = PersistableBundle().apply {
                    putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                }
            }
        }
        manager.setPrimaryClip(clip)
    }

    override fun copy(text: String) {
        manager.setPrimaryClip(ClipData.newPlainText("PassVault", text))
    }

    override fun readContents(): AndroidClipboardContents = try {
        manager.primaryClip?.let { clip ->
            AndroidClipboardContents.Readable(clip.description.label?.toString())
        } ?: AndroidClipboardContents.Unavailable
    } catch (_: Exception) {
        // Focus/access restrictions and transient provider failures are unknown,
        // never evidence that the service's previous sensitive clip was replaced.
        AndroidClipboardContents.Unavailable
    }

    override fun clear() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            manager.clearPrimaryClip()
        } else {
            manager.setPrimaryClip(ClipData.newPlainText("PassVault", ""))
        }
    }
}
