package com.passvault.feature.vault.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.passvault.feature.vault.presentation.VaultViewModel
import org.jetbrains.compose.resources.StringResource

@Composable
fun FilterChips(
    activeFilter: VaultViewModel.CredentialFilter,
    onFilterChanged: (VaultViewModel.CredentialFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CredentialFilterChip(
            option = VaultViewModel.CredentialFilter.ALL,
            activeFilter = activeFilter,
            icon = Icons.AutoMirrored.Filled.List,
            label = Res.string.ui_all,
            onFilterChanged = onFilterChanged,
        )
        CredentialFilterChip(
            option = VaultViewModel.CredentialFilter.FAVORITES,
            activeFilter = activeFilter,
            icon = Icons.Default.Favorite,
            label = Res.string.nav_favorites,
            onFilterChanged = onFilterChanged,
        )
        CredentialFilterChip(
            option = VaultViewModel.CredentialFilter.WEAK_PASSWORDS,
            activeFilter = activeFilter,
            icon = Icons.Default.Warning,
            label = Res.string.password_strength_weak,
            onFilterChanged = onFilterChanged,
        )
        CredentialFilterChip(
            option = VaultViewModel.CredentialFilter.DUPLICATES,
            activeFilter = activeFilter,
            icon = Icons.Default.ContentCopy,
            label = Res.string.ui_duplicates,
            onFilterChanged = onFilterChanged,
        )
        CredentialFilterChip(
            option = VaultViewModel.CredentialFilter.EXPIRED,
            activeFilter = activeFilter,
            icon = Icons.Default.Schedule,
            label = Res.string.ui_old,
            onFilterChanged = onFilterChanged,
        )
    }
}

@Composable
private fun CredentialFilterChip(
    option: VaultViewModel.CredentialFilter,
    activeFilter: VaultViewModel.CredentialFilter,
    icon: ImageVector,
    label: StringResource,
    onFilterChanged: (VaultViewModel.CredentialFilter) -> Unit,
) {
    FilterChip(
        selected = activeFilter == option,
        onClick = { onFilterChanged(option) },
        label = { Text(stringResource(label)) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        },
    )
}
