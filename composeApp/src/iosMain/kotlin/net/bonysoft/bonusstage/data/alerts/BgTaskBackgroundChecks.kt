package net.bonysoft.bonusstage.data.alerts

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.bonysoft.bonusstage.raapi.RaApiException
import org.koin.mp.KoinPlatform
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSinceNow

/**
 * The wishlist set check as a BGAppRefreshTask, the iOS counterpart of WishlistCheckWorker.
 * iOS decides when it actually runs; six hours is only the earliest start, as on Android.
 * The identifier must be listed under BGTaskSchedulerPermittedIdentifiers in Info.plist.
 */
class BgTaskBackgroundChecks : BackgroundChecks {

    override fun schedule() = submit()

    override fun cancel() {
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(TASK_ID)
    }

    companion object {
        private const val TASK_ID = "net.bonysoft.bonusstage.wishlist-check"
        private const val CHECK_INTERVAL_SECONDS = 6 * 60 * 60.0
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        /**
         * Must run before the app finishes launching, and before anything schedules the task, so it is
         * called before Koin starts. The checker comes from Koin when the task runs.
         */
        fun register() {
            BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(TASK_ID, usingQueue = null) { task ->
                if (task !is BGAppRefreshTask) return@registerForTaskWithIdentifier
                // The next run is booked first, so a check that expires or fails does not stop the chain.
                submit()
                val job = scope.launch {
                    val success = try {
                        KoinPlatform.getKoin().get<WishlistAlertChecker>().check()
                        true
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: RaApiException.Unauthorized) {
                        // Signed out or key revoked: nothing to retry until the user signs in again.
                        true
                    } catch (_: RaApiException) {
                        false
                    }
                    task.setTaskCompletedWithSuccess(success)
                }
                task.expirationHandler = {
                    job.cancel()
                    task.setTaskCompletedWithSuccess(false)
                }
            }
        }

        @OptIn(ExperimentalForeignApi::class) // The NSError out-parameter, left null.
        private fun submit() {
            val request = BGAppRefreshTaskRequest(TASK_ID).apply {
                earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(CHECK_INTERVAL_SECONDS)
            }
            // Fails on the simulator and when background refresh is off; there is nothing else to try.
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, error = null)
        }
    }
}

