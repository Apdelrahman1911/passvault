@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.passvault.feature.credential.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.domain.model.Folder
import com.passvault.core.domain.model.FolderId
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FolderSelector(
    folders: List<Folder>,
    selectedFolderId: FolderId?,
    isLoading: Boolean,
    loadFailed: Boolean,
    onFolderSelected: (FolderId?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedFolder = folders.firstOrNull { it.id == selectedFolderId }
    val selectedLabel = selectedFolderLabel(
        selectedFolder = selectedFolder,
        selectedFolderId = selectedFolderId,
        isLoading = isLoading,
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (!isLoading && !loadFailed) expanded = it
        },
        modifier = modifier,
    ) {
        FolderSelectorField(
            selectedLabel = selectedLabel,
            expanded = expanded,
            enabled = !isLoading && !loadFailed,
            loadFailed = loadFailed,
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = !isLoading && !loadFailed,
                )
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            FolderSelectorOptions(
                folders = folders,
                selectedFolderId = selectedFolderId,
                onFolderSelected = { folderId ->
                    expanded = false
                    onFolderSelected(folderId)
                },
            )
        }
    }
}

@Composable
private fun selectedFolderLabel(
    selectedFolder: Folder?,
    selectedFolderId: FolderId?,
    isLoading: Boolean,
): String = when {
    isLoading && selectedFolderId != null -> stringResource(Res.string.ui_loading_folders)
    selectedFolderId == null -> stringResource(Res.string.ui_no_folder)
    selectedFolder != null -> selectedFolder.name
    else -> stringResource(Res.string.ui_folder_unavailable)
}

@Composable
private fun FolderSelectorField(
    selectedLabel: String,
    expanded: Boolean,
    enabled: Boolean,
    loadFailed: Boolean,
    modifier: Modifier,
) {
    OutlinedTextField(
        value = selectedLabel,
        onValueChange = {},
        modifier = modifier,
        readOnly = true,
        enabled = enabled,
        isError = loadFailed,
        label = { Text(stringResource(Res.string.ui_folder)) },
        leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) },
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        supportingText = if (loadFailed) {
            { Text(stringResource(Res.string.error_folder_load)) }
        } else {
            null
        },
    )
}

@Composable
private fun FolderSelectorOptions(
    folders: List<Folder>,
    selectedFolderId: FolderId?,
    onFolderSelected: (FolderId?) -> Unit,
) {
    FolderOption(
        label = stringResource(Res.string.ui_no_folder),
        selected = selectedFolderId == null,
        onClick = { onFolderSelected(null) },
    )
    folders.forEach { folder ->
        FolderOption(
            label = folder.name,
            selected = selectedFolderId == folder.id,
            onClick = { onFolderSelected(folder.id) },
        )
    }
}

@Composable
private fun FolderOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = onClick,
        trailingIcon = if (selected) {
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
