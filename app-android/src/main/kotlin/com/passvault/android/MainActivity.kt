package com.passvault.android

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.passvault.android.backup.AndroidBackupFileStore
import com.passvault.android.attachment.AndroidAttachmentFileStore
import com.passvault.android.lifecycle.AndroidLifecycleLockCoordinator
import com.passvault.android.security.AndroidBiometricKeyStore
import com.passvault.android.security.AndroidScreenshotProtection
import com.passvault.shared.PassVaultApp
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
class MainActivity : FragmentActivity() {

    private val screenshotProtection: AndroidScreenshotProtection by inject()
    private val backupFileStore: AndroidBackupFileStore by inject()
    private val attachmentFileStore: AndroidAttachmentFileStore by inject()
    private val biometricKeyStore: AndroidBiometricKeyStore by inject()
    private val lifecycleLockCoordinator: AndroidLifecycleLockCoordinator by inject()

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
        attachmentFileStore.attach(this)
        biometricKeyStore.attach(this)

        // Apply screenshot protection immediately
        if (!BuildConfig.STORE_SCREENSHOT_MODE) {
            AndroidScreenshotProtection.applyToActivity(this)
        }

        setContent {
            val privacyCoverVisible by lifecycleLockCoordinator.privacyCoverVisible.collectAsState()
            val coverColor = if (isSystemInDarkTheme()) Color.Black else Color.White

            DisposableEffect(Unit) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                onDispose {}
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (privacyCoverVisible) {
                                Modifier.clearAndSetSemantics { }
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    PassVaultApp()
                }
                if (privacyCoverVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(coverColor)
                            .clearAndSetSemantics { }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent().changes.forEach { it.consume() }
                                    }
                                }
                            },
                    )
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        attachmentFileStore.onActivityResumed()
        lifecycleLockCoordinator.onActivityResumed()
        screenshotProtection.onActivityResumed(this)
        if (screenshotProtection.isEnabled()) {
            AndroidScreenshotProtection.applyToActivity(this)
        }
    }

    override fun onStop() {
        super.onStop()
        lifecycleLockCoordinator.onActivityStopped(isChangingConfigurations)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister from screenshot protection
        screenshotProtection.unregisterActivity(this)
        backupFileStore.detach(this, isChangingConfigurations)
        attachmentFileStore.detach(this, isChangingConfigurations)
        biometricKeyStore.detach(this)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Re-apply screenshot protection when window regains focus
            if (screenshotProtection.isEnabled()) {
                AndroidScreenshotProtection.applyToActivity(this)
            }
        }
    }

    /**
     * Configure window properties for edge-to-edge and security.
     */
    private fun configureWindow() {
        // Ensure FLAG_SECURE is applied before window becomes visible
        if (!BuildConfig.STORE_SCREENSHOT_MODE) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        // Configure insets handling for edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (
            !BuildConfig.STORE_SCREENSHOT_MODE &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        ) {
            setRecentsScreenshotEnabled(false)
        }

        // Handle secure flags for different Android versions
        if (!BuildConfig.STORE_SCREENSHOT_MODE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+: Additional privacy features
            window?.setHideOverlayWindows(true)
        }
    }

}
