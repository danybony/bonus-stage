package net.bonysoft.bonusstage.data.alerts

import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.notification_in_development_text
import net.bonysoft.bonusstage.resources.notification_in_development_title
import net.bonysoft.bonusstage.resources.notification_published_text
import net.bonysoft.bonusstage.resources.notification_published_title
import net.bonysoft.bonusstage.ui.common.splitTitleTag
import org.jetbrains.compose.resources.getString
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

/** Local notifications on iOS. Background scheduling (BGTaskScheduler) comes with the iOS release. */
class IosAlertNotifier : AlertNotifier {
    override suspend fun notify(alerts: List<WishlistAlert>) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        alerts.forEach { alert ->
            val title = splitTitleTag(alert.game.title).second
            val content = UNMutableNotificationContent().apply {
                when (alert) {
                    is WishlistAlert.SetInDevelopment -> {
                        setTitle(getString(Res.string.notification_in_development_title, title))
                        setBody(getString(Res.string.notification_in_development_text, alert.developer))
                    }
                    is WishlistAlert.SetPublished -> {
                        setTitle(getString(Res.string.notification_published_title, title))
                        setBody(getString(Res.string.notification_published_text, alert.achievements.toString()))
                    }
                }
            }
            center.addNotificationRequest(
                UNNotificationRequest.requestWithIdentifier("set-alert-${alert.game.gameId}", content, trigger = null),
                withCompletionHandler = null,
            )
        }
    }
}

/** No periodic check on iOS yet; see IosAlertNotifier. */
class NoBackgroundChecks : BackgroundChecks {
    override fun schedule() = Unit
    override fun cancel() = Unit
}
