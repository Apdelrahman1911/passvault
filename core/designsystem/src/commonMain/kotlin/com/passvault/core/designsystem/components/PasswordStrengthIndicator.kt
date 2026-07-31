package com.passvault.core.designsystem.components

import com.passvault.core.designsystem.generated.resources.Res
import com.passvault.core.designsystem.generated.resources.*
import org.jetbrains.compose.resources.stringResource

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.theme.VaultKeeperColors
import com.passvault.core.designsystem.tokens.MotionDuration

/**
 * Password strength level for evaluation.
 */
enum class PasswordStrengthLevel(
    val labelResId: String,
    val color: Color,
    val progress: Float
) {
    UNKNOWN("password_strength_unknown", VaultKeeperColors.MaskCharacter, 0f),
    VERY_WEAK("password_strength_very_weak", VaultKeeperColors.VeryWeak, 0.16f),
    WEAK("password_strength_weak", VaultKeeperColors.Weak, 0.33f),
    FAIR("password_strength_fair", VaultKeeperColors.Fair, 0.50f),
    GOOD("password_strength_good", VaultKeeperColors.Good, 0.66f),
    STRONG("password_strength_strong", VaultKeeperColors.Strong, 0.83f),
    VERY_STRONG("password_strength_very_strong", VaultKeeperColors.VeryStrong, 1f)
}

/**
 * Horizontal password strength indicator with segmented bars.
 */
@Composable
fun PasswordStrengthIndicator(
    strength: PasswordStrengthLevel,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
    showTips: Boolean = false
) {
    val strengthLabel = when (strength) {
        PasswordStrengthLevel.UNKNOWN -> stringResource(Res.string.password_strength_unknown)
        PasswordStrengthLevel.VERY_WEAK -> stringResource(Res.string.password_strength_very_weak)
        PasswordStrengthLevel.WEAK -> stringResource(Res.string.password_strength_weak)
        PasswordStrengthLevel.FAIR -> stringResource(Res.string.password_strength_fair)
        PasswordStrengthLevel.GOOD -> stringResource(Res.string.password_strength_good)
        PasswordStrengthLevel.STRONG -> stringResource(Res.string.password_strength_strong)
        PasswordStrengthLevel.VERY_STRONG -> stringResource(Res.string.password_strength_very_strong)
    }
    val animatedColor by animateColorAsState(
        targetValue = strength.color,
        animationSpec = tween(durationMillis = MotionDuration.long.inWholeMilliseconds.toInt()),
        label = "StrengthColor"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = strength.progress,
        animationSpec = tween(durationMillis = MotionDuration.long.inWholeMilliseconds.toInt()),
        label = "StrengthProgress"
    )

    Box(modifier = modifier) {
        if (showLabel) {
            PasswordStrengthIndicatorWithColorAndLabel(
                strength = strength,
                color = animatedColor,
                progress = animatedProgress,
                strengthLabel = strengthLabel,
                showTips = showTips
            )
        } else {
            CompactPasswordStrengthIndicator(
                color = animatedColor,
                progress = animatedProgress,
                strength = strength
            )
        }
    }
}

/**
 * Password strength indicator with color, label, and tips.
 */
@Composable
private fun PasswordStrengthIndicatorWithColorAndLabel(
    strength: PasswordStrengthLevel,
    color: Color,
    progress: Float,
    strengthLabel: String,
    showTips: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val segments = 6
            val segmentProgress = progress.coerceIn(0f, 1f)
            val fullSegments = (segments * segmentProgress).toInt()
            val partialSegment = (segments * segmentProgress) - fullSegments

            repeat(segments) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < fullSegments) {
                                color
                            } else if (index == fullSegments) {
                                color.copy(
                                    alpha = partialSegment.coerceIn(0f, 1f)
                                )
                            } else {
                                color.copy(alpha = 0.3f)
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = strengthLabel,
            style = MaterialTheme.typography.labelMedium,
            color = color
        )

        if (showTips) {
            PasswordTips(passwordStrength = strength)
        }
    }
}

/**
 * Compact password strength indicator with color.
 */
@Composable
private fun CompactPasswordStrengthIndicator(
    color: Color,
    progress: Float,
    strength: PasswordStrengthLevel
) {
    Box(
        modifier = Modifier
            .width(48.dp)
            .height(8.dp)
            .clip(CircleShape)
            .background(color)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(color)
        )
    }
}

/**
 * Password tips composable based on password strength.
 */
@Composable
private fun PasswordTips(passwordStrength: PasswordStrengthLevel) {
    val tips = when (passwordStrength) {
        PasswordStrengthLevel.UNKNOWN -> emptyList()
        PasswordStrengthLevel.VERY_WEAK -> listOf(
            stringResource(Res.string.passwordTips_very_weak_1),
            stringResource(Res.string.passwordTips_very_weak_2),
            stringResource(Res.string.passwordTips_very_weak_3)
        )
        PasswordStrengthLevel.WEAK -> listOf(
            stringResource(Res.string.passwordTips_weak_1),
            stringResource(Res.string.passwordTips_weak_2),
            stringResource(Res.string.passwordTips_weak_3)
        )
        PasswordStrengthLevel.FAIR -> listOf(
            stringResource(Res.string.passwordTips_fair_1),
            stringResource(Res.string.passwordTips_fair_2),
            stringResource(Res.string.passwordTips_fair_3)
        )
        PasswordStrengthLevel.GOOD -> listOf(
            stringResource(Res.string.passwordTips_good_1),
            stringResource(Res.string.passwordTips_good_2)
        )
        PasswordStrengthLevel.STRONG -> listOf()
        PasswordStrengthLevel.VERY_STRONG -> listOf()
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        tips.forEach { tip ->
            Text(
                text = tip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Circular password strength indicator.
 */
@Composable
fun CircularPasswordStrengthIndicator(
    strength: PasswordStrengthLevel,
    modifier: Modifier = Modifier,
    showDetailed: Boolean = false
) {
    val progress by animateFloatAsState(
        targetValue = strength.progress,
        animationSpec = tween(durationMillis = MotionDuration.long.inWholeMilliseconds.toInt()),
        label = "CircularStrengthProgress"
    )
    val color by animateColorAsState(
        targetValue = strength.color,
        animationSpec = tween(durationMillis = MotionDuration.long.inWholeMilliseconds.toInt()),
        label = "CircularStrengthColor"
    )

    Box(modifier = modifier) {
        LinearProgressIndicator(
            progress = { progress },
            color = color,
            modifier = Modifier.fillMaxWidth()
        )

        if (showDetailed) {
            val score = (strength.progress * 100).toInt()
            Text(
                text = stringResource(Res.string.detailed_password_score, score),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
