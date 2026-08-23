package com.subulalhuda.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Design tokens from the website's src/index.css.
 *
 * Light theme (primary):
 *   Primary: #0a0a0a | Accent: #D4A017 | Surface: #FFFFFF
 *   SurfaceAlt: #F5F3EE | Border: #E8E5DE
 *   TextSecondary: #5A5A5A | TextMuted: #8A8A8A
 *   Error: #DC2626 | Success: #16A34A
 *   Hero: #FFFFFF | Header: rgba(250,250,248,0.85)
 *
 * Dark theme:
 *   Primary: #F5F5F5 | Accent: #D9A84D | Surface: #141414
 *   SurfaceAlt: #1a1a1a | Border: #2a2a2a
 *   TextSecondary: #A0A0A0 | TextMuted: #707070
 *   Error: #F87171 | Success: #4ADE80
 *   Hero: #141414 | Header: rgba(10,10,10,0.85)
 *
 * Body background (not a token): #FAFAF8 light / #0a0a0a dark
 */

// region Light theme
val PrimaryLight = Color(0xFF0A0A0A)
val AccentLight = Color(0xFFD4A017)
val AccentHoverLight = Color(0xFFBF8A14)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceAltLight = Color(0xFFF5F3EE)
val BorderLight = Color(0xFFE8E5DE)
val TextSecondaryLight = Color(0xFF5A5A5A)
val TextMutedLight = Color(0xFF8A8A8A)
val ErrorLight = Color(0xFFDC2626)
val SuccessLight = Color(0xFF16A34A)
val HeroLight = Color(0xFFFFFFFF)
val HeaderLight = Color(0xD9FAFAF8) // rgba(250,250,248,0.85)
val BackgroundLight = Color(0xFFFAFAF8)
// endregion

// region Dark theme
val PrimaryDark = Color(0xFFF5F5F5)
val AccentDark = Color(0xFFD9A84D)
val AccentHoverDark = Color(0xFFE8C45A)
val SurfaceDark = Color(0xFF141414)
val SurfaceAltDark = Color(0xFF1A1A1A)
val BorderDark = Color(0xFF2A2A2A)
val TextSecondaryDark = Color(0xFFA0A0A0)
val TextMutedDark = Color(0xFF707070)
val ErrorDark = Color(0xFFF87171)
val SuccessDark = Color(0xFF4ADE80)
val HeroDark = Color(0xFF141414)
val HeaderDark = Color(0xD90A0A0A) // rgba(10,10,10,0.85)
val BackgroundDark = Color(0xFF0A0A0A)
// endregion
