package net.bonysoft.bonusstage.ui.friends

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bonysoft.bonusstage.domain.Player
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.friends_find_button
import net.bonysoft.bonusstage.resources.friends_find_field
import net.bonysoft.bonusstage.resources.friends_find_hint
import net.bonysoft.bonusstage.resources.friends_find_title
import net.bonysoft.bonusstage.resources.friends_followers_tab
import net.bonysoft.bonusstage.resources.friends_following_tab
import net.bonysoft.bonusstage.resources.friends_follows_you
import net.bonysoft.bonusstage.resources.friends_list_empty
import net.bonysoft.bonusstage.resources.friends_open_profile
import net.bonysoft.bonusstage.resources.friends_points
import net.bonysoft.bonusstage.resources.friends_title
import net.bonysoft.bonusstage.resources.friends_you_follow
import net.bonysoft.bonusstage.ui.adaptive.AdaptiveList
import net.bonysoft.bonusstage.ui.adaptive.fullLineItem
import net.bonysoft.bonusstage.ui.common.RaLinks
import net.bonysoft.bonusstage.ui.common.errorMessage
import net.bonysoft.bonusstage.ui.common.grouped
import net.bonysoft.bonusstage.ui.common.rememberWebOpener
import net.bonysoft.bonusstage.ui.components.RetroBadge
import net.bonysoft.bonusstage.ui.components.RetroButton
import net.bonysoft.bonusstage.ui.components.RetroChip
import net.bonysoft.bonusstage.ui.components.RetroScaffold
import net.bonysoft.bonusstage.ui.components.RetroTag
import net.bonysoft.bonusstage.ui.components.RetroTextField
import net.bonysoft.bonusstage.ui.components.RetroWindow
import net.bonysoft.bonusstage.ui.components.StaleNotice
import net.bonysoft.bonusstage.ui.components.retroFrame
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FriendsListRoute(onBack: () -> Unit, modifier: Modifier = Modifier, viewModel: FriendsListViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val web = rememberWebOpener()
    // Also when coming back from a profile page: the lists may have changed there.
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    FriendsListScreen(
        state = state,
        onBack = onBack,
        onShowFollowers = viewModel::showFollowers,
        onUsernameChange = viewModel::onUsernameChange,
        onFind = viewModel::find,
        onOpenProfile = { player ->
            viewModel.onOpenProfile()
            web.open(RaLinks.user(player.username))
        },
        modifier = modifier,
    )
}

@Composable
fun FriendsListScreen(
    state: FriendsListUiState,
    onBack: () -> Unit,
    onShowFollowers: (Boolean) -> Unit,
    onUsernameChange: (String) -> Unit,
    onFind: () -> Unit,
    onOpenProfile: (Player) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = RetroTheme.colors
    val players = if (state.showFollowers) state.followers else state.following
    RetroScaffold(title = stringResource(Res.string.friends_title), onBack = onBack, modifier = modifier) {
        AdaptiveList {
            state.status.error?.let { error -> fullLineItem(key = "stale") { StaleNotice(error) } }
            fullLineItem(key = "find") { FindWindow(state.lookup, onUsernameChange, onFind, onOpenProfile) }
            fullLineItem(key = "tabs") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    RetroChip(stringResource(Res.string.friends_following_tab, state.following.size), selected = !state.showFollowers, onClick = { onShowFollowers(false) })
                    RetroChip(stringResource(Res.string.friends_followers_tab, state.followers.size), selected = state.showFollowers, onClick = { onShowFollowers(true) })
                }
            }
            if (players.isEmpty() && !state.status.isRefreshing) {
                fullLineItem(key = "empty") {
                    Text(stringResource(Res.string.friends_list_empty), style = RetroTheme.typography.body, color = colors.onBackground)
                }
            }
            items(players, key = { it.ulid }) { player ->
                PlayerRow(player, isFollowersList = state.showFollowers, onClick = { onOpenProfile(player) })
            }
        }
    }
}

@Composable
private fun FindWindow(lookup: Lookup, onUsernameChange: (String) -> Unit, onFind: () -> Unit, onOpenProfile: (Player) -> Unit) {
    val colors = RetroTheme.colors
    RetroWindow(title = stringResource(Res.string.friends_find_title), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RetroTextField(
                value = lookup.username,
                onValueChange = onUsernameChange,
                label = stringResource(Res.string.friends_find_field),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(onSearch = { onFind() }),
                modifier = Modifier.weight(1f),
            )
            RetroButton(text = stringResource(Res.string.friends_find_button), onClick = onFind, enabled = lookup.username.isNotBlank() && !lookup.isSearching)
        }
        Spacer(Modifier.height(8.dp))
        Text(stringResource(Res.string.friends_find_hint), style = RetroTheme.typography.bodySmall, color = colors.onWindowMuted)
        lookup.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage(it), style = RetroTheme.typography.bodySmall, color = colors.error)
        }
        lookup.result?.let { player ->
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RetroBadge(imageUrl = player.avatarUrl, contentDescription = null, size = 56.dp)
                Column(Modifier.weight(1f)) {
                    Text(player.username, style = RetroTheme.typography.menu, color = colors.onWindow)
                    Text(stringResource(Res.string.friends_points, player.points.grouped()), style = RetroTheme.typography.label, color = colors.onWindowMuted)
                    player.motto?.let { Text("“$it”", style = RetroTheme.typography.bodySmall, color = colors.onWindow, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                    if (player.mutual) RetroTag(stringResource(Res.string.friends_you_follow), colors.progress)
                }
            }
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
                RetroButton(text = stringResource(Res.string.friends_open_profile), onClick = { onOpenProfile(player) })
            }
        }
    }
}

@Composable
private fun PlayerRow(player: Player, isFollowersList: Boolean, onClick: () -> Unit) {
    val colors = RetroTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .retroFrame(fill = colors.window, border = colors.windowBorder, highlight = colors.windowHighlight)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RetroBadge(imageUrl = player.avatarUrl, contentDescription = null, size = 48.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(player.username, style = RetroTheme.typography.menu, color = colors.onWindow, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(stringResource(Res.string.friends_points, player.points.grouped()), style = RetroTheme.typography.label, color = colors.onWindowMuted)
        }
        if (player.mutual) {
            RetroTag(
                stringResource(if (isFollowersList) Res.string.friends_you_follow else Res.string.friends_follows_you),
                if (isFollowersList) colors.progress else colors.softcore,
            )
        }
    }
}
