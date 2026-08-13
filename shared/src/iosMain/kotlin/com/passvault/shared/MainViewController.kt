package com.passvault.shared

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import com.passvault.shared.di.AppModule
import com.passvault.shared.di.iosModule
import com.passvault.shared.platform.applyIosNativeLayoutDirection
import com.passvault.shared.platform.initialIosLayoutDirection
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

    val controller = ComposeUIViewController(
        configure = {
            // Let each Compose screen draw beneath the iOS status bar and
            // Home indicator. WindowInsets.safeDrawing still keeps controls
            // inside the usable area.
            opaque = false
            // Compose UI owns the single start-edge Back input. Its default
            // end edge remains disabled so RTL never enables both edges.
        },
        content = { PassVaultApp() },
    )
    // Set UIKit's direction before the view enters its window hierarchy so Compose UI's default
    // edge-back recognizer is installed on the correct physical edge from the first frame.
    applyIosNativeLayoutDirection(controller, initialIosLayoutDirection())
    return controller
}
