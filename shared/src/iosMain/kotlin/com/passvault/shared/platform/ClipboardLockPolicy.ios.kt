package com.passvault.shared.platform

/**
 * iOS gives sensitive pasteboard items an OS expiration date in addition to
 * the ownership-aware fallback timer. Keep that bounded copy across an app
 * switch; manual, inactivity, memory-pressure, and restore locks still clear.
 */
internal actual fun preservesSensitiveClipboardOnBackgroundLock(): Boolean = true
