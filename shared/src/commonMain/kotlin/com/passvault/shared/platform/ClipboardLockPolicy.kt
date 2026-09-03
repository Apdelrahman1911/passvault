package com.passvault.shared.platform

/**
 * Whether this platform preserves an expiring sensitive clipboard when the
 * vault locks only because the application entered the background.
 */
internal expect fun preservesSensitiveClipboardOnBackgroundLock(): Boolean
