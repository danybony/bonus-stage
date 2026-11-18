package net.bonysoft.bonusstage.ui.game

import net.bonysoft.bonusstage.ui.transitions.GamePart
import net.bonysoft.bonusstage.ui.transitions.sharedGame
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bonysoft.bonusstage.domain.Achievement
import net.bonysoft.bonusstage.domain.AchievementType
import net.bonysoft.bonusstage.domain.Award
import net.bonysoft.bonusstage.domain.GameDetail
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.achievement_points
import net.bonysoft.bonusstage.resources.award_tag_beaten
import net.bonysoft.bonusstage.resources.award_tag_beaten_softcore
import net.bonysoft.bonusstage.resources.award_tag_completed
import net.bonysoft.bonusstage.resources.award_tag_mastered
import net.bonysoft.bonusstage.resources.share_card
import net.bonysoft.bonusstage.resources.share_text_award
import net.bonysoft.bonusstage.resources.achievement_rate
import net.bonysoft.bonusstage.resources.achievement_unlocked_on
import net.bonysoft.bonusstage.resources.game_achievements
import net.bonysoft.bonusstage.resources.game_developer
import net.bonysoft.bonusstage.resources.game_genre
import net.bonysoft.bonusstage.resources.game_info
import net.bonysoft.bonusstage.resources.game_no_achievements
import net.bonysoft.bonusstage.resources.game_players
import net.bonysoft.bonusstage.resources.game_points
import net.bonysoft.bonusstage.resources.game_progress
import net.bonysoft.bonusstage.resources.game_publisher
import net.bonysoft.bonusstage.resources.game_released
import net.bonysoft.bonusstage.resources.game_unlocked_count
import net.bonysoft.bonusstage.resources.spoiler_hidden_description
import net.bonysoft.bonusstage.resources.spoilers_hidden
import net.bonysoft.bonusstage.resources.spoilers_shown
import net.bonysoft.bonusstage.resources.tag_hardcore
import net.bonysoft.bonusstage.resources.type_missable
import net.bonysoft.bonusstage.resources.type_progression
import net.bonysoft.bonusstage.resources.type_win_condition
import net.bonysoft.bonusstage.ui.adaptive.AdaptiveList
import net.bonysoft.bonusstage.ui.adaptive.LocalWindowLayout
import net.bonysoft.bonusstage.ui.adaptive.fullLineItem
import net.bonysoft.bonusstage.ui.common.formatDate
import net.bonysoft.bonusstage.ui.common.formatTime
import net.bonysoft.bonusstage.ui.common.grouped
import net.bonysoft.bonusstage.ui.common.splitTitleTag
import net.bonysoft.bonusstage.ui.components.AwardTag
import net.bonysoft.bonusstage.ui.components.PixelIcon
import net.bonysoft.bonusstage.ui.components.PixelIcons
import net.bonysoft.bonusstage.ui.components.RefreshableContent
import net.bonysoft.bonusstage.ui.components.RetroBadge
import net.bonysoft.bonusstage.ui.components.RetroButton
import net.bonysoft.bonusstage.ui.components.RetroChip
import net.bonysoft.bonusstage.ui.components.RetroProgressBar
import net.bonysoft.bonusstage.ui.components.RetroScaffold
import net.bonysoft.bonusstage.ui.components.RetroTag
import net.bonysoft.bonusstage.ui.components.RetroWindow
import net.bonysoft.bonusstage.ui.components.SpoilerText
import net.bonysoft.bonusstage.ui.components.StaleNotice
import net.bonysoft.bonusstage.ui.components.asPercent
import net.bonysoft.bonusstage.ui.components.color
import net.bonysoft.bonusstage.ui.components.retroFrame
import net.bonysoft.bonusstage.ui.share.GameAwardCard
import net.bonysoft.bonusstage.ui.share.ShareCardDialog
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun GameDetailRoute(
    gameId: Long,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GameDetailViewModel = koinViewModel(key = "game-$gameId") { parametersOf(gameId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GameDetailScreen(
        state = state,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onToggleSpoilers = { viewModel.setHideSpoilers(!state.hideSpoilers) },
        modifier = modifier,
    )
}

@Composable
fun GameDetailScreen(
    state: GameDetailUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onToggleSpoilers: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Achievements revealed one by one while spoilers are hidden; kept across rotation.
    var revealed by rememberSaveable { mutableStateOf(setOf<Long>()) }
    var sharing by rememberSaveable { mutableStateOf(false) }
    val title = state.game?.let { splitTitleTag(it.title).second } ?: ""
    RetroScaffold(title = title, onBack = onBack, modifier = modifier) {
        RefreshableContent(data = state.game, status = state.status, onRefresh = onRefresh) { game ->
            val achievements: LazyGridScope.() -> Unit = {
                fullLineItem(key = "achievements-header") {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(Res.string.game_achievements),
                            style = RetroTheme.typography.title,
                            color = RetroTheme.colors.onBackground,
                            modifier = Modifier.weight(1f),
                        )
                        RetroChip(
                            text = stringResource(if (state.hideSpoilers) Res.string.spoilers_hidden else Res.string.spoilers_shown),
                            selected = state.hideSpoilers,
                            onClick = onToggleSpoilers,
                            icon = if (state.hideSpoilers) PixelIcons.EyeOff else PixelIcons.Eye,
                        )
                    }
                }
                if (game.achievements.isEmpty()) {
                    fullLineItem(key = "none") {
                        Text(stringResource(Res.string.game_no_achievements), style = RetroTheme.typography.body, color = RetroTheme.colors.onBackground)
                    }
                }
                items(game.achievements, key = { it.id }) { achievement ->
                    val hidden = state.hideSpoilers && !achievement.isUnlocked && achievement.id !in revealed
                    AchievementRow(
                        achievement = achievement,
                        hidden = hidden,
                        onReveal = { revealed = revealed + achievement.id },
                    )
                }
            }
            if (LocalWindowLayout.current.isExpandedWidth) {
                // Two panes: the game on the left, its achievements on the right.
                Row(Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.weight(0.4f).fillMaxHeight().verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        state.status.error?.let { StaleNotice(it) }
                        GameHeader(game)
                        ProgressWindow(game, onShare = { sharing = true })
                    }
                    AdaptiveList(modifier = Modifier.weight(0.6f), spacing = 16.dp, content = achievements)
                }
            } else {
                AdaptiveList(spacing = 16.dp) {
                    state.status.error?.let { error -> fullLineItem(key = "stale") { StaleNotice(error) } }
                    fullLineItem(key = "header") { GameHeader(game) }
                    fullLineItem(key = "progress") { ProgressWindow(game, onShare = { sharing = true }) }
                    achievements()
                }
            }
        }
    }
    val game = state.game
    val award = game?.award
    if (sharing && game != null && award != null) {
        val awardLabel = when (award) {
            Award.Mastered -> stringResource(Res.string.award_tag_mastered)
            Award.Completed -> stringResource(Res.string.award_tag_completed)
            Award.BeatenHardcore -> stringResource(Res.string.award_tag_beaten)
            Award.BeatenSoftcore -> stringResource(Res.string.award_tag_beaten_softcore)
        }
        ShareCardDialog(
            fileName = "bonus-stage-${game.id}.png",
            shareText = stringResource(Res.string.share_text_award, awardLabel, splitTitleTag(game.title).second),
            imageCount = 1,
            onDismiss = { sharing = false },
        ) { onLoaded ->
            GameAwardCard(game, award, state.username, game.awardedAt, onLoaded)
        }
    }
}

