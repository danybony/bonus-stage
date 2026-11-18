package net.bonysoft.bonusstage.ui.transitions

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import kotlinx.serialization.Serializable

/** The parts of a game that move from a list row to the game screen. */
enum class GamePart { Icon, Title, Console, Tag, Award }

/**
 * Where a game was opened from. Part of the shared element keys, so the game screen animates only
 * with the list that opened it: switching tabs between two lists showing the same game must not.
 */
@Serializable
enum class GameOrigin { Library, Profile, Wishlist, Other }

@OptIn(ExperimentalSharedTransitionApi::class)
private val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/** Makes shared element transitions available to every navigation entry inside. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedGameTransitions(modifier: Modifier = Modifier, content: @Composable SharedTransitionScope.() -> Unit) {
    SharedTransitionLayout(modifier) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            content()
        }
    }
}

/**
 * Tags a part of a game so it moves between the list that opened the game ([origin]) and the game
 * screen. Text is scaled rather than re-laid out, so styles can differ between the two screens.
 * A no-op for [GameOrigin.Other] and outside SharedGameTransitions or a navigation entry
 * (previews, dialogs, cards).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedGame(gameId: Long, part: GamePart, origin: GameOrigin): Modifier {
    if (origin == GameOrigin.Other) return this
    val transitions = LocalSharedTransitionScope.current ?: return this
    val entryScope = LocalNavAnimatedContentScope.current
    return with(transitions) {
        val state = rememberSharedContentState(key = "game-$gameId-${part.name}-${origin.name}")
        if (part == GamePart.Icon) {
            this@sharedGame.sharedElement(state, animatedVisibilityScope = entryScope)
        } else {
            this@sharedGame.sharedBounds(
                state,
                animatedVisibilityScope = entryScope,
                resizeMode = SharedTransitionScope.ResizeMode.scaleToBounds(),
            )
        }
    }
}
