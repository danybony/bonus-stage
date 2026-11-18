package net.bonysoft.bonusstage.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler

/** Safari for now; an SFSafariViewController can come with the iOS release. */
@Composable
actual fun rememberWebOpener(): WebOpener {
    val uriHandler = LocalUriHandler.current
    return remember(uriHandler) { WebOpener { uriHandler.openUri(it) } }
}
