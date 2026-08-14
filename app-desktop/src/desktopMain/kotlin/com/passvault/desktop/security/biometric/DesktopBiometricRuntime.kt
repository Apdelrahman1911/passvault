package com.passvault.desktop.security.biometric

import com.passvault.core.security.BiometricKeyStore
import com.passvault.core.security.BiometricType
import com.passvault.core.security.UnavailableBiometricKeyStore
import com.passvault.desktop.OperatingSystem
import com.passvault.desktop.getOperatingSystem

internal class DesktopBiometricRuntime private constructor(
    val keyStore: BiometricKeyStore,
    val host: DesktopBiometricHost,
) {
    companion object {
        fun create(
            operatingSystem: OperatingSystem = getOperatingSystem(),
            loader: () -> DesktopBiometricBridge = {
                DesktopBiometricNativeLoader(operatingSystem).load()
            },
        ): DesktopBiometricRuntime {
            val type = when (operatingSystem) {
                OperatingSystem.MACOS -> BiometricType.TOUCH_ID
                OperatingSystem.WINDOWS -> BiometricType.WINDOWS_HELLO
                OperatingSystem.LINUX,
                OperatingSystem.UNKNOWN,
                -> null
            }
            if (type == null) return unavailable(BiometricType.GENERIC)
            return runCatching(loader).fold(
                onSuccess = { bridge ->
                    val promptCoordinator = DesktopBiometricPromptCoordinator()
                    DesktopBiometricRuntime(
                        keyStore = DesktopBiometricKeyStore(bridge, promptCoordinator),
                        host = DesktopBiometricHost(bridge, promptCoordinator),
                    )
                },
                onFailure = { unavailable(type) },
            )
        }

        private fun unavailable(type: BiometricType): DesktopBiometricRuntime = DesktopBiometricRuntime(
            keyStore = UnavailableBiometricKeyStore(type),
            host = DesktopBiometricHost(null),
        )
    }
}
