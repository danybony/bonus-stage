package net.bonysoft.bonusstage.raapi

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.ComparableTimeMark
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Spaces requests out so callers cannot burst the API. RA does not publish its rate limits, so
 * this keeps every client to one request per [minInterval], in call order.
 */
class RequestThrottler(
    private val minInterval: Duration = DEFAULT_MIN_INTERVAL,
    private val timeSource: TimeSource.WithComparableMarks = TimeSource.Monotonic,
) {
    private val mutex = Mutex()
    private var lastRequest: ComparableTimeMark? = null

    suspend fun <T> throttle(block: suspend () -> T): T {
        mutex.withLock {
            lastRequest?.let { last ->
                val wait = minInterval - last.elapsedNow()
                if (wait.isPositive()) delay(wait)
            }
            lastRequest = timeSource.markNow()
        }
        return block()
    }

    companion object {
        val DEFAULT_MIN_INTERVAL = 350.milliseconds
    }
}
