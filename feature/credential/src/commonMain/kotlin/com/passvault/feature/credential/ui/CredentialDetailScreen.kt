@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.passvault.feature.credential.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialStatusBanner
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.codePointLength
import com.passvault.feature.credential.presentation.CredentialViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CredentialDetailScreen(
    viewModel: CredentialViewModel,
    credentialId: CredentialId,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (CredentialId) -> Unit,
    onCopyToClipboard: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    LaunchedEffect(credentialId) {
        viewModel.loadCredential(credentialId)
    }
    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                CredentialViewModel.CredentialEffect.NavigateBack -> onNavigateBack()
                is CredentialViewModel.CredentialEffect.CopyToClipboard ->
                    onCopyToClipboard(effect.text)
                is CredentialViewModel.CredentialEffect.LaunchUrl -> {
                    val opened = try {
                        uriHandler.openUri(effect.url)
                        true
                    } catch (_: Exception) {
                        false
                    }
                    viewModel.onEvent(CredentialViewModel.CredentialEvent.OnUrlLaunchResult(opened))
                }
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
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = ComponentSpacing.formMaxWidth)
                    .scaffoldVerticalScroll(rememberScrollState(), padding)
                    .padding(
                        start = ComponentSpacing.screenHorizontal,
                        end = ComponentSpacing.screenHorizontal,
                        bottom = ComponentSpacing.screenVertical,
                    ),
                verticalArrangement = Arrangement.spacedBy(Spacing.smMd),
            ) {
                TopAppBar(
                    title = {},
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.action_back),
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { onNavigateToEdit(credentialId) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = stringResource(Res.string.ui_edit_credential),
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.onEvent(CredentialViewModel.CredentialEvent.OnDeleteClick)
                            },
                            enabled = !state.isDeleting,
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.ui_delete_credential),
                            )
                        }
                    },
                    colors = passVaultTopAppBarColors(),
                )

                EditorialPageHeader(
                    eyebrow = stringResource(Res.string.ui_encrypted_vault),
                    title = state.displayTitle.resolve(),
                )

                state.errorMessage?.let { message ->
                    EditorialStatusBanner(
                        icon = Icons.Default.Error,
                        title = state.displayTitle.resolve(),
                        message = message.resolve(),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }

                CredentialField(stringResource(Res.string.ui_title), state.title, onCopy = null, isPassword = false)
                if (state.username.isNotBlank()) {
                    CredentialField(
                        stringResource(Res.string.ui_username),
                        state.username,
                        onCopy = {
                            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyUsernameClick)
                        },
                        isPassword = false,
                    )
                }
                if (state.email.isNotBlank()) {
                    CredentialField(
                        stringResource(Res.string.ui_email),
                        state.email,
                        onCopy = {
                            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyEmailClick)
                        },
                        isPassword = false,
                    )
                }
                if (state.password.isNotBlank()) {
                    CredentialField(
                        stringResource(Res.string.ui_password),
                        state.password,
                        onCopy = {
                            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyPasswordClick)
                        },
                        isPassword = true,
                    )
                }
                if (state.totpConfiguration != null) {
                    TotpCodeCard(
                        state = state,
                        onCopy = {
                            viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyTotpClick)
                        },
                    )
                }

                state.urls.forEach { url ->
                    CredentialField(
                        label = stringResource(Res.string.ui_website),
                        value = url,
                        onCopy = null,
                        isPassword = false,
                        action = {
                            TextButton(
                                onClick = {
                                    viewModel.onEvent(
                                        CredentialViewModel.CredentialEvent.OnLaunchUrlClick(url),
                                    )
                                },
                            ) {
                                Text(stringResource(Res.string.ui_open))
                                Icon(
                                    Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        },
                    )
                }

                if (state.notes.isNotBlank()) {
                    CredentialField(stringResource(Res.string.ui_notes), state.notes, onCopy = null, isPassword = false)
                }

                state.customFields.forEach { field ->
                    CredentialField(
                        label = field.name,
                        value = field.value.toStringUnsafe(),
                        onCopy = {
                            viewModel.onEvent(
                                CredentialViewModel.CredentialEvent.OnCopyCustomFieldClick(field.id),
                            )
                        },
                        isPassword = field.isSecret,
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.large,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                if (state.isFavorite) {
                                    stringResource(Res.string.ui_favorite_credential)
                                } else {
                                    stringResource(Res.string.ui_not_a_favorite)
                                },
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                        state.passwordHealth.ageDays?.let { ageDays ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                                Text(
                                    pluralStringResource(
                                        Res.plurals.ui_password_age_days,
                                        ageDays,
                                        ageDays,
                                    ),
                                )
                            }
                        }
                        if (state.passwordHealth.isWeak || state.passwordHealth.isDuplicate) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                                Text(
                                    stringResource(Res.string.ui_this_credential_needs_attention_in_password_health),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        } else if (state.passwordHealth.score != com.passvault.core.domain.model.PasswordScore.UNKNOWN) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(stringResource(Res.string.ui_no_local_password_health_issue_recorded))
                            }
                        }
                    }
                }

                if (state.attachments.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(stringResource(Res.string.ui_encrypted_attachments), style = MaterialTheme.typography.titleSmall)
                            state.attachments.forEach { attachment ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.InsertDriveFile, contentDescription = null)
                                    Text(
                                        stringResource(
                                            Res.string.ui_attachment_file_size,
                                            attachment.fileName,
                                            attachment.sizeBytes,
                                        ),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            Text(
                                stringResource(Res.string.ui_attachment_editing_and_file_opening_are_not_available),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }

    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(CredentialViewModel.CredentialEvent.OnDeleteCancel)
            },
            title = { Text(stringResource(Res.string.ui_delete_credential_permanently)) },
            text = {
                Text(
                    stringResource(Res.string.ui_this_removes_the_encrypted_credential_and_its_history) +
                        stringResource(Res.string.ui_create_a_backup_first_if_you_may_need_to_recover_it),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnDeleteConfirm)
                    },
                ) {
                    Text(stringResource(Res.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(CredentialViewModel.CredentialEvent.OnDeleteCancel)
                    },
                ) {
                    Text(stringResource(Res.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun TotpCodeCard(
    state: CredentialViewModel.CredentialState,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = state.totpConfiguration ?: return
    val displayCode = state.currentTotpCode.groupTotpCode()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.ui_totp_code),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (state.totpGenerationError || displayCode.isEmpty()) {
                Text(
                    text = stringResource(Res.string.ui_totp_code_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = displayCode,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(
                                Res.string.ui_copy_value,
                                stringResource(Res.string.ui_totp_code),
                            ),
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { state.totpProgress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = pluralStringResource(
                        Res.plurals.ui_totp_expires_in_seconds,
                        state.totpSecondsRemaining,
                        state.totpSecondsRemaining,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            val accountLabel = listOfNotNull(configuration.issuer, configuration.accountName)
                .filter(String::isNotBlank)
                .joinToString(" · ")
            if (accountLabel.isNotEmpty()) {
                Text(
                    text = accountLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                text = stringResource(Res.string.ui_totp_device_time_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

private fun String.groupTotpCode(): String = when (length) {
    6 -> "${take(3)} ${drop(3)}"
    8 -> "${take(4)} ${drop(4)}"
    else -> this
}

@Composable
private fun CredentialField(
    label: String,
    value: String,
    onCopy: (() -> Unit)?,
    isPassword: Boolean,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
) {
    var visible by remember(value, isPassword) { mutableStateOf(!isPassword) }
    LaunchedEffect(visible, value, isPassword) {
        if (visible && isPassword) {
            delay(15_000)
            visible = false
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.size(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (visible) value else "•".repeat(value.codePointLength().coerceAtMost(24)),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f),
                )
                if (isPassword) {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (visible) {
                                stringResource(Res.string.ui_hide_value, label)
                            } else {
                                stringResource(Res.string.ui_show_value, label)
                            },
                        )
                    }
                }
                if (onCopy != null) {
                    IconButton(onClick = onCopy) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = stringResource(Res.string.ui_copy_value, label),
                        )
                    }
                }
                action?.invoke()
            }
        }
    }
}
