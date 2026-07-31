package com.passvault.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import com.passvault.core.designsystem.theme.VaultShapes
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.MotionDuration
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
/**
 * Result states for copy operations.
 */
sealed class CopyResult {
    data object Idle : CopyResult()
    data object Copying : CopyResult()
    data object Success : CopyResult()
    data object Error : CopyResult()
}

/**
 * A secure copy button with visual feedback.
 *
 * @param textToCopy The text to copy when clicked.
 * @param modifier Modifier to be applied.
 * @param variant Button variant style.
 * @param contentDescription Optional content description for accessibility.
 * @param onCopy Callback after copy (receives success boolean).
 */
@Composable
fun CopyButton(
    onCopyAction: () -> Boolean,
    modifier: Modifier = Modifier,
    variant: CopyButtonVariant = CopyButtonVariant.DEFAULT,
    contentDescription: String? = null,
    onCopy: (Boolean) -> Unit = {}
) {
    var copyState by remember { mutableStateOf<CopyResult>(CopyResult.Idle) }

    // Auto-reset after success/error
    LaunchedEffect(copyState) {
        when (copyState) {
            is CopyResult.Success, is CopyResult.Error -> {
                delay(2000)
                copyState = CopyResult.Idle
            }
            else -> { /* No action needed */ }
        }
    }

    val copyAction = {
        copyState = CopyResult.Copying
        val copied = runCatching { onCopyAction() }.getOrDefault(false)
        if (copied) {
            copyState = CopyResult.Success
            onCopy.invoke(true)
        } else {
            copyState = CopyResult.Error
            onCopy.invoke(false)
        }
    }

    val semanticDescription = contentDescription ?: stringResource(Res.string.cd_copy_button)
    val stateDescription = when (copyState) {
        is CopyResult.Copying -> stringResource(Res.string.ui_copying)
        is CopyResult.Success -> stringResource(Res.string.action_copy_success)
        is CopyResult.Error -> stringResource(Res.string.action_copy_error)
        else -> stringResource(Res.string.ui_ready_to_copy)
    }

    when (variant) {
        CopyButtonVariant.DEFAULT -> {
            IconButton(
                onClick = copyAction,
                modifier = modifier
                    .size(ComponentSpacing.iconButtonMinSize)
                    .semantics {
                        this.contentDescription = semanticDescription
                        this.stateDescription = stateDescription
                    },
                enabled = copyState !is CopyResult.Copying
            ) {
                CopyButtonIcon(copyState = copyState)
            }
        }

        CopyButtonVariant.FILLED -> {
            FilledIconButton(
                onClick = copyAction,
                modifier = modifier
                    .size(ComponentSpacing.iconButtonMinSize)
                    .semantics {
                        this.contentDescription = semanticDescription
                        this.stateDescription = stateDescription
                    },
                enabled = copyState !is CopyResult.Copying
            ) {
                CopyButtonIcon(copyState = copyState)
            }
        }

        CopyButtonVariant.OUTLINED -> {
            OutlinedIconButton(
                onClick = copyAction,
                modifier = modifier
                    .size(ComponentSpacing.iconButtonMinSize)
                    .semantics {
                        this.contentDescription = semanticDescription
                        this.stateDescription = stateDescription
                    },
                enabled = copyState !is CopyResult.Copying
            ) {
                CopyButtonIcon(copyState = copyState)
            }
        }

        CopyButtonVariant.COMPACT -> {
            IconButton(
                onClick = copyAction,
                modifier = modifier
                    .size(32.dp)
                    .semantics {
                        this.contentDescription = semanticDescription
                        this.stateDescription = stateDescription
                    },
                enabled = copyState !is CopyResult.Copying
            ) {
                CopyButtonIcon(
                    copyState = copyState,
                    compact = true
                )
            }
        }
    }
}

/**
 * Icon for copy button with state-based animation.
 */
