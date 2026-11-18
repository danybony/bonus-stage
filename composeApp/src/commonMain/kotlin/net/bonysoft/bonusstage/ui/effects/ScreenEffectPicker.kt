package net.bonysoft.bonusstage.ui.effects

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.bonysoft.bonusstage.ui.components.retroFrame
import net.bonysoft.bonusstage.ui.theme.PixelSize
import net.bonysoft.bonusstage.ui.theme.RetroColors
import net.bonysoft.bonusstage.ui.theme.RetroTheme

/**
 * The four screen effects side by side, each previewed on a small pixel scene, framed like a game
 * icon. The previews are drawn, not shaded: runtime shaders nested inside scrolling lists can blank
 * the whole list on some GPUs, and a static picture is all a preview needs.
 */
@Composable
fun ScreenEffectPicker(
    selected: ScreenEffect,
    labels: Map<ScreenEffect, String>,
    onSelect: (ScreenEffect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RetroTheme.colors
    Row(modifier = modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ScreenEffect.entries.forEach { effect ->
            val isSelected = effect == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(effect) }),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .retroFrame(
                            fill = colors.track,
                            border = if (isSelected) colors.accent else colors.windowBorder,
                            highlight = null,
                        )
                        .padding(PixelSize * if (isSelected) 3 else 2),
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawScene(colors)
                        drawEffectPreview(effect, colors)
                    }
                }
                Text(
                    text = labels.getValue(effect).uppercase(),
                    style = RetroTheme.typography.label,
                    color = if (isSelected) colors.onWindow else colors.onWindowMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

/** A tiny 16x16 night scene: sky bands, moon, stars, hills and a hero, in the app palette. */
private fun DrawScope.drawScene(colors: RetroColors) {
    val cell = size.width / 16f
    fun px(x: Int, y: Int, w: Int = 1, h: Int = 1, color: androidx.compose.ui.graphics.Color) =
        drawRect(color, Offset(x * cell, y * cell), Size(w * cell, h * cell))
    px(0, 0, 16, 6, colors.background)
    px(0, 6, 16, 4, colors.backgroundVariant)
    px(11, 2, 3, 3, colors.accent)
    px(12, 1, 1, 1, colors.accent)
    listOf(2 to 2, 6 to 1, 8 to 4, 4 to 7).forEach { (x, y) -> px(x, y, color = colors.onBackground) }
    px(0, 10, 16, 6, colors.progress)
    px(2, 9, 5, 1, colors.progress)
    px(9, 8, 6, 2, colors.progress)
    px(0, 13, 16, 3, colors.windowBorder)
    // The hero: a little figure with a red cap.
    px(6, 10, 2, 1, colors.hardcore)
    px(6, 11, 2, 1, colors.window)
    px(6, 12, 2, 1, colors.softcore)
}

/** The effect drawn on top of the 16x16 scene, at the scene's own pixel scale so it reads at icon size. */
private fun DrawScope.drawEffectPreview(effect: ScreenEffect, colors: RetroColors) {
    val cell = size.width / 16f
    val line = maxOf(1f, cell / 5f)
    val shade = androidx.compose.ui.graphics.Color.Black
    when (effect) {
        ScreenEffect.Off -> Unit
        ScreenEffect.Lcd -> for (i in 0..16) {
            drawRect(shade.copy(alpha = 0.3f), Offset(0f, i * cell), Size(size.width, line))
            drawRect(shade.copy(alpha = 0.3f), Offset(i * cell, 0f), Size(line, size.height))
        }
        ScreenEffect.Scanlines -> scanlines(cell, shade.copy(alpha = 0.35f))
        ScreenEffect.Crt -> {
            scanlines(cell, shade.copy(alpha = 0.3f))
            drawRect(
                androidx.compose.ui.graphics.Brush.radialGradient(
                    0.55f to androidx.compose.ui.graphics.Color.Transparent,
                    1f to shade.copy(alpha = 0.6f),
                    center = center,
                    radius = size.minDimension * 0.75f,
                ),
            )
            // Stepped dark corners: the curved glass of a tube, in pixels.
            val corner = colors.shadow
            listOf(3, 2, 1).forEachIndexed { row, width ->
                val y = row * cell
                val w = width * cell
                drawRect(corner, Offset(0f, y), Size(w, cell))
                drawRect(corner, Offset(size.width - w, y), Size(w, cell))
                drawRect(corner, Offset(0f, size.height - y - cell), Size(w, cell))
                drawRect(corner, Offset(size.width - w, size.height - y - cell), Size(w, cell))
            }
        }
    }
}

private fun DrawScope.scanlines(cell: Float, color: androidx.compose.ui.graphics.Color) {
    var y = cell / 2
    while (y < size.height) {
        drawRect(color, Offset(0f, y), Size(size.width, cell / 2))
        y += cell
    }
}
