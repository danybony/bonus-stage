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

/** A few checks a day, only with a network, kept across reboots by WorkManager. */
class WorkManagerBackgroundChecks(private val context: Context) : BackgroundChecks {

    override fun schedule() {
        val request = PeriodicWorkRequestBuilder<WishlistCheckWorker>(6, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    override fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private companion object {
        const val WORK_NAME = "wishlist-set-check"
    }
}
