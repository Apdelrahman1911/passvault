package com.passvault.android.security

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import com.passvault.core.security.ClipboardService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
class AndroidClipboardService(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) : ClipboardService {

    private val clipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    private val clipboardMutex = Mutex()
    private var ownedToken: String? = null
    private var clearJob: Job? = null

    override suspend fun copySensitive(text: String, timeoutMs: Long): Job =
        clipboardMutex.withLock {
            cancelClearLocked()
            val token = ownershipToken("secret")
            val clipData = ClipData.newPlainText(token, text).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    description.extras?.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                }
            }
            clipboardManager.setPrimaryClip(clipData)
            ownedToken = token
            scope.launch {
                delay(timeoutMs.coerceIn(MIN_TIMEOUT_MS, MAX_TIMEOUT_MS))
                clipboardMutex.withLock { clearIfOwnedLocked(token) }
            }.also { clearJob = it }
        }

    override suspend fun copy(text: String) {
        clipboardMutex.withLock {
            cancelClearLocked()
            ownedToken = null
            clipboardManager.setPrimaryClip(ClipData.newPlainText("PassVault", text))
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
            currentToken() == token
        }

    /**
     * Compatibility helper for callers that want a visible label. The label
     * remains namespaced and still carries an unguessable ownership token.
     */
    fun copySensitiveWithLabel(text: String, label: String, timeoutMs: Long = DEFAULT_TIMEOUT_MS): Job {
        val result = scope.launch {
            clipboardMutex.withLock {
                cancelClearLocked()
                val token = ownershipToken(label)
                val clipData = ClipData.newPlainText(token, text).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        description.extras?.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                    }
                }
                clipboardManager.setPrimaryClip(clipData)
                ownedToken = token
                scope.launch {
                    delay(timeoutMs.coerceIn(MIN_TIMEOUT_MS, MAX_TIMEOUT_MS))
                    clipboardMutex.withLock { clearIfOwnedLocked(token) }
                }.also { clearJob = it }
            }
        }
        return result
    }

    fun getClipboardText(): String? =
        clipboardManager.primaryClip?.let { clip ->
            if (clip.itemCount > 0) clip.getItemAt(0).text?.toString() else null
        }

    fun hasClipboardContent(): Boolean = clipboardManager.hasPrimaryClip()

    fun destroy() {
        runBlocking {
            clipboardMutex.withLock {
                ownedToken?.let { clearIfOwnedLocked(it) }
                cancelClearLocked()
            }
        }
        scope.cancel()
    }

    private fun clearIfOwnedLocked(expectedToken: String) {
        if (currentToken() != expectedToken) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clipboardManager.clearPrimaryClip()
        } else {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("PassVault", ""))
        }
        if (ownedToken == expectedToken) {
            ownedToken = null
            cancelClearLocked()
        }
    }

    private fun currentToken(): String? =
        clipboardManager.primaryClip?.description?.label?.toString()

    private fun cancelClearLocked() {
        clearJob?.cancel()
        clearJob = null
    }

    private fun ownershipToken(label: String): String =
        "PassVault:$label:${UUID.randomUUID()}"

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 30_000L
        const val MIN_TIMEOUT_MS = 5_000L
        const val MAX_TIMEOUT_MS = 300_000L
    }
}
