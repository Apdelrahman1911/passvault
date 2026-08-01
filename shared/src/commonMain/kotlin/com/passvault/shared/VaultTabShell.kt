package com.passvault.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.passvault.feature.vault.ui.VaultActionDock

internal enum class VaultTab {
    HOME,
    GENERATOR,
    HEALTH,
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
    onAdd: () -> Unit,
    vaultContent: @Composable (Modifier) -> Unit,
    generatorContent: @Composable (Modifier) -> Unit,
    healthContent: @Composable (Modifier) -> Unit,
    settingsContent: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(VaultTab.HOME) }

    Box(modifier = modifier.fillMaxSize()) {
        when (selectedTab) {
            VaultTab.HOME -> vaultContent(Modifier.fillMaxSize())
            VaultTab.GENERATOR -> generatorContent(Modifier.fillMaxSize())
            VaultTab.HEALTH -> healthContent(Modifier.fillMaxSize())
            VaultTab.SETTINGS -> settingsContent(Modifier.fillMaxSize())
        }

        VaultActionDock(
            onAddClick = {
                selectedTab = VaultTab.HOME
                onAdd()
            },
            onGeneratorClick = {
                selectedTab = selectedTab.toggle(VaultTab.GENERATOR)
            },
            onHealthClick = {
                selectedTab = selectedTab.toggle(VaultTab.HEALTH)
            },
            onSettingsClick = {
                selectedTab = selectedTab.toggle(VaultTab.SETTINGS)
            },
            onHomeClick = { selectedTab = VaultTab.HOME },
            homeSelected = selectedTab == VaultTab.HOME,
            generatorSelected = selectedTab == VaultTab.GENERATOR,
            healthSelected = selectedTab == VaultTab.HEALTH,
            settingsSelected = selectedTab == VaultTab.SETTINGS,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
