package com.passvault.feature.health.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.text.resolve
import com.passvault.core.designsystem.tokens.Breakpoints
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.feature.health.presentation.HealthViewModel
import com.passvault.feature.health.presentation.HealthViewModel.ScoreRating
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OverviewContent(
    state: HealthViewModel.HealthState,
    onEvent: (HealthViewModel.HealthEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sectionSpacing),
    ) {
        HealthScoreCard(state)
        HealthStats(state)
        LocalChecksCard(state)
        if (state.hasIssues) {
            Button(
                onClick = { onEvent(HealthViewModel.HealthEvent.OnReviewIssues) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.ui_review_issues))
            }
        }
        PrivacyCard()
        state.lastScanAt?.let { scannedAt ->
            Text(
                text = stringResource(Res.string.ui_last_local_scan, scannedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(ComponentSpacing.sm))
    }
}

@Composable
private fun LocalChecksCard(state: HealthViewModel.HealthState) {
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
                stringResource(Res.string.ui_weak_passwords),
                state.weakPasswords.size,
                Icons.Default.Warning,
                MaterialTheme.colorScheme.error,
            )
            IssueRow(
                stringResource(Res.string.ui_duplicate_password_groups),
                state.duplicatePasswords.size,
                Icons.Default.ContentCopy,
                MaterialTheme.colorScheme.tertiary,
            )
            IssueRow(
                stringResource(Res.string.ui_passwords_at_least_365_days_old),
                state.oldPasswords.size,
                Icons.Default.Schedule,
                MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun PrivacyCard() {
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
                    text = stringResource(Res.string.ui_health_privacy_explanation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HealthScoreCard(state: HealthViewModel.HealthState) {
    val color = scoreColor(state.scoreRating)
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
            modifier = Modifier.fillMaxWidth().padding(ComponentSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ScoreIndicator(state = state, color = color)
            ScoreDescription(state = state, color = color, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ScoreIndicator(state: HealthViewModel.HealthState, color: Color) {
    Box(
        modifier = Modifier.size(88.dp).clip(CircleShape).background(color.copy(alpha = 0.16f)),
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
}

@Composable
private fun ScoreDescription(
    state: HealthViewModel.HealthState,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(ComponentSpacing.xs)) {
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

@Composable
private fun scoreColor(rating: ScoreRating): Color = when (rating) {
    ScoreRating.NOT_SCANNED -> MaterialTheme.colorScheme.onSurfaceVariant
    ScoreRating.EXCELLENT -> MaterialTheme.colorScheme.primary
    ScoreRating.GOOD -> MaterialTheme.colorScheme.tertiary
    ScoreRating.FAIR -> MaterialTheme.colorScheme.secondary
    ScoreRating.POOR -> MaterialTheme.colorScheme.error
}

@Composable
private fun HealthStats(state: HealthViewModel.HealthState) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        if (maxWidth >= Breakpoints.mediumMin) {
            Row(horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)) {
                StatCard(
                    stringResource(Res.string.ui_analyzed),
                    state.totalAnalyzed,
                    Icons.Default.Lock,
                    Modifier.weight(1f),
                )
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
                StatCard(
                    stringResource(Res.string.ui_passwords_analyzed),
                    state.totalAnalyzed,
                    Icons.Default.Lock,
                )
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
        modifier = Modifier.fillMaxWidth().heightIn(min = ComponentSpacing.touchTargetMin),
        horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Badge(containerColor = color, contentColor = contentColorFor(color)) {
            Text(count.toString())
        }
    }
}
