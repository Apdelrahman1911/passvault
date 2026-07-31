package com.passvault.core.designsystem.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.action_add
import org.jetbrains.compose.resources.stringResource

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.theme.VaultShapes
import com.passvault.core.designsystem.tokens.ComponentSpacing
import com.passvault.core.designsystem.tokens.MotionDuration

/**
 * Button state for async operations.
 */
sealed class ButtonState {
    data object Idle : ButtonState()
    data object Loading : ButtonState()
    data object Success : ButtonState()
    data object Error : ButtonState()
}

/**
 * Primary action button with loading state support.
 *
 * @param text The button text.
 * @param onClick Callback when clicked.
 * @param modifier Modifier to be applied.
 * @param state Current button state.
 * @param enabled Whether the button is enabled.
 * @param icon Optional leading icon.
 * @param shape Button shape.
 */
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    state: ButtonState = ButtonState.Idle,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    shape: Shape = VaultShapes.Button
) {
    val isLoading = state is ButtonState.Loading
    val isEnabled = enabled && !isLoading

    Button(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = text
        },
        enabled = isEnabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(
            horizontal = ComponentSpacing.buttonHorizontal,
            vertical = ComponentSpacing.buttonVertical
        )
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(tween(150)) + scaleIn(tween(200), initialScale = 0.8f) togetherWith
                        fadeOut(tween(100)) + scaleOut(tween(150), targetScale = 0.8f)
            },
            label = "ButtonContent"
        ) { currentState ->
            when (currentState) {
                is ButtonState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                is ButtonState.Success -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                is ButtonState.Error -> {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)
                    ) {
                        icon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(text = text)
                    }
                }
            }
        }
    }
}

/**
 * Secondary action button (filled tonal).
 *
 * @param text The button text.
 * @param onClick Callback when clicked.
 * @param modifier Modifier to be applied.
 * @param enabled Whether the button is enabled.
 * @param icon Optional leading icon.
 * @param shape Button shape.
 */
@Composable
fun SecondaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    shape: Shape = VaultShapes.Button
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = text
        },
        enabled = enabled,
        shape = shape,
        contentPadding = PaddingValues(
            horizontal = ComponentSpacing.buttonHorizontal,
            vertical = ComponentSpacing.buttonVertical
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(text = text)
        }
    }
}

/**
 * Outlined action button.
 *
 * @param text The button text.
 * @param onClick Callback when clicked.
 * @param modifier Modifier to be applied.
 * @param enabled Whether the button is enabled.
 * @param icon Optional leading icon.
 * @param shape Button shape.
 */
@Composable
fun OutlinedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    shape: Shape = VaultShapes.Button
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = text
        },
        enabled = enabled,
        shape = shape,
        contentPadding = PaddingValues(
            horizontal = ComponentSpacing.buttonHorizontal,
            vertical = ComponentSpacing.buttonVertical
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(text = text)
        }
    }
}

/**
 * Text action button (lowest emphasis).
 *
 * @param text The button text.
 * @param onClick Callback when clicked.
 * @param modifier Modifier to be applied.
 * @param enabled Whether the button is enabled.
 * @param icon Optional leading icon.
 */
@Composable
fun TextActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = text
        },
        enabled = enabled
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(text = text)
        }
    }
}

/**
 * Destructive action button (error/red themed).
 *
 * @param text The button text.
 * @param onClick Callback when clicked.
 * @param modifier Modifier to be applied.
 * @param enabled Whether the button is enabled.
 * @param icon Optional leading icon.
 * @param shape Button shape.
 * @param state Current button state.
 */
@Composable
fun DestructiveActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    shape: Shape = VaultShapes.Button,
    state: ButtonState = ButtonState.Idle
) {
    val isLoading = state is ButtonState.Loading
    val isEnabled = enabled && !isLoading

    Button(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = text
        },
        enabled = isEnabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(
            horizontal = ComponentSpacing.buttonHorizontal,
            vertical = ComponentSpacing.buttonVertical
        )
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(tween(150)) + scaleIn(tween(200), initialScale = 0.8f) togetherWith
                        fadeOut(tween(100)) + scaleOut(tween(150), targetScale = 0.8f)
            },
            label = "DestructiveButtonContent"
        ) { currentState ->
            when (currentState) {
                is ButtonState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)
                    ) {
                        icon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(text = text)
                    }
                }
            }
        }
    }
}

/**
 * Floating action button style primary action.
 *
 * @param onClick Callback when clicked.
 * @param modifier Modifier to be applied.
 * @param enabled Whether the button is enabled.
 * @param icon The FAB icon.
 * @param contentDescription Content description for accessibility.
 */
@Composable
fun VaultFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector = Icons.Default.Add,
    contentDescription: String? = null,
) {
    val resolvedContentDescription =
        contentDescription ?: stringResource(Res.string.action_add)
    androidx.compose.material3.FloatingActionButton(
        onClick = if (enabled) onClick else ({}),
        modifier = modifier.semantics {
            this.contentDescription = resolvedContentDescription
        },
        shape = VaultShapes.Fab
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )
    }
}

/**
 * Extended FAB with text and icon.
 *
 * @param text The button text.
 * @param onClick Callback when clicked.
 * @param modifier Modifier to be applied.
 * @param enabled Whether the button is enabled.
 * @param icon The FAB icon.
 */
@Composable
fun VaultExtendedFloatingActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector = Icons.Default.Add
) {
    androidx.compose.material3.ExtendedFloatingActionButton(
        onClick = if (enabled) onClick else ({}),
        modifier = modifier.semantics {
            contentDescription = text
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        text = { Text(text = text) },
        shape = VaultShapes.ExtendedFab
    )
}

/**
 * Button group for related actions.
 *
 * @param primaryText Primary action text.
 * @param onPrimaryClick Primary action callback.
 * @param secondaryText Secondary action text.
 * @param onSecondaryClick Secondary action callback.
 * @param modifier Modifier to be applied.
 * @param isLoading Whether primary action is loading.
 */
@Composable
fun ActionButtonGroup(
    primaryText: String,
    onPrimaryClick: () -> Unit,
    secondaryText: String,
    onSecondaryClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ComponentSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedActionButton(
            text = secondaryText,
            onClick = onSecondaryClick,
            enabled = !isLoading
        )
        PrimaryActionButton(
            text = primaryText,
            onClick = onPrimaryClick,
            state = if (isLoading) ButtonState.Loading else ButtonState.Idle
        )
    }
}
