@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.passvault.feature.credential.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.UiText
import com.passvault.core.designsystem.text.resolve
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
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.SecureTextField
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialPanel
import com.passvault.core.designsystem.components.EditorialStatusBanner
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.CredentialType
import com.passvault.feature.credential.presentation.CredentialViewModel
import com.passvault.feature.credential.ui.components.CredentialAttachmentSection
import com.passvault.feature.credential.ui.components.CustomFieldsEditor
import com.passvault.feature.credential.ui.components.FolderSelector
import com.passvault.feature.credential.ui.components.TotpEnrollmentSection
import kotlinx.coroutines.flow.collect

@Composable
fun CredentialEditScreen(
    viewModel: CredentialViewModel,
    credentialId: CredentialId?,
    onNavigateBack: () -> Unit,
    onSaveSuccess: (CredentialId?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    CredentialEditEffects(
        viewModel = viewModel,
        credentialId = credentialId,
        onNavigateBack = onNavigateBack,
        onSaveSuccess = onSaveSuccess,
    )

    if (state.isLoading) {
        CredentialEditLoading(modifier)
    } else {
        CredentialEditContent(
            state = state,
            onEvent = viewModel::onEvent,
            modifier = modifier,
        )
    }

    if (state.showDiscardConfirmation) {
        DiscardChangesDialog(onEvent = viewModel::onEvent)
    }
}

@Composable
private fun CredentialEditEffects(
    viewModel: CredentialViewModel,
    credentialId: CredentialId?,
    onNavigateBack: () -> Unit,
    onSaveSuccess: (CredentialId?) -> Unit,
) {
    LaunchedEffect(credentialId) {
        if (credentialId == null) {
            viewModel.createNewCredential(CredentialType.Login)
        } else {
            viewModel.loadCredential(credentialId)
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CredentialViewModel.CredentialEffect.NavigateBack -> onNavigateBack()
                is CredentialViewModel.CredentialEffect.SaveCompleted -> {
                    onSaveSuccess(effect.credentialId)
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun CredentialEditLoading(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CredentialEditContent(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
    modifier: Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = ComponentSpacing.formMaxWidth)
                    .fillMaxWidth()
                    .scaffoldVerticalScroll(rememberScrollState(), padding)
                    .padding(
                        start = ComponentSpacing.screenHorizontal,
                        end = ComponentSpacing.screenHorizontal,
                        bottom = ComponentSpacing.screenVertical,
                    ),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
            ) {
                CredentialEditTopBar(state, onEvent)
                CredentialEditHeader(state)
                CredentialFormPanel(state, onEvent)
                CredentialEditError(state, onEvent)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CredentialEditTopBar(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    TopAppBar(
        title = {},
        windowInsets = WindowInsets(0, 0, 0, 0),
        navigationIcon = {
            IconButton(onClick = { onEvent(CredentialViewModel.CredentialEvent.OnCancelClick) }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.action_back),
                )
            }
        },
        actions = {
            TextButton(
                onClick = { onEvent(CredentialViewModel.CredentialEvent.OnSaveClick) },
                enabled = state.canSave,
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp))
                } else {
                    Text(stringResource(Res.string.action_save))
                }
            }
        },
        colors = passVaultTopAppBarColors(),
    )
}

@Composable
private fun CredentialEditHeader(state: CredentialViewModel.CredentialState) {
    EditorialPageHeader(
        eyebrow = stringResource(Res.string.ui_encrypted_vault),
        title = if (state.isNewCredential) {
            stringResource(Res.string.ui_new_credential)
        } else {
            stringResource(Res.string.ui_edit_credential)
        },
    )
}

@Composable
private fun CredentialFormPanel(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    EditorialPanel(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Spacing.lg),
    ) {
        CredentialTitleAndFolder(state, onEvent)
        CredentialAccountFields(state, onEvent)
        CredentialPasswordEditor(state, onEvent)
        if (state.credentialType == CredentialType.Login) {
            TotpEnrollmentSection(state = state, onEvent = onEvent)
        }
        CredentialUrlEditor(state, onEvent)
        CredentialNotesEditor(state, onEvent)
        CredentialCustomFieldsEditor(state, onEvent)
        FavoriteToggle(state.isFavorite, onEvent)
        CredentialAttachmentSection(state = state, onEvent = onEvent)
    }
}

