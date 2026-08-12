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
     * Disable screenshot protection.
     * This should be called when navigating away from sensitive content.
     */
    fun disableProtection()

    /**
     * Check if protection is currently enabled.
     */
    fun isEnabled(): Boolean
}
