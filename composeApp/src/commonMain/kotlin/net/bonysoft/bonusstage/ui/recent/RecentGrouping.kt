package net.bonysoft.bonusstage.ui.recent

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import net.bonysoft.bonusstage.ui.common.localDate
import kotlin.time.Instant

data class Day<T>(val date: LocalDate, val items: List<T>)

/** Groups items by local calendar day, newest day first and newest item first within a day. */
fun <T> List<T>.groupByDay(timeZone: TimeZone, at: (T) -> Instant): List<Day<T>> =
    sortedByDescending(at)
        .groupBy { at(it).localDate(timeZone) }
        .map { (date, items) -> Day(date, items) }
