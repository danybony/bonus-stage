package net.bonysoft.bonusstage.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import net.bonysoft.bonusstage.ui.theme.PixelSize

/**
 * Text that can be censored: when [hidden], each line is covered by a solid pixel bar of the same
 * width, and screen readers get [hiddenDescription] instead of the text. Works on every platform
 * version, unlike a blur.
 */
@Composable
fun SpoilerText(
    text: String,
    hidden: Boolean,
    style: TextStyle,
    color: Color,
    barColor: Color,
    hiddenDescription: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val censored = if (hidden) {
        Modifier
            .clearAndSetSemantics { contentDescription = hiddenDescription }
            .drawWithContent {
                val result = layout ?: return@drawWithContent
                val p = PixelSize.toPx()
                for (line in 0 until result.lineCount) {
                    val top = result.getLineTop(line) + p
                    val height = result.getLineBottom(line) - result.getLineTop(line) - 2 * p
                    val left = result.getLineLeft(line)
                    val width = result.getLineRight(line) - left
                    drawRect(barColor, Offset(left, top), Size(width, height))
                }
            }
    } else {
        Modifier
    }
    Text(
        text = text,
        style = style,
        color = if (hidden) Color.Transparent else color,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { layout = it },
        modifier = modifier.then(censored),
    )
}
