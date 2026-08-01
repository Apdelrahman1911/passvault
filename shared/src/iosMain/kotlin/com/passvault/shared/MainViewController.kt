package com.passvault.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.passvault.shared.di.AppModule
import com.passvault.shared.di.iosModule
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform
import platform.UIKit.UIViewController

/**
 * UIKit entry point consumed by the lightweight SwiftUI host in iosApp.
 */
fun MainViewController(): UIViewController {
    if (KoinPlatform.getKoinOrNull() == null) {
        startKoin {
            modules(AppModule.getAllModules(iosModule))
        }
    }

    return ComposeUIViewController {
        PassVaultApp()
    }
}
