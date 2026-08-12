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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.core.designsystem.components.EditorialStatusBanner
import com.passvault.core.designsystem.platform.passVaultTopAppBarColors
import com.passvault.core.designsystem.platform.scaffoldVerticalScroll
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.domain.model.CredentialId
import com.passvault.core.domain.model.PasswordScore
import com.passvault.core.domain.model.SensitiveText
import com.passvault.feature.credential.presentation.CredentialViewModel
import com.passvault.feature.credential.ui.components.CredentialAttachmentSection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect

@Composable
fun CredentialDetailScreen(
    viewModel: CredentialViewModel,
    credentialId: CredentialId,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (CredentialId) -> Unit,
    onCopyToClipboard: suspend (String) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(credentialId) { viewModel.loadCredential(credentialId) }
    CredentialDetailEffects(viewModel, onNavigateBack, onCopyToClipboard)

    if (state.isLoading) {
        CredentialDetailLoading(modifier)
    } else {
        CredentialDetailContent(
            state = state,
            credentialId = credentialId,
            onNavigateToEdit = onNavigateToEdit,
            onEvent = viewModel::onEvent,
            modifier = modifier,
        )
    }
    if (state.showDeleteConfirmation) {
        DeleteCredentialDialog(onEvent = viewModel::onEvent)
    }
}

@Composable
private fun CredentialDetailEffects(
    viewModel: CredentialViewModel,
    onNavigateBack: () -> Unit,
    onCopyToClipboard: suspend (String) -> Boolean,
) {
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    LaunchedEffect(viewModel, uriHandler) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CredentialViewModel.CredentialEffect.NavigateBack -> onNavigateBack()
                is CredentialViewModel.CredentialEffect.CopyToClipboard -> {
                    val copied = try {
                        onCopyToClipboard(effect.text)
                    } catch (cancel: CancellationException) {
                        throw cancel
                    } catch (_: Exception) {
                        false
                    }
                    viewModel.onEvent(CredentialViewModel.CredentialEvent.OnCopyResult(copied))
                }
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
}

@Composable
private fun CredentialDetailLoading(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CredentialDetailContent(
    state: CredentialViewModel.CredentialState,
    credentialId: CredentialId,
    onNavigateToEdit: (CredentialId) -> Unit,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
    modifier: Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().imePadding(),
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
                verticalArrangement = Arrangement.spacedBy(Spacing.smMd),
            ) {
                CredentialDetailTopBar(state, credentialId, onNavigateToEdit, onEvent)
                EditorialPageHeader(
                    eyebrow = stringResource(Res.string.ui_encrypted_vault),
                    title = state.displayTitle.resolve(),
                )
                CredentialErrorBanner(state)
                CredentialLoginFields(state, onEvent)
                if (state.totpConfiguration != null) {
                    TotpCodeCard(
                        state = state,
                        onCopy = { onEvent(CredentialViewModel.CredentialEvent.OnCopyTotpClick) },
                    )
                }
                CredentialUrlsAndNotes(state, onEvent)
                CredentialCustomFields(state, onEvent)
                CredentialSensitiveLists(state, onEvent)
                CredentialHealthCard(state)
                CredentialAttachmentSection(state = state, onEvent = onEvent)
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun CredentialDetailTopBar(
    state: CredentialViewModel.CredentialState,
    credentialId: CredentialId,
    onNavigateToEdit: (CredentialId) -> Unit,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    TopAppBar(
        title = {},
        windowInsets = WindowInsets(0, 0, 0, 0),
        navigationIcon = {
            IconButton(onClick = { onEvent(CredentialViewModel.CredentialEvent.OnBackClick) }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.action_back),
                )
            }
        },
        actions = {
            IconButton(
                onClick = { onNavigateToEdit(credentialId) },
                enabled = state.isCredentialLoaded && !state.isBusy,
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(Res.string.ui_edit_credential),
                )
            }
            IconButton(
                onClick = { onEvent(CredentialViewModel.CredentialEvent.OnDeleteClick) },
                enabled = state.isCredentialLoaded && !state.isBusy,
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.ui_delete_credential),
                )
            }
        },
        colors = passVaultTopAppBarColors(),
    )
}

