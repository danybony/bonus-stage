package net.bonysoft.bonusstage.ui.share

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import net.bonysoft.bonusstage.data.repository.MonthlyRecap
import net.bonysoft.bonusstage.domain.Award
import net.bonysoft.bonusstage.domain.GameDetail
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.award_tag_beaten
import net.bonysoft.bonusstage.resources.award_tag_beaten_softcore
import net.bonysoft.bonusstage.resources.award_tag_completed
import net.bonysoft.bonusstage.resources.award_tag_mastered
import net.bonysoft.bonusstage.resources.card_achievements
import net.bonysoft.bonusstage.resources.card_best
import net.bonysoft.bonusstage.resources.card_games
import net.bonysoft.bonusstage.resources.card_points
import net.bonysoft.bonusstage.resources.card_quiet_month
import net.bonysoft.bonusstage.resources.card_top_games
import net.bonysoft.bonusstage.resources.card_unlocks
import net.bonysoft.bonusstage.ui.common.formatDate
import net.bonysoft.bonusstage.ui.common.grouped
import net.bonysoft.bonusstage.ui.common.splitTitleTag
import net.bonysoft.bonusstage.ui.components.PixelIcon
import net.bonysoft.bonusstage.ui.components.PixelIcons
import net.bonysoft.bonusstage.ui.components.color
import net.bonysoft.bonusstage.ui.components.retroFrame
import net.bonysoft.bonusstage.ui.theme.PixelSize
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/** Cards are laid out at this size and exported at the device density (about 1000x1250 px). */
val CardWidth: Dp = 360.dp
val CardHeight: Dp = 450.dp

private val monthNames = listOf(
    "JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
    "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER",
)

fun MonthlyRecap.monthLabel(): String = "${monthNames[month.month.ordinal]} ${month.year}"

/** A pixel image that reports when it has loaded, so the card is only captured once complete. */
@Composable
private fun CardImage(url: String, size: Dp, onLoaded: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .retroFrame(fill = RetroTheme.colors.track, border = RetroTheme.colors.windowBorder, highlight = null)
            .padding(PixelSize * 2),
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            filterQuality = FilterQuality.None,
            onSuccess = { onLoaded(url) },
            onError = { onLoaded(url) },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/** Night sky of pixel stars behind the card, always the same pattern. */
@Composable
private fun CardBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = RetroTheme.colors
    Box(modifier = modifier.background(colors.background)) {
        Canvas(Modifier.fillMaxSize()) {
            val p = PixelSize.toPx()
            var seed = 7L
            repeat(70) {
                seed = (seed * 1103515245 + 12345) and 0x7fffffff
                val x = (seed % 1000) / 1000f * size.width
                seed = (seed * 1103515245 + 12345) and 0x7fffffff
                val y = (seed % 1000) / 1000f * size.height
                val star = if (it % 9 == 0) colors.accent else colors.onBackgroundMuted.copy(alpha = 0.5f)
                drawRect(star, Offset(x, y), Size(p, p))
            }
            // A double pixel frame, so the card has edges on any background.
            val inset = p * 3
            listOf(0f to colors.accent, inset to colors.backgroundVariant).forEach { (offset, color) ->
                drawRect(color, Offset(offset, offset), Size(size.width - 2 * offset, p))
                drawRect(color, Offset(offset, size.height - offset - p), Size(size.width - 2 * offset, p))
                drawRect(color, Offset(offset, offset), Size(p, size.height - 2 * offset))
                drawRect(color, Offset(size.width - offset - p, offset), Size(p, size.height - 2 * offset))
            }
        }
        content()
    }
}

@Composable
private fun CardFooter(username: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(username, style = RetroTheme.typography.label, color = RetroTheme.colors.onBackground, modifier = Modifier.weight(1f))
        Text("BONUS STAGE", style = RetroTheme.typography.label, color = RetroTheme.colors.accent)
    }
}

