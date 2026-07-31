package com.passvault.android.security

import android.app.Activity
import android.os.Build
import android.view.WindowManager
import com.passvault.core.security.ScreenshotProtection
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

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
    private val protectionCount = AtomicInteger(0)
    private val protectedActivities = CopyOnWriteArrayList<Activity>()
    private val activityVisibility = HashMap<Activity, Boolean>()
    private val lock = Any()

    companion object {
        /**
         * Apply protection to a specific activity.
         * This is a convenience method for one-time application.
         */
        fun applyToActivity(activity: Activity) {
            activity.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.setRecentsScreenshotEnabled(false)
            }
        }

        /**
         * Remove protection from a specific activity.
         */
        fun removeFromActivity(activity: Activity) {
            activity.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                activity.setRecentsScreenshotEnabled(true)
            }
        }
    }

    /**
     * Enable screenshot protection globally.
     * This will apply FLAG_SECURE to all registered activities.
     */
    override fun enableProtection() {
        if (isProtectionEnabled.getAndSet(true)) {
            // Already enabled, just increment count
            protectionCount.incrementAndGet()
            return
        }

        protectionCount.set(1)
        applyToAllActivities()
    }

    /**
     * Disable screenshot protection globally.
     * Only actually disables when protection count reaches zero.
     */
    override fun disableProtection() {
        val count = protectionCount.decrementAndGet()

        if (count <= 0) {
            isProtectionEnabled.set(false)
            protectionCount.set(0)
            removeFromAllActivities()
        }
    }

    /**
     * Check if protection is currently enabled.
     */
    override fun isEnabled(): Boolean {
        return isProtectionEnabled.get()
    }

    /**
     * Apply protection to the specified activity.
     * This registers the activity for lifecycle-aware protection.
     */
    fun apply(activity: Activity) {
        synchronized(lock) {
            if (!protectedActivities.contains(activity)) {
                protectedActivities.add(activity)
                activityVisibility[activity] = activity.hasWindowFocus()

                // Apply immediately if protection is enabled
                if (isProtectionEnabled.get()) {
                    applyToActivity(activity)
                }
            }
        }
    }

    /**
     * Remove protection from the specified activity.
     * This unregisters the activity from lifecycle-aware protection.
     */
    fun remove(activity: Activity) {
        synchronized(lock) {
            protectedActivities.remove(activity)
            activityVisibility.remove(activity)
            removeFromActivity(activity)
        }
    }

    /**
     * Register activity for protection tracking.
     * Call this in Activity.onCreate() or when activity becomes active.
     */
    fun registerActivity(activity: Activity) {
        apply(activity)
    }

    /**
     * Unregister activity from protection tracking.
     * Call this in Activity.onDestroy().
     */
    fun unregisterActivity(activity: Activity) {
        remove(activity)
    }

    /**
     * Called when activity gains focus.
     * Re-applies protection if needed.
     */
    fun onActivityResumed(activity: Activity) {
        synchronized(lock) {
            activityVisibility[activity] = true

            if (isProtectionEnabled.get() && protectedActivities.contains(activity)) {
                applyToActivity(activity)
            }
        }
    }

    /**
     * Called when activity loses focus.
     * Keeps protection active (protection persists across lifecycle).
     */
    fun onActivityPaused(activity: Activity) {
        synchronized(lock) {
            activityVisibility[activity] = false
            // Protection remains active - we don't remove FLAG_SECURE on pause
            // This ensures screenshots can't be taken in app switcher
        }
    }

    /**
     * Temporarily disable protection for the specified activity.
     * Useful for allowing screenshots in specific non-sensitive screens.
     * Remember to call restoreProtection() when done.
     */
    fun temporarilyDisable(activity: Activity): Boolean {
        return synchronized(lock) {
            if (protectedActivities.contains(activity)) {
                removeFromActivity(activity)
                true
            } else {
                false
            }
        }
    }

    /**
     * Restore protection to the specified activity.
     * Call this after temporarilyDisable() when returning to sensitive content.
     */
    fun restoreProtection(activity: Activity): Boolean {
        return synchronized(lock) {
            if (protectedActivities.contains(activity) && isProtectionEnabled.get()) {
                applyToActivity(activity)
                true
            } else {
                false
            }
        }
    }

    /**
     * Temporarily disable protection for all registered activities.
     * Returns the previous protection state so it can be restored.
     */
    fun temporarilyDisableAll(): Boolean {
        val wasEnabled = isProtectionEnabled.get()
        if (wasEnabled) {
            disableProtection()
        }
        return wasEnabled
    }

    /**
     * Restore global protection state.
     * Call this after temporarilyDisableAll() when returning to sensitive content.
     */
    fun restoreGlobalProtection() {
        if (!isProtectionEnabled.get()) {
            enableProtection()
        }
    }

    /**
     * Apply protection to all registered activities.
     */
    private fun applyToAllActivities() {
        synchronized(lock) {
            protectedActivities.forEach { activity ->
                if (!activity.isFinishing && !activity.isDestroyed) {
                    applyToActivity(activity)
                }
            }
        }
    }

    /**
     * Remove protection from all registered activities.
     */
    private fun removeFromAllActivities() {
        synchronized(lock) {
            protectedActivities.forEach { activity ->
                if (!activity.isFinishing && !activity.isDestroyed) {
                    removeFromActivity(activity)
                }
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
                    activityVisibility.remove(activity)
                }
            }
        }
    }

    /**
     * Get the number of currently protected activities.
     */
    fun getProtectedActivityCount(): Int {
        return synchronized(lock) {
            cleanup()
            protectedActivities.size
        }
    }

    /**
     * Check if a specific activity is being protected.
     */
    fun isActivityProtected(activity: Activity): Boolean {
        return synchronized(lock) {
            protectedActivities.contains(activity)
        }
    }

    /**
     * Clear all registrations. Use with caution - only for testing or app termination.
     */
    fun clearAll() {
        synchronized(lock) {
            // Remove protection from all activities first
            removeFromAllActivities()

            // Clear tracking
            protectedActivities.clear()
            activityVisibility.clear()

            // Reset state
            isProtectionEnabled.set(false)
            protectionCount.set(0)
        }
    }
}

/**
 * Extension function to apply screenshot protection to an Activity.
 */
fun Activity.secureAgainstScreenshots() {
    AndroidScreenshotProtection.applyToActivity(this)
}

/**
 * Extension function to remove screenshot protection from an Activity.
 */
fun Activity.allowScreenshots() {
    AndroidScreenshotProtection.removeFromActivity(this)
}
