package com.passvault.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Editorial vault palette.
 *
 * The palette is intentionally warm and mostly monochrome. Sage, ochre, and blush are reserved for semantic
 * information so security states remain immediately recognisable without making the product visually noisy.
 */

// Primary - ink
internal val PrimaryLight = Color(0xFF20211E)
internal val OnPrimaryLight = Color(0xFFFAF9F3)
internal val PrimaryContainerLight = Color(0xFFE5E8DE)
internal val OnPrimaryContainerLight = Color(0xFF20251D)

// Secondary - muted sage
internal val SecondaryLight = Color(0xFF596452)
internal val OnSecondaryLight = Color(0xFFFFFFFF)
internal val SecondaryContainerLight = Color(0xFFE1E8DA)
internal val OnSecondaryContainerLight = Color(0xFF1B2418)

// Tertiary - muted ochre
internal val TertiaryLight = Color(0xFF755B2C)
internal val OnTertiaryLight = Color(0xFFFFFFFF)
internal val TertiaryContainerLight = Color(0xFFF1E5CA)
internal val OnTertiaryContainerLight = Color(0xFF2B210E)

// Error - restrained red with blush container
internal val ErrorLight = Color(0xFFA33E45)
internal val OnErrorLight = Color(0xFFFFFFFF)
internal val ErrorContainerLight = Color(0xFFF5DFE0)
internal val OnErrorContainerLight = Color(0xFF3C1014)

// Success - readable olive green
internal val SuccessLight = Color(0xFF496442)
internal val OnSuccessLight = Color(0xFFFFFFFF)
internal val SuccessContainerLight = Color(0xFFDDE8D8)
internal val OnSuccessContainerLight = Color(0xFF142210)

// Warning - amber
internal val WarningLight = Color(0xFF805C1F)
internal val OnWarningLight = Color(0xFFFFFFFF)
internal val WarningContainerLight = Color(0xFFF3E3C4)
internal val OnWarningContainerLight = Color(0xFF2C1D08)

// Neutral - warm paper
internal val BackgroundLight = Color(0xFFF3F1EA)
internal val OnBackgroundLight = Color(0xFF20211E)
internal val SurfaceLight = Color(0xFFF8F7F1)
internal val OnSurfaceLight = Color(0xFF20211E)
internal val SurfaceVariantLight = Color(0xFFE9E7DF)
internal val OnSurfaceVariantLight = Color(0xFF5D5E58)
internal val OutlineLight = Color(0xFF777872)
internal val OutlineVariantLight = Color(0xFFD2D1C8)

// Inverse - floating navigation and feedback
internal val InverseSurfaceLight = Color(0xFF20211E)
internal val InverseOnSurfaceLight = Color(0xFFF7F5ED)
internal val InversePrimaryLight = Color(0xFFDDE4D4)

// Scrim - For dialogs/modals
internal val ScrimLight = Color(0x99000000)

// Surface containers
internal val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
internal val SurfaceContainerLowLight = Color(0xFFFAF9F4)
internal val SurfaceContainerLight = Color(0xFFF1EFE8)
internal val SurfaceContainerHighLight = Color(0xFFEAE8E0)
internal val SurfaceContainerHighestLight = Color(0xFFE3E1D9)

// ============================================
// DARK THEME
// ============================================

// Primary - warm paper on ink
internal val PrimaryDark = Color(0xFFF0EFE7)
internal val OnPrimaryDark = Color(0xFF22231F)
internal val PrimaryContainerDark = Color(0xFF3D4338)
internal val OnPrimaryContainerDark = Color(0xFFE6ECDE)

// Secondary - light sage
internal val SecondaryDark = Color(0xFFBECBB5)
internal val OnSecondaryDark = Color(0xFF293326)
internal val SecondaryContainerDark = Color(0xFF3D4938)
internal val OnSecondaryContainerDark = Color(0xFFDDE9D5)

// Tertiary - soft gold
internal val TertiaryDark = Color(0xFFE0C58D)
internal val OnTertiaryDark = Color(0xFF3B2E13)
internal val TertiaryContainerDark = Color(0xFF55451F)
internal val OnTertiaryContainerDark = Color(0xFFF5E1B7)

// Error
internal val ErrorDark = Color(0xFFFFB4B7)
internal val OnErrorDark = Color(0xFF65000A)
internal val ErrorContainerDark = Color(0xFF6C2E33)
internal val OnErrorContainerDark = Color(0xFFFFDADC)

// Success
internal val SuccessDark = Color(0xFFB9D1AF)
internal val OnSuccessDark = Color(0xFF263C21)
internal val SuccessContainerDark = Color(0xFF34472F)
internal val OnSuccessContainerDark = Color(0xFFD4EACB)

// Warning
internal val WarningDark = Color(0xFFE7C27F)
internal val OnWarningDark = Color(0xFF432E0A)
internal val WarningContainerDark = Color(0xFF59431E)
internal val OnWarningContainerDark = Color(0xFFFFE1A9)

// Neutral - pure-black canvas with near-black elevation layers
internal val BackgroundDark = Color(0xFF000000)
internal val OnBackgroundDark = Color(0xFFF0EFE7)
internal val SurfaceDark = Color(0xFF080908)
internal val OnSurfaceDark = Color(0xFFF0EFE7)
internal val SurfaceVariantDark = Color(0xFF20221F)
internal val OnSurfaceVariantDark = Color(0xFFC8C8C0)
internal val OutlineDark = Color(0xFF92938B)
internal val OutlineVariantDark = Color(0xFF343630)

// Inverse
internal val InverseSurfaceDark = Color(0xFFF0EFE7)
internal val InverseOnSurfaceDark = Color(0xFF252620)
internal val InversePrimaryDark = Color(0xFF4B5945)

// Scrim
internal val ScrimDark = Color(0xB3000000)

// Surface containers
internal val SurfaceContainerLowestDark = Color(0xFF000000)
internal val SurfaceContainerLowDark = Color(0xFF0A0B0A)
internal val SurfaceContainerDark = Color(0xFF101110)
internal val SurfaceContainerHighDark = Color(0xFF171817)
internal val SurfaceContainerHighestDark = Color(0xFF20211F)

// ============================================
// EXTENDED COLORS - For password strength and security states
// ============================================

object VaultKeeperColors {
    // Password strength colors keep explicit hue differences for non-text status indicators.
    val VeryWeak = Color(0xFFB83E46)
    val Weak = Color(0xFFB65B35)
    val Fair = Color(0xFF9A742B)
    val Good = Color(0xFF64764F)
    val Strong = Color(0xFF4C7247)
    val VeryStrong = Color(0xFF37683F)
    
    // Security status colors
    val Secure = Color(0xFF4C7247)
    val AtRisk = Color(0xFFB83E46)
    val Warning = Color(0xFF9A742B)
    val Info = Color(0xFF596452)
    
    // Password mask color
    val MaskCharacter = Color(0xFF8B8C85)
}
