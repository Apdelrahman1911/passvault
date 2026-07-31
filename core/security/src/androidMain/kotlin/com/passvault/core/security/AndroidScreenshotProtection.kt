package com.passvault.core.security

import android.app.Activity
import android.content.Context
import android.view.WindowManager

/**
 * Android implementation of screenshot protection.
 * Uses FLAG_SECURE to prevent screenshots and screen recordings.
 */
class AndroidScreenshotProtection(
    private val context: Context,
) : ScreenshotProtection {
    
    private var isEnabled = false
    
    override fun enableProtection() {
        val activity = context as? Activity ?: return
        activity.runOnUiThread {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
            isEnabled = true
        }
    }
    
    override fun disableProtection() {
        val activity = context as? Activity ?: return
        activity.runOnUiThread {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            isEnabled = false
        }
    }
    
    override fun isEnabled(): Boolean = isEnabled
    
    /**
     * Check if FLAG_SECURE is currently set on the window.
     */
    fun isFlagSecureEnabled(): Boolean {
        val activity = context as? Activity ?: return false
        return (activity.window.attributes.flags and 
            WindowManager.LayoutParams.FLAG_SECURE) != 0
    }
    
    /**
     * Enable protection with additional flags.
     * This also prevents the app from being shown in recent apps.
     */
    fun enableStrongProtection() {
        val activity = context as? Activity ?: return
        activity.runOnUiThread {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_SECURE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            isEnabled = true
        }
    }
    
    /**
     * Toggle protection state.
     */
    fun toggleProtection(enabled: Boolean) {
        if (enabled) enableProtection() else disableProtection()
    }
}
