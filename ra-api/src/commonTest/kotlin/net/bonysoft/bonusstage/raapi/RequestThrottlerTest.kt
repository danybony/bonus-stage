package net.bonysoft.bonusstage.raapi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class RequestThrottlerTest {

    @Test
    fun spacesBurstsByMinInterval() = runTest {
        val throttler = RequestThrottler(minInterval = 500.milliseconds, timeSource = testScheduler.timeSource)
        val startTimes = mutableListOf<Long>()

        repeat(3) { launch { throttler.throttle { startTimes += currentTime } } }
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(0L, 500L, 1000L), startTimes)
    }

    @Test
    fun doesNotWaitWhenCallsAreAlreadySpaced() = runTest {
        val throttler = RequestThrottler(minInterval = 500.milliseconds, timeSource = testScheduler.timeSource)

        throttler.throttle { }
        testScheduler.advanceTimeBy(800)
        val before = currentTime
        throttler.throttle { }

        assertEquals(before, currentTime)
    }
}
