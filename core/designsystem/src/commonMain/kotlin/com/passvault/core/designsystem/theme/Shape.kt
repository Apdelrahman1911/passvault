package com.passvault.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * PassVault shape system.
 * Rounded corners for approachable, secure feel.
 */

internal val PassVaultShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * Extended shapes for specific components.
 */
object VaultShapes {
    /**
     * Cards and elevated surfaces - slightly rounded for security feel
     */
    val Card = RoundedCornerShape(20.dp)

    /**
     * Dialogs and modals - more pronounced rounding
     */
    val Dialog = RoundedCornerShape(28.dp)

    /**
     * Bottom sheets - top corners only
     */
    val BottomSheet = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )

    /**
     * Input fields - subtle rounding
     */
    val InputField = RoundedCornerShape(16.dp)

    /**
     * Buttons - medium rounding
     */
    val Button = RoundedCornerShape(16.dp)

    /**
     * Chips and tags - pill shape
     */
    val Chip = RoundedCornerShape(50)

    /**
     * Avatars and icon containers - circular
     */
    val Circle = RoundedCornerShape(50)

    /**
     * Full rounding for images
     */
    val Image = RoundedCornerShape(16.dp)

    /**
     * Menu and dropdown
     */
    val Menu = RoundedCornerShape(16.dp)

    /**
     * Snackbar - minimal rounding
     */
    val Snackbar = RoundedCornerShape(18.dp)

    /**
     * Tooltip - small rounding
     */
    val Tooltip = RoundedCornerShape(10.dp)

    /**
     * Navigation rail/drawer
     */
    val NavigationItem = RoundedCornerShape(18.dp)

    /**
     * Floating action button
     */
    val Fab = RoundedCornerShape(18.dp)

    /**
     * Extended FAB - pill shape
     */
    val ExtendedFab = RoundedCornerShape(50)
}
