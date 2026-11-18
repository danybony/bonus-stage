package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import net.bonysoft.bonusstage.ui.theme.PixelSize
import net.bonysoft.bonusstage.ui.theme.RetroTheme

/**
 * An achievement badge or game icon in a pixel frame. Locked badges are shown in grayscale.
 * Images are scaled without smoothing to keep their pixels sharp.
 */
@Composable
fun RetroBadge(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
    size: Dp = 56.dp,
    frameColor: Color = if (locked) RetroTheme.colors.beaten else RetroTheme.colors.windowBorder,
) {
    val grayscale = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
    Box(
        modifier = modifier
            .size(size)
            .retroFrame(fill = RetroTheme.colors.track, border = frameColor, highlight = null)
            .padding(PixelSize * 2),
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.None,
            colorFilter = if (locked) grayscale else null,
            alpha = if (locked) 0.7f else 1f,
        )
    }
}
