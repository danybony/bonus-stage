package net.bonysoft.bonusstage.debug

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import net.bonysoft.bonusstage.data.alerts.WishlistCheckWorker
import net.bonysoft.bonusstage.widget.BonusStageWidgetReceiver

/**
 * Development hooks, debug builds only:
 * - run the wishlist set check now instead of waiting for the periodic schedule:
 *   `adb shell am broadcast -a net.bonysoft.bonusstage.debug.CHECK_WISHLIST -p net.bonysoft.bonusstage`
 * - ask the launcher to add the home screen widget:
 *   `adb shell am broadcast -a net.bonysoft.bonusstage.debug.PIN_WIDGET -p net.bonysoft.bonusstage`
 */
class DebugCommandsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CHECK_WISHLIST ->
                WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<WishlistCheckWorker>().build())
            ACTION_PIN_WIDGET -> try {
                AppWidgetManager.getInstance(context)
                    .requestPinAppWidget(ComponentName(context, BonusStageWidgetReceiver::class.java), null, null)
            } catch (e: IllegalStateException) {
                Log.w("DebugCommands", "Open the app first: pinning needs a foreground activity", e)
            }
        }
    }

    private companion object {
        const val ACTION_CHECK_WISHLIST = "net.bonysoft.bonusstage.debug.CHECK_WISHLIST"
        const val ACTION_PIN_WIDGET = "net.bonysoft.bonusstage.debug.PIN_WIDGET"
    }
}
