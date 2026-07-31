package com.passvault.core.designsystem.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * PassVault elevation tokens.
 * Defines shadow depths for Material3 elevation levels.
 */

@Suppress("unused")
object Elevation {
    /**
     * Level 0 - No elevation (flat).
     * Used for: Background surfaces, base layers.
     */
    val level0 = 0.dp
    
    /**
     * Level 1 - Lowest elevation.
     * Used for: Subtle separation, hover states.
     */
    val level1 = 1.dp
    
    /**
     * Level 2 - Low elevation.
     * Used for: Navigation bars, tooltips.
     */
    val level2 = 3.dp
    
    /**
     * Level 3 - Medium-low elevation.
     * Used for: Cards at rest, chips.
     */
    val level3 = 6.dp
    
    /**
     * Level 4 - Medium elevation.
     * Used for: Elevated cards, FAB pressed state.
     */
    val level4 = 8.dp
    
    /**
     * Level 5 - High elevation.
     * Used for: Dropdown menus, dialogs.
     */
    val level5 = 12.dp
    
    /**
     * Level 6 - Very high elevation.
     * Used for: Modals, bottom sheets, side sheets.
     */
    val level6 = 16.dp
    
    /**
     * Level 7 - Maximum elevation.
     * Used for: Full-screen dialogs, critical alerts.
     */
    val level7 = 24.dp
}

/**
 * Component-specific elevation values.
 */
object ComponentElevation {
    /**
     * Card at rest - subtle elevation.
     */
    val cardRest = Elevation.level0
    
    /**
     * Card elevated - when hovered or focused.
     */
    val cardElevated = Elevation.level2
    
    /**
     * Card pressed - when pressed.
     */
    val cardPressed = Elevation.level1
    
    /**
     * Card dragged - when being dragged.
     */
    val cardDragged = Elevation.level5
    
    /**
     * FAB at rest.
     */
    val fabRest = Elevation.level2
    
    /**
     * FAB pressed.
     */
    val fabPressed = Elevation.level3
    
    /**
     * Navigation bar at rest.
     */
    val navigationBar = Elevation.level0
    
    /**
     * Navigation rail at rest.
     */
    val navigationRail = Elevation.level0
    
    /**
     * Navigation drawer at rest.
     */
    val navigationDrawer = Elevation.level0
    
    /**
     * Top app bar at rest.
     */
    val topAppBar = Elevation.level0
    
    /**
     * Top app bar scrolled.
     */
    val topAppBarScrolled = Elevation.level2
    
    /**
     * Bottom sheet at rest.
     */
    val bottomSheet = Elevation.level6
    
    /**
     * Dialog.
     */
    val dialog = Elevation.level6
    
    /**
     * Menu/dropdown.
     */
    val menu = Elevation.level5
    
    /**
     * Snackbar.
     */
    val snackbar = Elevation.level4
    
    /**
     * Tooltip.
     */
    val tooltip = Elevation.level2
    
    /**
     * Banner.
     */
    val banner = Elevation.level3
    
    /**
     * Side sheet.
     */
    val sideSheet = Elevation.level6
    
    /**
     * Modal scrim.
     */
    val modalScrim = Elevation.level6
    
    /**
     * Search bar at rest.
     */
    val searchBarRest = Elevation.level0
    
    /**
     * Search bar scrolled.
     */
    val searchBarScrolled = Elevation.level0
    
    /**
     * Badge.
     */
    val badge = Elevation.level2
    
    /**
     * Progress indicator.
     */
    val progressIndicator = Elevation.level0
    
    /**
     * Divider.
     */
    val divider = Elevation.level0
}
