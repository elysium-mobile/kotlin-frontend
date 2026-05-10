package com.elysium.softwork.shared.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * SoftWork brand palette for the Employee client.
 *
 * Tokens are intentionally exposed as top-level [Color] values so they can be referenced from
 * the [SoftWorkColors] semantic wrapper, the Material 3 [androidx.compose.material3.ColorScheme],
 * and component-local gradients without a layer of indirection.
 */

// region Brand
val PrimarySky: Color = Color(0xFF4DA8DA)
val PrimaryTeal: Color = Color(0xFF19A4A1)
val PrimaryNavy: Color = Color(0xFF1C4B78)
// endregion

// region Accents
val AccentWhite: Color = Color(0xFFF2F4F7)
val AccentDark: Color = Color(0xFF3E3E3E)
val AccentMint: Color = Color(0xFFA5E3D8)
// endregion

// region Semantic
val Success: Color = Color(0xFF19A4A1)
val Warning: Color = Color(0xFFE8A838)
val Danger: Color = Color(0xFFC94040)
// endregion

/**
 * Semantic color contract exposed by [SoftWorkTheme] via [LocalSoftWorkColors].
 *
 * Components should read brand tokens from this object rather than referencing raw [Color]
 * constants directly. This keeps room for a future dark variant without rewriting call sites.
 */
@Immutable
data class SoftWorkColors(
    val primarySky: Color,
    val primaryTeal: Color,
    val primaryNavy: Color,
    val accentWhite: Color,
    val accentDark: Color,
    val accentMint: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
)

/** Default light-palette mapping used by [SoftWorkTheme]. */
val LightSoftWorkColors: SoftWorkColors = SoftWorkColors(
    primarySky = PrimarySky,
    primaryTeal = PrimaryTeal,
    primaryNavy = PrimaryNavy,
    accentWhite = AccentWhite,
    accentDark = AccentDark,
    accentMint = AccentMint,
    success = Success,
    warning = Warning,
    danger = Danger,
)

/**
 * Composition local for the active [SoftWorkColors]. Provided by [SoftWorkTheme] —
 * accessing it outside the theme will throw to surface mis-wiring early.
 */
val LocalSoftWorkColors = compositionLocalOf<SoftWorkColors> {
    error("SoftWorkColors not provided. Wrap your UI with SoftWorkTheme.")
}
