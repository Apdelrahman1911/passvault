package com.passvault.core.designsystem.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.tokens.ComponentSpacing

/**
 * Error state types with predefined content.
 */
enum class ErrorStateType(
    val icon: ImageVector,
    val title: StringResource,
    val message: StringResource,
    val defaultActionText: StringResource? = null,
) {
    DEFAULT(
        Icons.Outlined.ErrorOutline,
        Res.string.error_state_title,
        Res.string.error_state_message,
        Res.string.action_retry,
    ),
    NETWORK(
        Icons.Outlined.SignalWifiOff,
        Res.string.error_state_network_title,
        Res.string.error_state_network_message,
        Res.string.ui_try_again,
    ),
    AUTH(
        Icons.Outlined.Lock,
        Res.string.error_state_auth_title,
        Res.string.error_state_auth_message,
        Res.string.ui_sign_in,
    ),
    VAULT_LOCKED(
        Icons.Outlined.Lock,
        Res.string.error_state_vault_locked_title,
        Res.string.error_state_vault_locked_message,
        Res.string.ui_unlock,
    ),
}

/**
 * Error state component for displaying errors.
 *
 * @param type The type of error state.
 * @param modifier Modifier to be applied.
 * @param onAction Optional primary action callback.
 * @param onDismiss Optional dismiss callback.
 * @param customIcon Optional custom icon.
 * @param customTitle Optional custom title.
 * @param customMessage Optional custom message.
 * @param actionText Optional custom action text.
 */
@Composable
fun ErrorState(
    type: ErrorStateType = ErrorStateType.DEFAULT,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    customIcon: ImageVector? = null,
    customTitle: String? = null,
    customMessage: String? = null,
    actionText: String? = null
) {
    val icon = customIcon ?: type.icon
    val title = customTitle ?: stringResource(type.title)
    val message = customMessage ?: stringResource(type.message)
    val primaryActionText = actionText ?: type.defaultActionText?.let { stringResource(it) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(ComponentSpacing.xl)
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(ComponentSpacing.lg))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(ComponentSpacing.sm))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = ComponentSpacing.lg)
        )

        if (onAction != null || onDismiss != null) {
            Spacer(modifier = Modifier.height(ComponentSpacing.xl))

            if (primaryActionText != null && onAction != null) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.padding(end = ComponentSpacing.sm)
                    )
                    Text(text = primaryActionText)
                }
            }

            if (onDismiss != null) {
                Spacer(modifier = Modifier.height(ComponentSpacing.sm))
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(0.7f)
                ) {
                    Text(text = stringResource(Res.string.action_close))
                }
            }
        }
    }
}

/**
 * Network error state.
 */
@Composable
fun NetworkErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorState(
        type = ErrorStateType.NETWORK,
        modifier = modifier,
        onAction = onRetry
    )
}

/**
 * Authentication error state.
 */
@Composable
fun AuthErrorState(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorState(
        type = ErrorStateType.AUTH,
        modifier = modifier,
        onAction = onSignIn
    )
}

/**
 * Vault locked error state.
 */
@Composable
fun VaultLockedErrorState(
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorState(
        type = ErrorStateType.VAULT_LOCKED,
        modifier = modifier,
        onAction = onUnlock
    )
}

/**
 * Compact inline error state.
 */
@Composable
fun InlineErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ComponentSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(ComponentSpacing.sm))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(ComponentSpacing.sm))
            TextButton(onClick = onRetry) {
                Text(text = stringResource(Res.string.action_retry))
            }
        }
    }
}

/**
 * Section error state for partial errors.
 */
@Composable
fun SectionErrorState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(ComponentSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(ComponentSpacing.md))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(ComponentSpacing.xs))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(ComponentSpacing.md))
            OutlinedButton(onClick = onRetry) {
                Text(text = stringResource(Res.string.action_retry))
            }
        }
    }
}

/**
 * Error banner for top-of-screen errors.
 */
@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onAction: (() -> Unit)? = null,
    actionText: String? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier.padding(ComponentSpacing.md)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                if (onAction != null && actionText != null) {
                    TextButton(
                        onClick = onAction,
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(text = actionText)
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text(text = stringResource(Res.string.action_close))
                }
            }
        }
    }
}
