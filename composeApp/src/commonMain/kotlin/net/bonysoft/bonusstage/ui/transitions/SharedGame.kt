package net.bonysoft.bonusstage.ui.transitions

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/** The parts of a game that move from a list row to the game screen. */
enum class GamePart { Icon, Title, Console }

@OptIn(ExperimentalSharedTransitionApi::class)
private val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/** Makes shared element transitions available to every navigation entry inside. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedGameTransitions(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    SharedTransitionLayout(modifier) {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            content()
        }
    }
}

/**
 * Tags a game's icon, title or console so it moves between screens when the game is opened.
 * Text is scaled rather than re-laid out, so title styles can differ between the two screens.
 * A no-op outside SharedGameTransitions or a navigation entry (previews, dialogs, cards).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedGame(gameId: Long, part: GamePart): Modifier {
    val transitions = LocalSharedTransitionScope.current ?: return this
    val entryScope = LocalNavAnimatedContentScope.current
    return with(transitions) {
        val state = rememberSharedContentState(key = "game-$gameId-${part.name}")
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
