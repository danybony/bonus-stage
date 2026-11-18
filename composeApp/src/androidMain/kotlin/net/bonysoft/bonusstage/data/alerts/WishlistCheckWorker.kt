package net.bonysoft.bonusstage.data.alerts

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.ktor.utils.io.CancellationException
import net.bonysoft.bonusstage.data.repository.RecentAchievementsRepository
import net.bonysoft.bonusstage.raapi.RaApiException
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.util.concurrent.TimeUnit

/** Runs the wishlist set check in the background (issue #12). */
class WishlistCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result = try {
        get<WishlistAlertChecker>().check()
        Result.success()
    } catch (e: CancellationException) {
        throw e
    } catch (_: RaApiException.Unauthorized) {
        // Signed out or key revoked: nothing to retry until the user signs in again.
        Result.success()
    } catch (_: RaApiException) {
        Result.retry()
    }
}

/** Polls recent unlocks for the widget (CLAUDE.md: "Recent achievements, widget: background polling"). */
class RecentSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result = try {
        // WidgetSync redraws the widget when the cache changes.
        get<RecentAchievementsRepository>().refresh()
        Result.success()
    } catch (e: CancellationException) {
        throw e
    } catch (_: RaApiException.Unauthorized) {
        Result.success()
    } catch (_: RaApiException) {
        Result.retry()
    }
}

/** Periodic background work, only with a network, kept across reboots by WorkManager. */
class WorkManagerBackgroundChecks(private val context: Context) : BackgroundChecks {

    override fun schedule() {
        val network = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniquePeriodicWork(
            WISHLIST_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<WishlistCheckWorker>(6, TimeUnit.HOURS).setConstraints(network).build(),
        )
        workManager.enqueueUniquePeriodicWork(
            RECENT_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<RecentSyncWorker>(1, TimeUnit.HOURS).setConstraints(network).build(),
        )
    }

    override fun cancel() {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(WISHLIST_WORK)
            cancelUniqueWork(RECENT_WORK)
        }
    }

    private companion object {
        const val WISHLIST_WORK = "wishlist-set-check"
        const val RECENT_WORK = "recent-sync"
    }
}
