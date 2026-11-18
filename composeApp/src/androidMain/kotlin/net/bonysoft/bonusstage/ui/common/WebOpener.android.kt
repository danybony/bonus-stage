package net.bonysoft.bonusstage.ui.common

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import net.bonysoft.bonusstage.ui.theme.BonusStageColors

@Composable
actual fun rememberWebOpener(): WebOpener {
    val context = LocalContext.current
    return remember(context) {
        WebOpener { url ->
            val uri = Uri.parse(url)
            try {
                // A Custom Tab when a browser supports them, the default browser otherwise.
                if (CustomTabsClient.getPackageName(context, emptyList()) != null) {
                    CustomTabsIntent.Builder()
                        .setDefaultColorSchemeParams(
                            CustomTabColorSchemeParams.Builder().setToolbarColor(BonusStageColors.background.toArgb()).build(),
                        )
                        .setShowTitle(true)
                        .build()
                        .launchUrl(context, uri)
                } else {
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            } catch (e: ActivityNotFoundException) {
                Log.w("WebOpener", "No browser for $url", e)
            }
        }
    }
}
