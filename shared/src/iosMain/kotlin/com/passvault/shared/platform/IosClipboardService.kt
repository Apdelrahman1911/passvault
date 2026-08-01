package com.passvault.shared.platform

import com.passvault.core.security.ClipboardService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import platform.UIKit.UIPasteboard

/**
 * Ownership-aware iOS clipboard adapter.
 *
 * UIPasteboard.changeCount lets the expiry timer avoid erasing clipboard
 * content copied by the user after a PassVault secret.
 */
class IosClipboardService(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) : ClipboardService {
    private val mutex = Mutex()
    private var ownedChangeCount: Long? = null
    private var clearJob: Job? = null

    override suspend fun copySensitive(text: String, timeoutMs: Long): Job = mutex.withLock {
        clearJob?.cancel()
        val changeCount = withContext(Dispatchers.Main) {
            UIPasteboard.generalPasteboard.string = text
            UIPasteboard.generalPasteboard.changeCount
        }
        ownedChangeCount = changeCount

        scope.launch {
            delay(timeoutMs.coerceIn(MIN_TIMEOUT_MS, MAX_TIMEOUT_MS))
            mutex.withLock {
                clearIfOwned(changeCount)
            }
        }.also { clearJob = it }
    }

    override suspend fun copy(text: String) {
        mutex.withLock {
            clearJob?.cancel()
            clearJob = null
            ownedChangeCount = null
            withContext(Dispatchers.Main) {
                UIPasteboard.generalPasteboard.string = text
            }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            ownedChangeCount?.let { clearIfOwned(it) }
        }
    }

    override suspend fun containsSensitive(): Boolean = mutex.withLock {
        val expected = ownedChangeCount ?: return@withLock false
        withContext(Dispatchers.Main) {
            UIPasteboard.generalPasteboard.changeCount == expected
        }
    }

    private suspend fun clearIfOwned(expectedChangeCount: Long) {
        withContext(Dispatchers.Main) {
            if (UIPasteboard.generalPasteboard.changeCount == expectedChangeCount) {
                UIPasteboard.generalPasteboard.string = ""
            }
        }
        if (ownedChangeCount == expectedChangeCount) {
            ownedChangeCount = null
            clearJob = null
        }
    }

    private companion object {
        const val MIN_TIMEOUT_MS = 5_000L
        const val MAX_TIMEOUT_MS = 300_000L
    }
}
