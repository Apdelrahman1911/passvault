package com.passvault.feature.vault.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.domain.model.CredentialSummary
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.PasswordScore
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CredentialCard(
    credential: CredentialSummary.Decrypted,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        CredentialCardContent(credential = credential, onFavoriteClick = onFavoriteClick)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CredentialCardContent(
    credential: CredentialSummary.Decrypted,
    onFavoriteClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(ComponentSpacing.listItemPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        CredentialIcon(
            type = credential.type,
            health = credential.passwordHealth.score,
            modifier = Modifier.size(48.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            CredentialTitle(credential)
            CredentialMetadata(credential)
        }
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (credential.isFavorite) {
                    Icons.Default.Favorite
                } else {
                    Icons.Default.FavoriteBorder
                },
                contentDescription = stringResource(
                    if (credential.isFavorite) {
                        Res.string.action_unfavorite
                    } else {
                        Res.string.action_favorite
                    },
                ),
            )
        }
    }
}

@Composable
private fun CredentialTitle(credential: CredentialSummary.Decrypted) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = credential.title,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (credential.isFavorite) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CredentialMetadata(credential: CredentialSummary.Decrypted) {
    credential.displayUsername?.let { username ->
        Text(
            text = username,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    FlowRow(
        modifier = Modifier.padding(top = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        HealthIndicator(score = credential.passwordHealth.score)
        if (credential.passwordHealth.isDuplicate) {
            Text(
                text = stringResource(Res.string.ui_duplicate),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun CredentialIcon(
    type: CredentialType,
    health: PasswordScore,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor) = healthColors(health)

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = credentialTypeIcon(type),
                contentDescription = null,
                tint = contentColor,
            )
        }
    }
}

@Composable
private fun HealthIndicator(
    score: PasswordScore,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(healthLabel(score))
    val (containerColor, contentColor) = healthColors(score)

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

internal fun healthLabel(score: PasswordScore): StringResource = when (score) {
    PasswordScore.UNKNOWN -> Res.string.password_strength_unknown
    PasswordScore.VERY_WEAK -> Res.string.password_strength_very_weak
    PasswordScore.WEAK -> Res.string.password_strength_weak
    PasswordScore.FAIR -> Res.string.password_strength_fair
    PasswordScore.GOOD -> Res.string.password_strength_good
    PasswordScore.STRONG -> Res.string.password_strength_strong
    PasswordScore.VERY_STRONG -> Res.string.password_strength_very_strong
}

@Composable
internal fun healthColors(score: PasswordScore) = when (score) {
    PasswordScore.VERY_WEAK, PasswordScore.WEAK ->
        MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    PasswordScore.FAIR ->
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
    PasswordScore.GOOD ->
        MaterialTheme.colorScheme.secondaryContainer to
            MaterialTheme.colorScheme.onSecondaryContainer
    PasswordScore.STRONG, PasswordScore.VERY_STRONG ->
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    PasswordScore.UNKNOWN ->
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
}

internal fun credentialTypeIcon(type: CredentialType): ImageVector = when (type) {
    is CredentialType.Login -> Icons.Default.Lock
    is CredentialType.SecureNote -> Icons.Default.Description
    is CredentialType.ApiKey -> Icons.Default.VpnKey
    is CredentialType.LicenseKey -> Icons.AutoMirrored.Filled.MenuBook
    is CredentialType.RecoveryCodes -> Icons.AutoMirrored.Filled.Article
    is CredentialType.WiFiCredential -> Icons.Default.Wifi
    is CredentialType.Identity -> Icons.Default.Badge
    is CredentialType.PaymentCard -> Icons.Default.CreditCard
    is CredentialType.Custom -> Icons.Default.Folder
}
