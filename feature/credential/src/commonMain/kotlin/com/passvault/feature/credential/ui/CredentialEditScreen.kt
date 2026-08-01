@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.passvault.feature.credential.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.resolve
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.SecureTextField
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.components.EditorialStatusBanner
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.feature.credential.presentation.CredentialViewModel
import com.passvault.feature.credential.ui.components.CustomFieldsEditor
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CredentialEditScreen(
    viewModel: CredentialViewModel,
    credentialId: CredentialId?,
    onNavigateBack: () -> Unit,
    onSaveSuccess: (CredentialId?) -> Unit,
    onNavigateToGenerator: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(credentialId) {
        if (credentialId == null) {
            viewModel.createNewCredential(CredentialType.Login)
        } else {
            viewModel.loadCredential(credentialId)
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                CredentialViewModel.CredentialEffect.NavigateBack -> onNavigateBack()
                CredentialViewModel.CredentialEffect.NavigateToGenerator -> onNavigateToGenerator()
                is CredentialViewModel.CredentialEffect.SaveCompleted -> onSaveSuccess(effect.credentialId)
                else -> Unit
            }
        }
    }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = ComponentSpacing.formMaxWidth)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = ComponentSpacing.screenHorizontal,
                        end = ComponentSpacing.screenHorizontal,
                        bottom = ComponentSpacing.screenVertical,
                    ),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
            ) {
                TopAppBar(
                    title = {},
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCancelClick)
                            },
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.action_back),
                            )
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
                            },
                            enabled = state.canSave,
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp))
                            } else {
                                Text(stringResource(Res.string.action_save))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )

                EditorialPageHeader(
                    eyebrow = stringResource(Res.string.ui_encrypted_vault),
                    title = if (state.isNewCredential) {
                        stringResource(Res.string.ui_new_credential)
                    } else {
                        stringResource(Res.string.ui_edit_credential)
                    },
                )

                EditorialPanel(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(Spacing.lg),
                ) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = {
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged(it))
                    },
                    label = { Text(stringResource(Res.string.ui_title)) },
                    supportingText = state.titleError?.let { error -> { Text(error.resolve()) } },
                    isError = state.titleError != null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )

                OutlinedTextField(
                    value = state.username,
                    onValueChange = {
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnUsernameChanged(it))
                    },
                    label = { Text(stringResource(Res.string.ui_username)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )

                OutlinedTextField(
                    value = state.email,
                    onValueChange = {
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnEmailChanged(it))
                    },
                    label = { Text(stringResource(Res.string.ui_email_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )

                SecureTextField(
                    value = state.password,
                    onValueChange = {
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnPasswordChanged(it))
                    },
                    label = stringResource(Res.string.ui_password_optional),
                    modifier = Modifier.fillMaxWidth(),
                    onGenerateClick = {
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnGeneratePasswordClick)
                    },
                    supportingText = stringResource(Res.string.ui_keep_this_value_private_it_stays_encrypted_in_the_vaul),
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnSaveClick)
                        },
                    ),
                )
                if (state.password.isNotEmpty()) {
                    PasswordStrengthBar(state.passwordStrength)
                }

                UrlEditor(
                    urls = state.urls,
                    urlErrors = state.urlErrors,
                    onUrlChanged = { index, value ->
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnUrlChanged(index, value))
                    },
                    onUrlAdded = {
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnUrlAdded(""))
                    },
                    onUrlRemoved = { index ->
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnUrlRemoved(index))
                    },
                )

                OutlinedTextField(
                    value = state.notes,
                    onValueChange = {
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnNotesChanged(it))
                    },
                    label = { Text(stringResource(Res.string.ui_notes_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                )

                CustomFieldsEditor(
                    fields = state.customFields,
                    onAdd = { name, value, isSecret ->
                        viewModel.onEvent(
                            CredentialViewModel.CredentialEvent.OnCustomFieldAdded(
                                name,
                                value,
                                isSecret,
                            ),
                        )
                    },
                    onRemove = { fieldId ->
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCustomFieldRemoved(fieldId))
                    },
                    onUpdate = { fieldId, name, value, isSecret ->
                        viewModel.onEvent(
                            CredentialViewModel.CredentialEvent.OnCustomFieldUpdated(
                                fieldId,
                                name,
                                value,
                                isSecret,
                            ),
                        )
                    },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = if (state.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(stringResource(Res.string.action_favorite), modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.isFavorite,
                        onCheckedChange = {
                            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnFavoriteChanged(it))
                        },
                    )
                }

                if (state.attachments.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Text(
                                pluralStringResource(
                                    Res.plurals.ui_credential_attachment_linked_count,
                                    state.attachments.size,
                                    state.attachments.size,
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
                }

                state.errorMessage?.let { message ->
                    EditorialStatusBanner(
                        icon = Icons.Default.Error,
                        title = if (state.isNewCredential) {
                            stringResource(Res.string.ui_new_credential)
                        } else {
                            stringResource(Res.string.ui_edit_credential)
                        },
                        message = message.resolve(),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        action = {
                            TextButton(
                                onClick = {
                                    viewModel.onEvent(CredentialViewModel.CredentialEvent.OnDismissError)
                                },
                            ) {
                                Text(stringResource(Res.string.ui_dismiss))
                            }
                        },
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (state.showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(CredentialViewModel.CredentialEvent.OnDiscardCancel)
            },
            title = { Text(stringResource(Res.string.ui_discard_changes)) },
            text = { Text(stringResource(Res.string.ui_your_unsaved_changes_will_be_removed)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnDiscardConfirm)
                    },
                ) {
                    Text(stringResource(Res.string.ui_discard), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnDiscardCancel)
                    },
                ) {
                    Text(stringResource(Res.string.ui_keep_editing))
                }
            },
        )
    }
}

