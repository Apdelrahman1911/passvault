package com.passvault.feature.credential.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId

@Composable
fun CustomFieldsEditor(
    fields: List<CustomField>,
    onAdd: (name: String, value: String, isSecret: Boolean) -> Unit,
    onRemove: (CustomFieldId) -> Unit,
    onUpdate: (CustomFieldId, name: String, value: String, isSecret: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(Res.string.ui_custom_fields),
                style = MaterialTheme.typography.titleSmall
            )

            TextButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(Res.string.action_add))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        fields.forEach { field ->
            CustomFieldItem(
                field = field,
                onUpdate = { name, value, isSecret ->
                    onUpdate(field.id, name, value, isSecret)
                },
                onRemove = { onRemove(field.id) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showAddDialog) {
        AddCustomFieldDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, value, isSecret ->
                onAdd(name, value, isSecret)
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomFieldItem(
    field: CustomField,
    onUpdate: (name: String, value: String, isSecret: Boolean) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember(field.id) { mutableStateOf(field.name) }
    var value by remember(field.id) { mutableStateOf(field.value.toStringUnsafe()) }
    var isSecret by remember(field.id) { mutableStateOf(field.isSecret) }

    if (isEditing) {
        Card(modifier = modifier) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.ui_field_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(stringResource(Res.string.ui_value)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Checkbox(
                            checked = isSecret,
                            onCheckedChange = { isSecret = it }
                        )
                        Text(stringResource(Res.string.ui_secret_field))
                    }

                    Row {
                        IconButton(onClick = { isEditing = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(Res.string.action_cancel)
                            )
                        }
                        IconButton(
                            onClick = {
                                onUpdate(name, value, isSecret)
                                isEditing = false
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(Res.string.action_save)
                            )
                        }
                    }
                }
            }
        }
    } else {
        Card(
            modifier = modifier,
            onClick = { isEditing = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            text = field.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (field.isSecret) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(Res.string.ui_secret),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    val displayValue = if (field.isSecret) {
                        "\u2022".repeat(field.value.length.coerceAtMost(20))
                    } else {
                        field.value.toStringUnsafe()
                    }

                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.action_edit)
                        )
                    }
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.ui_remove)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCustomFieldDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, value: String, isSecret: Boolean) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var isSecret by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.ui_add_custom_field)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.ui_field_name)) },
                    placeholder = { Text(stringResource(Res.string.ui_e_g_account_number)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(stringResource(Res.string.ui_value)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(
                        checked = isSecret,
                        onCheckedChange = { isSecret = it }
                    )
                    Text(stringResource(Res.string.ui_secret_field_masked))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, value, isSecret) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(Res.string.action_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}