@Composable
private fun CredentialTitleAndFolder(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    OutlinedTextField(
        value = state.title,
        onValueChange = { onEvent(CredentialViewModel.CredentialEvent.OnTitleChanged(it)) },
        label = { Text(stringResource(Res.string.ui_title)) },
        supportingText = state.titleError?.let { error -> { Text(error.resolve()) } },
        isError = state.titleError != null,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
    FolderSelector(
        folders = state.folders,
        selectedFolderId = state.folderId,
        isLoading = state.isLoadingFolders,
        loadFailed = state.folderLoadFailed,
        onFolderSelected = { folderId ->
            onEvent(CredentialViewModel.CredentialEvent.OnFolderChanged(folderId?.value))
        },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun CredentialAccountFields(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    OutlinedTextField(
        value = state.username,
        onValueChange = { onEvent(CredentialViewModel.CredentialEvent.OnUsernameChanged(it)) },
        label = { Text(stringResource(Res.string.ui_username)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    )
    OutlinedTextField(
        value = state.email,
        onValueChange = { onEvent(CredentialViewModel.CredentialEvent.OnEmailChanged(it)) },
        label = { Text(stringResource(Res.string.ui_email_optional)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
        ),
    )
}

@Composable
private fun CredentialPasswordEditor(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    SecureTextField(
        value = state.password,
        onValueChange = { onEvent(CredentialViewModel.CredentialEvent.OnPasswordChanged(it)) },
        label = stringResource(Res.string.ui_password_optional),
        modifier = Modifier.fillMaxWidth(),
        onGenerateClick = {
            onEvent(CredentialViewModel.CredentialEvent.OnGeneratePasswordClick)
        },
        supportingText = stringResource(
            Res.string.ui_keep_this_value_private_it_stays_encrypted_in_the_vaul,
        ),
        imeAction = ImeAction.Done,
        keyboardActions = KeyboardActions(
            onDone = { onEvent(CredentialViewModel.CredentialEvent.OnSaveClick) },
        ),
    )
    if (state.password.isNotEmpty()) {
        PasswordStrengthBar(state.passwordStrength)
    }
}

@Composable
private fun CredentialUrlEditor(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    UrlEditor(
        urls = state.urls,
        urlErrors = state.urlErrors,
        onUrlChanged = { index, value ->
            onEvent(CredentialViewModel.CredentialEvent.OnUrlChanged(index, value))
        },
        onUrlAdded = { onEvent(CredentialViewModel.CredentialEvent.OnUrlAdded("")) },
        onUrlRemoved = { index ->
            onEvent(CredentialViewModel.CredentialEvent.OnUrlRemoved(index))
        },
    )
}

@Composable
private fun CredentialNotesEditor(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    OutlinedTextField(
        value = state.notes,
        onValueChange = { onEvent(CredentialViewModel.CredentialEvent.OnNotesChanged(it)) },
        label = { Text(stringResource(Res.string.ui_notes_optional)) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 4,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
    )
}

@Composable
private fun CredentialCustomFieldsEditor(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    CustomFieldsEditor(
        fields = state.customFields,
        onAdd = { name, value, isSecret ->
            onEvent(
                CredentialViewModel.CredentialEvent.OnCustomFieldAdded(name, value, isSecret),
            )
        },
        onRemove = { fieldId ->
            onEvent(CredentialViewModel.CredentialEvent.OnCustomFieldRemoved(fieldId))
        },
        onUpdate = { fieldId, name, value, isSecret ->
            onEvent(
                CredentialViewModel.CredentialEvent.OnCustomFieldUpdated(
                    fieldId,
                    name,
                    value,
                    isSecret,
                ),
            )
        },
    )
}

@Composable
private fun FavoriteToggle(
    isFavorite: Boolean,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = isFavorite,
                role = Role.Switch,
                onValueChange = {
                    onEvent(CredentialViewModel.CredentialEvent.OnFavoriteChanged(it))
                },
            )
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = if (isFavorite) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(stringResource(Res.string.action_favorite), modifier = Modifier.weight(1f))
        Switch(checked = isFavorite, onCheckedChange = null)
    }
}

@Composable
private fun CredentialEditError(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    val message = state.errorMessage ?: return
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
                onClick = { onEvent(CredentialViewModel.CredentialEvent.OnDismissError) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Text(stringResource(Res.string.ui_dismiss))
            }
        },
    )
}

@Composable
private fun DiscardChangesDialog(
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(CredentialViewModel.CredentialEvent.OnDiscardCancel) },
        title = { Text(stringResource(Res.string.ui_discard_changes)) },
        text = { Text(stringResource(Res.string.ui_your_unsaved_changes_will_be_removed)) },
        confirmButton = {
            TextButton(
                onClick = { onEvent(CredentialViewModel.CredentialEvent.OnDiscardConfirm) },
            ) {
                Text(
                    text = stringResource(Res.string.ui_discard),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onEvent(CredentialViewModel.CredentialEvent.OnDiscardCancel) },
            ) {
                Text(stringResource(Res.string.ui_keep_editing))
            }
        },
    )
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
                    label = {
                        Text(
                            if (index == 0) {
                                stringResource(Res.string.ui_website_url)
                            } else {
                                stringResource(Res.string.ui_additional_url)
                            },
                        )
                    },
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
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(Res.string.ui_remove_url),
                        )
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
    val color = passwordStrengthColor(strength)
    val label = passwordStrengthLabel(strength)
    Column(modifier = modifier.fillMaxWidth()) {
        androidx.compose.material3.LinearProgressIndicator(
            progress = { passwordStrengthProgress(strength) },
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

@Composable
private fun passwordStrengthColor(strength: CredentialViewModel.PasswordStrength): Color =
    when (strength) {
        CredentialViewModel.PasswordStrength.EMPTY -> MaterialTheme.colorScheme.outline
        CredentialViewModel.PasswordStrength.TOO_SHORT,
        CredentialViewModel.PasswordStrength.VERY_WEAK,
        CredentialViewModel.PasswordStrength.WEAK,
        -> MaterialTheme.colorScheme.error
        CredentialViewModel.PasswordStrength.FAIR -> MaterialTheme.colorScheme.tertiary
        CredentialViewModel.PasswordStrength.GOOD,
        CredentialViewModel.PasswordStrength.STRONG,
        CredentialViewModel.PasswordStrength.VERY_STRONG,
        -> MaterialTheme.colorScheme.primary
    }

@Composable
private fun passwordStrengthLabel(strength: CredentialViewModel.PasswordStrength): String =
    when (strength) {
        CredentialViewModel.PasswordStrength.EMPTY -> ""
        CredentialViewModel.PasswordStrength.TOO_SHORT -> stringResource(Res.string.ui_too_short)
        CredentialViewModel.PasswordStrength.VERY_WEAK ->
            stringResource(Res.string.password_strength_very_weak)
        CredentialViewModel.PasswordStrength.WEAK ->
            stringResource(Res.string.password_strength_weak)
        CredentialViewModel.PasswordStrength.FAIR ->
            stringResource(Res.string.password_strength_fair)
        CredentialViewModel.PasswordStrength.GOOD ->
            stringResource(Res.string.password_strength_good)
        CredentialViewModel.PasswordStrength.STRONG ->
            stringResource(Res.string.password_strength_strong)
        CredentialViewModel.PasswordStrength.VERY_STRONG ->
            stringResource(Res.string.password_strength_very_strong)
    }

private fun passwordStrengthProgress(strength: CredentialViewModel.PasswordStrength): Float =
    when (strength) {
        CredentialViewModel.PasswordStrength.EMPTY -> 0f
        CredentialViewModel.PasswordStrength.TOO_SHORT -> 0.1f
        CredentialViewModel.PasswordStrength.VERY_WEAK -> 0.2f
        CredentialViewModel.PasswordStrength.WEAK -> 0.4f
        CredentialViewModel.PasswordStrength.FAIR -> 0.6f
        CredentialViewModel.PasswordStrength.GOOD -> 0.8f
        CredentialViewModel.PasswordStrength.STRONG -> 0.9f
        CredentialViewModel.PasswordStrength.VERY_STRONG -> 1f
    }
