@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.passvault.feature.health.ui

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.tokens.Breakpoints
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.designsystem.components.EditorialPageHeader
import com.passvault.feature.health.presentation.HealthViewModel
import com.passvault.feature.health.presentation.HealthViewModel.DuplicateGroup
import com.passvault.feature.health.presentation.HealthViewModel.HealthTab
import com.passvault.feature.health.presentation.HealthViewModel.OldPasswordItem
import com.passvault.feature.health.presentation.HealthViewModel.ScoreRating
import com.passvault.feature.health.presentation.HealthViewModel.WeakPasswordItem

private val HealthContentMaxWidth = 920.dp

@Composable
fun HealthScreen(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val transientMessage = state.transientMessage?.resolve()
    LaunchedEffect(transientMessage) {
        transientMessage?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(HealthViewModel.HealthEvent.OnDismissMessage)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onEvent(HealthViewModel.HealthEvent.OnBackClick) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.action_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onEvent(HealthViewModel.HealthEvent.OnCopySummary) },
                        enabled = !state.isLoading && state.lastScanAt != null,
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = stringResource(Res.string.ui_copy_health_summary))
                    }
                    IconButton(
                        onClick = { onEvent(HealthViewModel.HealthEvent.OnRefreshScan) },
                        enabled = !state.isLoading,
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.ui_scan_again))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = ComponentSpacing.screenHorizontal,
                        vertical = Spacing.sm,
                    ),
                contentAlignment = Alignment.TopCenter,
            ) {
                EditorialPageHeader(
                    eyebrow = stringResource(Res.string.ui_encrypted_vault),
                    title = stringResource(Res.string.ui_password_health),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = HealthContentMaxWidth),
                )
            }

            PrimaryScrollableTabRow(
                selectedTabIndex = state.selectedTab.ordinal,
                edgePadding = ComponentSpacing.screenHorizontal,
            ) {
                HealthTab.entries.forEach { tab ->
                    val count = when (tab) {
                        HealthTab.OVERVIEW -> 0
                        HealthTab.WEAK_PASSWORDS -> state.weakPasswords.size
                        HealthTab.DUPLICATES -> state.duplicatePasswords.size
                        HealthTab.OLD_PASSWORDS -> state.oldPasswords.size
                    }
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { onEvent(HealthViewModel.HealthEvent.OnTabChanged(tab)) },
                        text = { Text(tab.displayName.resolve()) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (count > 0) Badge { Text(count.toString()) }
                                },
                            ) {
                                Icon(
                                    imageVector = when (tab) {
                                        HealthTab.OVERVIEW -> Icons.Default.Info
                                        HealthTab.WEAK_PASSWORDS -> Icons.Default.Warning
                                        HealthTab.DUPLICATES -> Icons.Default.ContentCopy
                                        HealthTab.OLD_PASSWORDS -> Icons.Default.Schedule
                                    },
                                    contentDescription = null,
                                )
                            }
                        },
                    )
                }
            }

            state.errorMessage?.let { message ->
                ErrorBanner(
                    message = message.resolve(),
                    onRetry = { onEvent(HealthViewModel.HealthEvent.OnRefreshScan) },
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (state.selectedTab) {
                    HealthTab.OVERVIEW -> OverviewTab(state, onEvent)
                    HealthTab.WEAK_PASSWORDS -> WeakPasswordsTab(state, onEvent)
                    HealthTab.DUPLICATES -> DuplicatesTab(state, onEvent)
                    HealthTab.OLD_PASSWORDS -> OldPasswordsTab(state, onEvent)
                }
            }
        }
    }

    state.showingDuplicateGroup?.let { group ->
        DuplicateGroupDialog(
            group = group,
            onDismiss = { onEvent(HealthViewModel.HealthEvent.OnDismissDuplicateGroup) },
            onReviewFirst = {
                group.credentials.firstOrNull()?.let { first ->
                    onEvent(HealthViewModel.HealthEvent.OnFixWeakPasswordClick(first.credentialId))
                }
                onEvent(HealthViewModel.HealthEvent.OnDismissDuplicateGroup)
            },
        )
    }
}

