package com.elysium.softwork.shared.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * Material 3 [ColorScheme] derived from the SoftWork brand palette. Phase 1 ships a single
 * light scheme; a dark variant will be introduced when the design team signs off on values.
 */
private val SoftWorkLightColorScheme: ColorScheme = lightColorScheme(
    primary = PrimarySky,
    onPrimary = Color.White,
    primaryContainer = AccentMint,
    onPrimaryContainer = PrimaryNavy,
    secondary = PrimaryTeal,
    onSecondary = Color.White,
    secondaryContainer = AccentMint,
    onSecondaryContainer = PrimaryNavy,
    tertiary = PrimaryNavy,
    onTertiary = Color.White,
    background = Color.White,
    onBackground = AccentDark,
    surface = Color.White,
    onSurface = AccentDark,
    surfaceVariant = AccentWhite,
    onSurfaceVariant = AccentDark,
    error = Danger,
    onError = Color.White,
)

/**
 * Root theme for every SoftWork screen. Wraps [MaterialTheme] with the brand
 * [ColorScheme], [SoftWorkTypography], and [SoftWorkShapes], and exposes the semantic
 * [SoftWorkColors] via [LocalSoftWorkColors].
 *
 * @param darkTheme reserved for a future dark variant; currently always renders the light scheme.
 * @param content composable subtree that should consume the SoftWork design tokens.
 *
 * Example:
 * ```
 * setContent {
 *     SoftWorkTheme {
 *         AppNavGraph()
 *     }
 * }
 * ```
 */
@Composable
fun SoftWorkTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = SoftWorkLightColorScheme
    val softWorkColors: SoftWorkColors = LightSoftWorkColors

    CompositionLocalProvider(LocalSoftWorkColors provides softWorkColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SoftWorkTypography,
            shapes = SoftWorkShapes,
            content = content,
        )
    }
}

/** Convenience accessor for the active [SoftWorkColors] inside Composables. */
object SoftWorkTheme {
    val colors: SoftWorkColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSoftWorkColors.current
}
