package net.bonysoft.bonusstage.ui.wishlist

import net.bonysoft.bonusstage.ui.transitions.GamePart
import net.bonysoft.bonusstage.ui.transitions.sharedGame
import net.bonysoft.bonusstage.ui.adaptive.AdaptiveList
import net.bonysoft.bonusstage.ui.adaptive.fullLineItem
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bonysoft.bonusstage.data.alerts.NotificationPermissionState
import net.bonysoft.bonusstage.data.alerts.rememberNotificationPermissionState
import net.bonysoft.bonusstage.domain.SetStatus
import net.bonysoft.bonusstage.domain.WishlistGame
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.wishlist_alerts_denied
import net.bonysoft.bonusstage.resources.wishlist_alerts_enable
import net.bonysoft.bonusstage.resources.wishlist_alerts_text
import net.bonysoft.bonusstage.resources.wishlist_alerts_title
import net.bonysoft.bonusstage.resources.wishlist_empty
import net.bonysoft.bonusstage.resources.wishlist_in_development
import net.bonysoft.bonusstage.resources.wishlist_ready
import net.bonysoft.bonusstage.resources.wishlist_status_expected
import net.bonysoft.bonusstage.resources.wishlist_status_in_development
import net.bonysoft.bonusstage.resources.wishlist_status_no_set
import net.bonysoft.bonusstage.resources.wishlist_status_published
import net.bonysoft.bonusstage.resources.wishlist_status_revision
import net.bonysoft.bonusstage.resources.wishlist_title
import net.bonysoft.bonusstage.resources.wishlist_waiting
import net.bonysoft.bonusstage.ui.common.formatDate
import net.bonysoft.bonusstage.ui.common.grouped
import net.bonysoft.bonusstage.ui.common.splitTitleTag
import net.bonysoft.bonusstage.ui.components.PixelIcon
import net.bonysoft.bonusstage.ui.components.PixelIcons
import net.bonysoft.bonusstage.ui.components.RefreshableContent
import net.bonysoft.bonusstage.ui.components.RetroBadge
import net.bonysoft.bonusstage.ui.components.RetroButton
import net.bonysoft.bonusstage.ui.components.RetroScaffold
import net.bonysoft.bonusstage.ui.components.RetroTag
import net.bonysoft.bonusstage.ui.components.RetroWindow
import net.bonysoft.bonusstage.ui.components.StaleNotice
import net.bonysoft.bonusstage.ui.components.retroFrame
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WishlistRoute(onOpenGame: (Long) -> Unit, modifier: Modifier = Modifier, viewModel: WishlistViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    WishlistScreen(
        state = state,
        permission = rememberNotificationPermissionState(),
        onRefresh = viewModel::refresh,
        onOpenGame = onOpenGame,
        modifier = modifier,
    )
}

@Composable
fun WishlistScreen(
    state: WishlistUiState,
    permission: NotificationPermissionState,
    onRefresh: () -> Unit,
    onOpenGame: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RetroTheme.colors
    RetroScaffold(title = stringResource(Res.string.wishlist_title), modifier = modifier) {
        RefreshableContent(data = state.sections, status = state.status, onRefresh = onRefresh) { sections ->
            AdaptiveList {
                state.status.error?.let { error -> fullLineItem(key = "stale") { StaleNotice(error) } }
                if (!permission.isGranted) {
                    fullLineItem(key = "alerts") { AlertsWindow(permission) }
                }
                if (sections.isEmpty) {
                    fullLineItem(key = "empty") {
                        Text(stringResource(Res.string.wishlist_empty), style = RetroTheme.typography.body, color = colors.onBackground)
                    }
                }
                section("dev", Res.string.wishlist_in_development, colors.softcore, sections.inDevelopment, onOpenGame)
                section("ready", Res.string.wishlist_ready, colors.progress, sections.ready, onOpenGame)
                section("waiting", Res.string.wishlist_waiting, colors.onBackgroundMuted, sections.waiting, onOpenGame)
            }
        }
    }
}

private fun LazyGridScope.section(
    key: String,
    title: org.jetbrains.compose.resources.StringResource,
    color: Color,
    games: List<WishlistGame>,
    onOpenGame: (Long) -> Unit,
) {
    if (games.isEmpty()) return
    fullLineItem(key = "header-$key") {
        Row(
            modifier = Modifier.padding(top = 12.dp).semantics(mergeDescendants = true) { heading() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(stringResource(title), style = RetroTheme.typography.title, color = color)
            Text(games.size.toString(), style = RetroTheme.typography.label, color = RetroTheme.colors.onBackgroundMuted)
        }
    }
    items(games, key = { "$key-${it.gameId}" }) { game ->
        WishlistRow(game, onClick = { onOpenGame(game.gameId) })
    }
}

@Composable
private fun AlertsWindow(permission: NotificationPermissionState) {
    RetroWindow(title = stringResource(Res.string.wishlist_alerts_title), modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PixelIcon(PixelIcons.Wishlist, color = RetroTheme.colors.hardcore, size = 32.dp)
            Text(
                stringResource(if (permission.isDenied) Res.string.wishlist_alerts_denied else Res.string.wishlist_alerts_text),
                style = RetroTheme.typography.bodySmall,
                color = RetroTheme.colors.onWindow,
                modifier = Modifier.weight(1f),
            )
        }
        if (!permission.isDenied) {
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                RetroButton(text = stringResource(Res.string.wishlist_alerts_enable), onClick = permission::request)
            }
        }
    }
}

@Composable
private fun WishlistRow(game: WishlistGame, onClick: () -> Unit) {
    val colors = RetroTheme.colors
    val (tag, title) = splitTitleTag(game.title)
    val hasSet = game.status is SetStatus.Published
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (hasSet) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .retroFrame(fill = colors.window, border = colors.windowBorder, highlight = colors.windowHighlight)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RetroBadge(imageUrl = game.iconUrl, contentDescription = null, size = 56.dp, modifier = Modifier.sharedGame(game.gameId, GamePart.Icon))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                title,
                style = RetroTheme.typography.menu,
                color = colors.onWindow,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.sharedGame(game.gameId, GamePart.Title),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                if (tag != null) RetroTag(tag.uppercase(), colors.onWindowMuted)
                Text(
                    game.consoleName,
                    style = RetroTheme.typography.label,
                    color = colors.onWindowMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.sharedGame(game.gameId, GamePart.Console),
                )
            }
            when (val status = game.status) {
                is SetStatus.InDevelopment -> {
                    Text(
                        stringResource(
                            if (status.isRevision) Res.string.wishlist_status_revision else Res.string.wishlist_status_in_development,
                            status.developer,
                        ),
                        style = RetroTheme.typography.bodySmall,
                        color = colors.softcore,
                    )
                    status.expectedBy?.let {
                        Text(stringResource(Res.string.wishlist_status_expected, it.formatDate()), style = RetroTheme.typography.label, color = colors.onWindowMuted)
                    }
                }
                is SetStatus.Published -> Text(
                    stringResource(Res.string.wishlist_status_published, status.achievements, status.points.grouped()),
                    style = RetroTheme.typography.bodySmall,
                    color = colors.progress,
                )
                SetStatus.NoSet -> Text(stringResource(Res.string.wishlist_status_no_set), style = RetroTheme.typography.bodySmall, color = colors.onWindowMuted)
            }
        }
    }
}
