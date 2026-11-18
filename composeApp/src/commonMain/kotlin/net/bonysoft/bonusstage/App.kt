package net.bonysoft.bonusstage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.account.Session
import net.bonysoft.bonusstage.data.repository.SettingsRepository
import net.bonysoft.bonusstage.navigation.GameDetail
import net.bonysoft.bonusstage.navigation.LibraryTab
import net.bonysoft.bonusstage.navigation.ProfileTab
import net.bonysoft.bonusstage.navigation.Recap
import net.bonysoft.bonusstage.navigation.RecentTab
import net.bonysoft.bonusstage.navigation.TopLevel
import net.bonysoft.bonusstage.navigation.WishlistTab
import net.bonysoft.bonusstage.navigation.navSavedStateConfiguration
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.tab_library
import net.bonysoft.bonusstage.resources.tab_profile
import net.bonysoft.bonusstage.resources.tab_recent
import net.bonysoft.bonusstage.resources.tab_wishlist
import net.bonysoft.bonusstage.ui.components.PixelIcons
import net.bonysoft.bonusstage.ui.components.RetroTab
import net.bonysoft.bonusstage.ui.components.RetroTabBar
import net.bonysoft.bonusstage.ui.effects.ScreenEffect
import net.bonysoft.bonusstage.ui.effects.screenEffect
import net.bonysoft.bonusstage.ui.game.GameDetailRoute
import net.bonysoft.bonusstage.ui.library.LibraryRoute
import net.bonysoft.bonusstage.ui.profile.ProfileRoute
import net.bonysoft.bonusstage.ui.recent.RecentRoute
import net.bonysoft.bonusstage.ui.share.RecapRoute
import net.bonysoft.bonusstage.ui.signin.SignInRoute
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import net.bonysoft.bonusstage.ui.wishlist.WishlistRoute
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun App(
    modifier: Modifier = Modifier,
    accounts: AccountRepository = koinInject(),
    settings: SettingsRepository = koinInject(),
) {
    RetroTheme {
        val session by accounts.session.collectAsStateWithLifecycle()
        val effect by settings.screenEffect.collectAsStateWithLifecycle(initialValue = ScreenEffect.Off)
        Box(modifier.fillMaxSize().screenEffect(effect)) {
            when (val current = session) {
                Session.Loading -> Box(Modifier.fillMaxSize().background(RetroTheme.colors.background))
                Session.SignedOut -> SignInRoute()
                // A new back stack per account, so nothing of the previous user survives a sign-out.
                is Session.SignedIn -> key(current.account.ulid) { SignedInApp() }
            }
        }
    }
}

private val topLevel: List<TopLevel> = listOf(ProfileTab, LibraryTab, RecentTab, WishlistTab)

@Composable
private fun SignedInApp(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(navSavedStateConfiguration, ProfileTab)
    val openGame: (Long) -> Unit = { backStack.add(GameDetail(it)) }
    Column(modifier = modifier.fillMaxSize().background(RetroTheme.colors.background)) {
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<ProfileTab> { ProfileRoute(onOpenGame = openGame, onOpenRecap = { backStack.add(Recap) }) }
                entry<LibraryTab> { LibraryRoute(onOpenGame = openGame) }
                entry<RecentTab> { RecentRoute(onOpenGame = openGame) }
                entry<WishlistTab> { WishlistRoute(onOpenGame = openGame) }
                entry<Recap> { RecapRoute(onBack = { backStack.removeLastOrNull() }) }
                entry<GameDetail> { GameDetailRoute(gameId = it.gameId, onBack = { backStack.removeLastOrNull() }) }
            },
        )
        RetroTabBar(
            tabs = listOf(
                RetroTab(stringResource(Res.string.tab_profile), PixelIcons.Profile),
                RetroTab(stringResource(Res.string.tab_library), PixelIcons.Library),
                RetroTab(stringResource(Res.string.tab_recent), PixelIcons.Recent),
                RetroTab(stringResource(Res.string.tab_wishlist), PixelIcons.Wishlist),
            ),
            selectedIndex = topLevel.indexOf(backStack.firstOrNull()).coerceAtLeast(0),
            onSelect = { index -> backStack.selectTab(topLevel[index]) },
        )
    }
}

/** Tabs do not stack: selecting one starts a fresh stack from it (or pops back to it). */
private fun NavBackStack<NavKey>.selectTab(tab: TopLevel) {
    if (size == 1 && first() == tab) return
    clear()
    add(tab)
}
