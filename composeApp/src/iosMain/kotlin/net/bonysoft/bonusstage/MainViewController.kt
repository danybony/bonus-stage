package net.bonysoft.bonusstage

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.ComposeUIViewController
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.navigation.ProfileTab
import org.koin.compose.koinInject
import platform.UIKit.UIViewController

@Suppress("FunctionName", "unused") // Called from Swift.
fun MainViewController(): UIViewController = ComposeUIViewController {
    val screenshot = ScreenshotMode.startAt
    if (screenshot != null) {
        val accounts = koinInject<AccountRepository>()
        LaunchedEffect(Unit) { accounts.signInDemo() }
    }
    App(startAt = screenshot ?: listOf(ProfileTab))
}
