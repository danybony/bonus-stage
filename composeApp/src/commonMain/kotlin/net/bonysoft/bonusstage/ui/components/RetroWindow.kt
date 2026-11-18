package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import net.bonysoft.bonusstage.ui.theme.PixelSize
import net.bonysoft.bonusstage.ui.theme.RetroTheme

/**
 * A dialog box in the style of classic game menus: dark outline, white inner highlight,
 * cut pixel corners, and an optional title tab on the top edge.
 */
@Composable
fun RetroWindow(
    modifier: Modifier = Modifier,
    title: String? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = RetroTheme.colors
    Box(modifier = modifier.padding(top = if (title != null) 12.dp else 0.dp)) {
        Column(
            modifier = Modifier
                .retroFrame(fill = colors.window, border = colors.windowBorder, highlight = colors.windowHighlight)
                .padding(contentPadding)
                .padding(top = if (title != null) 8.dp else 0.dp),
            content = content,
        )
        if (title != null) {
            Text(
                text = title,
                style = RetroTheme.typography.label,
                color = colors.onAccent,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .offset(y = (-12).dp)
                    .retroFrame(fill = colors.accent, border = colors.windowBorder, highlight = null)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .semantics { heading() },
            )
        }
    }
}

/** Draws the pixel frame used by windows, buttons and badges. */
fun Modifier.retroFrame(fill: Color, border: Color, highlight: Color?): Modifier = drawBehind {
    drawPixelFrame(fill, border, highlight)
}

internal fun DrawScope.drawPixelFrame(fill: Color, border: Color, highlight: Color?) {
    val p = PixelSize.toPx()
    val w = size.width
    val h = size.height
    // Outline with the corner pixel cut away.
    drawRect(border, Offset(p, 0f), Size(w - 2 * p, h))
    drawRect(border, Offset(0f, p), Size(w, h - 2 * p))
    // Fill.
    drawRect(fill, Offset(p * 2, p), Size(w - 4 * p, h - 2 * p))
    drawRect(fill, Offset(p, p * 2), Size(w - 2 * p, h - 4 * p))
    if (highlight != null) {
        // A one-pixel inner bevel on the top and left edges.
        drawRect(highlight, Offset(p * 2, p * 2), Size(w - 4 * p, p))
        drawRect(highlight, Offset(p * 2, p * 2), Size(p, h - 4 * p))
    }
}
