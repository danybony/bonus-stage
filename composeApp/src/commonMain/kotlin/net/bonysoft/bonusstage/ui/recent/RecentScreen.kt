package net.bonysoft.bonusstage.ui.recent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import net.bonysoft.bonusstage.domain.RecentUnlock
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.achievement_points
import net.bonysoft.bonusstage.resources.recent_day_summary
import net.bonysoft.bonusstage.resources.recent_empty
import net.bonysoft.bonusstage.resources.recent_title
import net.bonysoft.bonusstage.resources.recent_today
import net.bonysoft.bonusstage.resources.recent_yesterday
import net.bonysoft.bonusstage.resources.tag_hardcore
import net.bonysoft.bonusstage.ui.common.formatDate
import net.bonysoft.bonusstage.ui.common.formatTime
import net.bonysoft.bonusstage.ui.common.grouped
import net.bonysoft.bonusstage.ui.common.splitTitleTag
import net.bonysoft.bonusstage.ui.components.RefreshableContent
import net.bonysoft.bonusstage.ui.components.RetroBadge
import net.bonysoft.bonusstage.ui.components.RetroScaffold
import net.bonysoft.bonusstage.ui.components.RetroTag
import net.bonysoft.bonusstage.ui.components.StaleNotice
import net.bonysoft.bonusstage.ui.components.retroFrame
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun RecentRoute(onOpenGame: (Long) -> Unit, modifier: Modifier = Modifier, viewModel: RecentViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RecentScreen(state = state, onRefresh = viewModel::refresh, onOpenGame = onOpenGame, modifier = modifier)
}

@Composable
fun RecentScreen(state: RecentUiState, onRefresh: () -> Unit, onOpenGame: (Long) -> Unit, modifier: Modifier = Modifier) {
    RetroScaffold(title = stringResource(Res.string.recent_title), modifier = modifier) {
        RefreshableContent(data = state.days, status = state.status, onRefresh = onRefresh) { days ->
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.status.error?.let { error -> item(key = "stale") { StaleNotice(error) } }
                if (days.isEmpty()) {
                    item(key = "empty") {
                        Text(stringResource(Res.string.recent_empty), style = RetroTheme.typography.body, color = RetroTheme.colors.onBackground)
                    }
                }
                days.forEach { day ->
                    item(key = "day-${day.date}") { DayHeader(day, today) }
                    items(day.unlocks, key = { "unlock-${it.achievementId}" }) { unlock ->
                        UnlockRow(unlock, onClick = { onOpenGame(unlock.gameId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(day: UnlockDay, today: LocalDate) {
    val colors = RetroTheme.colors
    val label = when (day.date) {
        today -> stringResource(Res.string.recent_today)
        today.minus(DatePeriod(days = 1)) -> stringResource(Res.string.recent_yesterday)
        else -> day.date.formatDate().uppercase()
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).semantics(mergeDescendants = true) { heading() },
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(label, style = RetroTheme.typography.title, color = colors.accent)
        Text(
            stringResource(Res.string.recent_day_summary, day.unlocks.size, day.points.grouped()),
            style = RetroTheme.typography.label,
            color = colors.onBackgroundMuted,
            modifier = Modifier.padding(bottom = 3.dp),
        )
    }
}

@Composable
private fun UnlockRow(unlock: RecentUnlock, onClick: () -> Unit) {
    val colors = RetroTheme.colors
    val gameTitle = splitTitleTag(unlock.gameTitle).second
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .retroFrame(fill = colors.window, border = colors.windowBorder, highlight = colors.windowHighlight)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RetroBadge(imageUrl = unlock.badgeUrl, contentDescription = null, size = 56.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(unlock.title, style = RetroTheme.typography.menu, color = colors.onWindow, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(unlock.unlockedAt.formatTime(), style = RetroTheme.typography.label, color = colors.onWindowMuted)
            }
            Text(unlock.description, style = RetroTheme.typography.bodySmall, color = colors.onWindow, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RetroBadge(imageUrl = unlock.gameIconUrl, contentDescription = null, size = 20.dp)
                Text(
                    gameTitle,
                    style = RetroTheme.typography.label,
                    color = colors.onWindowMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                RetroTag(stringResource(Res.string.achievement_points, unlock.points), colors.accent, textColor = colors.onAccent)
                if (unlock.hardcore) RetroTag(stringResource(Res.string.tag_hardcore), colors.hardcore)
            }
        }
    }
}
