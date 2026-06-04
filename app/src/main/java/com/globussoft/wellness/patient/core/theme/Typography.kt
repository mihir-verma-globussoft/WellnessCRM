package com.globussoft.wellness.patient.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontLoadingStrategy
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.globussoft.wellness.patient.R

val ManropeFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal, loadingStrategy = FontLoadingStrategy.OptionalLocal),
    Font(R.font.manrope_semi_bold, FontWeight.SemiBold, loadingStrategy = FontLoadingStrategy.OptionalLocal),
    Font(R.font.manrope_bold, FontWeight.Bold, loadingStrategy = FontLoadingStrategy.OptionalLocal),
)

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal, loadingStrategy = FontLoadingStrategy.OptionalLocal),
    Font(R.font.inter_medium, FontWeight.Medium, loadingStrategy = FontLoadingStrategy.OptionalLocal),
    Font(R.font.inter_semi_bold, FontWeight.SemiBold, loadingStrategy = FontLoadingStrategy.OptionalLocal),
)

// Kept for any future brand accent usage; not used in WellnessTypography.
val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_display_regular, FontWeight.Normal, loadingStrategy = FontLoadingStrategy.OptionalLocal),
    Font(R.font.playfair_display_medium, FontWeight.Medium, loadingStrategy = FontLoadingStrategy.OptionalLocal),
    Font(R.font.playfair_display_semi_bold, FontWeight.SemiBold, loadingStrategy = FontLoadingStrategy.OptionalLocal),
    Font(R.font.playfair_display_bold, FontWeight.Bold, loadingStrategy = FontLoadingStrategy.OptionalLocal),
    Font(R.font.playfair_display_italic, FontWeight.Normal, FontStyle.Italic, loadingStrategy = FontLoadingStrategy.OptionalLocal),
)

val WellnessTypography = Typography(
    // ── Display (Manrope) ───────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.8).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    // ── Headline (Manrope) ──────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    // ── Title: Large = Manrope, Medium/Small = Inter ────────────────────────
    titleLarge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    // ── Body (Inter) ────────────────────────────────────────────────────────
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // ── Label (Inter) ───────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
