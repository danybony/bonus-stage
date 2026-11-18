package net.bonysoft.bonusstage.ui.rom

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bonysoft.bonusstage.data.rom.MatchedRom
import net.bonysoft.bonusstage.data.rom.ScanProgress
import net.bonysoft.bonusstage.data.rom.rememberRomFolderPicker
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.rom_catalog
import net.bonysoft.bonusstage.resources.rom_change
import net.bonysoft.bonusstage.resources.rom_game_detail
import net.bonysoft.bonusstage.resources.rom_hashing
import net.bonysoft.bonusstage.resources.rom_intro
import net.bonysoft.bonusstage.resources.rom_intro_title
import net.bonysoft.bonusstage.resources.rom_listing
import net.bonysoft.bonusstage.resources.rom_pick
import net.bonysoft.bonusstage.resources.rom_rescan
import net.bonysoft.bonusstage.resources.rom_summary
import net.bonysoft.bonusstage.resources.rom_title
import net.bonysoft.bonusstage.resources.rom_unknown
import net.bonysoft.bonusstage.resources.rom_unknown_help
import net.bonysoft.bonusstage.resources.rom_with_achievements
import net.bonysoft.bonusstage.ui.common.errorMessage
import net.bonysoft.bonusstage.ui.common.grouped
import net.bonysoft.bonusstage.ui.common.splitTitleTag
import net.bonysoft.bonusstage.ui.components.RetroBadge
import net.bonysoft.bonusstage.ui.components.RetroButton
import net.bonysoft.bonusstage.ui.components.RetroButtonStyle
import net.bonysoft.bonusstage.ui.components.RetroProgressBar
import net.bonysoft.bonusstage.ui.components.RetroScaffold
import net.bonysoft.bonusstage.ui.components.RetroWindow
import net.bonysoft.bonusstage.ui.components.retroFrame
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RomScanRoute(onBack: () -> Unit, onOpenGame: (Long) -> Unit, modifier: Modifier = Modifier, viewModel: RomScanViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pick = rememberRomFolderPicker(onPicked = viewModel::pick)
    RomScanScreen(state, onBack, onPick = pick, onRescan = viewModel::scan, onOpenGame = onOpenGame, modifier = modifier)
}

@Composable
fun RomScanScreen(
    state: RomScanUiState,
    onBack: () -> Unit,
    onPick: (() -> Unit)?,
    onRescan: () -> Unit,
    onOpenGame: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RetroTheme.colors
    RetroScaffold(title = stringResource(Res.string.rom_title), onBack = onBack, modifier = modifier) {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item(key = "intro") {
                RetroWindow(title = stringResource(Res.string.rom_intro_title), modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.rom_intro), style = RetroTheme.typography.bodySmall, color = colors.onWindow)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
                        if (onPick != null) {
                            RetroButton(
                                text = stringResource(if (state.folder == null) Res.string.rom_pick else Res.string.rom_change),
                                onClick = onPick,
                                enabled = !state.isScanning,
                                style = if (state.folder == null) RetroButtonStyle.Primary else RetroButtonStyle.Secondary,
                            )
                        }
                        if (state.folder != null) {
                            RetroButton(text = stringResource(Res.string.rom_rescan), onClick = onRescan, enabled = !state.isScanning)
                        }
                    }
                }
            }
            state.error?.let { error ->
                item(key = "error") { Text(errorMessage(error), style = RetroTheme.typography.body, color = colors.error) }
            }
            when (val progress = state.progress) {
                null -> Unit
                is ScanProgress.Done -> results(progress, onOpenGame)
                else -> item(key = "progress") { ProgressWindow(progress) }
            }
        }
    }
}

@Composable
private fun ProgressWindow(progress: ScanProgress) {
    RetroWindow(modifier = Modifier.fillMaxWidth()) {
        val (text, fraction) = when (progress) {
            ScanProgress.Listing -> stringResource(Res.string.rom_listing) to 0f
            is ScanProgress.Hashing -> stringResource(Res.string.rom_hashing, progress.done + 1, progress.total) to
                if (progress.total == 0) 0f else progress.done.toFloat() / progress.total
            is ScanProgress.LoadingCatalog -> stringResource(Res.string.rom_catalog, progress.consoles) to 1f
            is ScanProgress.Done -> "" to 1f
        }
        Text(text, style = RetroTheme.typography.menu, color = RetroTheme.colors.onWindow)
        Spacer(Modifier.height(8.dp))
        RetroProgressBar(progress = fraction)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.results(done: ScanProgress.Done, onOpenGame: (Long) -> Unit) {
    val result = done.result
    item(key = "summary") {
        Text(
            stringResource(Res.string.rom_summary, result.scanned.grouped(), result.matched.size.grouped()),
            style = RetroTheme.typography.label,
            color = RetroTheme.colors.onBackgroundMuted,
        )
    }
    if (result.matched.isNotEmpty()) {
        item(key = "matched-header") {
            Text(stringResource(Res.string.rom_with_achievements), style = RetroTheme.typography.title, color = RetroTheme.colors.progress, modifier = Modifier.padding(top = 8.dp))
        }
        items(result.matched, key = { "game-${it.game.id}" }) { MatchedRow(it, onClick = { onOpenGame(it.game.id) }) }
    }
    if (result.unknown.isNotEmpty()) {
        item(key = "unknown-header") {
            Column(Modifier.padding(top = 8.dp)) {
                Text("${stringResource(Res.string.rom_unknown)} ${result.unknown.size}", style = RetroTheme.typography.title, color = RetroTheme.colors.onBackgroundMuted)
                Text(stringResource(Res.string.rom_unknown_help), style = RetroTheme.typography.bodySmall, color = RetroTheme.colors.onBackgroundMuted)
            }
        }
        items(result.unknown, key = { "file-${it.id}" }) { file ->
            Text(file.name, style = RetroTheme.typography.label, color = RetroTheme.colors.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun MatchedRow(match: MatchedRom, onClick: () -> Unit) {
    val colors = RetroTheme.colors
    val (tag, title) = splitTitleTag(match.game.title)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .retroFrame(fill = colors.window, border = colors.windowBorder, highlight = colors.windowHighlight)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RetroBadge(imageUrl = match.game.iconUrl, contentDescription = null, size = 56.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = RetroTheme.typography.menu, color = colors.onWindow, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(listOfNotNull(tag?.uppercase(), match.game.consoleName).joinToString(" · "), style = RetroTheme.typography.label, color = colors.onWindowMuted)
            Text(
                stringResource(Res.string.rom_game_detail, match.game.achievements, match.game.points.grouped()),
                style = RetroTheme.typography.bodySmall,
                color = colors.progress,
            )
            Text(match.file.name, style = RetroTheme.typography.label, color = colors.onWindowMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
