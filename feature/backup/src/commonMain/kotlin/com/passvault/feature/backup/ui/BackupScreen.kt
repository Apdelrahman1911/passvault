@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.passvault.feature.backup.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passvault.feature.backup.presentation.BackupViewModel
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors

@Composable
fun BackupScreen(
    state: BackupViewModel.BackupState,
    onEvent: (BackupViewModel.BackupEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onEvent(BackupViewModel.BackupEvent.OnBackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.action_back),
                        )
                    }
                },
                colors = passVaultTopAppBarColors(),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            PrimaryTabRow(selectedTabIndex = state.selectedTab.ordinal) {
                BackupViewModel.BackupTab.entries.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { onEvent(BackupViewModel.BackupEvent.OnTabChanged(tab)) },
                        enabled = !state.hasActiveOperation && !state.showRestoreConfirmation,
                        text = {
                            Text(
                                when (tab) {
                                    BackupViewModel.BackupTab.EXPORT ->
                                        stringResource(Res.string.ui_create_backup)
                                    BackupViewModel.BackupTab.IMPORT ->
                                        stringResource(Res.string.ui_restore_backup)
                                },
                            )
                        },
                        icon = {
                            Icon(
                                when (tab) {
                                    BackupViewModel.BackupTab.EXPORT -> Icons.Default.Upload
                                    BackupViewModel.BackupTab.IMPORT -> Icons.Default.Download
                                },
                                contentDescription = null,
                            )
                        },
                    )
                }
            }
            when (state.selectedTab) {
                BackupViewModel.BackupTab.EXPORT -> BackupExportContent(state, onEvent)
                BackupViewModel.BackupTab.IMPORT -> BackupImportContent(state, onEvent)
            }
        }
    }
}
