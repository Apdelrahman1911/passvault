import PassVaultShared
import SwiftUI
import UIKit

@main
struct PassVaultApp: App {
    @Environment(\.scenePhase) private var scenePhase
    @AppStorage("theme") private var themePreference = "SYSTEM"
    @State private var privacyCoverVisible = true
    @State private var hasCompletedInitialLaunch = false

    private let lifecycleBridge = IosAppLifecycleBridge()

    private var preferredColorScheme: ColorScheme? {
        switch themePreference {
        case "LIGHT": .light
        case "DARK": .dark
        default: nil
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView {
                if scenePhase == .active {
                    requestContentReadiness()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .accessibilityHidden(privacyCoverVisible)
            .allowsHitTesting(!privacyCoverVisible)
            .overlay {
                if privacyCoverVisible {
                    Group {
                        if hasCompletedInitialLaunch {
                            Color(uiColor: .systemBackground)
                        } else {
                            Color("LaunchBackground")
                        }
                    }
                    .ignoresSafeArea()
                    .accessibilityHidden(true)
                }
            }
            .preferredColorScheme(preferredColorScheme)
            .onChange(of: scenePhase, initial: true) { _, newPhase in
                if newPhase != .active {
                    privacyCoverVisible = true
                }
                switch newPhase {
                case .active:
                    // A background lock can be waiting behind an in-progress
                    // unlock. Keep the opaque cover until Kotlin confirms that
                    // serialized cleanup has completed.
                    requestContentReadiness()
                case .inactive:
                    lifecycleBridge.applicationWillResignActive()
                case .background:
                    lifecycleBridge.applicationDidEnterBackground()
                @unknown default:
                    // A future non-active scene phase must fail closed rather
                    // than leave the vault key resident behind the privacy cover.
                    lifecycleBridge.applicationDidEnterBackground()
                }
            }
        }
    }

    private func requestContentReadiness() {
        lifecycleBridge.applicationDidBecomeActive {
            if scenePhase == .active {
                hasCompletedInitialLaunch = true
                privacyCoverVisible = false
            }
        }
    }
}
