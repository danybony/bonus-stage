package net.bonysoft.bonusstage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.account.Session
import net.bonysoft.bonusstage.navigation.Home
import net.bonysoft.bonusstage.navigation.navSavedStateConfiguration
import net.bonysoft.bonusstage.ui.home.HomeScreen
import net.bonysoft.bonusstage.ui.signin.SignInRoute
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.koin.compose.koinInject

@Composable
fun App(modifier: Modifier = Modifier, accounts: AccountRepository = koinInject()) {
    RetroTheme {
        val session by accounts.session.collectAsStateWithLifecycle()
        when (val current = session) {
            Session.Loading -> Box(modifier.fillMaxSize().background(RetroTheme.colors.background))
            Session.SignedOut -> SignInRoute(modifier)
            // A new back stack per account, so nothing of the previous user survives a sign-out.
            is Session.SignedIn -> key(current.account.ulid) { SignedInApp(modifier) }
        }
    }
}

@Composable
private fun SignedInApp(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(navSavedStateConfiguration, Home)
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Home> { HomeScreen() }
        },
    )
}
