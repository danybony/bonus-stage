package net.bonysoft.bonusstage.data.repository

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.raapi.RaMedia
import net.bonysoft.bonusstage.raapi.model.RecentAchievement

data class RecapGame(val gameId: Long, val title: String, val iconUrl: String, val unlocks: Int, val points: Int)

data class MonthlyRecap(
    val month: YearMonth,
    val unlocks: Int,
    val hardcoreUnlocks: Int,
    val points: Int,
    val games: Int,
    val topGames: List<RecapGame>,
    val bestTitle: String?,
    val bestBadgeUrl: String?,
    val bestPoints: Int,
)

/** Builds a month's recap from its unlocks (pure, for tests). */
fun buildRecap(month: YearMonth, unlocks: List<RecentAchievement>): MonthlyRecap {
    val best = unlocks.maxByOrNull { it.points * 1000 + it.trueRatio }
    return MonthlyRecap(
        month = month,
        unlocks = unlocks.size,
        hardcoreUnlocks = unlocks.count { it.hardcoreMode },
        points = unlocks.sumOf { it.points },
        games = unlocks.distinctBy { it.gameId }.size,
        topGames = unlocks.groupBy { it.gameId }
            .map { (id, list) -> RecapGame(id, list.first().gameTitle, RaMedia.image(list.first().gameIcon), list.size, list.sumOf { it.points }) }
            .sortedWith(compareByDescending<RecapGame> { it.unlocks }.thenByDescending { it.points })
            .take(3),
        bestTitle = best?.title,
        bestBadgeUrl = best?.let { RaMedia.badge(it.badgeName) },
        bestPoints = best?.points ?: 0,
    )
}

/** Monthly recaps for the share cards: one API call per month, on demand. */
class RecapRepository(private val accounts: AccountRepository) {
    suspend fun recap(month: YearMonth, timeZone: TimeZone = TimeZone.currentSystemDefault()): MonthlyRecap {
        val start = LocalDate(month.year, month.month, 1)
        val from = start.atStartOfDayIn(timeZone).epochSeconds
        val to = start.plus(1, DateTimeUnit.MONTH).atStartOfDayIn(timeZone).epochSeconds - 1
        val unlocks = accounts.client().getAchievementsEarnedBetween(accounts.requireUlid(), from, to)
        return buildRecap(month, unlocks)
    }
}
