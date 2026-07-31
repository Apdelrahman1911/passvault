package com.passvault.core.designsystem.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    homeContent: @Composable () -> Unit = {},
    vaultContent: @Composable () -> Unit = {},
    settingsContent: @Composable () -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = modifier,
                containerColor = Color.Transparent
            ) {
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Filled.Home, contentDescription = null) },
                    label = { Text(text = stringResource(Res.string.ui_home)) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Filled.VpnKey, contentDescription = null) },
                    label = { Text(text = stringResource(Res.string.nav_vault)) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = null) },
                    label = { Text(text = stringResource(Res.string.action_settings)) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> homeContent()
            1 -> vaultContent()
            2 -> settingsContent()
        }
    }
}