@Composable
private fun CopyButtonIcon(
    copyState: CopyResult,
    compact: Boolean = false
) {
    val iconSize = if (compact) 16.dp else 24.dp

    AnimatedContent(
        targetState = copyState,
        transitionSpec = {
            fadeIn(tween(150)) + scaleIn(tween(200), initialScale = 0.5f) togetherWith
                    fadeOut(tween(100)) + scaleOut(tween(150), targetScale = 0.5f)
        },
        label = "CopyButtonIcon"
    ) { state ->
        when (state) {
            is CopyResult.Copying -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(iconSize),
                    strokeWidth = 2.dp
                )
            }

            is CopyResult.Success -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            is CopyResult.Error -> {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

/**
 * A copy button with label for better UX.
 *
 * @param textToCopy The text to copy.
 * @param label Label to display.
 * @param modifier Modifier to be applied.
 * @param onCopy Callback (receives success boolean).
 */
@Composable
fun CopyButtonWithLabel(
    onCopyAction: () -> Boolean,
    label: String,
    modifier: Modifier = Modifier,
    onCopy: (Boolean) -> Unit = {}
) {
    var isSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            delay(2000)
            isSuccess = false
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isSuccess) 1.1f else 1f,
        animationSpec = tween(MotionDuration.fast.inWholeMilliseconds.toInt()),
        label = "CopyButtonScale"
    )

    val backgroundColor = when {
        isSuccess -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }

    val contentColor = when {
        isSuccess -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .clip(VaultShapes.Button)
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = {
                    val copied = runCatching { onCopyAction() }.getOrDefault(false)
                    if (copied) {
                        isSuccess = true
                        onCopy.invoke(true)
                    } else {
                        onCopy.invoke(false)
                    }
                }
            )
            .padding(
                horizontal = ComponentSpacing.md,
                vertical = ComponentSpacing.sm
            )
            .scale(scale),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)
    ) {
        AnimatedContent(
            targetState = isSuccess,
            transitionSpec = {
                fadeIn(tween(150)) togetherWith fadeOut(tween(100))
            },
            label = "CopyLabelIcon"
        ) { success ->
            if (success) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    tint = contentColor
                )
            }
        }
        Text(
            text = if (isSuccess) stringResource(Res.string.action_copy_success) else label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

/**
 * Secure copy button with shield icon for sensitive data.
 *
 * @param textToCopy The text to copy.
 * @param modifier Modifier to be applied.
 * @param onCopy Callback (receives success boolean).
 */
@Composable
fun SecureCopyButton(
    onCopyAction: () -> Boolean,
    modifier: Modifier = Modifier,
    onCopy: (Boolean) -> Unit = {}
) {
    var copyState by remember { mutableStateOf<CopyResult>(CopyResult.Idle) }

    LaunchedEffect(copyState) {
        when (copyState) {
            is CopyResult.Success, is CopyResult.Error -> {
                delay(3000) // Longer display for secure items
                copyState = CopyResult.Idle
            }
            else -> { /* No action needed */ }
        }
    }

    val icon = when (copyState) {
        is CopyResult.Copying -> null // Shows progress
        is CopyResult.Success -> Icons.Outlined.Verified
        is CopyResult.Error -> Icons.Outlined.Lock
        else -> Icons.Outlined.Shield
    }

    val colors = when (copyState) {
        is CopyResult.Success -> IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary
        )
        is CopyResult.Error -> IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error
        )
        else -> IconButtonDefaults.filledIconButtonColors()
    }
    val secureCopyDescription = stringResource(Res.string.ui_secure_copy)

    FilledIconButton(
        onClick = {
            copyState = CopyResult.Copying
            val copied = runCatching { onCopyAction() }.getOrDefault(false)
            if (copied) {
                copyState = CopyResult.Success
                onCopy.invoke(true)
            } else {
                copyState = CopyResult.Error
                onCopy.invoke(false)
            }
        },
        modifier = modifier
            .size(ComponentSpacing.iconButtonMinSize)
            .semantics {
                contentDescription = secureCopyDescription
            },
        colors = colors,
        enabled = copyState !is CopyResult.Copying
    ) {
        when (copyState) {
            is CopyResult.Copying -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
            else -> {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

/**
 * Copy button variants.
 */
enum class CopyButtonVariant {
    DEFAULT,
    FILLED,
    OUTLINED,
    COMPACT
}





