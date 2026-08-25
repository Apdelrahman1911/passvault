package com.passvault.core.security

/**
 * Interface for screenshot protection.
 * Prevents sensitive content from being captured in screenshots or screen recordings.
 */
interface ScreenshotProtection {
    /**
     * Enable screenshot protection.
     * This should be called when sensitive content is visible.
     */
    fun enableProtection()

    /**
     * Check if protection is currently enabled.
     */
    fun isEnabled(): Boolean
}
