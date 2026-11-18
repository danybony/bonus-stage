package net.bonysoft.bonusstage.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bonysoft.bonusstage.data.rom.rememberRomFolderPicker
import net.bonysoft.bonusstage.domain.Award
import net.bonysoft.bonusstage.domain.LibraryGame
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.close
import net.bonysoft.bonusstage.resources.filter_all
import net.bonysoft.bonusstage.resources.filter_all_consoles
import net.bonysoft.bonusstage.resources.filter_beaten
import net.bonysoft.bonusstage.resources.filter_console_title
import net.bonysoft.bonusstage.resources.filter_in_progress
import net.bonysoft.bonusstage.resources.filter_mastered
import net.bonysoft.bonusstage.resources.library_empty
import net.bonysoft.bonusstage.resources.library_no_match
import net.bonysoft.bonusstage.resources.library_summary
import net.bonysoft.bonusstage.resources.library_title
import net.bonysoft.bonusstage.resources.no_achievements
import net.bonysoft.bonusstage.resources.progress_count
import net.bonysoft.bonusstage.resources.rom_scan
import net.bonysoft.bonusstage.ui.common.grouped
import net.bonysoft.bonusstage.ui.common.splitTitleTag
import net.bonysoft.bonusstage.ui.components.AwardTag
import net.bonysoft.bonusstage.ui.components.RefreshableContent
import net.bonysoft.bonusstage.ui.components.RetroBadge
import net.bonysoft.bonusstage.ui.components.RetroButton
import net.bonysoft.bonusstage.ui.components.RetroButtonStyle
import net.bonysoft.bonusstage.ui.components.RetroChip
import net.bonysoft.bonusstage.ui.components.RetroMenuItem
import net.bonysoft.bonusstage.ui.components.RetroProgressBar
import net.bonysoft.bonusstage.ui.components.RetroScaffold
import net.bonysoft.bonusstage.ui.components.RetroTag
import net.bonysoft.bonusstage.ui.components.RetroWindow
import net.bonysoft.bonusstage.ui.components.StaleNotice
import net.bonysoft.bonusstage.ui.components.asPercent
import net.bonysoft.bonusstage.ui.components.color
import net.bonysoft.bonusstage.ui.components.retroFrame
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryRoute(
    onOpenGame: (Long) -> Unit,
    onScanRoms: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LibraryScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onSelectConsole = viewModel::selectConsole,
        onSelectStatus = viewModel::selectStatus,
        onOpenGame = onOpenGame,
        // Only where ROM folders can be picked (Android for now).
        onScanRoms = onScanRoms.takeIf { rememberRomFolderPicker {} != null },
        modifier = modifier,
    )
}

@Composable
fun LibraryScreen(
    state: LibraryUiState,
    onRefresh: () -> Unit,
    onSelectConsole: (Int?) -> Unit,
    onSelectStatus: (StatusFilter) -> Unit,
    onOpenGame: (Long) -> Unit,
    onScanRoms: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var pickConsole by rememberSaveable { mutableStateOf(false) }
    RetroScaffold(
        title = stringResource(Res.string.library_title),
        modifier = modifier,
        actions = {
            if (onScanRoms != null) RetroChip(text = stringResource(Res.string.rom_scan), selected = false, onClick = onScanRoms)
        },
    ) {
        RefreshableContent(data = state.allGames, status = state.status, onRefresh = onRefresh) { all ->
            val games = state.games
            LazyColumn(
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                state.status.error?.let { error -> item(key = "stale") { StaleNotice(error, Modifier.padding(horizontal = 16.dp)) } }
                item(key = "filters") {
                    Filters(state, onSelectStatus, onOpenConsoles = { pickConsole = true })
                }
                item(key = "summary") {
                    Text(
                        text = stringResource(
                            Res.string.library_summary,
                            games.size.grouped(),
                            games.count { it.award == Award.BeatenHardcore || it.award == Award.BeatenSoftcore }.grouped(),
                            games.count { it.award == Award.Mastered || it.award == Award.Completed }.grouped(),
                        ),
                        style = RetroTheme.typography.label,
                        color = RetroTheme.colors.onBackgroundMuted,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (games.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            text = stringResource(if (all.isEmpty()) Res.string.library_empty else Res.string.library_no_match),
                            style = RetroTheme.typography.body,
                            color = RetroTheme.colors.onBackground,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
                items(games, key = { it.id }) { game ->
                    GameRow(game, onClick = { onOpenGame(game.id) }, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
    if (pickConsole) {
        ConsolePicker(
            options = state.consoles,
            selected = state.console,
            onSelect = {
                onSelectConsole(it)
                pickConsole = false
            },
            onDismiss = { pickConsole = false },
        )
    }
}

@Composable
private fun Filters(state: LibraryUiState, onSelectStatus: (StatusFilter) -> Unit, onOpenConsoles: () -> Unit) {
    val statusLabels = listOf(
        StatusFilter.All to stringResource(Res.string.filter_all),
        StatusFilter.InProgress to stringResource(Res.string.filter_in_progress),
        StatusFilter.Beaten to stringResource(Res.string.filter_beaten),
        StatusFilter.Mastered to stringResource(Res.string.filter_mastered),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(statusLabels, key = { it.first.name }) { (filter, label) ->
                RetroChip(text = label, selected = state.statusFilter == filter, onClick = { onSelectStatus(filter) })
            }
        }
        val consoleName = state.consoles.firstOrNull { it.id == state.console }?.name ?: stringResource(Res.string.filter_all_consoles)
        RetroChip(
            text = "$consoleName ▾",
            selected = state.console != null,
            onClick = onOpenConsoles,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Composable
internal fun GameRow(game: LibraryGame, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = RetroTheme.colors
    val (tag, title) = splitTitleTag(game.title)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .retroFrame(fill = colors.window, border = colors.windowBorder, highlight = colors.windowHighlight)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RetroBadge(imageUrl = game.iconUrl, contentDescription = null, size = 64.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = RetroTheme.typography.menu, color = colors.onWindow, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (tag != null) RetroTag(tag.uppercase(), colors.onWindowMuted)
                Text(game.consoleName, style = RetroTheme.typography.label, color = colors.onWindowMuted, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                game.award?.let { AwardTag(it) }
            }
            if (game.achievementsTotal > 0) {
                RetroProgressBar(progress = game.progress, color = game.award?.color(colors) ?: colors.progress, height = 12.dp)
                Text(
                    stringResource(Res.string.progress_count, game.unlocked, game.achievementsTotal, "${game.progress.asPercent()}%"),
                    style = RetroTheme.typography.label,
                    color = colors.onWindowMuted,
                )
            } else {
                Text(stringResource(Res.string.no_achievements), style = RetroTheme.typography.label, color = colors.onWindowMuted)
            }
        }
    }
}

@Composable
private fun ConsolePicker(options: List<ConsoleOption>, selected: Int?, onSelect: (Int?) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        RetroWindow(title = stringResource(Res.string.filter_console_title)) {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                RetroMenuItem(text = stringResource(Res.string.filter_all_consoles), selected = selected == null, onClick = { onSelect(null) })
                options.forEach { option ->
                    RetroMenuItem(
                        text = option.name,
                        supportingText = option.count.grouped(),
                        selected = selected == option.id,
                        onClick = { onSelect(option.id) },
                    )
                }
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                    RetroButton(text = stringResource(Res.string.close), onClick = onDismiss, style = RetroButtonStyle.Secondary)
                }
            }
        }
    }
}
