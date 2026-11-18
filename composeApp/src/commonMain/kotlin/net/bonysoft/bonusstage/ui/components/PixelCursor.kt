package net.bonysoft.bonusstage.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** The right-pointing menu cursor, drawn pixel by pixel (5 columns, 9 rows). */
@Composable
fun PixelCursor(color: Color, modifier: Modifier = Modifier, height: Dp = 18.dp) {
    Canvas(modifier = modifier.size(width = height * 5 / 9, height = height)) {
        val p = size.height / 9
        for (column in 0 until 5) {
            val top = column * p
            drawRect(color, Offset(column * p, top), Size(p, size.height - 2 * top))
        }
    }
}
