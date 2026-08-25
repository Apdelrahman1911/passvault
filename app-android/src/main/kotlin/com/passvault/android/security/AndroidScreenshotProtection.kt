package com.passvault.android.security

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import com.passvault.android.BuildConfig
import com.passvault.core.security.ScreenshotProtection
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android implementation of screenshot protection using FLAG_SECURE.
 * Prevents screenshots and screen recordings in activities showing sensitive content.
 *
 * Security features:
 * - FLAG_SECURE: Prevents screenshots and screen recordings
 * - Task snapshot exclusion: Prevents app switcher screenshots (Android 11+)
 * - Lifecycle-aware: Automatically manages protection based on activity lifecycle
 * - Multi-activity support: Can track multiple activities simultaneously
 */
class AndroidScreenshotProtection : ScreenshotProtection {

    private val isProtectionEnabled = AtomicBoolean(false)
    private val protectedActivities = CopyOnWriteArrayList<Activity>()
    private val lock = Any()

    companion object {
        /**
         * Apply protection to a specific activity.
         * This is a convenience method for one-time application.
         */
        fun applyToActivity(activity: Activity) {
            if (BuildConfig.STORE_SCREENSHOT_MODE) return
            activity.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.setRecentsScreenshotEnabled(false)
            }
        }

    }

    /**
     * Enable screenshot protection globally.
     * This will apply FLAG_SECURE to all registered activities.
     */
    override fun enableProtection() {
        synchronized(lock) {
            if (isProtectionEnabled.get()) return
            isProtectionEnabled.set(true)
            protectedActivities.forEach { activity ->
                if (!activity.isFinishing && !activity.isDestroyed) {
                    applyToActivity(activity)
                }
            }
        }
    }

    /**
     * Check if protection is currently enabled.
     */
    override fun isEnabled(): Boolean {
        return isProtectionEnabled.get()
    }

    /**
     * Register activity for protection tracking.
     * Call this in Activity.onCreate() or when activity becomes active.
     */
    fun registerActivity(activity: Activity) {
        synchronized(lock) {
            if (!protectedActivities.contains(activity)) {
                protectedActivities.add(activity)

                // Apply immediately if protection is enabled
                if (isProtectionEnabled.get()) {
                    applyToActivity(activity)
                }
            }
        }
    }

    /**
     * Unregister activity from protection tracking.
     * Call this in Activity.onDestroy().
     */
    fun unregisterActivity(activity: Activity) {
        synchronized(lock) {
            protectedActivities.remove(activity)
            // Destruction is not an authorization to expose the final window
            // buffer. Leave FLAG_SECURE on the retiring window until Android
            // releases it. Release builds have no runtime API that removes
            // protection from a live Activity.
        }
    }

    /**
     * Called when activity gains focus.
     * Re-applies protection if needed.
     */
    fun onActivityResumed(activity: Activity) {
        synchronized(lock) {
            if (isProtectionEnabled.get() && protectedActivities.contains(activity)) {
                applyToActivity(activity)
            }
        }
    }

    /**
     * Clean up finished activities from the tracking list.
     * Call this periodically or when needed.
     */
    fun cleanup() {
        synchronized(lock) {
            protectedActivities.toList().forEach { activity ->
                if (activity.isFinishing || activity.isDestroyed) {
                    protectedActivities.remove(activity)
                }
            }
        }
    }

}
