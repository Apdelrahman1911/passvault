package com.passvault.feature.vault.ui.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
            headlineContent = { CredentialHeadline(credential) },
            supportingContent = { CredentialSupportingContent(credential) },
            leadingContent = { CredentialTypeIcon(type = credential.type) },
            trailingContent = {
                CredentialFavoriteButton(credential.isFavorite, onFavoriteClick)
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        )
    }
}

@Composable
private fun CredentialHeadline(credential: CredentialSummary.Decrypted) {
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
}

@Composable
private fun CredentialSupportingContent(credential: CredentialSummary.Decrypted) {
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
            if (credential.passwordHealth.isDuplicate) {
                Text(
                    text = stringResource(Res.string.ui_duplicate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
private fun CredentialFavoriteButton(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isFavorite) {
                Icons.Default.Favorite
            } else {
                Icons.Default.FavoriteBorder
            },
            contentDescription = if (isFavorite) {
                stringResource(Res.string.action_unfavorite)
            } else {
                stringResource(Res.string.action_favorite)
            },
        )
    }
}

@Composable
private fun CredentialTypeIcon(
    type: CredentialType,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(40.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = credentialTypeIcon(type),
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
    val text = stringResource(healthLabel(score))
    val (containerColor, contentColor) = healthColors(score)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
