package net.bonysoft.bonusstage.ui.game

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import net.bonysoft.bonusstage.domain.GameDetail
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.close
import net.bonysoft.bonusstage.resources.game_media
import net.bonysoft.bonusstage.resources.media_box_art
import net.bonysoft.bonusstage.resources.media_in_game
import net.bonysoft.bonusstage.resources.media_title_screen
import net.bonysoft.bonusstage.ui.components.RetroButton
import net.bonysoft.bonusstage.ui.components.RetroWindow
import net.bonysoft.bonusstage.ui.components.retroFrame
import net.bonysoft.bonusstage.ui.theme.PixelSize
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** A picture of the game from RetroAchievements, with what it shows. */
internal data class GameMedia(val url: String, val label: StringResource, val portrait: Boolean = false)

internal val GameDetail.media: List<GameMedia>
    get() = listOfNotNull(
        titleScreenUrl?.let { GameMedia(it, Res.string.media_title_screen) },
        inGameUrl?.let { GameMedia(it, Res.string.media_in_game) },
        boxArtUrl?.let { GameMedia(it, Res.string.media_box_art, portrait = true) },
    )

/** The banner on top of the game's info: its title screen, or a screenshot when it has none. */
@Composable
internal fun GameHero(game: GameDetail, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val url = game.titleScreenUrl ?: game.inGameUrl ?: return
    PixelPicture(
        url = url,
        contentDescription = stringResource(if (game.titleScreenUrl != null) Res.string.media_title_screen else Res.string.media_in_game),
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxWidth().aspectRatio(16f / 9f).clickable(role = Role.Image, onClick = onClick),
    )
}

/** A row of the game's pictures; tapping one opens it full screen. */
@Composable
internal fun MediaWindow(media: List<GameMedia>, onOpen: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = RetroTheme.colors
    RetroWindow(title = stringResource(Res.string.game_media), modifier = modifier.fillMaxWidth()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(media, key = { _, item -> item.url }) { index, item ->
                val label = stringResource(item.label)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(role = Role.Image) { onOpen(index) },
                ) {
                    PixelPicture(
                        url = item.url,
                        contentDescription = label,
                        modifier = Modifier.size(width = thumbWidth(item), height = THUMB_HEIGHT),
                    )
                    Text(label.uppercase(), style = RetroTheme.typography.label, color = colors.onWindowMuted, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

/** The game's pictures one at a time, swiped like a carousel. */
@Composable
internal fun MediaViewer(media: List<GameMedia>, initialPage: Int, onDismiss: () -> Unit) {
    val pager = rememberPagerState(initialPage = initialPage) { media.size }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        RetroWindow(
            title = stringResource(media[pager.currentPage].label).uppercase(),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            HorizontalPager(state = pager, pageSpacing = 16.dp, modifier = Modifier.fillMaxWidth().height(320.dp)) { page ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    PixelPicture(
                        url = media[page].url,
                        contentDescription = stringResource(media[page].label),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                Text(
                    "${pager.currentPage + 1} / ${media.size}",
                    style = RetroTheme.typography.label,
                    color = RetroTheme.colors.onWindowMuted,
                    modifier = Modifier.weight(1f),
                )
                RetroButton(text = stringResource(Res.string.close), onClick = onDismiss)
            }
        }
    }
}

/** A picture in a pixel frame, scaled without smoothing. */
@Composable
private fun PixelPicture(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val colors = RetroTheme.colors
    Box(modifier.retroFrame(fill = colors.track, border = colors.windowBorder, highlight = null).padding(PixelSize * 2)) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            filterQuality = FilterQuality.None,
        )
    }
}

private val THUMB_HEIGHT = 120.dp

private fun thumbWidth(item: GameMedia): Dp = if (item.portrait) 90.dp else 160.dp
