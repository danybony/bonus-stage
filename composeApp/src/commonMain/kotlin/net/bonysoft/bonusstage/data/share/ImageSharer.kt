package net.bonysoft.bonusstage.data.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

/** Opens the system share sheet with a PNG image. */
fun interface ImageSharer {
    fun share(png: ByteArray, fileName: String, text: String)
}

@Composable
expect fun rememberImageSharer(): ImageSharer

expect fun ImageBitmap.encodePng(): ByteArray
