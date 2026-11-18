package net.bonysoft.bonusstage.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The Bonus Stage palette (ADR 0004): a night-blue backdrop with cream windows, like the dialog
 * boxes of 8 and 16-bit RPGs. Fixed colors, no Material You.
 */
@Immutable
data class RetroColors(
    val background: Color,
    val backgroundVariant: Color,
    val onBackground: Color,
    val onBackgroundMuted: Color,
    val window: Color,
    val windowBorder: Color,
    val windowHighlight: Color,
    /** Drop shadows: darker than [background] so they show on it too. */
    val shadow: Color,
    val onWindow: Color,
    val onWindowMuted: Color,
    val track: Color,
    val accent: Color,
    val onAccent: Color,
    val hardcore: Color,
    val softcore: Color,
    val progress: Color,
    val mastered: Color,
    val beaten: Color,
    val error: Color,
)

val BonusStageColors = RetroColors(
    background = Color(0xFF1B1A33),
    backgroundVariant = Color(0xFF2A2850),
    onBackground = Color(0xFFF6EEDC),
    onBackgroundMuted = Color(0xFFA9A3C9),
    window = Color(0xFFF6EEDC),
    windowBorder = Color(0xFF1B1A33),
    windowHighlight = Color(0xFFFFFFFF),
    shadow = Color(0xFF0B0A17),
    onWindow = Color(0xFF1E1C2E),
    onWindowMuted = Color(0xFF6B6680),
    track = Color(0xFFDCD1B8),
    accent = Color(0xFFF4B536),
    onAccent = Color(0xFF1E1C2E),
    hardcore = Color(0xFFD9483B),
    softcore = Color(0xFF4D8FD9),
    progress = Color(0xFF4FA65A),
    mastered = Color(0xFFF4B536),
    beaten = Color(0xFF9C98B8),
    error = Color(0xFFD9483B),
)

val LocalRetroColors = staticCompositionLocalOf { BonusStageColors }
