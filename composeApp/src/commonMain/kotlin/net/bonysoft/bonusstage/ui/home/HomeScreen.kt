package net.bonysoft.bonusstage.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.no
import net.bonysoft.bonusstage.resources.sign_out
import net.bonysoft.bonusstage.resources.sign_out_text
import net.bonysoft.bonusstage.resources.sign_out_title
import net.bonysoft.bonusstage.resources.yes
import net.bonysoft.bonusstage.ui.components.RetroDialog
import net.bonysoft.bonusstage.ui.components.RetroMenuItem
import net.bonysoft.bonusstage.ui.components.RetroWindow
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Temporary landing screen until the profile screen exists. */
@Composable
fun HomeScreen(modifier: Modifier = Modifier, accounts: AccountRepository = koinInject()) {
    val scope = rememberCoroutineScope()
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    val account = accounts.currentAccount ?: return
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RetroTheme.colors.background)
            .safeDrawingPadding()
            .padding(16.dp),
    ) {
        RetroWindow(title = account.username.uppercase(), modifier = Modifier.fillMaxWidth()) {
            Text("ULID ${account.ulid}", style = RetroTheme.typography.label, color = RetroTheme.colors.onWindowMuted)
            RetroMenuItem(text = stringResource(Res.string.sign_out), onClick = { confirmSignOut = true })
        }
    }
    if (confirmSignOut) {
        RetroDialog(
            title = stringResource(Res.string.sign_out_title),
            text = stringResource(Res.string.sign_out_text),
            confirmText = stringResource(Res.string.yes),
            dismissText = stringResource(Res.string.no),
            onConfirm = {
                confirmSignOut = false
                scope.launch { accounts.signOut() }
            },
            onDismiss = { confirmSignOut = false },
        )
    }
}
