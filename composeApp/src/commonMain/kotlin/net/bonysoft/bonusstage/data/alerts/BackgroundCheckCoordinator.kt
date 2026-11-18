package net.bonysoft.bonusstage.data.alerts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.account.Session

/** Keeps the background check scheduled exactly while someone is signed in. */
class BackgroundCheckCoordinator(accounts: AccountRepository, checks: BackgroundChecks, scope: CoroutineScope) {
    init {
        scope.launch {
            accounts.session
                .map { it is Session.SignedIn }
                .distinctUntilChanged()
                .collect { signedIn -> if (signedIn) checks.schedule() else checks.cancel() }
        }
    }
}