@Composable
private fun GameHeader(game: GameDetail) {
    val colors = RetroTheme.colors
    val (tag, title) = splitTitleTag(game.title)
    RetroWindow(title = stringResource(Res.string.game_info), modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            RetroBadge(imageUrl = game.iconUrl, contentDescription = null, size = 96.dp, modifier = Modifier.sharedGame(game.id, GamePart.Icon))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Text(title, style = RetroTheme.typography.title, color = colors.onWindow, modifier = Modifier.sharedGame(game.id, GamePart.Title))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (tag != null) RetroTag(tag.uppercase(), colors.onWindowMuted)
                    Text(
                        game.consoleName,
                        style = RetroTheme.typography.label,
                        color = colors.onWindowMuted,
                        modifier = Modifier.sharedGame(game.id, GamePart.Console),
                    )
                }
            }
        }
        val facts = listOfNotNull(
            game.publisher?.let { stringResource(Res.string.game_publisher) to it },
            game.developer?.let { stringResource(Res.string.game_developer) to it },
            game.genre?.let { stringResource(Res.string.game_genre) to it },
            game.released?.let { stringResource(Res.string.game_released) to it.take(10) },
            game.players?.let { stringResource(Res.string.game_players) to it.grouped() },
        )
        if (facts.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            facts.forEach { (label, value) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Text(label.uppercase(), style = RetroTheme.typography.label, color = colors.onWindowMuted, modifier = Modifier.weight(0.4f))
                    Text(value, style = RetroTheme.typography.bodySmall, color = colors.onWindow, modifier = Modifier.weight(0.6f))
                }
            }
        }
    }
}

