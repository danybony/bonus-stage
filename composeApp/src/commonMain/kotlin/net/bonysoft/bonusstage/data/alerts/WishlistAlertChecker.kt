package net.bonysoft.bonusstage.data.alerts

import kotlinx.coroutines.flow.first
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.account.Session
import net.bonysoft.bonusstage.data.repository.WishlistRepository

/** Shows alerts to the user (system notifications). */
fun interface AlertNotifier {
    suspend fun notify(alerts: List<WishlistAlert>)
}

/**
 * The background set check: refreshes the wishlist and active claims (two API calls, whatever the
 * list size) and notifies about changes since the last check.
 */
class WishlistAlertChecker(
    private val accounts: AccountRepository,
    private val wishlist: WishlistRepository,
    private val notifier: AlertNotifier,
) {
    suspend fun check(): List<WishlistAlert> {
        // In a background process the stored account may still be loading.
        if (accounts.session.first { it != Session.Loading } !is Session.SignedIn) return emptyList()
        val firstSync = !wishlist.hasBeenFetched()
        val before = wishlist.cachedWishlist()
        wishlist.refresh(force = true)
        // The first sync only records a baseline: everything would look new.
        if (firstSync) return emptyList()
        val alerts = detectAlerts(before, wishlist.cachedWishlist())
        if (alerts.isNotEmpty()) notifier.notify(alerts)
        return alerts
    }
}
