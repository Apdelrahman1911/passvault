package com.passvault.android

import android.app.Application
import android.os.StrictMode
import com.passvault.android.di.androidModule
import com.passvault.android.security.AndroidScreenshotProtection
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.navigation.AppCommand
import com.passvault.core.navigation.AppCommandDispatcher
import com.passvault.shared.di.AppModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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
    private val vaultRepository: VaultRepository by inject()
    private val commandDispatcher: AppCommandDispatcher by inject()
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        initializeKoin()
        configureDebugStrictMode()
        if (!BuildConfig.STORE_SCREENSHOT_MODE) {
            applicationScope.launch {
                screenshotProtection.enableProtection()
            }
        }
    }

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
        screenshotProtection.clearAll()
        applicationScope.cancel()
        super.onTerminate()
    }

    private fun lockAndReleaseSensitiveResources() {
        commandDispatcher.dispatch(AppCommand.LOCK)
        screenshotProtection.cleanup()
        applicationScope.launch {
            vaultRepository.lock()
        }
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
