package com.passvault.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.passvault.feature.vault.ui.VaultActionDock

internal enum class VaultTab {
    GENERATOR,
    HEALTH,
    SETTINGS,
}

internal fun VaultTab?.toggle(destination: VaultTab): VaultTab? =
    if (this == destination) null else destination

/**
 * Compact root navigation keeps the action dock mounted while switching its
 * three top-level destinations. Selecting the active destination closes it
 * and returns to the vault.
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
    var selectedTab by remember { mutableStateOf<VaultTab?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            VaultActionDock(
                onAddClick = {
                    selectedTab = null
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
                generatorSelected = selectedTab == VaultTab.GENERATOR,
                healthSelected = selectedTab == VaultTab.HEALTH,
                settingsSelected = selectedTab == VaultTab.SETTINGS,
            )
        },
    ) { contentPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .consumeWindowInsets(contentPadding)

        Box(modifier = contentModifier) {
            when (selectedTab) {
                VaultTab.GENERATOR -> generatorContent(Modifier.fillMaxSize())
                VaultTab.HEALTH -> healthContent(Modifier.fillMaxSize())
                VaultTab.SETTINGS -> settingsContent(Modifier.fillMaxSize())
                null -> vaultContent(Modifier.fillMaxSize())
            }
        }
    }
}
