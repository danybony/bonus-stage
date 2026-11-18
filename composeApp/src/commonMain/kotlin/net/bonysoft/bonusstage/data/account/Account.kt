package net.bonysoft.bonusstage.data.account

import kotlinx.serialization.Serializable
import net.bonysoft.bonusstage.raapi.RaCredentials

/** The signed-in RetroAchievements user. Stored encrypted by [CredentialStore]. */
@Serializable
data class Account(
    val username: String,
    /** Stable user id: usernames can change, so everything cached is keyed by this. */
    val ulid: String,
    val webApiKey: String,
    /** The bundled demo account: served by DemoEngine, never calls the real API. */
    val isDemo: Boolean = false,
) {
    val credentials: RaCredentials get() = RaCredentials(username, webApiKey)

    override fun toString(): String = "Account(username=$username, ulid=$ulid, isDemo=$isDemo)"
}

sealed interface Session {
    data object Loading : Session
    data object SignedOut : Session
    data class SignedIn(val account: Account) : Session
}
