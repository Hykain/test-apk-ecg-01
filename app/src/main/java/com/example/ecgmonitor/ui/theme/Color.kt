package com.example.ecgmonitor.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// Material 3 Medical Clinical Fallback Palette (Light & Dark)
// =========================================================================

// --- Light Scheme ---
val MdLightPrimary = Color(0xFF006A60)
val MdLightOnPrimary = Color(0xFFFFFFFF)
val MdLightPrimaryContainer = Color(0xFF73F8E5)
val MdLightOnPrimaryContainer = Color(0xFF00201C)

val MdLightSecondary = Color(0xFF4A635F)
val MdLightOnSecondary = Color(0xFFFFFFFF)
val MdLightSecondaryContainer = Color(0xFFCCE8E3)
val MdLightOnSecondaryContainer = Color(0xFF05201C)

val MdLightTertiary = Color(0xFF456179)
val MdLightOnTertiary = Color(0xFFFFFFFF)
val MdLightTertiaryContainer = Color(0xFFCCE5FF)
val MdLightOnTertiaryContainer = Color(0xFF001D32)

val MdLightError = Color(0xFFBA1A1A)
val MdLightOnError = Color(0xFFFFFFFF)
val MdLightErrorContainer = Color(0xFFFFDAD6)
val MdLightOnErrorContainer = Color(0xFF410002)

val MdLightBackground = Color(0xFFF4FAF7)
val MdLightOnBackground = Color(0xFF161D1B)
val MdLightSurface = Color(0xFFF4FAF7)
val MdLightOnSurface = Color(0xFF161D1B)
val MdLightSurfaceVariant = Color(0xFFDAE5E1)
val MdLightOnSurfaceVariant = Color(0xFF3F4946)
val MdLightOutline = Color(0xFF6F7976)
val MdLightOutlineVariant = Color(0xFFBFC9C5)

// M3 Surface Container Roles (Light)
val MdLightSurfaceContainerLowest = Color(0xFFFFFFFF)
val MdLightSurfaceContainerLow = Color(0xFFEEF5F2)
val MdLightSurfaceContainer = Color(0xFFE8EFEC)
val MdLightSurfaceContainerHigh = Color(0xFFE2EAE6)
val MdLightSurfaceContainerHighest = Color(0xFFDCE4E1)

// --- Dark Scheme ---
val MdDarkPrimary = Color(0xFF53DBC9)
val MdDarkOnPrimary = Color(0xFF003731)
val MdDarkPrimaryContainer = Color(0xFF005048)
val MdDarkOnPrimaryContainer = Color(0xFF73F8E5)

val MdDarkSecondary = Color(0xFFB0CCC7)
val MdDarkOnSecondary = Color(0xFF1B3531)
val MdDarkSecondaryContainer = Color(0xFF324B47)
val MdDarkOnSecondaryContainer = Color(0xFFCCE8E3)

val MdDarkTertiary = Color(0xFFACC7E5)
val MdDarkOnTertiary = Color(0xFF143349)
val MdDarkTertiaryContainer = Color(0xFF2D4960)
val MdDarkOnTertiaryContainer = Color(0xFFCCE5FF)

val MdDarkError = Color(0xFFFFB4AB)
val MdDarkOnError = Color(0xFF690005)
val MdDarkErrorContainer = Color(0xFF93000A)
val MdDarkOnErrorContainer = Color(0xFFFFDAD6)

val MdDarkBackground = Color(0xFF0E1514)
val MdDarkOnBackground = Color(0xFFDCE4E1)
val MdDarkSurface = Color(0xFF0E1514)
val MdDarkOnSurface = Color(0xFFDCE4E1)
val MdDarkSurfaceVariant = Color(0xFF3F4946)
val MdDarkOnSurfaceVariant = Color(0xFFBFC9C5)
val MdDarkOutline = Color(0xFF899390)
val MdDarkOutlineVariant = Color(0xFF3F4946)

// M3 Surface Container Roles (Dark)
val MdDarkSurfaceContainerLowest = Color(0xFF090F0E)
val MdDarkSurfaceContainerLow = Color(0xFF161D1C)
val MdDarkSurfaceContainer = Color(0xFF1A2120)
val MdDarkSurfaceContainerHigh = Color(0xFF252B2A)
val MdDarkSurfaceContainerHighest = Color(0xFF2F3635)

// =========================================================================
// Specialized Medical ECG Waveform & Grid Colors (Adaptive M3)
// =========================================================================

@Immutable
data class EcgThemeColors(
    val canvasBackground: Color,
    val gridMajor: Color,
    val gridMinor: Color,
    val rawChannel: Color,
    val espFilteredChannel: Color,
    val androidFilteredChannel: Color,
    val peakMarker: Color,
    val spo2Color: Color,
    val ppgBpmColor: Color,
    val rhythmNormal: Color,
    val rhythmSvc: Color,
    val rhythmPvc: Color,
    val rhythmFusion: Color,
    val rhythmUnknown: Color
)

// Option A: Dark mode = ICU Phosphor clinical monitor
val DarkEcgColors = EcgThemeColors(
    canvasBackground = Color(0xFF081216),
    gridMajor = Color(0x334DD0E1),     // 20% cyan for 0.2s major boxes
    gridMinor = Color(0x124DD0E1),     // 7% cyan for 0.04s minor subdivisions
    rawChannel = Color(0xFFFF6E6E),    // Distinct coral
    espFilteredChannel = Color(0xFF00E5A8), // Vibrant clinical phosphor mint
    androidFilteredChannel = Color(0xFF38BDF8), // Tech cyan
    peakMarker = Color(0xFFFF4D4D),
    spo2Color = Color(0xFF38BDF8),
    ppgBpmColor = Color(0xFF2DD4BF),
    rhythmNormal = Color(0xFF34D399),
    rhythmSvc = Color(0xFFFBBF24),
    rhythmPvc = Color(0xFFF87171),
    rhythmFusion = Color(0xFFA78BFA),
    rhythmUnknown = Color(0xFF9CA3AF)
)

// Option A: Light mode = Authentic Classic Pink/Salmon Medical ECG Paper
val LightEcgColors = EcgThemeColors(
    canvasBackground = Color(0xFFFFFBF8),
    gridMajor = Color(0x55F87171),     // Crisp medical pink major squares
    gridMinor = Color(0x22F87171),     // Subtle 1mm subdivisions
    rawChannel = Color(0xFFDC2626),    // Classic dark clinical red
    espFilteredChannel = Color(0xFF047857), // Deep clinical emerald
    androidFilteredChannel = Color(0xFF0284C7), // Medical blue
    peakMarker = Color(0xFFDC2626),
    spo2Color = Color(0xFF0284C7),
    ppgBpmColor = Color(0xFF0D9488),
    rhythmNormal = Color(0xFF059669),
    rhythmSvc = Color(0xFFD97706),
    rhythmPvc = Color(0xFFDC2626),
    rhythmFusion = Color(0xFF7C3AED),
    rhythmUnknown = Color(0xFF6B7280)
)

val LocalEcgColors = staticCompositionLocalOf { DarkEcgColors }