@Composable
private fun UrlEditor(
    urls: List<String>,
    urlErrors: Map<Int, UiText>,
    onUrlChanged: (Int, String) -> Unit,
    onUrlAdded: () -> Unit,
    onUrlRemoved: (Int) -> Unit,
) {
    val visibleUrls = if (urls.isEmpty()) listOf("") else urls
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(Res.string.ui_websites), style = MaterialTheme.typography.titleSmall)
        visibleUrls.forEachIndexed { index, url ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { onUrlChanged(index, it) },
                    label = { Text(if (index == 0) stringResource(Res.string.ui_website_url) else stringResource(Res.string.ui_additional_url)) },
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null)
                    },
                    supportingText = urlErrors[index]?.let { error -> { Text(error.resolve()) } },
                    isError = urlErrors[index] != null,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next,
                    ),
                )
                if (visibleUrls.size > 1) {
                    IconButton(onClick = { onUrlRemoved(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.ui_remove_url))
                    }
                }
            }
        }
        TextButton(onClick = onUrlAdded) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(Res.string.ui_add_another_website))
        }
    }
}

@Composable
private fun PasswordStrengthBar(
    strength: CredentialViewModel.PasswordStrength,
    modifier: Modifier = Modifier,
) {
    val (color, label) = when (strength) {
        CredentialViewModel.PasswordStrength.EMPTY -> MaterialTheme.colorScheme.outline to ""
        CredentialViewModel.PasswordStrength.TOO_SHORT -> MaterialTheme.colorScheme.error to stringResource(Res.string.ui_too_short)
        CredentialViewModel.PasswordStrength.VERY_WEAK -> MaterialTheme.colorScheme.error to stringResource(Res.string.password_strength_very_weak)
        CredentialViewModel.PasswordStrength.WEAK -> MaterialTheme.colorScheme.error to stringResource(Res.string.password_strength_weak)
        CredentialViewModel.PasswordStrength.FAIR -> MaterialTheme.colorScheme.tertiary to stringResource(Res.string.password_strength_fair)
        CredentialViewModel.PasswordStrength.GOOD -> MaterialTheme.colorScheme.primary to stringResource(Res.string.password_strength_good)
        CredentialViewModel.PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary to stringResource(Res.string.password_strength_strong)
        CredentialViewModel.PasswordStrength.VERY_STRONG -> MaterialTheme.colorScheme.primary to stringResource(Res.string.password_strength_very_strong)
    }
    val progress = when (strength) {
        CredentialViewModel.PasswordStrength.EMPTY -> 0f
        CredentialViewModel.PasswordStrength.TOO_SHORT -> 0.1f
        CredentialViewModel.PasswordStrength.VERY_WEAK -> 0.2f
        CredentialViewModel.PasswordStrength.WEAK -> 0.4f
        CredentialViewModel.PasswordStrength.FAIR -> 0.6f
        CredentialViewModel.PasswordStrength.GOOD -> 0.8f
        CredentialViewModel.PasswordStrength.STRONG -> 0.9f
        CredentialViewModel.PasswordStrength.VERY_STRONG -> 1f
    }
    Column(modifier = modifier.fillMaxWidth()) {
        androidx.compose.material3.LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = color,
        )
        if (label.isNotEmpty()) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = color,
            )
        }
    }
}
