package com.passvault.feature.vault.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.passvault.core.domain.model.Folder
import com.passvault.core.domain.model.FolderId

@Composable
fun FolderSidebar(
    folders: List<Folder>,
    selectedFolderId: FolderId?,
    onFolderSelected: (FolderId?) -> Unit,
    onNewFolder: () -> Unit,
    onDeleteFolder: (FolderId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp),
        ) {
            FolderSidebarHeader()
            AllItemsOption(
                selected = selectedFolderId == null,
                onClick = { onFolderSelected(null) },
            )
            FolderDivider()
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(folders, key = { it.id.value }) { folder ->
                    FolderItem(
                        folder = folder,
                        isSelected = folder.id == selectedFolderId,
                        onClick = { onFolderSelected(folder.id) },
                        onDelete = { onDeleteFolder(folder.id) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            FolderDivider()
            NewFolderOption(onClick = onNewFolder)
        }
    }
}

@Composable
private fun FolderSidebarHeader() {
    Text(
        text = stringResource(Res.string.ui_folders),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

@Composable
private fun AllItemsOption(selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(stringResource(Res.string.ui_all_items)) },
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
            )
        },
        modifier = Modifier.padding(horizontal = 12.dp),
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
private fun FolderDivider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp))
}

@Composable
private fun NewFolderOption(onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(stringResource(Res.string.ui_new_folder)) },
        selected = false,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Default.CreateNewFolder,
                contentDescription = null,
            )
        },
        modifier = Modifier.padding(horizontal = 12.dp),
        shape = MaterialTheme.shapes.large,
    )
}

@Composable
private fun FolderItem(
    folder: Folder,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavigationDrawerItem(
            label = {
                Text(
                    text = folder.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            selected = isSelected,
            onClick = onClick,
            icon = {
                val folderIcon = folder.icon
                if (folderIcon.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                    )
                } else {
                    Text(
                        text = folderIcon,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            },
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.large,
        )
        if (isSelected) {
            IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = stringResource(Res.string.ui_delete_folder),
                )
            }
        }
    }
}
