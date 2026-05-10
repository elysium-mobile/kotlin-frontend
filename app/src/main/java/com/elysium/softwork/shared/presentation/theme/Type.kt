package com.elysium.softwork.shared.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.elysium.softwork.R

/**
 * Google Fonts provider backed by the Google Play Services certificate set bundled in
 * `res/values/font_certs.xml`. Required so Compose can fetch the [ExoFontFamily] at runtime.
 */
private val SoftWorkGoogleFontProvider: GoogleFont.Provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val ExoGoogleFont: GoogleFont = GoogleFont("Exo")

/**
 * Brand font family. SoftWork uses Exo across the entire UI; weights are limited to the four
 * actually consumed by [SoftWorkTypography] to keep cold-start font fetches small.
 */
val ExoFontFamily: FontFamily = FontFamily(
    Font(googleFont = ExoGoogleFont, fontProvider = SoftWorkGoogleFontProvider, weight = FontWeight.Normal, style = FontStyle.Normal),
    Font(googleFont = ExoGoogleFont, fontProvider = SoftWorkGoogleFontProvider, weight = FontWeight.Medium, style = FontStyle.Normal),
    Font(googleFont = ExoGoogleFont, fontProvider = SoftWorkGoogleFontProvider, weight = FontWeight.SemiBold, style = FontStyle.Normal),
    Font(googleFont = ExoGoogleFont, fontProvider = SoftWorkGoogleFontProvider, weight = FontWeight.Bold, style = FontStyle.Normal),
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
