package com.passvault.feature.vault.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialSummary
import com.passvault.core.domain.model.PasswordScore
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
        AlertDialog(
            onDismissRequest = {
                onEvent(VaultViewModel.VaultEvent.OnDismissNewFolder)
            },
            title = { Text(stringResource(Res.string.ui_new_folder)) },
            text = {
                OutlinedTextField(
                    value = state.newFolderName,
                    onValueChange = {
                        onEvent(
                            VaultViewModel.VaultEvent.OnNewFolderNameChanged(
                                it.take(200),
                            ),
                        )
                    },
                    label = { Text(stringResource(Res.string.ui_folder_name)) },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(VaultViewModel.VaultEvent.OnCreateFolderClick)
                    },
                    enabled = state.newFolderName.trim().isNotEmpty() && !state.isCreatingFolder,
                ) {
                    if (state.isCreatingFolder) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(Res.string.action_create))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onEvent(VaultViewModel.VaultEvent.OnDismissNewFolder)
                    },
                    enabled = !state.isCreatingFolder,
                ) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }

    state.folderPendingDeletion?.let { folder ->
        AlertDialog(
            onDismissRequest = {
                onEvent(VaultViewModel.VaultEvent.OnDismissDeleteFolder)
            },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = {
                Text(stringResource(Res.string.ui_delete_folder_title, folder.name))
            },
            text = {
                Text(stringResource(Res.string.ui_delete_folder_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(VaultViewModel.VaultEvent.OnConfirmDeleteFolder)
                    },
                    enabled = !state.isDeletingFolder,
                ) {
                    if (state.isDeletingFolder) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(Res.string.action_delete))
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onEvent(VaultViewModel.VaultEvent.OnDismissDeleteFolder)
                    },
                    enabled = !state.isDeletingFolder,
                ) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun CompactCredentialList(
    state: VaultViewModel.VaultState,
    onEvent: (VaultViewModel.VaultEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Filter chips
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

        LazyRow(
            contentPadding = PaddingValues(horizontal = ComponentSpacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                FilterChip(
                    selected = state.selectedFolderId == null,
                    onClick = {
                        onEvent(VaultViewModel.VaultEvent.OnFolderSelected(null))
                    },
                    label = { Text(stringResource(Res.string.ui_all_folders)) },
                )
            }
            items(state.folders, key = { it.id.value }) { folder ->
                FilterChip(
                    selected = state.selectedFolderId == folder.id,
                    onClick = {
                        onEvent(VaultViewModel.VaultEvent.OnFolderSelected(folder.id.value))
                    },
                    label = { Text(folder.name) },
                )
            }
            state.selectedFolderId?.let { selectedFolderId ->
                if (state.folders.any { it.id == selectedFolderId }) {
                    item(key = "delete-folder-${selectedFolderId.value}") {
                        AssistChip(
                            onClick = {
                                onEvent(VaultViewModel.VaultEvent.OnDeleteFolderClick(selectedFolderId))
                            },
                            label = { Text(stringResource(Res.string.ui_delete_folder)) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            },
                        )
                    }
                }
            }
            item {
                AssistChip(
                    onClick = {
                        onEvent(VaultViewModel.VaultEvent.OnNewFolderClick)
                    },
                    label = { Text(stringResource(Res.string.ui_new_folder)) },
                )
            }
        }

        // Tag cloud
        if (state.tags.isNotEmpty()) {
            TagCloud(
                tags = state.tags,
                selectedTagId = state.selectedTagId,
                onTagSelected = {
                    onEvent(VaultViewModel.VaultEvent.OnTagSelected(it?.value))
                },
                modifier = Modifier.padding(
                    horizontal = ComponentSpacing.screenHorizontal,
                    vertical = Spacing.sm,
                ),
            )
        }

        // Credential list
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.isEmpty -> {
                EmptyState(
                    hasCredentials = state.hasCredentials,
                    onAddClick = { onEvent(VaultViewModel.VaultEvent.OnAddCredentialClick) },
                    onClearFilters = {
                        onEvent(VaultViewModel.VaultEvent.OnSearchDismiss)
                        onEvent(VaultViewModel.VaultEvent.OnFilterChanged(VaultViewModel.CredentialFilter.ALL))
                        onEvent(VaultViewModel.VaultEvent.OnFolderSelected(null))
                        onEvent(VaultViewModel.VaultEvent.OnTagSelected(null))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
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
                    items(
                        items = state.filteredCredentials,
                        key = { it.id.value }
                    ) { credential ->
                        CredentialCard(
                            credential = credential,
                            onClick = {
                                onEvent(VaultViewModel.VaultEvent.OnCredentialClick(credential.id))
                            },
                            onFavoriteClick = {
                                onEvent(VaultViewModel.VaultEvent.OnCredentialFavoriteClick(credential.id))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
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
        // Sidebar with folders
        FolderSidebar(
            folders = state.folders,
            selectedFolderId = state.selectedFolderId,
            onFolderSelected = {
                onEvent(VaultViewModel.VaultEvent.OnFolderSelected(it?.value))
            },
            onNewFolder = {
                onEvent(VaultViewModel.VaultEvent.OnNewFolderClick)
            },
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

        // Main content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Filter and tags
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

            if (state.tags.isNotEmpty()) {
                TagCloud(
                    tags = state.tags,
                    selectedTagId = state.selectedTagId,
                    onTagSelected = {
                        onEvent(VaultViewModel.VaultEvent.OnTagSelected(it?.value))
                    },
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                )
            }

            // Credential list
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.isEmpty -> {
                    EmptyState(
                        hasCredentials = state.hasCredentials,
                        onAddClick = { onEvent(VaultViewModel.VaultEvent.OnAddCredentialClick) },
                        onClearFilters = {
                            onEvent(VaultViewModel.VaultEvent.OnSearchDismiss)
                            onEvent(VaultViewModel.VaultEvent.OnFilterChanged(VaultViewModel.CredentialFilter.ALL))
                            onEvent(VaultViewModel.VaultEvent.OnFolderSelected(null))
                            onEvent(VaultViewModel.VaultEvent.OnTagSelected(null))
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.smMd),
                        verticalArrangement = Arrangement.spacedBy(ComponentSpacing.listItemSpacing),
                    ) {
                        items(
                            items = state.filteredCredentials,
                            key = { it.id.value }
                        ) { credential ->
                            CredentialRow(
                                credential = credential,
                                onClick = {
                                    onEvent(VaultViewModel.VaultEvent.OnCredentialClick(credential.id))
                                },
                                onFavoriteClick = {
                                    onEvent(VaultViewModel.VaultEvent.OnCredentialFavoriteClick(credential.id))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
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
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
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

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
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
                    VaultViewModel.SortOrder.LAST_USED -> stringResource(Res.string.ui_recently_used)
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
                                VaultViewModel.SortOrder.NAME_ASC -> stringResource(Res.string.ui_name_a_z)
                                VaultViewModel.SortOrder.NAME_DESC -> stringResource(Res.string.ui_name_z_a)
                                VaultViewModel.SortOrder.LAST_USED -> stringResource(Res.string.ui_recently_used)
                                VaultViewModel.SortOrder.CREATED -> stringResource(Res.string.ui_newest_first)
                            },
                        )
                    },
                    onClick = {
                        expanded = false
                        onSortChanged(option)
                    },
                    trailingIcon = if (option == sortOrder) {
                        { Icon(Icons.Default.Check, contentDescription = stringResource(Res.string.ui_selected)) }
                    } else {
                        null
                    },
                )
            }
        }
    }
}
