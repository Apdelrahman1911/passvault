package com.passvault.shared.platform

import com.passvault.core.security.ClipboardService
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
import platform.Foundation.NSDate
import platform.UIKit.UIPasteboard
import platform.UIKit.UIPasteboardOptionExpirationDate
import platform.UIKit.UIPasteboardOptionLocalOnly
import platform.UniformTypeIdentifiers.UTTypePlainText

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

    override suspend fun copySensitive(text: String, timeoutMs: Long) {
        mutex.withLock {
            val safeTimeoutMs = timeoutMs.coerceIn(MIN_TIMEOUT_MS, MAX_TIMEOUT_MS)
            // Returning from a dispatcher hop is a prompt-cancellation point.
            // Once iOS accepts the secret, always record ownership and install
            // the fallback timer; the pasteboard expiration remains an
            // independent OS-enforced cleanup boundary.
            withContext(NonCancellable + Dispatchers.Main) {
                val expirationDate = NSDate(
                    timeIntervalSinceReferenceDate = NSDate().timeIntervalSinceReferenceDate +
                        safeTimeoutMs / MILLIS_PER_SECOND,
                )
                UIPasteboard.generalPasteboard.setItems(
                    items = listOf(mapOf(UTTypePlainText.identifier to text)),
                    options = mapOf(
                        UIPasteboardOptionLocalOnly to true,
                        UIPasteboardOptionExpirationDate to expirationDate,
                    ),
                )
                val changeCount = UIPasteboard.generalPasteboard.changeCount
                clearJob?.cancel()
                ownedChangeCount = changeCount

                clearJob = scope.launch {
                    delay(safeTimeoutMs)
                    mutex.withLock {
                        clearIfOwned(changeCount)
                    }
                }
            }
        }
    }

    override suspend fun copy(text: String) {
        mutex.withLock {
            withContext(Dispatchers.Main) {
                UIPasteboard.generalPasteboard.string = text
            }
            clearJob?.cancel()
            clearJob = null
            ownedChangeCount = null
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            ownedChangeCount?.let { clearIfOwned(it) }
        }
    }

    override suspend fun containsSensitive(): Boolean = mutex.withLock {
        val expected = ownedChangeCount ?: return@withLock false
        val isOwned = withContext(Dispatchers.Main) {
            UIPasteboard.generalPasteboard.changeCount == expected
        }
        if (!isOwned && ownedChangeCount == expected) {
            ownedChangeCount = null
            clearJob?.cancel()
            clearJob = null
        }
        isOwned
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
        const val MILLIS_PER_SECOND = 1_000.0
    }
}
