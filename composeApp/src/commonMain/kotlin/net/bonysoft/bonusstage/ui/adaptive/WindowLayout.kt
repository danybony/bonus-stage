package net.bonysoft.bonusstage.ui.adaptive

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The size of the window the app draws in, with the breakpoints the screens use. Covers phones,
 * foldables, tablets and small or short secondary screens (for example the AYN Thor's 3.9" one,
 * about 470x410 dp).
 */
@Immutable
data class WindowLayout(val width: Dp, val height: Dp) {
    /** Tablets, unfolded foldables, landscape phones. */
    val isMediumWidth: Boolean get() = width >= 600.dp

    /** Large tablets and desktop-like windows: room for two panes. */
    val isExpandedWidth: Boolean get() = width >= 840.dp

    /** Landscape phones and small secondary screens: every vertical dp counts. */
    val isCompactHeight: Boolean get() = height < 480.dp

    /** A side rail instead of the bottom bar: it saves height where height is scarce. */
    val useNavigationRail: Boolean get() = isMediumWidth || isCompactHeight

    companion object {
        val Phone = WindowLayout(400.dp, 800.dp)
    }
}

val LocalWindowLayout = staticCompositionLocalOf { WindowLayout.Phone }

/** Measures the available space once at the root and provides it to every screen. */
@Composable
fun ProvideWindowLayout(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier) {
        CompositionLocalProvider(LocalWindowLayout provides WindowLayout(maxWidth, maxHeight)) {
            content()
        }
    }
}

/** Minimum width of one column in the adaptive lists and grids. */
val ListColumnMinWidth: Dp = 340.dp
