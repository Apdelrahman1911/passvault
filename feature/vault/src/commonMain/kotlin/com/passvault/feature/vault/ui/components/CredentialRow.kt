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
import com.passvault.core.domain.model.CredentialSummary
import com.passvault.core.domain.model.CredentialType
import com.passvault.core.domain.model.PasswordScore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialRow(
    credential: CredentialSummary.Decrypted,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        ListItem(
            headlineContent = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            },
            supportingContent = {
                Column {
                    credential.displayUsername?.let { username ->
                        Text(
                            text = username,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        HealthBadge(score = credential.passwordHealth.score)

                        if (credential.passwordHealth.isWeak) {
                            Text(
                                text = stringResource(Res.string.password_strength_weak),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }

                        if (credential.passwordHealth.isDuplicate) {
                            Text(
                                text = stringResource(Res.string.ui_duplicate),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }
            },
            leadingContent = {
                CredentialTypeIcon(type = credential.type)
            },
            trailingContent = {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (credential.isFavorite) {
                            Icons.Default.Favorite
                        } else {
                            Icons.Default.FavoriteBorder
                        },
                        contentDescription = if (credential.isFavorite) {
                            stringResource(Res.string.action_unfavorite)
                        } else {
                            stringResource(Res.string.action_favorite)
                        },
                    )
                }
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        )
    }
}

@Composable
private fun CredentialTypeIcon(
    type: CredentialType,
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

    Surface(
        modifier = modifier.size(40.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun HealthBadge(
    score: PasswordScore,
    modifier: Modifier = Modifier,
) {
    val (text, color) = when (score) {
        PasswordScore.VERY_WEAK -> stringResource(Res.string.password_strength_very_weak) to MaterialTheme.colorScheme.error
        PasswordScore.WEAK -> stringResource(Res.string.password_strength_weak) to MaterialTheme.colorScheme.error
        PasswordScore.FAIR -> stringResource(Res.string.password_strength_fair) to MaterialTheme.colorScheme.tertiary
        PasswordScore.GOOD -> stringResource(Res.string.password_strength_good) to MaterialTheme.colorScheme.secondary
        PasswordScore.STRONG -> stringResource(Res.string.password_strength_strong) to MaterialTheme.colorScheme.primary
        PasswordScore.VERY_STRONG -> stringResource(Res.string.password_strength_very_strong) to MaterialTheme.colorScheme.primary
        else -> stringResource(Res.string.password_strength_unknown) to MaterialTheme.colorScheme.outline
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = modifier
    )
}
