package com.passvault.feature.vault.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.Spacing
import com.passvault.core.domain.model.CredentialSummary
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.PasswordScore

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComponentSpacing.listItemPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            // Icon
            CredentialIcon(
                type = credential.type,
                health = credential.passwordHealth.score,
                modifier = Modifier.size(48.dp)
            )

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    Text(
                        text = credential.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (credential.isFavorite) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = stringResource(Res.string.credential_card_favorite),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                credential.displayUsername?.let { username ->
                    Text(
                        text = username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.padding(top = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    HealthIndicator(score = credential.passwordHealth.score)

                    if (credential.passwordHealth.isWeak) {
                        Text(
                            text = stringResource(Res.string.password_strength_weak),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (credential.passwordHealth.isDuplicate) {
                        Text(
                            text = stringResource(Res.string.ui_duplicate),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Actions
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (credential.isFavorite) {
                        Icons.Default.Favorite
                    } else {
                        Icons.Default.FavoriteBorder
                    },
                    contentDescription = if (credential.isFavorite) stringResource(Res.string.action_unfavorite) else stringResource(Res.string.action_favorite)
                )
            }
        }
    }
}

@Composable
private fun CredentialIcon(
    type: CredentialType,
    health: PasswordScore,
    modifier: Modifier = Modifier,
) {
    val icon = when (type) {
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

    val containerColor = when (health) {
        PasswordScore.VERY_WEAK, PasswordScore.WEAK -> MaterialTheme.colorScheme.errorContainer
        PasswordScore.FAIR -> MaterialTheme.colorScheme.tertiaryContainer
        PasswordScore.GOOD -> MaterialTheme.colorScheme.secondaryContainer
        PasswordScore.STRONG, PasswordScore.VERY_STRONG -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = containerColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun HealthIndicator(
    score: PasswordScore,
    modifier: Modifier = Modifier,
) {
    val color = when (score) {
        PasswordScore.VERY_WEAK, PasswordScore.WEAK -> MaterialTheme.colorScheme.error
        PasswordScore.FAIR -> MaterialTheme.colorScheme.tertiary
        PasswordScore.GOOD -> MaterialTheme.colorScheme.secondary
        PasswordScore.STRONG, PasswordScore.VERY_STRONG -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = modifier
            .size(8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.small,
            color = color
        ) {}
    }
}
