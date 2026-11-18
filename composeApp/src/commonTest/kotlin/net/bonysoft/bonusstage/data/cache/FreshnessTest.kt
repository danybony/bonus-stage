package net.bonysoft.bonusstage.data.cache

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class FreshnessTest {
    private val now = Instant.parse("2026-11-18T12:00:00Z")

    @Test
    fun neverFetchedIsStale() = assertTrue(Freshness.isStale(null, 15.minutes, now))

    @Test
    fun youngerThanMaxAgeIsFresh() = assertFalse(Freshness.isStale(now - 14.minutes, 15.minutes, now))

    @Test
    fun exactlyMaxAgeIsStale() = assertTrue(Freshness.isStale(now - 15.minutes, 15.minutes, now))

    @Test
    fun fetchedInTheFutureIsStale() = assertTrue(Freshness.isStale(now + 1.minutes, 15.minutes, now))
}
