package net.bonysoft.bonusstage.data.share

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun rememberImageSharer(): ImageSharer = remember {
    ImageSharer { png, _, text ->
        val data = png.usePinned { NSData.create(bytes = it.addressOf(0), length = png.size.toULong()) }
        val image = UIImage.imageWithData(data) ?: return@ImageSharer
        val controller = UIActivityViewController(activityItems = listOf(image, text), applicationActivities = null)
        @Suppress("DEPRECATION")
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return@ImageSharer
        var top = root
        while (top.presentedViewController != null) top = top.presentedViewController!!
        top.presentViewController(controller, animated = true, completion = null)
    }
}

actual fun ImageBitmap.encodePng(): ByteArray =
    Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes ?: ByteArray(0)
