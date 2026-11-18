package net.bonysoft.bonusstage.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.atkinson_hyperlegible_bold
import net.bonysoft.bonusstage.resources.atkinson_hyperlegible_regular
import net.bonysoft.bonusstage.resources.pixel_operator
import net.bonysoft.bonusstage.resources.pixel_operator_bold
import org.jetbrains.compose.resources.Font

/**
 * Pixel Operator for the interface (titles, menus, numbers) and Atkinson Hyperlegible for long text.
 * Pixel sizes are multiples of 4 so the 16 px pixel font stays as crisp as possible.
 */
@Immutable
data class RetroTypography(
    val display: TextStyle,
    val title: TextStyle,
    val menu: TextStyle,
    val label: TextStyle,
    val number: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
)

@Composable
fun pixelFontFamily(): FontFamily = FontFamily(
    Font(Res.font.pixel_operator, FontWeight.Normal),
    Font(Res.font.pixel_operator_bold, FontWeight.Bold),
)

@Composable
fun readableFontFamily(): FontFamily = FontFamily(
    Font(Res.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    Font(Res.font.atkinson_hyperlegible_bold, FontWeight.Bold),
)

@Composable
fun bonusStageTypography(): RetroTypography {
    val pixel = pixelFontFamily()
    val readable = readableFontFamily()
    return RetroTypography(
        display = TextStyle(fontFamily = pixel, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 36.sp),
        title = TextStyle(fontFamily = pixel, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 28.sp),
        menu = TextStyle(fontFamily = pixel, fontSize = 20.sp, lineHeight = 24.sp),
        label = TextStyle(fontFamily = pixel, fontSize = 16.sp, lineHeight = 20.sp),
        number = TextStyle(fontFamily = pixel, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 24.sp),
        body = TextStyle(fontFamily = readable, fontSize = 16.sp, lineHeight = 22.sp),
        bodySmall = TextStyle(fontFamily = readable, fontSize = 14.sp, lineHeight = 19.sp),
    )
}

val LocalRetroTypography = staticCompositionLocalOf<RetroTypography> {
    error("RetroTypography not provided: wrap the content in RetroTheme")
}
