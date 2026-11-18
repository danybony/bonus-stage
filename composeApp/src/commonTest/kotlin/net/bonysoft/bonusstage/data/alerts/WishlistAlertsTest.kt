package net.bonysoft.bonusstage.data.alerts

import net.bonysoft.bonusstage.domain.SetStatus
import net.bonysoft.bonusstage.domain.WishlistGame
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WishlistAlertsTest {

    private fun game(id: Long, status: SetStatus) = WishlistGame(id, "Game $id", "", "Console", status)

    private val claimed = SetStatus.InDevelopment("Dev", isRevision = false, expectedBy = null)
    private val published = SetStatus.Published(achievements = 30, points = 400)

    @Test
    fun claimOnAGameWithoutSet() {
        val alert = detectAlerts(listOf(game(1, SetStatus.NoSet)), listOf(game(1, claimed))).single()

        assertIs<WishlistAlert.SetInDevelopment>(alert)
        assertEquals("Dev", alert.developer)
    }

    @Test
    fun publishedAfterDevelopmentOrFromNothing() {
        val alerts = detectAlerts(
            listOf(game(1, claimed), game(2, SetStatus.NoSet)),
            listOf(game(1, published), game(2, published)),
        )

        assertEquals(listOf(1L, 2L), alerts.map { it.game.gameId })
        assertTrue(alerts.all { it is WishlistAlert.SetPublished })
    }

    @Test
    fun noAlertWithoutChangeOrForNewEntries() {
        val alerts = detectAlerts(
            listOf(game(1, claimed), game(2, published)),
            listOf(game(1, claimed), game(2, published), game(3, published)),
        )

        assertTrue(alerts.isEmpty())
    }

    @Test
    fun revisionClaimOnPublishedSetIsNotAnAlert() {
        assertTrue(detectAlerts(listOf(game(1, published)), listOf(game(1, published))).isEmpty())
    }

    @Test
    fun claimDroppedIsNotAnAlert() {
        assertTrue(detectAlerts(listOf(game(1, claimed)), listOf(game(1, SetStatus.NoSet))).isEmpty())
    }
}
