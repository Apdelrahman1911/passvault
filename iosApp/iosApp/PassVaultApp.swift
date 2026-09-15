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
    @State private var protectedDataAvailable = UIApplication.shared.isProtectedDataAvailable
    @State private var runtimeMounted = UIApplication.shared.isProtectedDataAvailable
    @State private var runtimeGeneration = 0
    @State private var runtimeControllerAttached = false
    @State private var protectedDataTransitionInFlight = false
    @State private var protectedDataRecoveryPending = false

    private let lifecycleBridge = IosAppLifecycleBridge()
    private let protectedDataBackgroundTask = ProtectedDataBackgroundTask()

    private var preferredColorScheme: ColorScheme? {
        switch themePreference {
        case "LIGHT": .light
        case "DARK": .dark
        default: nil
        }
    }

    var body: some Scene {
        WindowGroup {
            Group {
                if runtimeMounted {
                    ContentView(
                        onControllerReady: {
                            runtimeControllerAttached = true
                            lifecycleBridge.composeRuntimeDidStart()
                            if scenePhase == .active {
                                requestContentReadiness()
                            }
                        },
                        onControllerDisposed: {
                            runtimeControllerAttached = false
                            completeProtectedDataRuntimeTeardown()
                        }
                    )
                    .id(runtimeGeneration)
                } else {
                    Color("LaunchBackground")
                        .ignoresSafeArea()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .accessibilityHidden(privacyCoverVisible)
            .allowsHitTesting(!privacyCoverVisible)
            .overlay {
                if privacyCoverVisible {
                    if privacyRecoveryRequired {
                        PrivacyRecoveryView {
                            retryPrivacyRecovery()
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
            .onReceive(
                NotificationCenter.default.publisher(
                    for: UIApplication.protectedDataWillBecomeUnavailableNotification
                )
            ) { _ in
                protectedDataWillBecomeUnavailable()
            }
            .onReceive(
                NotificationCenter.default.publisher(
                    for: UIApplication.protectedDataDidBecomeAvailableNotification
                )
            ) { _ in
                protectedDataDidBecomeAvailable()
            }
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
                    activateAvailableRuntime()
                case .inactive:
                    if runtimeMounted && !protectedDataTransitionInFlight {
                        lifecycleBridge.applicationWillResignActive()
                    }
                case .background:
                    if runtimeMounted && !protectedDataTransitionInFlight {
                        lifecycleBridge.applicationDidEnterBackground()
                    }
                @unknown default:
                    // A future non-active scene phase must fail closed rather
                    // than leave the vault key resident behind the privacy cover.
                    if runtimeMounted && !protectedDataTransitionInFlight {
                        lifecycleBridge.applicationDidEnterBackground()
                    }
                }
            }
        }
    }

    private func requestContentReadiness() {
        guard protectedDataAvailable,
              runtimeMounted,
              !protectedDataTransitionInFlight else {
            return
        }
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

    private func activateAvailableRuntime() {
        guard protectedDataAvailable, !protectedDataTransitionInFlight else { return }
        if runtimeMounted {
            requestContentReadiness()
        } else if protectedDataRecoveryPending {
            privacyRecoveryRequired = true
        } else {
            startRuntime()
        }
    }

    private func protectedDataWillBecomeUnavailable() {
        protectedDataAvailable = false
        privacyCoverVisible = true
        privacyRecoveryRequired = false
        guard runtimeMounted, !protectedDataTransitionInFlight else { return }

        protectedDataTransitionInFlight = true
        protectedDataRecoveryPending = false
        protectedDataBackgroundTask.begin()
        lifecycleBridge.applicationProtectedDataWillBecomeUnavailable(
            onRuntimeTeardownRequired: {
                detachRuntime(recoveryRequired: false)
            },
            onRecoveryRequired: {
                detachRuntime(recoveryRequired: true)
            }
        )
    }

    private func protectedDataDidBecomeAvailable() {
        protectedDataAvailable = true
        lifecycleBridge.applicationProtectedDataDidBecomeAvailable()
        if scenePhase == .active {
            activateAvailableRuntime()
        }
    }

    private func detachRuntime(recoveryRequired: Bool) {
        if recoveryRequired {
            protectedDataRecoveryPending = true
        }
        let controllerWasAttached = runtimeControllerAttached
        runtimeMounted = false
        if !controllerWasAttached {
            completeProtectedDataRuntimeTeardown()
        }
    }

    private func completeProtectedDataRuntimeTeardown() {
        guard protectedDataTransitionInFlight else { return }
        lifecycleBridge.composeRuntimeDidDetach(
            onRuntimeStopped: {
                finishProtectedDataRuntimeTeardown(recoveryRequired: false)
            },
            onRecoveryRequired: {
                finishProtectedDataRuntimeTeardown(recoveryRequired: true)
            }
        )
    }

    private func finishProtectedDataRuntimeTeardown(recoveryRequired: Bool) {
        if recoveryRequired {
            protectedDataRecoveryPending = true
        }
        protectedDataTransitionInFlight = false
        protectedDataBackgroundTask.end()
        if scenePhase == .active {
            activateAvailableRuntime()
        }
    }

    private func retryPrivacyRecovery() {
        privacyRecoveryRequired = false
        if runtimeMounted {
            requestContentReadiness()
        } else if protectedDataAvailable && !protectedDataTransitionInFlight {
            protectedDataRecoveryPending = false
            startRuntime()
        }
    }

    private func startRuntime() {
        guard protectedDataAvailable,
              !protectedDataTransitionInFlight,
              !runtimeMounted else {
            return
        }
        privacyRecoveryRequired = false
        protectedDataRecoveryPending = false
        runtimeControllerAttached = false
        runtimeGeneration += 1
        runtimeMounted = true
    }
}

private final class ProtectedDataBackgroundTask {
    private var identifier = UIBackgroundTaskIdentifier.invalid

    func begin() {
        guard identifier == .invalid else { return }
        identifier = UIApplication.shared.beginBackgroundTask(
            withName: "PassVault protected-data shutdown",
            expirationHandler: { [weak self] in
                DispatchQueue.main.async {
                    self?.end()
                }
            }
        )
    }

    func end() {
        guard identifier != .invalid else { return }
        UIApplication.shared.endBackgroundTask(identifier)
        identifier = .invalid
    }

    deinit {
        end()
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
