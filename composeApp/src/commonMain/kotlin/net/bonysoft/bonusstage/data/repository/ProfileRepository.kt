package net.bonysoft.bonusstage.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import net.bonysoft.bonusstage.data.account.AccountRepository
import net.bonysoft.bonusstage.data.cache.RefreshPolicy
import net.bonysoft.bonusstage.data.cache.Refresher
import net.bonysoft.bonusstage.db.BonusStageDatabase
import net.bonysoft.bonusstage.db.Recently_played
import net.bonysoft.bonusstage.db.User_award_summary
import net.bonysoft.bonusstage.db.User_profile
import net.bonysoft.bonusstage.domain.AwardCounts
import net.bonysoft.bonusstage.domain.Profile
import net.bonysoft.bonusstage.domain.RecentlyPlayedGame
import net.bonysoft.bonusstage.raapi.RaMedia
import kotlin.time.Instant

class ProfileRepository(
    private val database: BonusStageDatabase,
    private val accounts: AccountRepository,
    private val refresher: Refresher,
    private val policy: RefreshPolicy,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val queries get() = database.cacheQueries

    fun observeProfile(): Flow<Profile?> {
        val ulid = accounts.requireUlid()
        return combine(
            queries.profile(ulid).asFlow().mapToOneOrNull(dispatcher),
            queries.recentlyPlayed(ulid).asFlow().mapToList(dispatcher),
            queries.awardSummary(ulid).asFlow().mapToOneOrNull(dispatcher),
        ) { profile, recent, awards -> profile?.toDomain(recent, awards) }
    }

    suspend fun refresh(force: Boolean = false): Boolean {
        val ulid = accounts.requireUlid()
        return refresher.refresh("profile:$ulid", policy.profile, force) {
            val client = accounts.client()
            val summary = client.getUserSummary(ulid, recentGames = 5)
            val awards = client.getUserAwards(ulid)
            database.transaction {
                queries.upsertProfile(
                    User_profile(
                        ulid = ulid,
                        username = summary.user,
                        user_pic = summary.userPic,
                        member_since = summary.memberSince.toEpochMillis(),
                        motto = summary.motto?.takeIf { it.isNotBlank() },
                        rich_presence = summary.richPresenceMsg?.takeIf { it.isNotBlank() },
                        last_game_id = summary.lastGameId,
                        hardcore_points = summary.totalPoints,
                        softcore_points = summary.totalSoftcorePoints,
                        true_points = summary.totalTruePoints,
                        rank = summary.rank,
                        total_ranked = summary.totalRanked,
                        status = summary.status,
                    ),
                )
                queries.deleteRecentlyPlayed(ulid)
                summary.recentlyPlayed.forEachIndexed { index, game ->
                    queries.insertGameIfMissing(game.gameId, game.title, game.consoleId.toLong(), game.consoleName, game.imageIcon)
                    queries.insertRecentlyPlayed(
                        Recently_played(ulid, game.gameId, index.toLong(), game.title, game.consoleName, game.imageIcon, game.lastPlayed.toEpochMillis()),
                    )
                }
                queries.upsertAwardSummary(
                    User_award_summary(
                        ulid = ulid,
                        mastered = awards.masteryAwardsCount.toLong(),
                        completed = awards.completionAwardsCount.toLong(),
                        beaten_hardcore = awards.beatenHardcoreAwardsCount.toLong(),
                        beaten_softcore = awards.beatenSoftcoreAwardsCount.toLong(),
                        events = awards.eventAwardsCount.toLong(),
                    ),
                )
            }
        }
    }

    private fun User_profile.toDomain(recent: List<Recently_played>, awards: User_award_summary?) = Profile(
        username = username,
        avatarUrl = RaMedia.image(user_pic),
        memberSince = member_since?.let(Instant::fromEpochMilliseconds),
        motto = motto,
        richPresence = rich_presence,
        hardcorePoints = hardcore_points,
        softcorePoints = softcore_points,
        truePoints = true_points,
        rank = rank,
        totalRanked = total_ranked,
        isOnline = status.equals("Online", ignoreCase = true),
        recentlyPlayed = recent.map {
            RecentlyPlayedGame(it.game_id, it.title, it.console_name, RaMedia.image(it.image_icon), it.last_played?.let(Instant::fromEpochMilliseconds))
        },
        awards = awards?.let {
            AwardCounts(it.mastered.toInt(), it.completed.toInt(), it.beaten_hardcore.toInt(), it.beaten_softcore.toInt(), it.events.toInt())
        },
    )
}
