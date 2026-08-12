package com.passvault.feature.health.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.feature.health.presentation.HealthViewModel.DuplicateGroup
import com.passvault.feature.health.presentation.HealthViewModel.OldPasswordItem
import com.passvault.feature.health.presentation.HealthViewModel.WeakPasswordItem
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HealthLoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 320.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(ComponentSpacing.md))
        Text(
            text = stringResource(Res.string.ui_scanning_locally),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun WeakPasswordCard(
    item: WeakPasswordItem,
    onFixClick: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(ComponentSpacing.listItemPadding),
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                item.username?.let { username ->
                    Text(
                        username,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    item.reason.displayName.resolve(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            TextButton(onClick = onFixClick) { Text(stringResource(Res.string.ui_review)) }
        }
    }
}

@Composable
internal fun DuplicateGroupCard(group: DuplicateGroup, onViewClick: () -> Unit) {
    Card(
        onClick = onViewClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(ComponentSpacing.listItemPadding),
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.ContentCopy, null, tint = MaterialTheme.colorScheme.tertiary)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pluralStringResource(
                        Res.plurals.ui_duplicate_password_login_count,
                        group.count,
                        group.count,
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    group.credentials.joinToString { it.title },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            TextButton(onClick = onViewClick) { Text(stringResource(Res.string.ui_review)) }
        }
    }
}

@Composable
internal fun OldPasswordCard(item: OldPasswordItem, onUpdateClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(ComponentSpacing.listItemPadding),
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                item.username?.let { username ->
                    Text(
                        username,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    pluralStringResource(
                        Res.plurals.ui_days_since_password_change,
                        item.ageDays,
                        item.ageDays,
                    ),
                )
            }
            TextButton(onClick = onUpdateClick) { Text(stringResource(Res.string.ui_review)) }
        }
    }
}

@Composable
internal fun DuplicateGroupDialog(
    group: DuplicateGroup,
    onDismiss: () -> Unit,
    onReviewFirst: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.ui_shared_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)) {
                Text(
                    stringResource(
                        Res.string.ui_these_logins_use_an_identical_password_change_each_log,
                    ),
                )
                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                    items(group.credentials, key = { it.credentialId.value }) { credential ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    credential.title,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            supportingContent = {
                                credential.username?.let {
                                    Text(
                                        it,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            },
                            leadingContent = {
                                Icon(Icons.Default.Lock, contentDescription = null)
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onReviewFirst) {
                Text(stringResource(Res.string.ui_review_first_login))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_close)) }
        },
    )
}

@Composable
internal fun HealthEmptyState(title: String, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 320.dp).padding(ComponentSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(ComponentSpacing.md))
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Spacer(modifier = Modifier.height(ComponentSpacing.sm))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(
            modifier = Modifier.padding(ComponentSpacing.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Error, contentDescription = null)
            Text(
                message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onRetry) { Text(stringResource(Res.string.action_retry)) }
        }
    }
}