@Composable
private fun CredentialErrorBanner(state: CredentialViewModel.CredentialState) {
    state.errorMessage?.let { message ->
        EditorialStatusBanner(
            icon = Icons.Default.Error,
            title = state.displayTitle.resolve(),
            message = message.resolve(),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    }
}

@Composable
private fun CredentialLoginFields(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    CredentialField(
        label = stringResource(Res.string.ui_title),
        value = state.title,
        onCopy = null,
        isPassword = false,
    )
    if (state.username.isNotBlank()) {
        CredentialField(
            stringResource(Res.string.ui_username),
            state.username,
            onCopy = { onEvent(CredentialViewModel.CredentialEvent.OnCopyUsernameClick) },
            isPassword = false,
        )
    }
    if (state.email.isNotBlank()) {
        CredentialField(
            stringResource(Res.string.ui_email),
            state.email,
            onCopy = { onEvent(CredentialViewModel.CredentialEvent.OnCopyEmailClick) },
            isPassword = false,
        )
    }
    if (state.password.isNotBlank()) {
        CredentialField(
            stringResource(Res.string.ui_password),
            state.password,
            onCopy = { onEvent(CredentialViewModel.CredentialEvent.OnCopyPasswordClick) },
            isPassword = true,
        )
    }
}

@Composable
private fun CredentialUrlsAndNotes(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    state.urls.forEach { url ->
        CredentialField(
            label = stringResource(Res.string.ui_website),
            value = url,
            onCopy = null,
            isPassword = false,
            action = {
                TextButton(
                    onClick = {
                        onEvent(CredentialViewModel.CredentialEvent.OnLaunchUrlClick(url))
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
        CredentialField(
            label = stringResource(Res.string.ui_notes),
            value = state.notes,
            onCopy = null,
            isPassword = false,
        )
    }
}

@Composable
private fun CredentialCustomFields(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    state.customFields.forEach { field ->
        val onCopy = {
            onEvent(CredentialViewModel.CredentialEvent.OnCopyCustomFieldClick(field.id))
        }
        if (field.isSecret) {
            SensitiveCredentialField(label = field.name, value = field.value, onCopy = onCopy)
        } else {
            CredentialField(
                label = field.name,
                value = field.value.toStringUnsafe(),
                onCopy = onCopy,
                isPassword = false,
            )
        }
    }
}

@Composable
private fun CredentialSensitiveLists(
    state: CredentialViewModel.CredentialState,
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    state.recoveryCodes.forEachIndexed { index, value ->
        SensitiveCredentialField(
            label = stringResource(Res.string.ui_recovery_code_number, index + 1),
            value = value,
            onCopy = {
                onEvent(CredentialViewModel.CredentialEvent.OnCopyRecoveryCodeClick(index))
            },
        )
    }
    state.apiKeys.forEachIndexed { index, value ->
        SensitiveCredentialField(
            label = stringResource(Res.string.ui_api_key_number, index + 1),
            value = value,
            onCopy = { onEvent(CredentialViewModel.CredentialEvent.OnCopyApiKeyClick(index)) },
        )
    }
    state.licenseKeys.forEachIndexed { index, value ->
        SensitiveCredentialField(
            label = stringResource(Res.string.ui_license_key_number, index + 1),
            value = value,
            onCopy = {
                onEvent(CredentialViewModel.CredentialEvent.OnCopyLicenseKeyClick(index))
            },
        )
    }
}

@Composable
private fun CredentialHealthCard(state: CredentialViewModel.CredentialState) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FavoriteStatus(state.isFavorite)
            state.passwordHealth.ageDays?.let { PasswordAgeStatus(it) }
            PasswordHealthStatus(state)
        }
    }
}

@Composable
private fun FavoriteStatus(isFavorite: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            if (isFavorite) {
                stringResource(Res.string.ui_favorite_credential)
            } else {
                stringResource(Res.string.ui_not_a_favorite)
            },
            style = MaterialTheme.typography.titleSmall,
        )
    }
}

@Composable
private fun PasswordAgeStatus(ageDays: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Schedule, contentDescription = null)
        Text(pluralStringResource(Res.plurals.ui_password_age_days, ageDays, ageDays))
    }
}

@Composable
private fun PasswordHealthStatus(state: CredentialViewModel.CredentialState) {
    val needsAttention = state.passwordHealth.isWeak || state.passwordHealth.isDuplicate
    val hasScore = state.passwordHealth.score != PasswordScore.UNKNOWN
    if (!needsAttention && !hasScore) return
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (needsAttention) Icons.Default.Error else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (needsAttention) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        )
        Text(
            text = if (needsAttention) {
                stringResource(Res.string.ui_this_credential_needs_attention_in_password_health)
            } else {
                stringResource(Res.string.ui_no_local_password_health_issue_recorded)
            },
            color = if (needsAttention) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
    }
}

