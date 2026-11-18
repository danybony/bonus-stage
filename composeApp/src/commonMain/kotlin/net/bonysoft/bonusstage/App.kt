package net.bonysoft.bonusstage

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
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
import net.bonysoft.bonusstage.navigation.FriendsList
import net.bonysoft.bonusstage.navigation.GameDetail
import net.bonysoft.bonusstage.navigation.GameSearch
import net.bonysoft.bonusstage.navigation.LibraryTab
import net.bonysoft.bonusstage.navigation.ProfileTab
import net.bonysoft.bonusstage.navigation.Recap
import net.bonysoft.bonusstage.navigation.RecentTab
import net.bonysoft.bonusstage.navigation.RomScan
import net.bonysoft.bonusstage.navigation.TopLevel
import net.bonysoft.bonusstage.navigation.WishlistTab
import net.bonysoft.bonusstage.navigation.navSavedStateConfiguration
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.tab_library
import net.bonysoft.bonusstage.resources.tab_profile
import net.bonysoft.bonusstage.resources.tab_recent
import net.bonysoft.bonusstage.resources.tab_wishlist
import net.bonysoft.bonusstage.ui.adaptive.LocalWindowLayout
import net.bonysoft.bonusstage.ui.adaptive.ProvideWindowLayout
import net.bonysoft.bonusstage.ui.components.PixelIcons
import net.bonysoft.bonusstage.ui.components.RetroNavigationRail
import net.bonysoft.bonusstage.ui.components.RetroTab
import net.bonysoft.bonusstage.ui.components.RetroTabBar
import net.bonysoft.bonusstage.ui.effects.ScreenEffect
import net.bonysoft.bonusstage.ui.effects.screenEffect
import net.bonysoft.bonusstage.ui.friends.FriendsListRoute
import net.bonysoft.bonusstage.ui.game.GameDetailRoute
import net.bonysoft.bonusstage.ui.library.LibraryRoute
import net.bonysoft.bonusstage.ui.profile.ProfileRoute
import net.bonysoft.bonusstage.ui.recent.RecentRoute
import net.bonysoft.bonusstage.ui.rom.RomScanRoute
import net.bonysoft.bonusstage.ui.search.GameSearchRoute
import net.bonysoft.bonusstage.ui.share.RecapRoute
import net.bonysoft.bonusstage.ui.signin.SignInRoute
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import net.bonysoft.bonusstage.ui.transitions.GameOrigin
import net.bonysoft.bonusstage.ui.transitions.SharedGameTransitions
import net.bonysoft.bonusstage.ui.wishlist.WishlistRoute
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun App(
    modifier: Modifier = Modifier,
    // Where a signed-in session opens; the platform can override it (iOS screenshots in CI).
    startAt: List<NavKey> = listOf(ProfileTab),
    accounts: AccountRepository = koinInject(),
    settings: SettingsRepository = koinInject(),
) {
    RetroTheme {
        val session by accounts.session.collectAsStateWithLifecycle()
        val effect by settings.screenEffect.collectAsStateWithLifecycle(initialValue = ScreenEffect.Off)
        ProvideWindowLayout(modifier.fillMaxSize().screenEffect(effect)) {
            when (val current = session) {
                Session.Loading -> Box(Modifier.fillMaxSize().background(RetroTheme.colors.background))
                Session.SignedOut -> SignInRoute()
                // A new back stack per account, so nothing of the previous user survives a sign-out.
                is Session.SignedIn -> key(current.account.ulid) { SignedInApp(startAt) }
            }
        }
    }
}

private const val TRANSITION_MS = 350

private val topLevel: List<TopLevel> = listOf(ProfileTab, LibraryTab, RecentTab, WishlistTab)

@Composable
private fun SignedInApp(startAt: List<NavKey>, modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(navSavedStateConfiguration, *startAt.toTypedArray())
    val tabs = listOf(
        RetroTab(stringResource(Res.string.tab_profile), PixelIcons.Profile),
        RetroTab(stringResource(Res.string.tab_library), PixelIcons.Library),
        RetroTab(stringResource(Res.string.tab_recent), PixelIcons.Recent),
        RetroTab(stringResource(Res.string.tab_wishlist), PixelIcons.Wishlist),
    )
    val selected = topLevel.indexOf(backStack.firstOrNull()).coerceAtLeast(0)
    val onSelect: (Int) -> Unit = { index -> backStack.selectTab(topLevel[index]) }
    // Movable, so switching between rail and bottom bar (rotation, unfolding) keeps every screen's state.
    val content = remember(backStack) {
        movableContentOf { contentModifier: Modifier ->
        val openGame = { origin: GameOrigin -> { gameId: Long -> backStack.add(GameDetail(gameId, origin)); Unit } }
        SharedGameTransitions(contentModifier) {
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            // Lets NavDisplay drive the shared elements with the back gesture too.
            sharedTransitionScope = this,
            // Cross-fades everywhere, including predictive back: the default predictive pop shrinks
            // the screen, and shared elements (tracked in full-screen coordinates) left it hollow.
            transitionSpec = { fadeIn(tween(TRANSITION_MS)) togetherWith fadeOut(tween(TRANSITION_MS)) },
            popTransitionSpec = { fadeIn(tween(TRANSITION_MS)) togetherWith fadeOut(tween(TRANSITION_MS)) },
            predictivePopTransitionSpec = { fadeIn(tween(TRANSITION_MS)) togetherWith fadeOut(tween(TRANSITION_MS)) },
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<ProfileTab> {
                    ProfileRoute(onOpenGame = openGame(GameOrigin.Profile), onOpenRecap = { backStack.add(Recap) }, onOpenFriends = { backStack.add(FriendsList) })
                }
                entry<LibraryTab> { LibraryRoute(onOpenGame = openGame(GameOrigin.Library), onScanRoms = { backStack.add(RomScan) }) }
                entry<RecentTab> { RecentRoute(onOpenGame = openGame(GameOrigin.Other), onManageFriends = { backStack.add(FriendsList) }) }
                entry<WishlistTab> { WishlistRoute(onOpenGame = openGame(GameOrigin.Wishlist), onSearch = { backStack.add(GameSearch) }) }
                entry<FriendsList> { FriendsListRoute(onBack = { backStack.removeLastOrNull() }) }
                entry<GameSearch> { GameSearchRoute(onBack = { backStack.removeLastOrNull() }) }
                entry<RomScan> { RomScanRoute(onBack = { backStack.removeLastOrNull() }, onOpenGame = openGame(GameOrigin.Other)) }
                entry<Recap> { RecapRoute(onBack = { backStack.removeLastOrNull() }) }
                entry<GameDetail> { GameDetailRoute(gameId = it.gameId, origin = it.origin, onBack = { backStack.removeLastOrNull() }) }
            },
        )
        }
        }
    }
    if (LocalWindowLayout.current.useNavigationRail) {
        Row(modifier = modifier.fillMaxSize().background(RetroTheme.colors.background)) {
            RetroNavigationRail(tabs, selected, onSelect)
            content(Modifier.weight(1f).fillMaxHeight())
        }
    } else {
        Column(modifier = modifier.fillMaxSize().background(RetroTheme.colors.background)) {
            content(Modifier.weight(1f).fillMaxWidth())
            RetroTabBar(tabs, selected, onSelect)
        }
    }
}

/** Tabs do not stack: selecting one starts a fresh stack from it (or pops back to it). */
private fun NavBackStack<NavKey>.selectTab(tab: TopLevel) {
    if (size == 1 && first() == tab) return
    clear()
    add(tab)
}
