package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.bonysoft.bonusstage.ui.theme.PixelSize
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import kotlin.math.roundToInt

/**
 * A segmented bar like an HP gauge. [progress] is 0..1; a partially filled segment is shown
 * full only once reached, as in the games.
 */
@Composable
fun RetroProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = RetroTheme.colors.progress,
    trackColor: Color = RetroTheme.colors.track,
    borderColor: Color = RetroTheme.colors.windowBorder,
    segments: Int = 20,
    height: Dp = 14.dp,
) {
    val clamped = progress.coerceIn(0f, 1f)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(clamped, 0f..1f) },
    ) {
        val p = PixelSize.toPx()
        drawRect(borderColor)
        val innerWidth = size.width - 2 * p
        val innerHeight = size.height - 2 * p
        drawRect(trackColor, Offset(p, p), Size(innerWidth, innerHeight))
        val filled = (clamped * segments).let { if (clamped > 0f && it < 1f) 1 else it.toInt() }
        val segmentWidth = (innerWidth - (segments - 1) * p) / segments
        for (i in 0 until filled) {
            val left = p + i * (segmentWidth + p)
            drawRect(color, Offset(left, p), Size(segmentWidth, innerHeight))
            drawRect(Color.White.copy(alpha = 0.35f), Offset(left, p), Size(segmentWidth, p))
        }
    }
}

/** "12 / 23" style label helper, with the percentage rounded down like the RA site. */
fun progressPercent(done: Int, total: Int): Int = if (total == 0) 0 else (done * 100f / total).toInt().coerceIn(0, 100)

/** Rounded percentage for display only. */
fun Float.asPercent(): Int = (this * 100).roundToInt()
