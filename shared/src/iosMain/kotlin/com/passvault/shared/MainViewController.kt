package com.passvault.shared

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import com.passvault.shared.di.AppModule
import com.passvault.shared.di.iosModule
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

/**
 * UIKit entry point consumed by the lightweight SwiftUI host in iosApp.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun mainViewController(): UIViewController {
    if (KoinPlatform.getKoinOrNull() == null) {
        startKoin {
            modules(AppModule.getAllModules(iosModule))
        }
    }

    return ComposeUIViewController(
        configure = {
            // Let each Compose screen draw beneath the iOS status bar and
            // Home indicator. WindowInsets.safeDrawing still keeps controls
            // inside the usable area.
            opaque = false
        },
        content = { PassVaultApp() },
    )
}
