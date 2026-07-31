package com.passvault.core.security

/**
 * Interface for desktop window protection.
 * Controls window security features like screenshot protection.
 */
interface WindowProtection {
    /**
     * Enable protection for the application window.
     * Prevents screenshots and screen recording.
     */
    fun enableProtection()
    
    /**
     * Disable protection for the application window.
     * Allows screenshots and screen recording.
     */
    fun disableProtection()
    
    /**
     * Check if protection is currently enabled.
     */
    fun isProtected(): Boolean
    
    /**
     * Minimize the window (lock to taskbar).
     */
    fun minimize()
    
    /**
     * Lock the window (show lock screen overlay).
     */
    fun lock()
    
    /**
     * Unlock the window (hide lock screen overlay).
     */
    fun unlock()
}
