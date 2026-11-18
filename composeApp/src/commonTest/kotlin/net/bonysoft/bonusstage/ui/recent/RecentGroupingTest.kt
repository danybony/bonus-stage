package net.bonysoft.bonusstage.ui.recent

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import net.bonysoft.bonusstage.domain.RecentUnlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class RecentGroupingTest {

    private fun unlock(id: Long, at: String, points: Int = 5) =
        RecentUnlock(id, "A$id", "", points, "", hardcore = true, Instant.parse(at), 1, "Game", "", "Console")

    @Test
    fun groupsByLocalDayNewestFirst() {
        val unlocks = listOf(
            unlock(1, "2026-11-17T10:00:00Z"),
            unlock(2, "2026-11-18T09:00:00Z", points = 10),
            unlock(3, "2026-11-18T23:30:00Z"),
        )

        val days = unlocks.groupByDay(TimeZone.UTC)

        assertEquals(listOf(LocalDate(2026, 11, 18), LocalDate(2026, 11, 17)), days.map { it.date })
        assertEquals(listOf(3L, 2L), days[0].unlocks.map { it.achievementId })
        assertEquals(15, days[0].points)
    }

    @Test
    fun usesTheDeviceTimeZone() {
        // 23:30 UTC is already the next day in Rome.
        val days = listOf(unlock(1, "2026-11-18T23:30:00Z")).groupByDay(TimeZone.of("Europe/Rome"))

        assertEquals(LocalDate(2026, 11, 19), days.single().date)
    }
}
