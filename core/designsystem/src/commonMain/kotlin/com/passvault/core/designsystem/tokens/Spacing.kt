package com.passvault.core.designsystem.tokens

import androidx.compose.ui.unit.dp

/**
 * PassVault spacing tokens.
 * Based on 4dp grid system for consistent spacing throughout the app.
 */

object Spacing {
    /**
     * 4dp - Extra small spacing.
     * Used for tight gaps between related elements.
     */
    val xs = 4.dp

    /**
     * 8dp - Small spacing.
     * Used for spacing between related elements like icon + text.
     */
    val sm = 8.dp

    /**
     * 12dp - Small-medium spacing.
     * Used for inner padding of compact components.
     */
    val smMd = 12.dp

    /**
     * 16dp - Medium spacing.
     * Standard padding for cards and content areas.
     */
    val md = 16.dp

    /**
     * 20dp - Medium-large spacing.
     * Used for slightly larger gaps.
     */
    val mdLg = 20.dp

    /**
     * 24dp - Large spacing.
     * Used for section spacing and card margins.
     */
    val lg = 24.dp

    /**
     * 32dp - Extra large spacing.
     * Used for major section dividers.
     */
    val xl = 32.dp
}

/**
 * Component-specific spacing values.
 */
object ComponentSpacing {
    val xs = Spacing.xs
    val sm = Spacing.sm
    val md = Spacing.md
    val lg = Spacing.lg
    val xl = Spacing.xl

    /**
     * Card padding - standard internal padding for cards.
     */
    val cardPadding = Spacing.mdLg

    /**
     * Card content spacing - space between elements inside a card.
     */
    val cardContentSpacing = Spacing.sm

    /**
     * List item spacing - vertical space between list items.
     */
    val listItemSpacing = Spacing.smMd

    /**
     * List item padding - internal padding of list items.
     */
    val listItemPadding = Spacing.mdLg

    /**
     * Screen horizontal padding - standard screen edge padding.
     */
    val screenHorizontal = Spacing.mdLg

    /**
     * Screen vertical padding - standard screen top/bottom padding.
     */
    val screenVertical = Spacing.mdLg

    /**
     * Section spacing - space between major sections.
     */
    val sectionSpacing = Spacing.lg

    /**
     * Maximum readable width for forms and narrative content.
     */
    val formMaxWidth = 680.dp

    /**
     * Maximum width for primary application content on expanded windows.
     */
    val contentMaxWidth = 1120.dp

    /**
     * Touch target minimum size (accessibility).
     */
    val touchTargetMin = 48.dp
}

/**
 * Screen size breakpoints for responsive layouts.
 */
object Breakpoints {
    /**
     * Medium width (tablet portrait, phone landscape) begins at 600dp.
     */
    val mediumMin = 600.dp

    /**
     * Expanded width (tablet landscape, desktop) - 840dp and up.
     */
    val expandedMin = 840.dp
}
