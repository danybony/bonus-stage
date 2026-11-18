package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.bonysoft.bonusstage.ui.theme.RetroTheme

/**
 * One entry of a game-style menu. The cursor appears when the item is [selected], pressed,
 * focused (keyboard, D-pad) or hovered.
 */
@Composable
fun RetroMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    supportingText: String? = null,
    color: Color = RetroTheme.colors.onWindow,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val hovered by interactionSource.collectIsHoveredAsState()
    val showCursor = selected || pressed || focused || hovered

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.width(14.dp), contentAlignment = Alignment.Center) {
            if (showCursor) PixelCursor(color = color)
        }
        leading?.invoke()
        Column(modifier = Modifier.weight(1f)) {
            Text(text = text, style = RetroTheme.typography.menu, color = color, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (supportingText != null) {
                Text(text = supportingText, style = RetroTheme.typography.label, color = RetroTheme.colors.onWindowMuted)
            }
        }
        trailing?.invoke()
    }
}
