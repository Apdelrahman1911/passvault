package com.passvault.feature.vault.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.feature.vault.presentation.VaultViewModel
import com.passvault.feature.vault.ui.components.CredentialCard
import com.passvault.feature.vault.ui.components.CredentialRow
import com.passvault.feature.vault.ui.components.FolderSidebar
import com.passvault.feature.vault.ui.components.FilterChips
import com.passvault.feature.vault.ui.components.TagCloud

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialListScreen(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = true,
) {
    if (isCompact) {
        // Compact layout - single column with bottom nav or drawer
        CompactCredentialList(
            state = state,
            onEvent = onEvent,
            modifier = modifier
        )
    } else {
        // Medium/Expanded layout - sidebar + list
        AdaptiveCredentialList(
            state = state,
            onEvent = onEvent,
            modifier = modifier
        )
    }

    if (state.showNewFolderDialog) {
        NewFolderDialog(state, onEvent)
    }

    state.folderPendingDeletion?.let { folder ->
        DeleteFolderDialog(folder.name, state.isDeletingFolder, onEvent)
    }
}

@Composable
private fun NewFolderDialog(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    val folderError = state.folderError?.resolve()
    AlertDialog(
        onDismissRequest = { onEvent(VaultViewModel.VaultEvent.OnDismissNewFolder) },
        title = { Text(stringResource(Res.string.ui_new_folder)) },
        text = {
            OutlinedTextField(
                value = state.newFolderName,
                onValueChange = {
                    onEvent(VaultViewModel.VaultEvent.OnNewFolderNameChanged(it))
                },
                label = { Text(stringResource(Res.string.ui_folder_name)) },
                singleLine = true,
                isError = folderError != null,
                supportingText = folderError?.let { error -> { Text(error) } },
            )
        },
        confirmButton = {
            FolderDialogButton(
                text = Res.string.action_create,
                isWorking = state.isCreatingFolder,
                enabled = state.canCreateFolder,
                onClick = { onEvent(VaultViewModel.VaultEvent.OnCreateFolderClick) },
            )
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(VaultViewModel.VaultEvent.OnDismissNewFolder) },
                enabled = !state.isCreatingFolder,
            ) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}

@Composable
private fun DeleteFolderDialog(
    folderName: String,
    isDeleting: Boolean,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(VaultViewModel.VaultEvent.OnDismissDeleteFolder) },
        icon = { Icon(Icons.Default.Delete, contentDescription = null) },
        title = { Text(stringResource(Res.string.ui_delete_folder_title, folderName)) },
        text = { Text(stringResource(Res.string.ui_delete_folder_message)) },
        confirmButton = {
            FolderDialogButton(
                text = Res.string.action_delete,
                isWorking = isDeleting,
                enabled = !isDeleting,
                onClick = { onEvent(VaultViewModel.VaultEvent.OnConfirmDeleteFolder) },
            )
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(VaultViewModel.VaultEvent.OnDismissDeleteFolder) },
                enabled = !isDeleting,
            ) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}

@Composable
private fun FolderDialogButton(
    text: org.jetbrains.compose.resources.StringResource,
    isWorking: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick, enabled = enabled) {
        if (isWorking) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Text(stringResource(text))
        }
    }
}

@Composable
private fun CompactCredentialList(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        CompactListControls(state, onEvent)
        CompactFolderFilters(state, onEvent)
        CredentialTagFilters(
            state = state,
            onEvent = onEvent,
            modifier = Modifier.padding(
                horizontal = ComponentSpacing.screenHorizontal,
                vertical = Spacing.sm,
            ),
        )
        CompactCredentialCollection(state, onEvent)
    }
}

@Composable
private fun CompactListControls(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    FilterChips(
        activeFilter = state.activeFilter,
        onFilterChanged = { onEvent(VaultViewModel.VaultEvent.OnFilterChanged(it)) },
        modifier = Modifier.padding(
            horizontal = ComponentSpacing.screenHorizontal,
            vertical = Spacing.sm,
        ),
    )
    SortSelector(
        sortOrder = state.sortOrder,
        onSortChanged = { onEvent(VaultViewModel.VaultEvent.OnSortChanged(it)) },
        modifier = Modifier.padding(horizontal = ComponentSpacing.screenHorizontal),
    )
}

