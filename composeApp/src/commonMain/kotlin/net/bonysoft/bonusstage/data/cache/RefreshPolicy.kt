package net.bonysoft.bonusstage.data.cache

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * How long cached data stays fresh (ADR 0002, table in CLAUDE.md). Tune the values here only.
 * A pull-to-refresh always fetches, whatever the age.
 */
data class RefreshPolicy(
    /** Consoles and game catalog. */
    val catalog: Duration = 7.days,
    /** Profile, points and awards: refreshed on open when older than this. */
    val profile: Duration = 15.minutes,
    /** The completion progress list behind the library. */
    val library: Duration = 15.minutes,
    /** Game detail: "on every open", with a short guard against refetching on back and forth. */
    val gameDetail: Duration = 1.minutes,
    /** Recent achievements, also polled in the background. */
    val recentAchievements: Duration = 10.minutes,
    /** Want to Play list and active claims: a few times a day. */
    val wishlist: Duration = 6.hours,
)

object Freshness {
    /** True when data fetched at [fetchedAt] must be fetched again at [now]. */
    fun isStale(fetchedAt: Instant?, maxAge: Duration, now: Instant): Boolean {
        if (fetchedAt == null) return true
        // A clock moved backwards (manual change, timezone bug): do not trust the cache.
        if (fetchedAt > now) return true
        return now - fetchedAt >= maxAge
    }
}
