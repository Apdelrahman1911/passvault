@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.passvault.feature.backup.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.passvault.feature.backup.presentation.BackupViewModel
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors

@Composable
fun ImportScreen(
    viewModel: BackupViewModel,
    onNavigateBack: () -> Unit,
    onImportComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = viewModel.state.collectAsState().value
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            if (effect is BackupViewModel.BackupEffect.ShowImportSuccess) {
                onImportComplete()
            }
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        enabled = !state.hasActiveOperation,
                    ) {
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
        BackupImportContent(
            state = state,
            onEvent = viewModel::onEvent,
            modifier = Modifier,
            contentPadding = padding,
        )
    }
}
