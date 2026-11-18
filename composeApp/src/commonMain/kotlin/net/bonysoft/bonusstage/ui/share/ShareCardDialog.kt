package net.bonysoft.bonusstage.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import net.bonysoft.bonusstage.data.share.encodePng
import net.bonysoft.bonusstage.data.share.rememberImageSharer
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.close
import net.bonysoft.bonusstage.resources.share
import net.bonysoft.bonusstage.resources.share_preparing
import net.bonysoft.bonusstage.ui.components.RetroButton
import net.bonysoft.bonusstage.ui.components.RetroButtonStyle
import org.jetbrains.compose.resources.stringResource

/** [ShareableCard] in a full-width dialog. */
@Composable
fun ShareCardDialog(
    fileName: String,
    shareText: String,
    imageCount: Int,
    onDismiss: () -> Unit,
    card: @Composable (onImageLoaded: (String) -> Unit) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        ShareableCard(fileName, shareText, imageCount, onClose = onDismiss, card = card)
    }
}

/**
 * Shows a card scaled to the available width and shares it as a PNG at full size. The card is
 * recorded into a graphics layer before scaling, so the exported image keeps the card's own
 * resolution. Share is enabled once all [imageCount] images have loaded.
 */
@Composable
fun ShareableCard(
    fileName: String,
    shareText: String,
    imageCount: Int,
    modifier: Modifier = Modifier,
    onClose: (() -> Unit)? = null,
    card: @Composable (onImageLoaded: (String) -> Unit) -> Unit,
) {
    val layer = rememberGraphicsLayer()
    val sharer = rememberImageSharer()
    val scope = rememberCoroutineScope()
    var loaded by remember(fileName) { mutableStateOf(setOf<String>()) }
    val ready = loaded.size >= imageCount
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            val scale = minOf(1f, (maxWidth - 32.dp) / CardWidth)
            Box(Modifier.size(CardWidth * scale, CardHeight * scale), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .requiredSize(CardWidth, CardHeight)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .drawWithContent {
                            layer.record { this@drawWithContent.drawContent() }
                            drawLayer(layer)
                        },
                ) {
                    card { url -> loaded = loaded + url }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (onClose != null) {
                RetroButton(text = stringResource(Res.string.close), onClick = onClose, style = RetroButtonStyle.Secondary)
            }
            RetroButton(
                text = stringResource(if (ready) Res.string.share else Res.string.share_preparing),
                enabled = ready,
                onClick = {
                    scope.launch {
                        val png = layer.toImageBitmap().encodePng()
                        sharer.share(png, fileName, shareText)
                    }
                },
            )
        }
    }
}
