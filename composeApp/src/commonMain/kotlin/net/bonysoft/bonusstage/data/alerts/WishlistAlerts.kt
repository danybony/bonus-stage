package net.bonysoft.bonusstage.data.alerts

import net.bonysoft.bonusstage.domain.SetStatus
import net.bonysoft.bonusstage.domain.WishlistGame

/** Something worth a notification about a game on the wishlist. */
sealed interface WishlistAlert {
    val game: WishlistGame

    /** Someone claimed the game: a set is being made. */
    data class SetInDevelopment(override val game: WishlistGame, val developer: String) : WishlistAlert

    /** The set went live: the game now has achievements. */
    data class SetPublished(override val game: WishlistGame, val achievements: Int) : WishlistAlert
}

/**
 * Compares the wishlist before and after a refresh. Only games present both times are considered,
 * so adding a game to the list never triggers an alert.
 */
fun detectAlerts(before: List<WishlistGame>, after: List<WishlistGame>): List<WishlistAlert> {
    val previous = before.associateBy { it.gameId }
    return after.mapNotNull { game ->
        val old = previous[game.gameId]?.status ?: return@mapNotNull null
        when (val new = game.status) {
            is SetStatus.Published ->
                if (old !is SetStatus.Published) WishlistAlert.SetPublished(game, new.achievements) else null
            is SetStatus.InDevelopment ->
                if (old is SetStatus.NoSet) WishlistAlert.SetInDevelopment(game, new.developer) else null
            SetStatus.NoSet -> null
        }
    }
}