@Composable
private fun CompactFolderFilters(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = ComponentSpacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        item {
            FolderFilterChip(
                label = stringResource(Res.string.ui_all_folders),
                selected = state.selectedFolderId == null,
                onClick = { onEvent(VaultViewModel.VaultEvent.OnFolderSelected(null)) },
            )
        }
        items(state.folders, key = { it.id.value }) { folder ->
            FolderFilterChip(
                label = folder.name,
                selected = state.selectedFolderId == folder.id,
                onClick = {
                    onEvent(VaultViewModel.VaultEvent.OnFolderSelected(folder.id.value))
                },
            )
        }
        val selectedFolderId = state.selectedFolderId
        if (selectedFolderId != null && state.folders.any { it.id == selectedFolderId }) {
            item(key = "delete-folder-${selectedFolderId.value}") {
                SelectedFolderDeleteChip {
                    onEvent(VaultViewModel.VaultEvent.OnDeleteFolderClick(selectedFolderId))
                }
            }
        }
        item {
            AssistChip(
                onClick = { onEvent(VaultViewModel.VaultEvent.OnNewFolderClick) },
                label = { Text(stringResource(Res.string.ui_new_folder)) },
            )
        }
    }
}

@Composable
private fun FolderFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun SelectedFolderDeleteChip(onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(stringResource(Res.string.ui_delete_folder)) },
        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
    )
}

@Composable
private fun CredentialTagFilters(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
    modifier: Modifier,
) {
    if (state.tags.isNotEmpty()) {
        TagCloud(
            tags = state.tags,
            selectedTagId = state.selectedTagId,
            onTagSelected = { onEvent(VaultViewModel.VaultEvent.OnTagSelected(it?.value)) },
            modifier = modifier,
        )
    }
}

@Composable
private fun CompactCredentialCollection(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    when {
        state.isLoading -> CredentialListLoading(Modifier.fillMaxSize())
        state.isEmpty -> EmptyState(
            hasCredentials = state.hasCredentials,
            onAddClick = { onEvent(VaultViewModel.VaultEvent.OnAddCredentialClick) },
            onClearFilters = { clearCredentialFilters(onEvent) },
            modifier = Modifier.fillMaxSize(),
        )
        else -> CompactCredentialRows(state, onEvent)
    }
}

