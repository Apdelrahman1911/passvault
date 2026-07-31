package com.passvault.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.passvault.core.designsystem.theme.VaultShapes
import com.passvault.core.designsystem.tokens.ComponentElevation
import com.passvault.core.designsystem.tokens.ComponentSpacing

/**
 * Shimmer line placeholder.
 *
 * @param width Width fraction (0.0 to 1.0).
 * @param modifier Modifier to be applied.
 * @param height Height of the line.
 */
@Composable
fun ShimmerLine(
    width: Float = 1.0f,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 12.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ShimmerPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        ),
        label = "ShimmerAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth(width)
            .height(height)
            .alpha(alpha)
            .clip(MaterialTheme.shapes.small),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {}
}

/**
 * Card loading placeholder for shimmer effect.
 *
 * @param modifier Modifier to be applied.
 * @param lines Number of placeholder lines.
 */
@Composable
fun CardLoadingPlaceholder(
    modifier: Modifier = Modifier,
    lines: Int = 3
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = ComponentSpacing.md, vertical = ComponentSpacing.sm),
        shape = VaultShapes.Card,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = ComponentElevation.cardRest
        ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(ComponentSpacing.md),
            verticalArrangement = Arrangement.spacedBy(ComponentSpacing.sm)
        ) {
            // Title placeholder
            ShimmerLine(width = 0.6f)

            // Content lines
            repeat(lines) {
                ShimmerLine(width = 0.4f)
            }
        }
    }
}
