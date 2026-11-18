package net.bonysoft.bonusstage.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import net.bonysoft.bonusstage.data.alerts.WishlistCheckWorker

/**
 * Runs the wishlist set check now, instead of waiting for the periodic schedule:
 * `adb shell am broadcast -a net.bonysoft.bonusstage.debug.CHECK_WISHLIST -p net.bonysoft.bonusstage`
 */
class DebugCommandsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CHECK_WISHLIST) {
            WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<WishlistCheckWorker>().build())
        }
    }

    private companion object {
        const val ACTION_CHECK_WISHLIST = "net.bonysoft.bonusstage.debug.CHECK_WISHLIST"
    }
}
