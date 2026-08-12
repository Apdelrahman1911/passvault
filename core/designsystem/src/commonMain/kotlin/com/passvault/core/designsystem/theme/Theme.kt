package com.passvault.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.passvault.core.designsystem.platform.ConfigureSystemBarAppearance

/** Curated primary-color families with contrast-safe light and dark roles. */
enum class PassVaultAccent {
    NEUTRAL,
    SAGE,
    BLUE,
    PURPLE,
    ROSE,
    AMBER,
}

private data class AccentPalette(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val inversePrimary: Color,
)

/**
 * Extended color scheme with PassVault-specific semantic colors.
 */
data class ExtendedColorScheme(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val scrim: Color,
)

/**
 * Light theme extended colors.
 */
internal val LightExtendedColors = ExtendedColorScheme(
    success = SuccessLight,
    onSuccess = OnSuccessLight,
    successContainer = SuccessContainerLight,
    onSuccessContainer = OnSuccessContainerLight,
    warning = WarningLight,
    onWarning = OnWarningLight,
    warningContainer = WarningContainerLight,
    onWarningContainer = OnWarningContainerLight,
    scrim = ScrimLight,
)

/**
 * Dark theme extended colors.
 */
internal val DarkExtendedColors = ExtendedColorScheme(
    success = SuccessDark,
    onSuccess = OnSuccessDark,
    successContainer = SuccessContainerDark,
    onSuccessContainer = OnSuccessContainerDark,
    warning = WarningDark,
    onWarning = OnWarningDark,
    warningContainer = WarningContainerDark,
    onWarningContainer = OnWarningContainerDark,
    scrim = ScrimDark,
)

/**
 * Light color scheme for PassVault.
 */
internal val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
)

/**
 * Dark color scheme for PassVault.
 */
internal val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
)

private val LightAccentPalettes = mapOf(
    PassVaultAccent.NEUTRAL to AccentPalette(
        PrimaryLight,
        OnPrimaryLight,
        PrimaryContainerLight,
        OnPrimaryContainerLight,
        InversePrimaryLight,
    ),
    PassVaultAccent.SAGE to AccentPalette(
        primary = Color(0xFF496442),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDDE8D8),
        onPrimaryContainer = Color(0xFF142210),
        inversePrimary = Color(0xFFB9D1AF),
    ),
    PassVaultAccent.BLUE to AccentPalette(
        primary = Color(0xFF3D5F8F),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDCE7F7),
        onPrimaryContainer = Color(0xFF13233A),
        inversePrimary = Color(0xFFAFC9F0),
    ),
    PassVaultAccent.PURPLE to AccentPalette(
        primary = Color(0xFF67507D),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEADFF2),
        onPrimaryContainer = Color(0xFF291737),
        inversePrimary = Color(0xFFD9BCEB),
    ),
    PassVaultAccent.ROSE to AccentPalette(
        primary = Color(0xFF8B465A),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFF2DEE4),
        onPrimaryContainer = Color(0xFF35121D),
        inversePrimary = Color(0xFFF0B8C8),
    ),
    PassVaultAccent.AMBER to AccentPalette(
        primary = Color(0xFF755B2C),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFF1E5CA),
        onPrimaryContainer = Color(0xFF2B210E),
        inversePrimary = Color(0xFFE0C58D),
    ),
)

private val DarkAccentPalettes = mapOf(
    PassVaultAccent.NEUTRAL to AccentPalette(
        PrimaryDark,
        OnPrimaryDark,
        PrimaryContainerDark,
        OnPrimaryContainerDark,
        InversePrimaryDark,
    ),
    PassVaultAccent.SAGE to AccentPalette(
            primary = Color(0xFFB9D1AF),
            onPrimary = Color(0xFF263C21),
            primaryContainer = Color(0xFF34472F),
            onPrimaryContainer = Color(0xFFD4EACB),
            inversePrimary = Color(0xFF496442),
    ),
    PassVaultAccent.BLUE to AccentPalette(
            primary = Color(0xFFAFC9F0),
            onPrimary = Color(0xFF19304E),
            primaryContainer = Color(0xFF243B5C),
            onPrimaryContainer = Color(0xFFDCE8FA),
            inversePrimary = Color(0xFF496A98),
    ),
    PassVaultAccent.PURPLE to AccentPalette(
            primary = Color(0xFFD9BCEB),
            onPrimary = Color(0xFF382247),
            primaryContainer = Color(0xFF4A3258),
            onPrimaryContainer = Color(0xFFF1DCF9),
            inversePrimary = Color(0xFF73558B),
    ),
    PassVaultAccent.ROSE to AccentPalette(
            primary = Color(0xFFF0B8C8),
            onPrimary = Color(0xFF4B1F2B),
            primaryContainer = Color(0xFF5C303C),
            onPrimaryContainer = Color(0xFFFFD9E3),
            inversePrimary = Color(0xFF945166),
    ),
    PassVaultAccent.AMBER to AccentPalette(
            primary = Color(0xFFE0C58D),
            onPrimary = Color(0xFF3B2E13),
            primaryContainer = Color(0xFF55451F),
            onPrimaryContainer = Color(0xFFF5E1B7),
            inversePrimary = Color(0xFF755B2C),
    ),
)

private fun PassVaultAccent.palette(darkTheme: Boolean): AccentPalette =
    if (darkTheme) DarkAccentPalettes.getValue(this) else LightAccentPalettes.getValue(this)

/** Representative swatch for an accent in the requested brightness mode. */
fun PassVaultAccent.previewColor(darkTheme: Boolean): Color = palette(darkTheme).primary

internal fun colorSchemeFor(
    darkTheme: Boolean,
    accent: PassVaultAccent,
): ColorScheme {
    val base = if (darkTheme) DarkColorScheme else LightColorScheme
    val palette = accent.palette(darkTheme)
    return base.copy(
        primary = palette.primary,
        onPrimary = palette.onPrimary,
        primaryContainer = palette.primaryContainer,
        onPrimaryContainer = palette.onPrimaryContainer,
        inversePrimary = palette.inversePrimary,
    )
}

/**
 * CompositionLocal for accessing extended colors.
 */
val LocalExtendedColors = staticCompositionLocalOf {
    LightExtendedColors
}

/**
 * MaterialTheme extension providing PassVault extended colors.
 */
val MaterialTheme.extendedColors: ExtendedColorScheme
    @Composable
    get() = LocalExtendedColors.current

/**
 * Main PassVault theme composable.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param content The content to be themed.
 */
@Composable
fun PassVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accent: PassVaultAccent = PassVaultAccent.NEUTRAL,
    content: @Composable () -> Unit
) {
    ConfigureSystemBarAppearance(darkTheme)
    val colorScheme = colorSchemeFor(darkTheme, accent)
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PassVaultTypography,
            shapes = PassVaultShapes,
            content = content
        )
    }
}

/**
 * Preview-friendly theme that allows explicit dark/light toggle.
 *
 * @param useDarkTheme Whether to force dark theme.
 * @param content The content to be themed.
 */
@Composable
fun PassVaultPreviewTheme(
    useDarkTheme: Boolean = false,
    accent: PassVaultAccent = PassVaultAccent.NEUTRAL,
    content: @Composable () -> Unit
) {
    PassVaultTheme(darkTheme = useDarkTheme, accent = accent, content = content)
}
