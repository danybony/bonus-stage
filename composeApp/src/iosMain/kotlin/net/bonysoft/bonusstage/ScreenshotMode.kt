package net.bonysoft.bonusstage

import androidx.navigation3.runtime.NavKey
import net.bonysoft.bonusstage.navigation.GameDetail
import net.bonysoft.bonusstage.navigation.LibraryTab
import net.bonysoft.bonusstage.navigation.ProfileTab
import net.bonysoft.bonusstage.navigation.RecentTab
import net.bonysoft.bonusstage.navigation.WishlistTab
import platform.Foundation.NSProcessInfo

/**
 * CI takes iOS screenshots without UI tests: `simctl launch <app> -screenshot library` signs in
 * to the demo account and opens that screen.
 */
internal object ScreenshotMode {
    private val screens: Map<String, List<NavKey>> = mapOf(
        "profile" to listOf(ProfileTab),
        "library" to listOf(LibraryTab),
        "recent" to listOf(RecentTab),
        "wishlist" to listOf(WishlistTab),
        "game" to listOf(LibraryTab, GameDetail(gameId = 1)),
    )

    /** The screen asked for at launch, or null for a normal start. */
    val startAt: List<NavKey>? by lazy {
        val arguments = NSProcessInfo.processInfo.arguments.map { it.toString() }
        val index = arguments.indexOf("-screenshot")
        arguments.getOrNull(index + 1)?.takeIf { index >= 0 }?.let(screens::get)
    }
}
