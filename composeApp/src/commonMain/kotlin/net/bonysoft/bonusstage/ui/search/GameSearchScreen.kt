package net.bonysoft.bonusstage.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bonysoft.bonusstage.data.repository.CatalogRepository
import net.bonysoft.bonusstage.data.rom.CatalogGame
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.rom_game_detail
import net.bonysoft.bonusstage.resources.search_field
import net.bonysoft.bonusstage.resources.search_hint
import net.bonysoft.bonusstage.resources.search_no_results
import net.bonysoft.bonusstage.resources.search_no_set
import net.bonysoft.bonusstage.resources.search_syncing
import net.bonysoft.bonusstage.resources.search_title
import net.bonysoft.bonusstage.ui.adaptive.AdaptiveList
import net.bonysoft.bonusstage.ui.adaptive.fullLineItem
import net.bonysoft.bonusstage.ui.common.RaLinks
import net.bonysoft.bonusstage.ui.common.errorMessage
import net.bonysoft.bonusstage.ui.common.grouped
import net.bonysoft.bonusstage.ui.common.rememberWebOpener
import net.bonysoft.bonusstage.ui.common.splitTitleTag
import net.bonysoft.bonusstage.ui.components.RetroBadge
import net.bonysoft.bonusstage.ui.components.RetroProgressBar
import net.bonysoft.bonusstage.ui.components.RetroScaffold
import net.bonysoft.bonusstage.ui.components.RetroTag
import net.bonysoft.bonusstage.ui.components.RetroTextField
import net.bonysoft.bonusstage.ui.components.RetroWindow
import net.bonysoft.bonusstage.ui.components.retroFrame
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GameSearchRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: GameSearchViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val web = rememberWebOpener()
    GameSearchScreen(
        state = state,
        onBack = onBack,
        onQueryChange = viewModel::onQueryChange,
        onOpenGame = { game ->
            viewModel.onOpenGame()
            web.open(RaLinks.game(game.id))
        },
        modifier = modifier,
    )
}

@Composable
fun GameSearchScreen(
    state: GameSearchUiState,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onOpenGame: (CatalogGame) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RetroTheme.colors
    RetroScaffold(title = stringResource(Res.string.search_title), onBack = onBack, modifier = modifier) {
        AdaptiveList {
            fullLineItem(key = "field") {
                RetroWindow(modifier = Modifier.fillMaxWidth()) {
                    RetroTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        label = stringResource(Res.string.search_field),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Search,
                        ),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(Res.string.search_hint), style = RetroTheme.typography.bodySmall, color = colors.onWindowMuted)
                }
            }
            val sync = state.sync
            if (sync != null && !sync.isComplete) {
                fullLineItem(key = "sync") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(Res.string.search_syncing, sync.done, sync.total), style = RetroTheme.typography.label, color = colors.onBackgroundMuted)
                        RetroProgressBar(progress = if (sync.total == 0) 0f else sync.done.toFloat() / sync.total)
                    }
                }
            }
            state.error?.let { error ->
                fullLineItem(key = "error") { Text(errorMessage(error), style = RetroTheme.typography.body, color = colors.error) }
            }
            if (state.query.trim().length >= CatalogRepository.MIN_QUERY && state.results.isEmpty() && sync?.isComplete == true) {
                fullLineItem(key = "empty") {
                    Text(stringResource(Res.string.search_no_results, state.query.trim()), style = RetroTheme.typography.body, color = colors.onBackground)
                }
            }
            items(state.results, key = { it.id }) { game -> ResultRow(game, onClick = { onOpenGame(game) }) }
        }
    }
}

@Composable
private fun ResultRow(game: CatalogGame, onClick: () -> Unit) {
    val colors = RetroTheme.colors
    val (tag, title) = splitTitleTag(game.title)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .retroFrame(fill = colors.window, border = colors.windowBorder, highlight = colors.windowHighlight)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RetroBadge(imageUrl = game.iconUrl, contentDescription = null, size = 52.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = RetroTheme.typography.menu, color = colors.onWindow, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (tag != null) RetroTag(tag.uppercase(), colors.onWindowMuted)
                Text(game.consoleName, style = RetroTheme.typography.label, color = colors.onWindowMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(
                if (game.achievements > 0) stringResource(Res.string.rom_game_detail, game.achievements, game.points.grouped())
                else stringResource(Res.string.search_no_set),
                style = RetroTheme.typography.bodySmall,
                color = if (game.achievements > 0) colors.progress else colors.onWindowMuted,
            )
        }
    }
}
