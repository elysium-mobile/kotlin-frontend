package com.elysium.softwork.shared.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.elysium.softwork.R
import androidx.compose.ui.text.googlefonts.Font as GoogleFontEntry

/**
 * Google Fonts provider backed by the Google Play Services certificate set bundled in
 * `res/values/font_certs.xml`. The downloadable provider is a non-blocking enhancement
 * layered on top of the bundled variable font — see [ExoFontFamily].
 */
private val SoftWorkGoogleFontProvider: GoogleFont.Provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val ExoGoogleFont: GoogleFont = GoogleFont("Exo")

/**
 * Builds a [Font] entry that points at the bundled Exo variable TTF and pins its `wght`
 * axis to the supplied [weight]. Isolated as a single helper so the experimental opt-in
 * has the narrowest possible scope — every other declaration in this file stays on the
 * stable API surface.
 *
 * The `variationSettings` parameter of `Font(resId, ...)` is annotated
 * `@ExperimentalTextApi` in Compose UI Text. Opting in here (rather than at file scope)
 * keeps the experimental surface localized: if the API ships under a new name or signature
 * in a future Compose release, only this function needs to change.
 */
@OptIn(ExperimentalTextApi::class)
private fun exoVariableFont(weight: FontWeight): Font = Font(
    resId = R.font.exo_variable,
    weight = weight,
    style = FontStyle.Normal,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

/**
 * Brand font family.
 *
 * Resolution order — Compose picks the first [Font] in this list that matches the
 * requested weight/style **and** is already loaded:
 *
 * 1. **Bundled variable font** (`res/font/exo_variable.ttf`). Resource fonts default to
 *    [androidx.compose.ui.text.font.FontLoadingStrategy.Blocking], so the very first
 *    composition renders with Exo — no FOUT, no waiting on GMS, no network. Required
 *    to keep Compose `@Preview` rendering in Exo and to support devices that don't ship
 *    Google Play Services Fonts (some emulators, some carriers, AOSP builds).
 * 2. **Downloadable Google Fonts entry**. Once the GMS provider serves the optimized
 *    weight, Compose swaps it in transparently. Acts as the online upgrade path; the
 *    bundled font keeps the UI looking correct in the meantime.
 *
 * Weights enumerated: Normal/Medium/SemiBold/Bold — the four consumed by
 * [SoftWorkTypography]. Add new weights here, not in a screen-local override.
 */
val ExoFontFamily: FontFamily = FontFamily(
    exoVariableFont(FontWeight.Normal),
    exoVariableFont(FontWeight.Medium),
    exoVariableFont(FontWeight.SemiBold),
    exoVariableFont(FontWeight.Bold),
    GoogleFontEntry(googleFont = ExoGoogleFont, fontProvider = SoftWorkGoogleFontProvider, weight = FontWeight.Normal, style = FontStyle.Normal),
    GoogleFontEntry(googleFont = ExoGoogleFont, fontProvider = SoftWorkGoogleFontProvider, weight = FontWeight.Medium, style = FontStyle.Normal),
    GoogleFontEntry(googleFont = ExoGoogleFont, fontProvider = SoftWorkGoogleFontProvider, weight = FontWeight.SemiBold, style = FontStyle.Normal),
    GoogleFontEntry(googleFont = ExoGoogleFont, fontProvider = SoftWorkGoogleFontProvider, weight = FontWeight.Bold, style = FontStyle.Normal),
)

/**
 * Material 3 [Typography] aligned to the SoftWork visual language.
 * Display/headline weights lean bold for marketing surfaces; body/label stay readable.
 */
val SoftWorkTypography: Typography = Typography(
    displayLarge = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp),
    displayMedium = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = ExoFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)