@Composable
private fun DeleteCredentialDialog(
    onEvent: (CredentialViewModel.CredentialEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(CredentialViewModel.CredentialEvent.OnDeleteCancel) },
        title = { Text(stringResource(Res.string.ui_delete_credential_permanently)) },
        text = { Text(stringResource(Res.string.ui_delete_credential_backup_warning)) },
        confirmButton = {
            TextButton(onClick = { onEvent(CredentialViewModel.CredentialEvent.OnDeleteConfirm) }) {
                Text(
                    text = stringResource(Res.string.action_delete),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(CredentialViewModel.CredentialEvent.OnDeleteCancel) }) {
                Text(stringResource(Res.string.action_cancel))
            }
        },
    )
}

@Composable
private fun TotpCodeCard(
    state: CredentialViewModel.CredentialState,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = state.totpConfiguration ?: return
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
            TotpCodeValue(state, onCopy)
            TotpAccountInfo(configuration.issuer, configuration.accountName)
        }
    }
}

@Composable
private fun TotpCodeValue(
    state: CredentialViewModel.CredentialState,
    onCopy: () -> Unit,
) {
    val displayCode = state.currentTotpCode.groupTotpCode()
    if (state.totpGenerationError || displayCode.isEmpty()) {
        Text(
            text = stringResource(Res.string.ui_totp_code_unavailable),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        return
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
    LinearProgressIndicator(progress = { state.totpProgress }, modifier = Modifier.fillMaxWidth())
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

@Composable
private fun TotpAccountInfo(issuer: String?, accountName: String?) {
    val accountLabel = listOfNotNull(issuer, accountName)
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
    val hiddenDescription = stringResource(Res.string.secure_field_password_hidden)
    LaunchedEffect(visible, value, isPassword) {
        if (visible && isPassword) {
            delay(SENSITIVE_VALUE_REVEAL_MILLIS)
            visible = false
        }
    }

    CredentialFieldCard(
        label = label,
        value = value,
        visible = visible,
        hiddenDescription = hiddenDescription,
        isPassword = isPassword,
        onVisibilityClick = { visible = !visible },
        onCopy = onCopy,
        modifier = modifier,
        action = action,
    )
}

/** Keeps wrapped secrets out of immutable UI strings until the user explicitly reveals one. */
@Composable
private fun SensitiveCredentialField(
    label: String,
    value: SensitiveText,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var visible by remember(value) { mutableStateOf(false) }
    val hiddenDescription = stringResource(Res.string.secure_field_password_hidden)
    LaunchedEffect(visible, value) {
        if (visible) {
            delay(SENSITIVE_VALUE_REVEAL_MILLIS)
            visible = false
        }
    }

    CredentialFieldCard(
        label = label,
        value = if (visible) value.toStringUnsafe() else "",
        visible = visible,
        hiddenDescription = hiddenDescription,
        isPassword = true,
        onVisibilityClick = { visible = !visible },
        onCopy = onCopy,
        modifier = modifier,
        action = null,
    )
}

@Composable
private fun CredentialFieldCard(
    label: String,
    value: String,
    visible: Boolean,
    hiddenDescription: String,
    isPassword: Boolean,
    onVisibilityClick: () -> Unit,
    onCopy: (() -> Unit)?,
    modifier: Modifier,
    action: @Composable (() -> Unit)?,
) {
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
            CredentialFieldValueRow(
                label = label,
                value = value,
                visible = visible,
                hiddenDescription = hiddenDescription,
                isPassword = isPassword,
                onVisibilityClick = onVisibilityClick,
                onCopy = onCopy,
                action = action,
            )
        }
    }
}

@Composable
private fun CredentialFieldValueRow(
    label: String,
    value: String,
    visible: Boolean,
    hiddenDescription: String,
    isPassword: Boolean,
    onVisibilityClick: () -> Unit,
    onCopy: (() -> Unit)?,
    action: @Composable (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val valueModifier = Modifier.weight(1f).then(
            if (!visible && isPassword) {
                Modifier.clearAndSetSemantics { contentDescription = hiddenDescription }
            } else {
                Modifier
            },
        )
        Text(
            text = if (visible) value else "•".repeat(HIDDEN_VALUE_BULLET_COUNT),
            style = MaterialTheme.typography.bodyLarge,
            modifier = valueModifier,
        )
        if (isPassword) {
            IconButton(onClick = onVisibilityClick) {
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

private const val HIDDEN_VALUE_BULLET_COUNT = 12
private const val SENSITIVE_VALUE_REVEAL_MILLIS = 15_000L
