package net.bonysoft.bonusstage.ui.effects

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Optional display effects over the whole app (ADR 0004). Off by default. */
enum class ScreenEffect { Off, Lcd, Scanlines, Crt }

/** Applies [effect] to everything drawn inside: a runtime shader where available, an overlay otherwise. */
expect fun Modifier.screenEffect(effect: ScreenEffect): Modifier

/**
 * The shader shared by both platforms: valid AGSL (Android RuntimeShader) and SkSL (Skia on iOS,
 * which draws through Metal). [content] is the app, [resolution] its size in pixels.
 */
internal const val SCREEN_EFFECT_SHADER = """
uniform shader content;
uniform float2 resolution;
uniform float mode;

half4 main(float2 coord) {
    // 1 = LCD grid, 2 = scanlines, 3 = CRT.
    if (mode < 1.5) {
        half4 color = content.eval(coord);
        float2 cell = mod(coord, 4.0);
        float grid = (cell.x < 1.0 || cell.y < 1.0) ? 0.82 : 1.0;
        return half4(color.rgb * grid, color.a);
    }
    if (mode < 2.5) {
        half4 color = content.eval(coord);
        float line = mod(coord.y, 3.0) < 1.0 ? 0.72 : 1.0;
        return half4(color.rgb * line, color.a);
    }
    float2 uv = coord / resolution * 2.0 - 1.0;
    uv *= 1.0 + 0.035 * dot(uv.yx, uv.yx);
    if (abs(uv.x) > 1.0 || abs(uv.y) > 1.0) {
        return half4(0.0, 0.0, 0.0, 1.0);
    }
    float2 bent = (uv + 1.0) * 0.5 * resolution;
    half r = content.eval(bent + float2(1.5, 0.0)).r;
    half g = content.eval(bent).g;
    half b = content.eval(bent - float2(1.5, 0.0)).b;
    float line = mod(bent.y, 3.0) < 1.0 ? 0.75 : 1.0;
    float vignette = 1.0 - 0.35 * dot(uv * 0.9, uv * 0.9);
    return half4(half3(r, g, b) * line * vignette, 1.0);
}
"""

/** For platforms without runtime shaders: draws the grid or the scanlines over the content. */
fun Modifier.overlayEffect(effect: ScreenEffect): Modifier {
    if (effect == ScreenEffect.Off) return this
    return drawWithContent {
        drawContent()
        val shade = Color.Black.copy(alpha = if (effect == ScreenEffect.Lcd) 0.18f else 0.28f)
        val step = if (effect == ScreenEffect.Lcd) 4f else 3f
        var y = 0f
        while (y < size.height) {
            drawRect(shade, Offset(0f, y), Size(size.width, 1f))
            y += step
        }
        if (effect == ScreenEffect.Lcd) {
            var x = 0f
            while (x < size.width) {
                drawRect(shade, Offset(x, 0f), Size(1f, size.height))
                x += step
            }
        }
        if (effect == ScreenEffect.Crt) {
            drawRect(
                Brush.radialGradient(
                    0.6f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.45f),
                    center = center,
                    radius = size.maxDimension * 0.75f,
                ),
            )
        }
    }
}

fun ScreenEffect.shaderMode(): Float = when (this) {
    ScreenEffect.Off -> 0f
    ScreenEffect.Lcd -> 1f
    ScreenEffect.Scanlines -> 2f
    ScreenEffect.Crt -> 3f
}
