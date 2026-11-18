package net.bonysoft.bonusstage.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.bonysoft.bonusstage.domain.AwardCounts
import net.bonysoft.bonusstage.domain.Profile
import net.bonysoft.bonusstage.domain.RecentlyPlayedGame
import net.bonysoft.bonusstage.resources.Res
import net.bonysoft.bonusstage.resources.award_beaten_hardcore
import net.bonysoft.bonusstage.resources.award_beaten_softcore
import net.bonysoft.bonusstage.resources.award_completed
import net.bonysoft.bonusstage.resources.award_events
import net.bonysoft.bonusstage.resources.award_mastered
import net.bonysoft.bonusstage.resources.demo_badge
import net.bonysoft.bonusstage.resources.no
import net.bonysoft.bonusstage.resources.profile_awards
import net.bonysoft.bonusstage.resources.profile_hardcore
import net.bonysoft.bonusstage.resources.profile_member_since
import net.bonysoft.bonusstage.resources.profile_now_playing
import net.bonysoft.bonusstage.resources.profile_offline
import net.bonysoft.bonusstage.resources.profile_online
import net.bonysoft.bonusstage.resources.profile_options
import net.bonysoft.bonusstage.resources.profile_points
import net.bonysoft.bonusstage.resources.profile_rank
import net.bonysoft.bonusstage.resources.profile_recently_played
import net.bonysoft.bonusstage.resources.profile_softcore
import net.bonysoft.bonusstage.resources.profile_title
import net.bonysoft.bonusstage.resources.profile_true_points
import net.bonysoft.bonusstage.resources.profile_unranked
import net.bonysoft.bonusstage.resources.effect_crt
import net.bonysoft.bonusstage.resources.effect_lcd
import net.bonysoft.bonusstage.resources.effect_menu
import net.bonysoft.bonusstage.resources.effect_off
import net.bonysoft.bonusstage.resources.effect_scanlines
import net.bonysoft.bonusstage.resources.effect_title
import net.bonysoft.bonusstage.resources.recap_title
import net.bonysoft.bonusstage.resources.sign_out
import net.bonysoft.bonusstage.resources.sign_out_text
import net.bonysoft.bonusstage.resources.sign_out_title
import net.bonysoft.bonusstage.resources.yes
import net.bonysoft.bonusstage.ui.common.RefreshStatus
import net.bonysoft.bonusstage.ui.common.formatDate
import net.bonysoft.bonusstage.ui.common.grouped
import net.bonysoft.bonusstage.ui.common.relativeTo
import net.bonysoft.bonusstage.ui.components.PixelIcon
import net.bonysoft.bonusstage.ui.components.PixelIcons
import net.bonysoft.bonusstage.ui.components.RefreshableContent
import net.bonysoft.bonusstage.ui.components.RetroBadge
import net.bonysoft.bonusstage.ui.components.RetroDialog
import net.bonysoft.bonusstage.ui.components.RetroMenuItem
import net.bonysoft.bonusstage.ui.components.RetroScaffold
import net.bonysoft.bonusstage.ui.components.RetroTag
import net.bonysoft.bonusstage.ui.components.RetroWindow
import net.bonysoft.bonusstage.ui.effects.ScreenEffect
import net.bonysoft.bonusstage.ui.components.StaleNotice
import net.bonysoft.bonusstage.ui.components.retroFrame
import net.bonysoft.bonusstage.ui.theme.RetroTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

