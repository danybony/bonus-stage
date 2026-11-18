package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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

class RetroTab(val label: String, val icon: PixelIcons.Icon)

/** Bottom navigation: pixel icons, the selected tab lit in gold with a marker underneath. */
@Composable
fun RetroTabBar(tabs: List<RetroTab>, selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = RetroTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.backgroundVariant)
            .drawBehind { drawRect(colors.shadow, Offset.Zero, Size(size.width, PixelSize.toPx())) }
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
            .selectableGroup(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = index == selectedIndex
            val color = if (selected) colors.accent else colors.onBackgroundMuted
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
                    .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(index) })
                    .drawBehind {
                        if (selected) {
                            val p = PixelSize.toPx()
                            drawRect(colors.accent, Offset(size.width / 2 - 8 * p, size.height - 3 * p), Size(16 * p, 2 * p))
                        }
                    }
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                PixelIcon(tab.icon, color = color)
                Text(tab.label.uppercase(), style = RetroTheme.typography.label, color = color)
            }
        }
    }
}