@Composable
private fun OverviewTab(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .widthIn(max = HealthContentMaxWidth)
                .verticalScroll(rememberScrollState())
                .padding(ComponentSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
        ) {
            HealthScoreCard(state)
            HealthStats(state)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(ComponentSpacing.cardPadding),
                    verticalArrangement = Arrangement.spacedBy(ComponentSpacing.cardContentSpacing),
                ) {
                    Text(
                        text = stringResource(Res.string.ui_local_checks),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                    IssueRow(
                        title = stringResource(Res.string.ui_weak_passwords),
                        count = state.weakPasswords.size,
                        icon = Icons.Default.Warning,
                        color = MaterialTheme.colorScheme.error,
                    )
                    IssueRow(
                        title = stringResource(Res.string.ui_duplicate_password_groups),
                        count = state.duplicatePasswords.size,
                        icon = Icons.Default.ContentCopy,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    IssueRow(
                        title = stringResource(Res.string.ui_passwords_at_least_365_days_old),
                        count = state.oldPasswords.size,
                        icon = Icons.Default.Schedule,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            if (state.hasIssues) {
                Button(
                    onClick = { onEvent(HealthViewModel.HealthEvent.OnReviewIssues) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.ui_review_issues))
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(ComponentSpacing.cardPadding),
                    horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.md),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(ComponentSpacing.xs)) {
                        Text(
                            text = stringResource(Res.string.ui_private_on_device_scan),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = stringResource(Res.string.ui_passvault_checks_password_strength_duplicate_use_and_a) +
                                stringResource(Res.string.ui_it_does_not_upload_passwords_or_claim_to_check_public),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            state.lastScanAt?.let {
                Text(
                    text = stringResource(Res.string.ui_last_local_scan, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(ComponentSpacing.sm))
        }
    }
}

@Composable
private fun HealthScoreCard(state: HealthViewModel.HealthState) {
    val color = when (state.scoreRating) {
        ScoreRating.NOT_SCANNED -> MaterialTheme.colorScheme.onSurfaceVariant
        ScoreRating.EXCELLENT -> MaterialTheme.colorScheme.primary
        ScoreRating.GOOD -> MaterialTheme.colorScheme.tertiary
        ScoreRating.FAIR -> MaterialTheme.colorScheme.secondary
        ScoreRating.POOR -> MaterialTheme.colorScheme.error
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComponentSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp), color = color)
                } else {
                    Text(
                        text = if (state.totalAnalyzed == 0) "—" else state.overallScore.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        color = color,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(ComponentSpacing.xs),
            ) {
                Text(
                    text = if (state.isLoading) {
                        stringResource(Res.string.ui_scanning_locally)
                    } else {
                        state.scoreRating.displayName.resolve()
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = color,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = if (state.isLoading) {
                        stringResource(Res.string.ui_checking_unlocked_login_records_without_sending_data_o)
                    } else {
                        state.scoreRating.description.resolve()
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HealthStats(state: HealthViewModel.HealthState) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= Breakpoints.mediumMin) {
            Row(horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)) {
                StatCard(stringResource(Res.string.ui_analyzed), state.totalAnalyzed, Icons.Default.Lock, Modifier.weight(1f))
                StatCard(
                    stringResource(Res.string.password_strength_weak),
                    state.criticalIssues,
                    Icons.Default.Error,
                    Modifier.weight(1f),
                    MaterialTheme.colorScheme.error,
                )
                StatCard(
                    stringResource(Res.string.ui_warnings),
                    state.warningIssues,
                    Icons.Default.Warning,
                    Modifier.weight(1f),
                    MaterialTheme.colorScheme.tertiary,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)) {
                StatCard(stringResource(Res.string.ui_passwords_analyzed), state.totalAnalyzed, Icons.Default.Lock)
                Row(horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)) {
                    StatCard(
                        stringResource(Res.string.password_strength_weak),
                        state.criticalIssues,
                        Icons.Default.Error,
                        Modifier.weight(1f),
                        MaterialTheme.colorScheme.error,
                    )
                    StatCard(
                        stringResource(Res.string.ui_warnings),
                        state.warningIssues,
                        Icons.Default.Warning,
                        Modifier.weight(1f),
                        MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeakPasswordsTab(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
) {
    if (!state.isLoading && state.weakPasswords.isEmpty()) {
        HealthEmptyState(
            title = stringResource(Res.string.ui_no_weak_passwords_found),
            message = stringResource(Res.string.ui_the_local_strength_check_found_no_weak_passwords_among),
        )
        return
    }
    HealthList {
        items(state.weakPasswords, key = { it.credentialId.value }) { item ->
            WeakPasswordCard(
                item = item,
                onFixClick = {
                    onEvent(HealthViewModel.HealthEvent.OnFixWeakPasswordClick(item.credentialId))
                },
                onClick = {
                    onEvent(HealthViewModel.HealthEvent.OnCredentialClick(item.credentialId))
                },
            )
        }
    }
}

@Composable
private fun DuplicatesTab(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
) {
    if (!state.isLoading && state.duplicatePasswords.isEmpty()) {
        HealthEmptyState(
            title = stringResource(Res.string.ui_no_duplicate_groups_found),
            message = stringResource(Res.string.ui_the_local_scan_found_no_identical_passwords_shared_by),
        )
        return
    }
    HealthList {
        items(
            items = state.duplicatePasswords,
            key = { group -> group.credentials.joinToString("|") { it.credentialId.value } },
        ) { group ->
            DuplicateGroupCard(
                group = group,
                onViewClick = {
                    onEvent(HealthViewModel.HealthEvent.OnFixDuplicateClick(group))
                },
            )
        }
    }
}

@Composable
private fun OldPasswordsTab(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
) {
    if (!state.isLoading && state.oldPasswords.isEmpty()) {
        HealthEmptyState(
            title = stringResource(Res.string.ui_no_old_passwords_found),
            message = stringResource(Res.string.ui_no_scanned_password_is_known_to_be_at_least_365_days_o),
        )
        return
    }
    HealthList {
        items(state.oldPasswords, key = { it.credentialId.value }) { item ->
            OldPasswordCard(
                item = item,
                onUpdateClick = {
                    onEvent(HealthViewModel.HealthEvent.OnFixOldPasswordClick(item.credentialId))
                },
            )
        }
    }
}

@Composable
private fun HealthList(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = HealthContentMaxWidth),
            contentPadding = PaddingValues(ComponentSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.listItemSpacing),
            content = content,
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
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
        Row(
            modifier = Modifier.padding(ComponentSpacing.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = color)
            Column {
                Text(value.toString(), style = MaterialTheme.typography.titleLarge, color = color)
                Text(title, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun IssueRow(title: String, count: Int, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ComponentSpacing.touchTargetMin),
        horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Badge(containerColor = color) { Text(count.toString()) }
    }
}

@Composable
private fun WeakPasswordCard(
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComponentSpacing.listItemPadding),
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                item.username?.let {
                    Text(
                        it,
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
private fun DuplicateGroupCard(group: DuplicateGroup, onViewClick: () -> Unit) {
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComponentSpacing.listItemPadding),
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
            )
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
private fun OldPasswordCard(item: OldPasswordItem, onUpdateClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComponentSpacing.listItemPadding),
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                item.username?.let {
                    Text(
                        it,
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
private fun DuplicateGroupDialog(
    group: DuplicateGroup,
    onDismiss: () -> Unit,
    onReviewFirst: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.ui_shared_password)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)) {
                Text(stringResource(Res.string.ui_these_logins_use_an_identical_password_change_each_log))
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
                                    Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
            TextButton(onClick = onReviewFirst) { Text(stringResource(Res.string.ui_review_first_login)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.action_close)) }
        },
    )
}

@Composable
private fun HealthEmptyState(title: String, message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ComponentSpacing.xl),
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
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ComponentSpacing.screenHorizontal,
                vertical = ComponentSpacing.sm,
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(ComponentSpacing.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Error, contentDescription = null)
            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text(stringResource(Res.string.action_retry)) }
        }
    }
}
