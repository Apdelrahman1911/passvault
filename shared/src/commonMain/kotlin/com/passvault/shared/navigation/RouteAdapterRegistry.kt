package com.passvault.shared.navigation

import com.passvault.core.navigation.PassVaultRouteKind
import com.passvault.shared.navigation.adapters.authRouteAdapterKinds
import com.passvault.shared.navigation.adapters.settingsRouteAdapterKinds
import com.passvault.shared.navigation.adapters.toolsRouteAdapterKinds
import com.passvault.shared.navigation.adapters.vaultRouteAdapterKinds

internal val registeredRouteAdapterKinds: Set<PassVaultRouteKind> =
    authRouteAdapterKinds + vaultRouteAdapterKinds + toolsRouteAdapterKinds + settingsRouteAdapterKinds

internal fun verifyRouteAdapterRegistry() {
    val declarationCount = authRouteAdapterKinds.size + vaultRouteAdapterKinds.size +
        toolsRouteAdapterKinds.size + settingsRouteAdapterKinds.size
    check(registeredRouteAdapterKinds == PassVaultRouteKind.entries.toSet()) {
        "Every PassVault route kind must have a feature-owned Nav3 entry adapter"
    }
    check(declarationCount == registeredRouteAdapterKinds.size) {
        "Every PassVault route kind must be owned by exactly one feature adapter group"
    }
}
