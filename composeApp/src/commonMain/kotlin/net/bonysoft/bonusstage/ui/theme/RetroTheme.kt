package net.bonysoft.bonusstage.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Size of one "pixel" of the retro frames and borders. */
val PixelSize: Dp = 2.dp

object RetroTheme {
    val colors: RetroColors
        @Composable @ReadOnlyComposable get() = LocalRetroColors.current

    val typography: RetroTypography
        @Composable @ReadOnlyComposable get() = LocalRetroTypography.current
}

/**
 * Provides the retro palette and fonts. Material is kept underneath only for the components we do
 * not restyle (ripples, text selection, pull-to-refresh), mapped onto the same palette.
 */
@Composable
fun RetroTheme(content: @Composable () -> Unit) {
    val colors = BonusStageColors
    val typography = bonusStageTypography()
    CompositionLocalProvider(
        LocalRetroColors provides colors,
        LocalRetroTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = colors.accent,
                onPrimary = colors.onAccent,
                secondary = colors.softcore,
                background = colors.background,
                onBackground = colors.onBackground,
                surface = colors.background,
                onSurface = colors.onBackground,
                surfaceVariant = colors.backgroundVariant,
                error = colors.error,
            ),
            typography = Typography(
                bodyLarge = typography.body,
                bodyMedium = typography.bodySmall,
                labelLarge = typography.label,
                titleLarge = typography.title,
            ),
            content = content,
        )
    }
}
