package net.bonysoft.bonusstage

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import net.bonysoft.bonusstage.navigation.Home
import net.bonysoft.bonusstage.navigation.navSavedStateConfiguration
import net.bonysoft.bonusstage.ui.gallery.ThemeGallery
import net.bonysoft.bonusstage.ui.theme.RetroTheme

@Composable
fun App(modifier: Modifier = Modifier) {
    RetroTheme {
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
                entry<Home> { ThemeGallery() }
            },
        )
    }
}
