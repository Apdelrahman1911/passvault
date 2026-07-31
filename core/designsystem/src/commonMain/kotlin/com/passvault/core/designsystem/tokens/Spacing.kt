package com.passvault.core.designsystem.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * PassVault spacing tokens.
 * Based on 4dp grid system for consistent spacing throughout the app.
 */

@Suppress("unused")
object Spacing {
    /**
     * Zero spacing.
     */
    val none = 0.dp

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

    /**
     * 40dp - 2x Extra large spacing.
     * Used for major content sections.
     */
    val xl2 = 40.dp

    /**
     * 48dp - 3x Extra large spacing.
     * Used for page-level spacing.
     */
    val xl3 = 48.dp

    /**
     * 64dp - 4x Extra large spacing.
     * Used for large page sections.
     */
    val xl4 = 64.dp

    /**
     * 80dp - 5x Extra large spacing.
     * Used for very large sections.
     */
    val xl5 = 80.dp

    /**
     * 96dp - 6x Extra large spacing.
     * Maximum standard spacing.
     */
    val xl6 = 96.dp
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
     * Button horizontal padding - standard button internal padding.
     */
    val buttonHorizontal = Spacing.lg

    /**
     * Button vertical padding - standard button internal padding.
     */
    val buttonVertical = Spacing.smMd

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
     * Form field spacing - space between form fields.
     */
    val formFieldSpacing = Spacing.md

    /**
     * Dialog padding - internal padding for dialogs.
     */
    val dialogPadding = Spacing.lg

    /**
     * Dialog content spacing - space between dialog elements.
     */
    val dialogContentSpacing = Spacing.md

    /**
     * Icon button padding - padding for icon-only buttons.
     */
    val iconButtonPadding = Spacing.sm

    /**
     * Chip spacing - space between chips.
     */
    val chipSpacing = Spacing.sm

    /**
     * Chip padding - internal chip padding.
     */
    val chipPadding = Spacing.sm

    /**
     * Top bar padding - top bar content padding.
     */
    val topBarPadding = Spacing.md

    /**
     * Bottom bar padding - bottom bar content padding.
     */
    val bottomBarPadding = Spacing.md

    /**
     * Navigation rail width.
     */
    val navigationRailWidth = 80.dp

    /**
     * Navigation drawer width.
     */
    val navigationDrawerWidth = 280.dp

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

    /**
     * Minimum icon button size.
     */
    val iconButtonMinSize = 48.dp
}

/**
 * Screen size breakpoints for responsive layouts.
 */
object Breakpoints {
    /**
     * Compact width (phone portrait) - less than 600dp.
     */
    val compactMax = 599.dp

    /**
     * Medium width (tablet portrait, phone landscape) - 600dp to 839dp.
     */
    val mediumMin = 600.dp
    val mediumMax = 839.dp

    /**
     * Expanded width (tablet landscape, desktop) - 840dp and up.
     */
    val expandedMin = 840.dp
}
