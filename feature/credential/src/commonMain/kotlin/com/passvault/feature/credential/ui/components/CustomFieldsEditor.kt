package com.passvault.feature.credential.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.SecureTextField
import com.passvault.core.domain.model.CustomField
import com.passvault.core.domain.model.CustomFieldId
import com.passvault.core.domain.model.takeCodePoints

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
    var draft by remember(field.id) { mutableStateOf<CustomFieldDraft?>(null) }
    val currentDraft = draft
    if (currentDraft == null) {
        CustomFieldDisplayCard(
            field = field,
            onEdit = {
                draft = CustomFieldDraft(
                    name = field.name,
                    value = field.value.toStringUnsafe(),
                    isSecret = field.isSecret,
                )
            },
            onRemove = onRemove,
            modifier = modifier,
        )
    } else {
        CustomFieldEditCard(
            draft = currentDraft,
            onDraftChange = { draft = it },
            onCancel = { draft = null },
            onSave = {
                onUpdate(currentDraft.name, currentDraft.value, currentDraft.isSecret)
                draft = null
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun CustomFieldEditCard(
    draft: CustomFieldDraft,
    onDraftChange: (CustomFieldDraft) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            CustomFieldNameInput(draft, onDraftChange)
            Spacer(modifier = Modifier.height(8.dp))
            CustomFieldValueInput(draft, onDraftChange)
            Spacer(modifier = Modifier.height(8.dp))
            CustomFieldEditFooter(
                draft = draft,
                onDraftChange = onDraftChange,
                onCancel = onCancel,
                onSave = onSave,
            )
        }
    }
}

@Composable
private fun CustomFieldNameInput(
    draft: CustomFieldDraft,
    onDraftChange: (CustomFieldDraft) -> Unit,
) {
    OutlinedTextField(
        value = draft.name,
        onValueChange = {
            onDraftChange(draft.copy(name = it.takeCodePoints(MAX_CUSTOM_FIELD_NAME_LENGTH)))
        },
        label = { Text(stringResource(Res.string.ui_field_name)) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CustomFieldValueInput(
    draft: CustomFieldDraft,
    onDraftChange: (CustomFieldDraft) -> Unit,
) {
    val onValueChange: (String) -> Unit = {
        onDraftChange(draft.copy(value = it.takeCodePoints(MAX_CUSTOM_FIELD_VALUE_LENGTH)))
    }
    if (draft.isSecret) {
        SecureTextField(
            value = draft.value,
            onValueChange = onValueChange,
            label = stringResource(Res.string.ui_value),
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        OutlinedTextField(
            value = draft.value,
            onValueChange = onValueChange,
            label = { Text(stringResource(Res.string.ui_value)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CustomFieldEditFooter(
    draft: CustomFieldDraft,
    onDraftChange: (CustomFieldDraft) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .toggleable(
                    value = draft.isSecret,
                    role = Role.Checkbox,
                    onValueChange = { onDraftChange(draft.copy(isSecret = it)) },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = draft.isSecret, onCheckedChange = null)
            Text(stringResource(Res.string.ui_secret_field))
        }
        Row {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.action_cancel),
                )
            }
            IconButton(onClick = onSave, enabled = draft.name.isNotBlank()) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(Res.string.action_save),
                )
            }
        }
    }
}

@Composable
private fun CustomFieldDisplayCard(
    field: CustomField,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier,
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            CustomFieldDisplayValue(field, Modifier.weight(1f))
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(Res.string.action_edit),
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.ui_remove),
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomFieldDisplayValue(field: CustomField, modifier: Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = field.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (field.isSecret) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(Res.string.ui_secret),
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        val displayValue = if (field.isSecret) {
            "\u2022".repeat(HIDDEN_VALUE_BULLET_COUNT)
        } else {
            field.value.toStringUnsafe()
        }
        val hiddenDescription = stringResource(Res.string.secure_field_password_hidden)
        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodyMedium,
            modifier = if (field.isSecret) {
                Modifier.clearAndSetSemantics { contentDescription = hiddenDescription }
            } else {
                Modifier
            },
        )
    }
}

private data class CustomFieldDraft(
    val name: String,
    val value: String,
    val isSecret: Boolean,
)

@Composable
private fun AddCustomFieldDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, value: String, isSecret: Boolean) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var isSecret by remember { mutableStateOf(false) }
    val dismiss = {
        name = ""
        value = ""
        isSecret = false
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = dismiss,
        title = { Text(stringResource(Res.string.ui_add_custom_field)) },
        text = {
            AddCustomFieldFields(
                name = name,
                value = value,
                isSecret = isSecret,
                onNameChange = { name = it.takeCodePoints(MAX_CUSTOM_FIELD_NAME_LENGTH) },
                onValueChange = { value = it.takeCodePoints(MAX_CUSTOM_FIELD_VALUE_LENGTH) },
                onSecretChange = { isSecret = it },
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(name, value, isSecret)
                    name = ""
                    value = ""
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(Res.string.action_add))
            }
        },
        dismissButton = {
            TextButton(onClick = dismiss) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}

@Composable
private fun AddCustomFieldFields(
    name: String,
    value: String,
    isSecret: Boolean,
    onNameChange: (String) -> Unit,
    onValueChange: (String) -> Unit,
    onSecretChange: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(Res.string.ui_field_name)) },
            placeholder = { Text(stringResource(Res.string.ui_e_g_account_number)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (isSecret) {
            SecureTextField(
                value = value,
                onValueChange = onValueChange,
                label = stringResource(Res.string.ui_value),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(stringResource(Res.string.ui_value)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.toggleable(
                value = isSecret,
                role = Role.Checkbox,
                onValueChange = onSecretChange,
            ),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Checkbox(checked = isSecret, onCheckedChange = null)
            Text(stringResource(Res.string.ui_secret_field_masked))
        }
    }
}

private const val MAX_CUSTOM_FIELD_NAME_LENGTH = 200
private const val MAX_CUSTOM_FIELD_VALUE_LENGTH = 20_000
private const val HIDDEN_VALUE_BULLET_COUNT = 12