@Composable
private fun ProgressWindow(game: GameDetail, onShare: () -> Unit) {
    val colors = RetroTheme.colors
    val total = game.achievements.size
    val progress = if (total == 0) 0f else game.unlocked.toFloat() / total
    RetroWindow(title = stringResource(Res.string.game_progress), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${progress.asPercent()}%", style = RetroTheme.typography.display, color = game.award?.color(colors) ?: colors.progress)
            Spacer(Modifier.weight(1f))
            game.award?.let { AwardTag(it) }
        }
        Spacer(Modifier.height(8.dp))
        RetroProgressBar(progress = progress, color = game.award?.color(colors) ?: colors.progress)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(Res.string.game_unlocked_count, game.unlocked, total, game.unlockedHardcore),
            style = RetroTheme.typography.label,
            color = colors.onWindowMuted,
        )
        Text(
            stringResource(Res.string.game_points, game.earnedPoints.grouped(), game.totalPoints.grouped()),
            style = RetroTheme.typography.label,
            color = colors.onWindowMuted,
        )
        if (game.award != null) {
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                RetroButton(text = stringResource(Res.string.share_card), onClick = onShare)
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: Achievement, hidden: Boolean, onReveal: () -> Unit) {
    val colors = RetroTheme.colors
    val hiddenDescription = stringResource(Res.string.spoiler_hidden_description)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (hidden) Modifier.clickable(role = Role.Button, onClick = onReveal) else Modifier)
            .retroFrame(
                fill = if (achievement.isUnlocked) colors.window else colors.track,
                border = colors.windowBorder,
                highlight = if (achievement.isUnlocked) colors.windowHighlight else null,
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RetroBadge(
            imageUrl = if (achievement.isUnlocked) achievement.badgeUrl else achievement.lockedBadgeUrl,
            contentDescription = null,
            locked = !achievement.isUnlocked,
            size = 64.dp,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SpoilerText(
                    text = achievement.title,
                    hidden = hidden,
                    style = RetroTheme.typography.menu,
                    color = colors.onWindow,
                    barColor = colors.onWindowMuted,
                    hiddenDescription = hiddenDescription,
                    maxLines = 2,
                    modifier = Modifier.weight(1f),
                )
                if (hidden) PixelIcon(PixelIcons.Eye, color = colors.onWindowMuted, size = 20.dp)
            }
            SpoilerText(
                text = achievement.description,
                hidden = hidden,
                style = RetroTheme.typography.bodySmall,
                color = colors.onWindow,
                barColor = colors.beaten,
                hiddenDescription = hiddenDescription,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                RetroTag(stringResource(Res.string.achievement_points, achievement.points), colors.accent, textColor = colors.onAccent)
                achievement.type?.let { type ->
                    val label = when (type) {
                        AchievementType.Progression -> stringResource(Res.string.type_progression)
                        AchievementType.WinCondition -> stringResource(Res.string.type_win_condition)
                        AchievementType.Missable -> stringResource(Res.string.type_missable)
                    }
                    RetroTag(label, if (type == AchievementType.Missable) colors.hardcore else colors.onWindowMuted)
                }
                if (achievement.unlockedHardcoreAt != null) RetroTag(stringResource(Res.string.tag_hardcore), colors.hardcore)
            }
            val unlockedAt = achievement.unlockedHardcoreAt ?: achievement.unlockedAt
            val detail = when {
                unlockedAt != null -> stringResource(Res.string.achievement_unlocked_on, "${unlockedAt.formatDate()} ${unlockedAt.formatTime()}")
                achievement.unlockRate != null -> stringResource(Res.string.achievement_rate, "${achievement.unlockRate.asPercent()}%")
                else -> null
            }
            if (detail != null) {
                Text(detail, style = RetroTheme.typography.label, color = colors.onWindowMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
