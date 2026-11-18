package net.bonysoft.bonusstage.data.repository

import kotlinx.datetime.YearMonth
import net.bonysoft.bonusstage.raapi.model.RecentAchievement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecapTest {

    private fun unlock(id: Long, game: Long, points: Int, hardcore: Boolean = true) = RecentAchievement(
        date = "2026-11-10 10:00:00", hardcoreMode = hardcore, achievementId = id, title = "A$id", description = "",
        badgeName = "$id", points = points, trueRatio = points, gameTitle = "Game $game", gameIcon = "/Images/$game.png",
        gameId = game, consoleName = "Console",
    )

    @Test
    fun summarisesTheMonth() {
        val recap = buildRecap(
            YearMonth(2026, 11),
            listOf(unlock(1, 10, 5), unlock(2, 10, 10), unlock(3, 20, 25, hardcore = false), unlock(4, 30, 3)),
        )

        assertEquals(4, recap.unlocks)
        assertEquals(3, recap.hardcoreUnlocks)
        assertEquals(43, recap.points)
        assertEquals(3, recap.games)
        assertEquals(listOf(10L, 20L, 30L), recap.topGames.map { it.gameId })
        assertEquals("A3", recap.bestTitle)
    }

    @Test
    fun emptyMonth() {
        val recap = buildRecap(YearMonth(2026, 1), emptyList())

        assertEquals(0, recap.unlocks)
        assertNull(recap.bestTitle)
    }
}
