package com.crm.enhance_wellness.core.theme

import androidx.compose.ui.graphics.Color

// ── Light mode M3 roles — "Dr. Enhance Wellness" gold/silver palette ────────
// Hue mapping: Primary = Gold, Secondary = Silver, Tertiary = Charcoal/Onyx
// (mirrors the 3 tones literally present in the logo)

val WellnessPrimary = Color(0xFF8A6D23)              // antique bronze-gold (darkened from logo gold #D4AF37 for white-text contrast)
val WellnessOnPrimary = Color.White
val WellnessPrimaryContainer = Color(0xFFF5DFA0)     // pale gold tint
val WellnessOnPrimaryContainer = Color(0xFF2B1F00)   // deep gold-brown

val WellnessSecondary = Color(0xFF6E6656)            // warm silver-taupe (silver read as warm neutral, not cold grey)
val WellnessOnSecondary = Color.White
val WellnessSecondaryContainer = Color(0xFFECE6D6)   // pale warm silver
val WellnessOnSecondaryContainer = Color(0xFF242015)

val WellnessTertiary = Color(0xFF3A362E)             // onyx/charcoal, from the logo's dark outline strokes
val WellnessOnTertiary = Color(0xFFF5F1E8)           // off-white (matches brand bg, not stark white)
val WellnessTertiaryContainer = Color(0xFFDAD5C8)    // pale warm grey-beige
val WellnessOnTertiaryContainer = Color(0xFF1D1B14)

// ── Backgrounds — warm cream/linen (kept warm-neutral, close to logo's off-white backdrop) ─
val WellnessBackground = Color(0xFFF5F1E8)
val WellnessSurface = Color(0xFFF5F1E8)
val WellnessSurfaceContainerLowest = Color(0xFFFFFFFF)
val WellnessSurfaceContainerLow = Color(0xFFFAF6EE)
val WellnessSurfaceContainer = Color(0xFFF0EBDD)
val WellnessSurfaceContainerHigh = Color(0xFFEAE4D3)
val WellnessSurfaceContainerHighest = Color(0xFFE3DDC9)
val WellnessSurfaceDim = Color(0xFFCFC8B2)
val WellnessSurfaceBright = Color(0xFFF5F1E8)
val WellnessOnSurface = Color(0xFF1C1B16)
val WellnessOnSurfaceVariant = Color(0xFF4A4638)
val WellnessSurfaceVariant = Color(0xFFE6DFC9)
val WellnessSurfaceTint = Color(0xFF8A6D23)

// ── Borders — warm taupe (matches Secondary hue, replaces cold grey) ────────
val WellnessOutline = Color(0xFF7A7260)
val WellnessOutlineVariant = Color(0xFFDDD6C4)
val WellnessInverseSurface = Color(0xFF322F27)
val WellnessInverseOnSurface = Color(0xFFF1EEE3)
val WellnessInversePrimary = Color(0xFFF0C75E)       // bright logo-gold highlight, used as inverse (= dark mode's Primary)

val WellnessError = Color(0xFFBA1A1A)
val WellnessOnError = Color.White
val WellnessErrorContainer = Color(0xFFFFDAD6)
val WellnessOnErrorContainer = Color(0xFF93000A)

// ── Dark mode — warm near-black charcoal (replaces cold navy) ───────────────
val WellnessDarkBackground = Color(0xFF141210)
val WellnessDarkSurface = Color(0xFF141210)
val WellnessDarkSurfaceContainerLowest = Color(0xFF0A0908)
val WellnessDarkSurfaceContainerLow = Color(0xFF1D1A15)
val WellnessDarkSurfaceContainer = Color(0xFF221F19)
val WellnessDarkSurfaceContainerHigh = Color(0xFF2C2822)
val WellnessDarkSurfaceContainerHighest = Color(0xFF37322A)
val WellnessDarkOnSurface = Color(0xFFE7E2D5)
val WellnessDarkOnSurfaceVariant = Color(0xFFCBC3AE)
val WellnessDarkSurfaceVariant = Color(0xFF4A4536)
val WellnessDarkOutline = Color(0xFF948C78)
val WellnessDarkOutlineVariant = Color(0xFF4A4536)

val WellnessDarkPrimary = Color(0xFFF0C75E)          // bright logo gold — this is literally the highlight gold from the icon
val WellnessDarkOnPrimary = Color(0xFF3D2E00)
val WellnessDarkPrimaryContainer = Color(0xFF8A6D23)
val WellnessDarkOnPrimaryContainer = Color(0xFFF5DFA0)

val WellnessDarkSecondary = Color(0xFFD8D0BC)        // light warm silver
val WellnessDarkOnSecondary = Color(0xFF2E2B1D)
val WellnessDarkSecondaryContainer = Color(0xFF554F3D)
val WellnessDarkOnSecondaryContainer = Color(0xFFECE6D6)

val WellnessDarkTertiary = Color(0xFFC9C3B3)         // pale warm grey (inverse of onyx)
val WellnessDarkOnTertiary = Color(0xFF1D1B14)
val WellnessDarkTertiaryContainer = Color(0xFF3A362C)
val WellnessDarkOnTertiaryContainer = Color(0xFFDAD5C8)

val WellnessDarkInverseSurface = Color(0xFFE7E2D5)
val WellnessDarkInverseOnSurface = Color(0xFF322F27)
val WellnessDarkInversePrimary = Color(0xFF8A6D23)

// ── Semantic extras (direct-use constants, not M3 roles) ─────────────────────
val WellnessGold = Color(0xFFD4AF37)          // exact logo gold — use for icon/splash/hero accents only
val WellnessGoldContainer = Color(0xFFF5DFA0)
val MedicalBlue = Color(0xFF3B6FA0)           // muted/warmed so it doesn't clash with the gold-silver brand; utility (info) color only
val MintGreen = Color(0xFF3F8F6C)             // muted/warmed; utility (success) color only