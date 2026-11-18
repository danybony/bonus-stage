package net.bonysoft.bonusstage.ui.common

import androidx.compose.runtime.Composable

/** Pages on retroachievements.org the app links to (the API cannot add to lists or follow people). */
object RaLinks {
    private const val SITE = "https://retroachievements.org"

    const val SETTINGS = "$SITE/settings"

    fun game(gameId: Long) = "$SITE/game/$gameId"

    fun user(username: String) = "$SITE/user/$username"
}

/** Opens a web page: an in-app browser tab where available (Chrome Custom Tabs), the browser otherwise. */
fun interface WebOpener {
    fun open(url: String)
}

@Composable
expect fun rememberWebOpener(): WebOpener
