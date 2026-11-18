package net.bonysoft.bonusstage.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.uikit.LocalUIViewController
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIColor

/** An in-app Safari sheet in the app's colours; Safari itself for links it cannot show. */
@Composable
actual fun rememberWebOpener(): WebOpener {
    val controller = LocalUIViewController.current
    val uriHandler = LocalUriHandler.current
    return remember(controller, uriHandler) {
        WebOpener { url ->
            val nsUrl = NSURL.URLWithString(url)
            // SFSafariViewController only takes http(s) pages and throws on anything else.
            if (nsUrl == null || nsUrl.scheme !in setOf("http", "https")) {
                uriHandler.openUri(url)
                return@WebOpener
            }
            val safari = SFSafariViewController(uRL = nsUrl).apply {
                preferredBarTintColor = rgb(0x1B1A33)
                preferredControlTintColor = rgb(0xF4B536)
            }
            // Present from the topmost controller, in case a sheet is already showing.
            var top = controller
            while (true) top = top.presentedViewController ?: break
            top.presentViewController(safari, animated = true, completion = null)
        }
    }
}

private fun rgb(hex: Int) = UIColor.colorWithRed(
    red = (hex shr 16 and 0xFF) / 255.0,
    green = (hex shr 8 and 0xFF) / 255.0,
    blue = (hex and 0xFF) / 255.0,
    alpha = 1.0,
)
