package net.bonysoft.bonusstage.ui.recent

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import net.bonysoft.bonusstage.domain.RecentUnlock
import net.bonysoft.bonusstage.ui.common.localDate

data class UnlockDay(val date: LocalDate, val unlocks: List<RecentUnlock>) {
    val points: Int get() = unlocks.sumOf { it.points }
}

/** Groups unlocks by local calendar day, newest day first and newest unlock first within a day. */
fun List<RecentUnlock>.groupByDay(timeZone: TimeZone): List<UnlockDay> =
    sortedByDescending { it.unlockedAt }
        .groupBy { it.unlockedAt.localDate(timeZone) }
        .map { (date, unlocks) -> UnlockDay(date, unlocks) }
