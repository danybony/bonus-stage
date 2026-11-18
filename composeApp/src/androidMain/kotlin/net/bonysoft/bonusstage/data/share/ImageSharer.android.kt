package net.bonysoft.bonusstage.data.share

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File

@Composable
actual fun rememberImageSharer(): ImageSharer {
    val context = LocalContext.current
    return remember(context) {
        ImageSharer { png, fileName, text ->
            // Shared through a FileProvider: only this file in cache/cards is exposed.
            val dir = File(context.cacheDir, "cards").apply { mkdirs() }
            val file = File(dir, fileName).apply { writeBytes(png) }
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.cards", file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, null))
        }
    }
}

actual fun ImageBitmap.encodePng(): ByteArray =
    ByteArrayOutputStream().use { out ->
        asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)
        out.toByteArray()
    }