@Composable
fun ProfileRoute(
    onOpenGame: (Long) -> Unit,
    onOpenRecap: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProfileScreen(
        state = state,
        onRefresh = viewModel::refresh,
        onOpenGame = onOpenGame,
        onOpenRecap = onOpenRecap,
        onSelectEffect = viewModel::setScreenEffect,
        onSignOut = viewModel::signOut,
        modifier = modifier,
    )
}

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onRefresh: () -> Unit,
    onOpenGame: (Long) -> Unit,
    onOpenRecap: () -> Unit,
    onSelectEffect: (ScreenEffect) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    var pickEffect by rememberSaveable { mutableStateOf(false) }
    RetroScaffold(title = stringResource(Res.string.profile_title), modifier = modifier) {
        RefreshableContent(data = state.profile, status = state.status, onRefresh = onRefresh) { profile ->
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                state.status.error?.let { error -> item(key = "stale") { StaleNotice(error) } }
                item(key = "header") { ProfileHeader(profile, state.isDemo) }
                profile.richPresence?.let { presence -> item(key = "presence") { NowPlaying(presence) } }
                item(key = "points") { PointsWindow(profile) }
                profile.awards?.let { awards -> item(key = "awards") { AwardsWindow(awards) } }
                if (profile.recentlyPlayed.isNotEmpty()) {
                    item(key = "recent") { RecentlyPlayedWindow(profile.recentlyPlayed, onOpenGame) }
                }
                item(key = "options") {
                    RetroWindow(title = stringResource(Res.string.profile_options), modifier = Modifier.fillMaxWidth()) {
                        RetroMenuItem(text = stringResource(Res.string.recap_title), onClick = onOpenRecap)
                        RetroMenuItem(
                            text = stringResource(Res.string.effect_menu, effectLabel(state.screenEffect)),
                            onClick = { pickEffect = true },
                        )
                        RetroMenuItem(text = stringResource(Res.string.sign_out), onClick = { confirmSignOut = true })
                    }
                }
            }
        }
    }
    if (pickEffect) {
        Dialog(onDismissRequest = { pickEffect = false }) {
            RetroWindow(title = stringResource(Res.string.effect_title)) {
                ScreenEffect.entries.forEach { effect ->
                    RetroMenuItem(
                        text = effectLabel(effect),
                        selected = effect == state.screenEffect,
                        onClick = {
                            onSelectEffect(effect)
                            pickEffect = false
                        },
                    )
                }
            }
        }
    }
    if (confirmSignOut) {
        RetroDialog(
            title = stringResource(Res.string.sign_out_title),
            text = stringResource(Res.string.sign_out_text),
            confirmText = stringResource(Res.string.yes),
            dismissText = stringResource(Res.string.no),
            onConfirm = {
                confirmSignOut = false
                onSignOut()
            },
            onDismiss = { confirmSignOut = false },
        )
    }
}

@Composable
private fun ProfileHeader(profile: Profile, isDemo: Boolean) {
    val colors = RetroTheme.colors
    RetroWindow(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RetroBadge(imageUrl = profile.avatarUrl, contentDescription = null, size = 80.dp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(profile.username, style = RetroTheme.typography.title, color = colors.onWindow, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (isDemo) RetroTag(stringResource(Res.string.demo_badge), colors.softcore)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val online = profile.isOnline
                    Spacer(
                        Modifier.size(10.dp).retroFrame(
                            fill = if (online) colors.progress else colors.beaten,
                            border = colors.windowBorder,
                            highlight = null,
                        ),
                    )
                    Text(
                        stringResource(if (online) Res.string.profile_online else Res.string.profile_offline).uppercase(),
                        style = RetroTheme.typography.label,
                        color = colors.onWindowMuted,
                    )
                }
                profile.memberSince?.let {
                    Text(stringResource(Res.string.profile_member_since, it.formatDate()), style = RetroTheme.typography.bodySmall, color = colors.onWindowMuted)
                }
            }
        }
        profile.motto?.let {
            Spacer(Modifier.height(12.dp))
            Text("“$it”", style = RetroTheme.typography.body, color = colors.onWindow)
        }
    }
}

@Composable
private fun NowPlaying(presence: String) {
    RetroWindow(title = stringResource(Res.string.profile_now_playing), modifier = Modifier.fillMaxWidth()) {
        Text(presence, style = RetroTheme.typography.body, color = RetroTheme.colors.onWindow)
    }
}

