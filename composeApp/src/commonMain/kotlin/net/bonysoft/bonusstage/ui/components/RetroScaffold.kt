package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.back
import net.bonysoft.bonusstage.ui.adaptive.LocalWindowLayout
import net.bonysoft.bonusstage.ui.theme.PixelSize
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Screen frame: a pixel header with the title (and back arrow when [onBack] is set), then the
 * content. Bottom insets are left to the content so lists can scroll under the navigation bar.
 */
@Composable
fun RetroScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable () -> Unit,
) {
    val colors = RetroTheme.colors
    // Short screens get a slimmer header: the title in the menu font, less padding.
    val compact = LocalWindowLayout.current.isCompactHeight
    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.backgroundVariant)
                .drawBehind {
                    val p = PixelSize.toPx()
                    drawRect(colors.shadow, Offset(0f, size.height - p), Size(size.width, p))
                }
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal))
                .padding(horizontal = 8.dp, vertical = if (compact) 0.dp else 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (onBack != null) {
                val backLabel = stringResource(Res.string.back)
                Box(
                    modifier = Modifier
                        .size(if (compact) 40.dp else 48.dp)
                        .clickable(role = Role.Button, onClick = onBack)
                        .semantics { contentDescription = backLabel },
                    contentAlignment = Alignment.Center,
                ) {
                    PixelIcon(PixelIcons.Back, color = colors.accent)
                }
            } else {
                Box(Modifier.size(width = 8.dp, height = if (compact) 40.dp else 48.dp))
            }
            Text(
                text = title.uppercase(),
                style = if (compact) RetroTheme.typography.menu else RetroTheme.typography.title,
                color = colors.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            actions()
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            content()
        }
    }
}
