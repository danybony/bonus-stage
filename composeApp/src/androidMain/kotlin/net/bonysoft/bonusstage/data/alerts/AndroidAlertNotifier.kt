package net.bonysoft.bonusstage.data.alerts

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import net.bonysoft.bonusstage.shared.R
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.notification_channel_description
import net.bonysoft.bonusstage.resources.notification_channel_name
import net.bonysoft.bonusstage.resources.notification_in_development_text
import net.bonysoft.bonusstage.resources.notification_in_development_title
import net.bonysoft.bonusstage.resources.notification_published_text
import net.bonysoft.bonusstage.resources.notification_published_title
import net.bonysoft.bonusstage.ui.common.splitTitleTag
import org.jetbrains.compose.resources.getString

/** One notification per alert, in the "Set alerts" channel, opening the app when tapped. */
class AndroidAlertNotifier(private val context: Context) : AlertNotifier {

    override suspend fun notify(alerts: List<WishlistAlert>) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            android.os.Build.VERSION.SDK_INT >= 33
        ) {
            return
        }
        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(getString(Res.string.notification_channel_name))
                .setDescription(getString(Res.string.notification_channel_description))
                .build(),
        )
        val open = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
        alerts.forEach { alert ->
            val title = splitTitleTag(alert.game.title).second
            val (heading, text) = when (alert) {
                is WishlistAlert.SetInDevelopment -> getString(Res.string.notification_in_development_title, title) to
                    getString(Res.string.notification_in_development_text, alert.developer)
                is WishlistAlert.SetPublished -> getString(Res.string.notification_published_title, title) to
                    getString(Res.string.notification_published_text, alert.achievements.toString())
            }
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(heading)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(open)
                .setAutoCancel(true)
                .build()
            @Suppress("MissingPermission") // Checked above.
            manager.notify(alert.game.gameId.toInt(), notification)
        }
    }

    private companion object {
        const val CHANNEL_ID = "set_alerts"
    }
}
