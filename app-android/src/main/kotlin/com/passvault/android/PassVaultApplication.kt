package com.passvault.android

import android.app.Application
import android.os.StrictMode
import com.passvault.android.attachment.AndroidAttachmentFileStore
import com.passvault.android.di.androidModule
import com.passvault.android.lifecycle.AndroidLifecycleLockCoordinator
import com.passvault.android.security.AndroidScreenshotProtection
import com.passvault.shared.di.AppModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Initializes dependency injection and process-wide security policy.
 *
 * Activity registration and normal background locking are owned by
 * [MainActivity]. This class additionally locks on severe process memory
 * pressure so the session key is not retained by an app likely to be killed.
 */
class PassVaultApplication : Application() {
    private val screenshotProtection: AndroidScreenshotProtection by inject()
    private val lifecycleLockCoordinator: AndroidLifecycleLockCoordinator by inject()
    private val attachmentFileStore: AndroidAttachmentFileStore by inject()

    override fun onCreate() {
        super.onCreate()
        initializeKoin()
        configureDebugStrictMode()
        if (!BuildConfig.STORE_SCREENSHOT_MODE) {
            // Window flags are a main-thread UI boundary. Enable the singleton
            // synchronously before the first Activity can register itself.
            screenshotProtection.enableProtection()
        }
    }

    @Suppress("DEPRECATION") // Keep foreground pressure signals on pre-Android 14 devices.
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (
            level == TRIM_MEMORY_RUNNING_LOW ||
            level == TRIM_MEMORY_RUNNING_CRITICAL ||
            level >= TRIM_MEMORY_BACKGROUND
        ) {
            lockAndReleaseSensitiveResources()
        }
    }

    @Deprecated("Deprecated in Android")
    override fun onLowMemory() {
        super.onLowMemory()
        lockAndReleaseSensitiveResources()
    }

    override fun onTerminate() {
        // Do not remove FLAG_SECURE from any retiring Activity. Android only
        // calls this hook in emulated process environments, and clearing the
        // flag can expose the final native window buffer before destruction.
        super.onTerminate()
    }

    private fun lockAndReleaseSensitiveResources() {
        screenshotProtection.cleanup()
        runCatching { attachmentFileStore.cleanupForMemoryPressure() }
        lifecycleLockCoordinator.onMemoryPressure()
    }

    private fun initializeKoin() {
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@PassVaultApplication)
            modules(AppModule.getAllModules(androidModule))
        }
    }

    private fun configureDebugStrictMode() {
        if (!BuildConfig.DEBUG) return

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build(),
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .penaltyLog()
                .build(),
        )
    }
}
