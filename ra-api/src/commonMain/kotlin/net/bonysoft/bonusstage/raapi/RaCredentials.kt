package net.bonysoft.bonusstage.raapi

/**
 * The user's own RetroAchievements credentials. The web API key is a secret: never log it.
 */
data class RaCredentials(
    val username: String,
    val webApiKey: String,
) {
    override fun toString(): String = "RaCredentials(username=$username, webApiKey=***)"
}
