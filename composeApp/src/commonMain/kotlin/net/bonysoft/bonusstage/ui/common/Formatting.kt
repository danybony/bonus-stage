package net.bonysoft.bonusstage.ui.common

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** 1234567 -> "1,234,567". */
fun Long.grouped(): String {
    val digits = kotlin.math.abs(this).toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return if (this < 0) "-$grouped" else grouped
}

fun Int.grouped(): String = toLong().grouped()

private val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** "18 Nov 2026" in the device's time zone. */
fun Instant.formatDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): String =
    toLocalDateTime(timeZone).date.formatDate()

fun LocalDate.formatDate(): String = "$day ${months[month.ordinal]} $year"

/** "14:05" in the device's time zone. */
fun Instant.formatTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val time = toLocalDateTime(timeZone).time
    return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

fun Instant.localDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate = toLocalDateTime(timeZone).date

/** Compact age: "now", "5m", "3h", "2d", then the date. */
fun Instant.relativeTo(now: Instant = Clock.System.now()): String {
    val elapsed = now - this
    return when {
        elapsed.inWholeMinutes < 1 -> "now"
        elapsed.inWholeHours < 1 -> "${elapsed.inWholeMinutes}m"
        elapsed.inWholeDays < 1 -> "${elapsed.inWholeHours}h"
        elapsed.inWholeDays < 7 -> "${elapsed.inWholeDays}d"
        else -> formatDate()
    }
}

/** Strips RA's "~Hack~ " style prefixes into a tag shown separately. */
fun splitTitleTag(title: String): Pair<String?, String> {
    val match = Regex("^~([^~]+)~\\s*").find(title) ?: return null to title
    return match.groupValues[1] to title.substring(match.range.last + 1)
}
