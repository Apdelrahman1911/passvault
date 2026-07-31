package com.passvault.feature.vault.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = stringResource(Res.string.ui_folders),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            // All items option
            NavigationDrawerItem(
                label = { Text(stringResource(Res.string.ui_all_items)) },
                selected = selectedFolderId == null,
                onClick = { onFolderSelected(null) },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null
                    )
                },
                modifier = Modifier.padding(horizontal = 12.dp),
                shape = MaterialTheme.shapes.large,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp)
            )

            // Folder list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(folders, key = { it.id.value }) { folder ->
                    FolderItem(
                        folder = folder,
                        isSelected = folder.id == selectedFolderId,
                        onClick = { onFolderSelected(folder.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 20.dp)
            )

            // Add folder button
            NavigationDrawerItem(
                label = { Text(stringResource(Res.string.ui_new_folder)) },
                selected = false,
                onClick = onNewFolder,
                icon = {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = null
                    )
                },
                modifier = Modifier.padding(horizontal = 12.dp),
                shape = MaterialTheme.shapes.large,
            )
        }
    }
}

@Composable
private fun FolderItem(
    folder: Folder,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        modifier = modifier.padding(horizontal = 12.dp),
        shape = MaterialTheme.shapes.large,
    )
}