/** The card for a beaten or mastered game. [onImageLoaded] is called once per image. */
@Composable
fun GameAwardCard(game: GameDetail, award: Award, username: String, awardedAt: Instant?, onImageLoaded: (String) -> Unit) {
    val colors = RetroTheme.colors
    val awardColor = award.color(colors)
    val label = when (award) {
        Award.Mastered -> stringResource(Res.string.award_tag_mastered)
        Award.Completed -> stringResource(Res.string.award_tag_completed)
        Award.BeatenHardcore -> stringResource(Res.string.award_tag_beaten)
        Award.BeatenSoftcore -> stringResource(Res.string.award_tag_beaten_softcore)
    }
    val (tag, title) = splitTitleTag(game.title)
    CardBackground(Modifier.size(CardWidth, CardHeight)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PixelIcon(PixelIcons.Star, color = awardColor, size = 20.dp)
                Text(label, style = RetroTheme.typography.display, color = awardColor)
                PixelIcon(PixelIcons.Star, color = awardColor, size = 20.dp)
            }
            Spacer(Modifier.height(16.dp))
            CardImage(game.iconUrl, 132.dp, onImageLoaded)
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .retroFrame(fill = colors.window, border = colors.windowBorder, highlight = colors.windowHighlight)
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(title, style = RetroTheme.typography.title, color = colors.onWindow, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(tag?.uppercase(), game.consoleName).joinToString(" · "), style = RetroTheme.typography.label, color = colors.onWindowMuted)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    CardStat("${game.unlocked}/${game.achievements.size}", stringResource(Res.string.card_achievements), colors.onWindow)
                    CardStat(game.earnedPoints.grouped(), stringResource(Res.string.card_points), awardColor)
                }
            }
            Spacer(Modifier.weight(1f))
            awardedAt?.let {
                Text(it.formatDate().uppercase(), style = RetroTheme.typography.label, color = colors.onBackgroundMuted)
                Spacer(Modifier.height(6.dp))
            }
            CardFooter(username)
        }
    }
}

@Composable
private fun CardStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = RetroTheme.typography.number, color = color)
        Text(label, style = RetroTheme.typography.label, color = RetroTheme.colors.onWindowMuted)
    }
}

/** The monthly recap card. */
@Composable
fun MonthlyRecapCard(recap: MonthlyRecap, username: String, onImageLoaded: (String) -> Unit) {
    val colors = RetroTheme.colors
    CardBackground(Modifier.size(CardWidth, CardHeight)) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Text(recap.monthLabel(), style = RetroTheme.typography.title, color = colors.accent)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .retroFrame(fill = colors.window, border = colors.windowBorder, highlight = colors.windowHighlight)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                CardStat(recap.unlocks.grouped(), stringResource(Res.string.card_unlocks), colors.onWindow)
                CardStat(recap.points.grouped(), stringResource(Res.string.card_points), colors.hardcore)
                CardStat(recap.games.grouped(), stringResource(Res.string.card_games), colors.softcore)
            }
            Spacer(Modifier.height(14.dp))
            if (recap.unlocks == 0) {
                Text(stringResource(Res.string.card_quiet_month), style = RetroTheme.typography.body, color = colors.onBackground)
            } else {
                Text(stringResource(Res.string.card_top_games), style = RetroTheme.typography.label, color = colors.onBackgroundMuted)
                Spacer(Modifier.height(6.dp))
                recap.topGames.forEach { game ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        CardImage(game.iconUrl, 36.dp, onImageLoaded)
                        Spacer(Modifier.size(10.dp))
                        Text(
                            splitTitleTag(game.title).second,
                            style = RetroTheme.typography.menu,
                            color = colors.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Text("×${game.unlocks}", style = RetroTheme.typography.number, color = colors.accent)
                    }
                }
                recap.bestBadgeUrl?.let { badge ->
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(Res.string.card_best), style = RetroTheme.typography.label, color = colors.onBackgroundMuted)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CardImage(badge, 44.dp, onImageLoaded)
                        Spacer(Modifier.size(10.dp))
                        Text(recap.bestTitle.orEmpty(), style = RetroTheme.typography.menu, color = colors.onBackground, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${recap.bestPoints} PTS", style = RetroTheme.typography.number, color = colors.mastered)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            CardFooter(username)
        }
    }
}

/** Every image a recap card shows, to know when it is complete. */
fun MonthlyRecap.imageUrls(): Set<String> = (topGames.map { it.iconUrl } + listOfNotNull(bestBadgeUrl)).toSet()
