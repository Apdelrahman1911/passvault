package com.passvault.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.passvault.core.navigation.TopLevelDestination
import com.passvault.feature.vault.ui.VaultActionDock

/**
 * Presentation shell only. Every layout receives the same Nav3-owned logical
 * tab state; compact/expanded differences must never create a second graph.
 */
@Composable
internal fun VaultTabShell(
    selectedTab: TopLevelDestination,
    onSelectedTab: (TopLevelDestination) -> Unit,
    onAdd: () -> Unit,
    content: @Composable (Modifier) -> Unit,
    showActionDock: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        content(Modifier.fillMaxSize())

        if (showActionDock) {
            VaultActionDock(
                onAddClick = onAdd,
                onGeneratorClick = { onSelectedTab(TopLevelDestination.GENERATOR) },
                onTwoFactorCodesClick = { onSelectedTab(TopLevelDestination.TWO_FACTOR_CODES) },
                onSettingsClick = { onSelectedTab(TopLevelDestination.SETTINGS) },
                onHomeClick = { onSelectedTab(TopLevelDestination.HOME) },
                homeSelected = selectedTab == TopLevelDestination.HOME,
                generatorSelected = selectedTab == TopLevelDestination.GENERATOR,
                twoFactorCodesSelected = selectedTab == TopLevelDestination.TWO_FACTOR_CODES,
                settingsSelected = selectedTab == TopLevelDestination.SETTINGS,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
