package com.subulalhuda.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.subulalhuda.R

/**
 * Typography matching the website's CSS custom properties:
 *   --font-heading: "Noto Kufi Arabic", "Segoe UI", Tahoma, sans-serif
 *   --font-body:    "Amiri", "Times New Roman", serif
 *
 * Heading weights: 300-500 max per AGENTS.md rules.
 * Body uses Amiri at 400/700.
 */
// Noto Kufi Arabic is a variable font — single file, multiple weights via FontWeight.
// Android API 26+ supports variable fonts in res/font/.
val NotoKufiArabic = FontFamily(
    Font(R.font.noto_kufi_arabic, FontWeight.Light),
    Font(R.font.noto_kufi_arabic, FontWeight.Normal),
    Font(R.font.noto_kufi_arabic, FontWeight.Medium),
    Font(R.font.noto_kufi_arabic, FontWeight.Bold),
)

val AmiriFont = FontFamily(
    Font(R.font.amiri_regular, FontWeight.Normal),
    Font(R.font.amiri_bold, FontWeight.Bold),
)

val SubulTypography = Typography(
    // Headings — Noto Kufi Arabic, light/medium weights
    displayLarge = TextStyle(
        fontFamily = NotoKufiArabic,
        fontWeight = FontWeight.Light,
        fontSize = 40.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = NotoKufiArabic,
        fontWeight = FontWeight.Light,
        fontSize = 32.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.3).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = NotoKufiArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 38.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = NotoKufiArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 34.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = NotoKufiArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = NotoKufiArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 26.sp,
    ),

    // Titles
    titleLarge = TextStyle(
        fontFamily = NotoKufiArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = NotoKufiArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = NotoKufiArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),

    // Body — Amiri
    bodyLarge = TextStyle(
        fontFamily = AmiriFont,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 30.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = AmiriFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = AmiriFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
    ),

    // Labels — Noto Kufi Arabic
    labelLarge = TextStyle(
        fontFamily = NotoKufiArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = NotoKufiArabic,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = NotoKufiArabic,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)
