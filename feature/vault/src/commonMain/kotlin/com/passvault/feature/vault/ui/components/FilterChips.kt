package com.passvault.feature.vault.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.passvault.feature.vault.presentation.VaultViewModel

@Composable
fun FilterChips(
    activeFilter: VaultViewModel.CredentialFilter,
    onFilterChanged: (VaultViewModel.CredentialFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = activeFilter == VaultViewModel.CredentialFilter.ALL,
            onClick = { onFilterChanged(VaultViewModel.CredentialFilter.ALL) },
            label = { Text(stringResource(Res.string.ui_all)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        FilterChip(
            selected = activeFilter == VaultViewModel.CredentialFilter.FAVORITES,
            onClick = { onFilterChanged(VaultViewModel.CredentialFilter.FAVORITES) },
            label = { Text(stringResource(Res.string.nav_favorites)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        FilterChip(
            selected = activeFilter == VaultViewModel.CredentialFilter.WEAK_PASSWORDS,
            onClick = { onFilterChanged(VaultViewModel.CredentialFilter.WEAK_PASSWORDS) },
            label = { Text(stringResource(Res.string.password_strength_weak)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        FilterChip(
            selected = activeFilter == VaultViewModel.CredentialFilter.DUPLICATES,
            onClick = { onFilterChanged(VaultViewModel.CredentialFilter.DUPLICATES) },
            label = { Text(stringResource(Res.string.ui_duplicates)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        FilterChip(
            selected = activeFilter == VaultViewModel.CredentialFilter.EXPIRED,
            onClick = { onFilterChanged(VaultViewModel.CredentialFilter.EXPIRED) },
            label = { Text(stringResource(Res.string.ui_old)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}
