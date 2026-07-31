package com.passvault.android

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.passvault.android.security.AndroidScreenshotProtection
import com.passvault.android.backup.AndroidBackupFileStore
import com.passvault.core.domain.repository.VaultRepository
import com.passvault.core.navigation.AppCommand
import com.passvault.core.navigation.AppCommandDispatcher
import com.passvault.shared.PassVaultApp
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * MainActivity for PassVault Android application.
 * 
 * Handles:
 * - Edge-to-edge display with proper insets
 * - Screenshot protection for sensitive content
 * - Vault session lifecycle (auto-lock on background)
 * - Lifecycle-aware security features
 * - Configuration change handling
 */
class MainActivity : ComponentActivity() {

    private val screenshotProtection: AndroidScreenshotProtection by inject()
    private val backupFileStore: AndroidBackupFileStore by inject()
    private val vaultRepository: VaultRepository by inject()
    private val commandDispatcher: AppCommandDispatcher by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Configure window for edge-to-edge
        configureWindow()

        // Register this activity for screenshot protection
        screenshotProtection.registerActivity(this)
        backupFileStore.attach(this)

        // Apply screenshot protection immediately
        screenshotProtection.apply(this)

        setContent {
            DisposableEffect(Unit) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                onDispose {}
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                PassVaultApp()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        screenshotProtection.onActivityResumed(this)
        if (screenshotProtection.isEnabled()) {
            screenshotProtection.apply(this)
        }
    }

    override fun onPause() {
        super.onPause()
        screenshotProtection.onActivityPaused(this)
    }

    override fun onStop() {
        super.onStop()
        commandDispatcher.dispatch(AppCommand.LOCK)
        lifecycleScope.launch {
            vaultRepository.lock()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister from screenshot protection
        screenshotProtection.unregisterActivity(this)
        backupFileStore.detach(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Re-apply screenshot protection when window regains focus
            if (screenshotProtection.isEnabled()) {
                screenshotProtection.apply(this)
            }
        }
    }

    /**
     * Configure window properties for edge-to-edge and security.
     */
    private fun configureWindow() {
        // Ensure FLAG_SECURE is applied before window becomes visible
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        // Configure insets handling for edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }

        // Handle secure flags for different Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+: Additional privacy features
            window?.setHideOverlayWindows(true)
        }
    }

}
