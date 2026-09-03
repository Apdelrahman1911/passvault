import PassVaultShared
import SwiftUI
import UIKit

@main
struct PassVaultApp: App {
    @Environment(\.scenePhase) private var scenePhase
    @AppStorage("theme") private var themePreference = "SYSTEM"
    @State private var privacyCoverVisible = true
    @State private var privacyRecoveryRequired = false
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
                    if privacyRecoveryRequired {
                        PrivacyRecoveryView {
                            requestContentReadiness()
                        }
                    } else {
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
            }
            .preferredColorScheme(preferredColorScheme)
            .onChange(of: scenePhase, initial: true) { _, newPhase in
                if newPhase != .active {
                    privacyCoverVisible = true
                    privacyRecoveryRequired = false
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
        privacyRecoveryRequired = false
        lifecycleBridge.applicationDidBecomeActive(
            onReady: {
                if scenePhase == .active {
                    hasCompletedInitialLaunch = true
                    privacyCoverVisible = false
                }
            },
            onRecoveryRequired: {
                if scenePhase == .active {
                    privacyCoverVisible = true
                    privacyRecoveryRequired = true
                }
            }
        )
    }
}

private struct PrivacyRecoveryView: View {
    let onRetry: () -> Void

    var body: some View {
        ZStack {
            Color(uiColor: .systemBackground)
                .ignoresSafeArea()

            VStack(spacing: 16) {
                Image(systemName: "lock.shield")
                    .font(.system(size: 36, weight: .semibold))
                    .accessibilityHidden(true)
                Text("privacy_recovery_title")
                    .font(.headline)
                    .multilineTextAlignment(.center)
                Text("privacy_recovery_message")
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                Button("privacy_recovery_retry", action: onRetry)
                    .buttonStyle(.borderedProminent)
            }
            .padding(32)
        }
    }
}
