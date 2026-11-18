package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import net.bonysoft.bonusstage.ui.theme.PixelSize
import net.bonysoft.bonusstage.ui.theme.RetroTheme

/** The tab bar as a vertical rail, for wide windows and short screens. */
@Composable
fun RetroNavigationRail(tabs: List<RetroTab>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = RetroTheme.colors
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(colors.backgroundVariant)
            .drawBehind { drawRect(colors.shadow, Offset(size.width - PixelSize.toPx(), 0f), Size(PixelSize.toPx(), size.height)) }
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start + WindowInsetsSides.Vertical))
            .width(88.dp)
            .verticalScroll(rememberScrollState())
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            val color = if (selected) colors.accent else colors.onBackgroundMuted
            Column(
                modifier = Modifier
                    .width(88.dp)
                    .heightIn(min = 56.dp)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
                    .drawBehind {
                        if (selected) {
                            val p = PixelSize.toPx()
                            drawRect(colors.accent, Offset(0f, size.height / 2 - 8 * p), Size(2 * p, 16 * p))
                        }
                    }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                PixelIcon(tab.icon, color = color)
                Text(tab.label.uppercase(), style = RetroTheme.typography.label, color = color, maxLines = 1)
            }
        }
    }
}