@Composable
private fun CompactCredentialRows(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = ComponentSpacing.screenHorizontal,
            top = Spacing.smMd,
            end = ComponentSpacing.screenHorizontal,
            bottom = 112.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(ComponentSpacing.listItemSpacing),
    ) {
        items(items = state.filteredCredentials, key = { it.id.value }) { credential ->
            CredentialCard(
                credential = credential,
                onClick = {
                    onEvent(VaultViewModel.VaultEvent.OnCredentialClick(credential.id))
                },
                onFavoriteClick = {
                    onEvent(
                        VaultViewModel.VaultEvent.OnCredentialFavoriteClick(credential.id),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AdaptiveCredentialList(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxSize()) {
        FolderSidebar(
            folders = state.folders,
            selectedFolderId = state.selectedFolderId,
            onFolderSelected = {
                onEvent(VaultViewModel.VaultEvent.OnFolderSelected(it?.value))
            },
            onNewFolder = { onEvent(VaultViewModel.VaultEvent.OnNewFolderClick) },
            onDeleteFolder = {
                onEvent(VaultViewModel.VaultEvent.OnDeleteFolderClick(it))
            },
            modifier = Modifier
                .width(300.dp)
                .padding(
                    start = ComponentSpacing.screenHorizontal,
                    top = Spacing.sm,
                    bottom = Spacing.md,
                ),
        )
        AdaptiveCredentialContent(state, onEvent)
    }
}

@Composable
private fun AdaptiveCredentialContent(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FilterChips(
            activeFilter = state.activeFilter,
            onFilterChanged = { onEvent(VaultViewModel.VaultEvent.OnFilterChanged(it)) },
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.smMd),
        )
        SortSelector(
            sortOrder = state.sortOrder,
            onSortChanged = { onEvent(VaultViewModel.VaultEvent.OnSortChanged(it)) },
            modifier = Modifier.padding(horizontal = Spacing.lg),
        )
        CredentialTagFilters(
            state = state,
            onEvent = onEvent,
            modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        )
        AdaptiveCredentialCollection(state, onEvent)
    }
}

@Composable
private fun AdaptiveCredentialCollection(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    when {
        state.isLoading -> CredentialListLoading(Modifier.fillMaxSize())
        state.isEmpty -> EmptyState(
            hasCredentials = state.hasCredentials,
            onAddClick = { onEvent(VaultViewModel.VaultEvent.OnAddCredentialClick) },
            onClearFilters = { clearCredentialFilters(onEvent) },
            modifier = Modifier.fillMaxSize(),
        )
        else -> AdaptiveCredentialRows(state, onEvent)
    }
}

@Composable
private fun AdaptiveCredentialRows(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Spacing.lg,
            vertical = Spacing.smMd,
        ),
        verticalArrangement = Arrangement.spacedBy(ComponentSpacing.listItemSpacing),
    ) {
        items(items = state.filteredCredentials, key = { it.id.value }) { credential ->
            CredentialRow(
                credential = credential,
                onClick = {
                    onEvent(VaultViewModel.VaultEvent.OnCredentialClick(credential.id))
                },
                onFavoriteClick = {
                    onEvent(
                        VaultViewModel.VaultEvent.OnCredentialFavoriteClick(credential.id),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CredentialListLoading(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

private fun clearCredentialFilters(onEvent: (VaultViewModel.VaultEvent) -> Unit) {
    onEvent(VaultViewModel.VaultEvent.OnSearchDismiss)
    onEvent(
        VaultViewModel.VaultEvent.OnFilterChanged(VaultViewModel.CredentialFilter.ALL),
    )
    onEvent(VaultViewModel.VaultEvent.OnFolderSelected(null))
    onEvent(VaultViewModel.VaultEvent.OnTagSelected(null))
}

@Composable
private fun EmptyState(
    hasCredentials: Boolean,
    onAddClick: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(ComponentSpacing.screenHorizontal),
        contentAlignment = Alignment.Center,
    ) {
        EditorialPanel(
            modifier = Modifier.widthIn(max = 520.dp),
            contentPadding = PaddingValues(Spacing.xl),
        ) {
            EmptyStateIcon()
            EmptyStateCopy(hasCredentials)
            EmptyStateAction(hasCredentials, onAddClick, onClearFilters)
        }
    }
}

@Composable
private fun EmptyStateIcon() {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(72.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCopy(hasCredentials: Boolean) {
    Text(
        text = if (hasCredentials) {
            stringResource(Res.string.ui_no_matching_credentials)
        } else {
            stringResource(Res.string.empty_state_vault_title)
        },
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = if (hasCredentials) {
            stringResource(Res.string.ui_try_clearing_a_filter_or_changing_your_search)
        } else {
            stringResource(Res.string.ui_add_your_first_credential_to_get_started)
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun EmptyStateAction(
    hasCredentials: Boolean,
    onAddClick: () -> Unit,
    onClearFilters: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (hasCredentials) {
            OutlinedButton(
                onClick = onClearFilters,
                modifier = Modifier.heightIn(min = ComponentSpacing.touchTargetMin),
            ) {
                Text(stringResource(Res.string.ui_clear_filters))
            }
        } else {
            Button(
                onClick = onAddClick,
                modifier = Modifier.heightIn(min = ComponentSpacing.touchTargetMin),
            ) {
                Text(stringResource(Res.string.ui_add_credential))
            }
        }
    }
}

@Composable
private fun SortSelector(
    sortOrder: VaultViewModel.SortOrder,
    onSortChanged: (VaultViewModel.SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                when (sortOrder) {
                    VaultViewModel.SortOrder.NAME_ASC -> stringResource(Res.string.ui_name_a_z)
                    VaultViewModel.SortOrder.NAME_DESC -> stringResource(Res.string.ui_name_z_a)
                    VaultViewModel.SortOrder.LAST_USED ->
                        stringResource(Res.string.ui_recently_used)
                    VaultViewModel.SortOrder.CREATED -> stringResource(Res.string.ui_newest_first)
                },
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            VaultViewModel.SortOrder.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (option) {
                                VaultViewModel.SortOrder.NAME_ASC ->
                                    stringResource(Res.string.ui_name_a_z)
                                VaultViewModel.SortOrder.NAME_DESC ->
                                    stringResource(Res.string.ui_name_z_a)
                                VaultViewModel.SortOrder.LAST_USED ->
                                    stringResource(Res.string.ui_recently_used)
                                VaultViewModel.SortOrder.CREATED ->
                                    stringResource(Res.string.ui_newest_first)
                            },
                        )
                    },
                    onClick = {
                        expanded = false
                        onSortChanged(option)
                    },
                    trailingIcon = if (option == sortOrder) {
                        {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = stringResource(Res.string.ui_selected),
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}
