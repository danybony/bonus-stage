package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import net.bonysoft.bonusstage.ui.theme.PixelSize
import net.bonysoft.bonusstage.ui.theme.RetroTheme

enum class RetroButtonStyle { Primary, Secondary }

/** A chunky pixel button with a drop shadow; it sinks onto the shadow while pressed. */
@Composable
fun RetroButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: RetroButtonStyle = RetroButtonStyle.Primary,
) {
    val colors = RetroTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val fill = when {
        !enabled -> colors.track
        style == RetroButtonStyle.Primary -> colors.accent
        else -> colors.window
    }
    val shadow = PixelSize * 2
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 96.dp, minHeight = 48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .drawBehind {
                if (!pressed) {
                    translate(shadow.toPx(), shadow.toPx()) {
                        drawPixelFrame(colors.shadow, colors.shadow, null)
                    }
                }
            }
            .offset { if (pressed) IntOffset(shadow.roundToPx(), shadow.roundToPx()) else IntOffset.Zero }
            .retroFrame(fill = fill, border = colors.windowBorder, highlight = if (enabled) colors.windowHighlight else null)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.uppercase(),
            style = RetroTheme.typography.menu,
            color = if (enabled) colors.onAccent else colors.onWindowMuted,
        )
    }
}