@Composable
private fun PointsWindow(profile: Profile) {
    val colors = RetroTheme.colors
    RetroWindow(title = stringResource(Res.string.profile_points), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Stat(stringResource(Res.string.profile_hardcore), profile.hardcorePoints.grouped(), colors.hardcore, Modifier.weight(1f))
            Stat(stringResource(Res.string.profile_softcore), profile.softcorePoints.grouped(), colors.softcore, Modifier.weight(1f))
            Stat(stringResource(Res.string.profile_true_points), profile.truePoints.grouped(), colors.mastered, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        val rank = profile.rank
        val total = profile.totalRanked
        Text(
            text = if (rank != null && total != null && total > 0) {
                val top = ((rank * 100) / total).toInt().coerceIn(1, 100)
                stringResource(Res.string.profile_rank, rank.grouped(), total.grouped(), "$top%")
            } else {
                stringResource(Res.string.profile_unranked)
            },
            style = RetroTheme.typography.label,
            color = colors.onWindowMuted,
        )
    }
}

@Composable
private fun Stat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = RetroTheme.typography.number, color = color, maxLines = 1)
        Text(label.uppercase(), style = RetroTheme.typography.label, color = RetroTheme.colors.onWindowMuted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun AwardsWindow(awards: AwardCounts) {
    val colors = RetroTheme.colors
    RetroWindow(title = stringResource(Res.string.profile_awards), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Medal(stringResource(Res.string.award_mastered), awards.mastered + awards.completed, colors.mastered, Modifier.weight(1f))
            Medal(stringResource(Res.string.award_beaten_hardcore), awards.beatenHardcore, colors.hardcore, Modifier.weight(1f))
            Medal(stringResource(Res.string.award_beaten_softcore), awards.beatenSoftcore, colors.softcore, Modifier.weight(1f))
            Medal(stringResource(Res.string.award_events), awards.events, colors.progress, Modifier.weight(1f))
        }
    }
}

@Composable
private fun Medal(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PixelIcon(PixelIcons.Star, color = if (count > 0) color else RetroTheme.colors.track, size = 32.dp)
        Text(count.grouped(), style = RetroTheme.typography.number, color = RetroTheme.colors.onWindow)
        Text(label.uppercase(), style = RetroTheme.typography.label, color = RetroTheme.colors.onWindowMuted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun RecentlyPlayedWindow(games: List<RecentlyPlayedGame>, onOpenGame: (Long) -> Unit) {
    RetroWindow(title = stringResource(Res.string.profile_recently_played), modifier = Modifier.fillMaxWidth()) {
        games.forEach { game ->
            RetroMenuItem(
                text = game.title,
                supportingText = listOfNotNull(game.consoleName, game.lastPlayed?.relativeTo()).joinToString(" · "),
                onClick = { onOpenGame(game.gameId) },
                leading = { RetroBadge(imageUrl = game.iconUrl, contentDescription = null, size = 44.dp) },
            )
        }
    }
}

@Composable
private fun effectLabel(effect: ScreenEffect): String = stringResource(
    when (effect) {
        ScreenEffect.Off -> Res.string.effect_off
        ScreenEffect.Lcd -> Res.string.effect_lcd
        ScreenEffect.Scanlines -> Res.string.effect_scanlines
        ScreenEffect.Crt -> Res.string.effect_crt
    },
)

@Preview
@Composable
private fun ProfileScreenPreview() {
    RetroTheme {
        ProfileScreen(
            state = ProfileUiState(
                profile = Profile(
                    username = "BonusDemo", avatarUrl = "", memberSince = Instant.parse("2024-06-03T10:00:00Z"),
                    motto = "One more continue.", richPresence = "Green Hill Zone Act 3", hardcorePoints = 1_245,
                    softcorePoints = 37, truePoints = 4_310, rank = 48_213, totalRanked = 91_544, isOnline = true,
                    recentlyPlayed = emptyList(), awards = AwardCounts(1, 0, 1, 1, 0),
                ),
                isDemo = true,
                status = RefreshStatus(),
            ),
            onRefresh = {}, onOpenGame = {}, onOpenRecap = {}, onSelectEffect = {}, onSignOut = {},
        )
    }
}
