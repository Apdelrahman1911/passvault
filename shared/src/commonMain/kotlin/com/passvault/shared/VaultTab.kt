package com.passvault.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.passvault.feature.vault.ui.VaultActionDock

internal enum class VaultTab {
    HOME,
    GENERATOR,
    TWO_FACTOR_CODES,
    SETTINGS,
}

internal fun VaultTab.toggle(destination: VaultTab): VaultTab =
    if (this == destination && destination != VaultTab.HOME) VaultTab.HOME else destination

/**
 * Compact root navigation keeps the action dock overlaid while switching its
 * top-level destinations. Selecting an active non-home destination returns
 * to Home.
 */
@Composable
internal fun VaultTabShell(
    selectedTab: VaultTab,
    onSelectedTabChanged: (VaultTab) -> Unit,
    onAdd: () -> Unit,
    vaultContent: @Composable (Modifier) -> Unit,
    generatorContent: @Composable (Modifier) -> Unit,
    twoFactorCodesContent: @Composable (Modifier) -> Unit,
    settingsContent: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (selectedTab) {
            VaultTab.HOME -> vaultContent(Modifier.fillMaxSize())
            VaultTab.GENERATOR -> generatorContent(Modifier.fillMaxSize())
            VaultTab.TWO_FACTOR_CODES -> twoFactorCodesContent(Modifier.fillMaxSize())
            VaultTab.SETTINGS -> settingsContent(Modifier.fillMaxSize())
        }

        VaultActionDock(
            onAddClick = {
                onSelectedTabChanged(VaultTab.HOME)
                onAdd()
            },
            onGeneratorClick = {
                onSelectedTabChanged(selectedTab.toggle(VaultTab.GENERATOR))
            },
            onTwoFactorCodesClick = {
                onSelectedTabChanged(selectedTab.toggle(VaultTab.TWO_FACTOR_CODES))
            },
            onSettingsClick = {
                onSelectedTabChanged(selectedTab.toggle(VaultTab.SETTINGS))
            },
            onHomeClick = { onSelectedTabChanged(VaultTab.HOME) },
            homeSelected = selectedTab == VaultTab.HOME,
            generatorSelected = selectedTab == VaultTab.GENERATOR,
            twoFactorCodesSelected = selectedTab == VaultTab.TWO_FACTOR_CODES,
            settingsSelected = selectedTab == VaultTab.SETTINGS,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
